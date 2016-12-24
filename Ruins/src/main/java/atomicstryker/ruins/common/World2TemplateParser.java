package atomicstryker.ruins.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

class World2TemplateParser extends Thread
{

    private static final int SPAWN_RULE_EXISTSBELOW = 1;
    private static final int SPAWN_RULE_EXISTSADJACENT = 2;

    /**
     * Block upon which the parser is started, is used to define the template
     * area and disregarded when detecting Blocks. Can be used for support in
     * templates.
     */
    private final BlockData templateHelperBlock;
    private final BlockData nothing = new BlockData(Blocks.AIR, 0, null, 0);

    /**
     * Starting point for the template parse scan
     */
    private final int x, y, z;

    /**
     * These values denote the template size and location
     */
    private int lowestX, lowestZ, xLength, zLength;

    /**
     * This keeps track how high above the baseplate the first template content
     * appeared
     */
    private int yPadding;

    /**
     * Blocks and metas found while parsing the template. Each different
     * instance will be made a rule
     */
    private final ArrayList<BlockData> usedBlocks;

    /**
     * Threedimensional Layerdata, starts with the first layer containing a
     * Block
     */
    private ArrayList<BlockData[][]> layerData;

    /**
     * World instance
     */
    private final World world;

    /**
     * Template target filename
     */
    private final String fileName;

    /**
     * Player that executed the command
     */
    private final EntityPlayer player;

    /**
     * Keeps track of and reports result back to player
     */
    private boolean failed;

    /**
     * Counts in the Background to stop near-finite loops by accident
     */
    private int blocker;

    /**
     * Prepares the conversion of an ingame construction to Ruins template file.
     * The player must select any block instance of a rectangular plate of the
     * same Block which defines the template size. Any different Blocks found
     * above this plate are considered to make up the template.
     */
    public World2TemplateParser(EntityPlayer p, int a, int b, int c, String fName)
    {
        player = p;
        world = p.world;
        x = a;
        y = b;
        z = c;
        fileName = fName;
        IBlockState state = world.getBlockState(new BlockPos(a, b, c));
        templateHelperBlock = new BlockData(state.getBlock(), state.getBlock().getMetaFromState(state), null, 0);
        usedBlocks = new ArrayList<>();
        layerData = new ArrayList<>();
    }

    @Override
    public void run()
    {
        failed = templateHelperBlock.block == Blocks.AIR;

        if (!failed)
        {
            blocker = 0;
            lowestX = x - 1;
            while (templateHelperBlock.matchesBlock(world, lowestX, y, z))
            {
                lowestX--;
                checkLockup();
            }
            lowestX++;

            blocker = 0;
            lowestZ = z - 1;
            while (templateHelperBlock.matchesBlock(world, x, y, lowestZ))
            {
                lowestZ--;
                checkLockup();
            }
            lowestZ++;

            blocker = 0;
            int xmax = x + 1;
            while (templateHelperBlock.matchesBlock(world, xmax, y, z))
            {
                xmax++;
                checkLockup();
            }
            xmax--;
            xLength = 1 + xmax - lowestX;

            blocker = 0;
            int zmax = z + 1;
            while (templateHelperBlock.matchesBlock(world, x, y, zmax))
            {
                zmax++;
                checkLockup();
            }
            zmax--;
            zLength = 1 + zmax - lowestZ;

            readBlocks(world);
            player.sendMessage(new TextComponentTranslation("Block reading finished. Rules: " + usedBlocks.size() + ", layers: " + layerData.size() + ", xlen: " + xLength + ", zlen: " + zLength));

            File templateFile = new File(RuinsMod.getMinecraftBaseDir(), "mods/resources/ruins/templateparser/" + fileName + ".tml");
            toFile(templateFile);

            if (!failed)
            {
                player.sendMessage(new TextComponentTranslation("Success writing templatefile " + templateFile));
            }
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("Template Parse fail, chosen Block was air WTF?!"));
        }
    }

    private void checkLockup()
    {
        if (blocker++ > 1024)
        {
            throw new IndexOutOfBoundsException("Runaway loop detected! Did you hit the ground?!");
        }
    }

    private void readBlocks(World world)
    {
        yPadding = 0;
        int highestY = y + 1;
        BlockData temp = nothing.copy();
        BlockData currentinstance;
        BlockData[][] currentLayer;
        blocker = 0;

        for (int yi = y + 1; true; yi++)
        {
            currentLayer = new BlockData[xLength][zLength];
            layerData.add(currentLayer);

            if (yi > (highestY + 100))
            {
                // strip off the empty layers again
                layerData = new ArrayList<>(layerData.subList(yPadding, highestY - y));
                return;
            }
            checkLockup();

            int blockx, blocky, blockz;
            for (int xi = 0; xi < xLength; xi++)
            {
                for (int zi = 0; zi < zLength; zi++)
                {
                    blockx = xi + lowestX;
                    blocky = yi;
                    blockz = zi + lowestZ;

                    IBlockState state = world.getBlockState(new BlockPos(blockx, blocky, blockz));
                    temp.block = state.getBlock();
                    temp.meta = temp.block.getMetaFromState(state);
                    temp.data = null;
                    temp.spawnRule = 0;

                    if (temp.block == Blocks.AIR || temp.equals(templateHelperBlock))
                    {
                        currentLayer[xi][zi] = nothing;
                        continue;
                    }

                    if (highestY == -1)
                    {
                        yPadding = yi - y;
                    }
                    highestY = yi;

                    TileEntity te = world.getTileEntity(new BlockPos(new BlockPos(blockx, blocky, blockz)));
                    /* handle special blocks */
                    if (te != null && FileHandler.registeredTEBlocks.contains(temp.block))
                    {
                        NBTTagCompound tc = new NBTTagCompound();
                        te.writeToNBT(tc);
                        temp.data = "teBlock;" + Block.REGISTRY.getNameForObject(temp.block).toString() + ";" + tc.toString() + "-" + temp.meta;
                    }
                    else if (temp.block == Blocks.MOB_SPAWNER)
                    {
                        try
                        {
                            if (te != null)
                            {
                                Field f = ((TileEntityMobSpawner) te).getSpawnerBaseLogic().getClass().getDeclaredFields()[1];
                                f.setAccessible(true);
                                temp.data = (String) f.get(((TileEntityMobSpawner) te).getSpawnerBaseLogic());
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (temp.block == Blocks.TORCH || temp.block == Blocks.REDSTONE_TORCH)
                    {
                        // if meta says FLOOR, add FLOOR dependency, alse
                        // ADJACENT dependency
                        temp.spawnRule = (temp.meta == 0 || temp.meta == 5) ? SPAWN_RULE_EXISTSBELOW : SPAWN_RULE_EXISTSADJACENT;
                    }
                    else if (temp.block == Blocks.PISTON_HEAD || temp.block == Blocks.PISTON_EXTENSION)
                    {
                        temp.spawnRule = SPAWN_RULE_EXISTSADJACENT;
                    }
                    else if (temp.block == Blocks.WOODEN_BUTTON || temp.block == Blocks.STONE_BUTTON)
                    {
                        // if meta says FLOOR, add FLOOR dependency, alse
                        // ADJACENT dependency
                        temp.spawnRule = temp.meta == 5 ? SPAWN_RULE_EXISTSBELOW : SPAWN_RULE_EXISTSADJACENT;
                    }
                    else if (temp.block == Blocks.CHEST)
                    {
                        temp.data = "ChestGenHook:chests/simple_dungeon:5-" + temp.meta;
                    }
                    else if (te instanceof IInventory && !isIInventoryEmpty((IInventory) te))
                    {
                        IInventory inventory = (IInventory) te;
                        final ArrayList<ItemStack> invItems = new ArrayList<>();
                        final ArrayList<Integer> slots = new ArrayList<>();
                        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
                        {
                            if (inventory.getStackInSlot(slot) != ItemStack.EMPTY)
                            {
                                invItems.add(inventory.getStackInSlot(slot));
                                slots.add(slot);
                            }
                        }

                        StringBuilder sb = new StringBuilder("IInventory;");
                        sb.append(Block.REGISTRY.getNameForObject(temp.block));
                        sb.append(';');
                        for (int index = 0; index < invItems.size(); index++)
                        {
                            ItemStack stack = invItems.get(index);
                            String ident;
                            if (stack.getItem() instanceof ItemBlock)
                            {
                                ItemStack cs = stack.getItem().getContainerItem(stack);
                                if (cs != null)
                                {
                                    // ident =
                                    // GameData.getBlockRegistry().getNameForObject(cs).toString();
                                    // TODO: Is this correct? i dont remember
                                    // which case this code was handling
                                    ident = Item.REGISTRY.getNameForObject(cs.getItem()).toString();
                                }
                                else
                                {
                                    ident = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
                                }
                            }
                            else
                            {
                                ident = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
                            }
                            if (ident != null)
                            {
                                sb.append(ident);
                            }
                            else
                            {
                                sb.append(stack.getUnlocalizedName());
                            }
                            sb.append('#');
                            if (stack.getTagCompound() != null)
                            {
                                sb.append(stack.getTagCompound().toString());
                            }
                            else
                            {
                                sb.append(stack.getCount());
                            }
                            sb.append('#');
                            sb.append(stack.getItemDamage());
                            sb.append('#');
                            sb.append(slots.get(index));
                            sb.append('+');
                        }

                        temp.data = sb.toString();
                        int iLastSep = temp.data.lastIndexOf("+");
                        if (iLastSep != -1)
                        {
                            temp.data = temp.data.substring(0, iLastSep) + "-" + temp.meta;
                        }
                        else
                        {
                            temp.data = temp.data + "-" + temp.meta;
                        }
                    }
                    else if (temp.block == Blocks.COMMAND_BLOCK)
                    {
                        TileEntityCommandBlock tec = (TileEntityCommandBlock) te;
                        if (tec != null)
                        {
                            temp.data = "CommandBlock:" + tec.getCommandBlockLogic().getCommand() + ":" + tec.getCommandBlockLogic().getName() + "-" + temp.meta;
                        }
                    }
                    else if (temp.block == Blocks.STANDING_SIGN)
                    {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("StandingSign:", tes) + "-" + temp.meta;
                        temp.spawnRule = SPAWN_RULE_EXISTSBELOW;
                    }
                    else if (temp.block == Blocks.WALL_SIGN)
                    {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("WallSign:", tes) + "-" + temp.meta;
                        temp.spawnRule = SPAWN_RULE_EXISTSADJACENT;
                    }
                    else if (temp.block == Blocks.SKULL)
                    {
                        TileEntitySkull tes = (TileEntitySkull) te;
                        int skulltype = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 0);
                        int rot = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 1);
                        String specialType = "";
                        GameProfile playerhead = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 2);
                        if (playerhead != null)
                        {
                            specialType = playerhead.getId().toString() + "-" + playerhead.getName();
                        }
                        temp.data = "Skull:" + skulltype + ":" + rot + ((specialType.equals("")) ? "" : ":" + specialType) + "-" + temp.meta;
                    }

                    int indexInList = usedBlocks.indexOf(temp);
                    if (indexInList == -1)
                    {
                        currentinstance = temp.copy();
                        usedBlocks.add(currentinstance);
                    }
                    else
                    {
                        currentinstance = usedBlocks.get(indexInList);
                    }
                    currentLayer[xi][zi] = currentinstance;
                }
            }
        }
    }

    private boolean isIInventoryEmpty(IInventory inventory)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (inventory.getStackInSlot(slot) != ItemStack.EMPTY)
            {
                return false;
            }
        }
        return true;
    }

    private String convertSignStrings(String prefix, TileEntitySign sign)
    {
        String a = sign.signText[0].getUnformattedText();
        if (a.equals(""))
        {
            a = "null";
        }
        String b = sign.signText[1].getUnformattedText();
        if (b.equals(""))
        {
            b = "null";
        }
        String c = sign.signText[2].getUnformattedText();
        if (c.equals(""))
        {
            c = "null";
        }
        String d = sign.signText[3].getUnformattedText();
        if (d.equals(""))
        {
            d = "null";
        }
        return prefix + a + ":" + b + ":" + c + ":" + d;
    }

    private void toFile(File file)
    {
        try
        {
            if (file.exists())
            {
                if (!file.delete())
                {
                    throw new RuntimeException("Ruins crashed trying to access file " + file);
                }
            }
            else
            {
                if (!file.createNewFile())
                {
                    throw new RuntimeException("Ruins crashed trying to access file " + file);
                }
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            pw.println();
            pw.println("# Created by Ruins mod version " + RuinsMod.modversion + " Ingame Parser");
            pw.println("# authoring Player: " + player.getName());
            pw.println();

            pw.println("weight=1");
            pw.println("embed_into_distance=" + yPadding);
            pw.println("acceptable_target_blocks=");
            pw.println("unacceptable_target_blocks=flowing_water,water,flowing_lava,lava");
            pw.println("dimensions=" + layerData.size() + "," + xLength + "," + zLength);
            pw.println("allowable_overhang=0");
            pw.println("max_leveling=2");
            pw.println("leveling_buffer=0");
            pw.println("preserve_water=0");
            pw.println("preserve_lava=0");
            pw.println();

            int rulenum = 1;
            for (BlockData bd : usedBlocks)
            {
                pw.println("rule" + rulenum + "=" + StringEscapeUtils.escapeJava(bd.toString()));
                rulenum++;
            }

            pw.println();

            for (BlockData[][] layer : layerData)
            {
                pw.println("layer");

                for (BlockData[] aLayer : layer)
                {
                    /* have to invert this for some reason */
                    for (int j = 0, j2 = layer[0].length - 1; j < layer[0].length; j++, j2--)
                    {
                        /*
                         * since 'nothing' is not contained, it returns -1 + 1 =
                         * 0, which is the default preserveBlock rule
                         */
                        pw.print(usedBlocks.indexOf(aLayer[j2]) + 1);

                        if (j < layer[0].length - 1)
                        {
                            pw.print(",");
                        }
                    }
                    pw.println();
                }

                pw.println("endlayer");
                pw.println();
            }

            pw.close();

            CommandTestTemplate.parsedRuin = new RuinTemplate(new PrintWriter(System.out), file.getCanonicalPath(), file.getName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            failed = true;
            player.sendMessage(new TextComponentTranslation("Something broke! See server logfile for exception message and get it to AtomicStryker."));
            player.sendMessage(new TextComponentTranslation("First line of stacktrace: " + e.getMessage()));
        }
    }

    private class BlockData
    {
        Block block;
        int meta;
        String data;
        int spawnRule;

        BlockData(Block b, int m, String d, int sr)
        {
            block = b;
            meta = m;
            data = d;
            spawnRule = sr;
        }

        BlockData copy()
        {
            return new BlockData(block, meta, data, spawnRule);
        }

        boolean matchesBlock(World w, int x, int y, int z)
        {
            IBlockState state = w.getBlockState(new BlockPos(x, y, z));
            return state.getBlock() == block && meta == block.getMetaFromState(state);
        }

        @Override
        public String toString()
        {
            return spawnRule + ",100," + ((data != null) ? data : Block.REGISTRY.getNameForObject(block).toString() + "-" + meta);
        }

        @Override
        public int hashCode()
        {
            if (data != null)
            {
                return data.hashCode() + meta;
            }
            return block.getUnlocalizedName().hashCode() + meta;
        }

        @Override
        public boolean equals(Object o)
        {
            return o instanceof BlockData && o.toString().equals(this.toString());
        }
    }
}
