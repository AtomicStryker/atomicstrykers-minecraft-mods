package atomicstryker.ruins.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class World2TemplateParser extends Thread
{

    /**
     * Block upon which the parser is started, is used to define the template
     * area and disregarded when detecting Blocks. Can be used for support in
     * templates.
     */
    private final BlockData templateHelperBlock;
    private final BlockData nothing = new BlockData(Blocks.air, 0, null);

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
        world = p.worldObj;
        x = a;
        y = b;
        z = c;
        fileName = fName;
        templateHelperBlock = new BlockData(world.getBlock(a, b, c), world.getBlockMetadata(a, b, c), null);
        usedBlocks = new ArrayList<BlockData>();
        layerData = new ArrayList<BlockData[][]>();
    }

    @Override
    public void run()
    {
        failed = templateHelperBlock.block == Blocks.air;

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
            player.addChatMessage(new ChatComponentText("Block reading finished. Rules: " + usedBlocks.size() + ", layers: " + layerData.size()
                    + ", xlen: " + xLength + ", zlen: " + zLength));

            File templateFile = new File(RuinsMod.getMinecraftBaseDir(), "mods/resources/ruins/templateparser/" + fileName + ".tml");
            toFile(templateFile);

            if (!failed)
            {
                player.addChatMessage(new ChatComponentText("Success writing templatefile " + templateFile));
            }
        }
        else
        {
            player.addChatMessage(new ChatComponentText("Template Parse fail, chosen Block was air WTF?!"));
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
                layerData = new ArrayList<BlockData[][]>(layerData.subList(yPadding, highestY - y));
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

                    temp.block = world.getBlock(blockx, blocky, blockz);
                    temp.meta = world.getBlockMetadata(blockx, blocky, blockz);
                    temp.data = null;

                    if (temp.block == Blocks.air || temp.equals(templateHelperBlock))
                    {
                        currentLayer[xi][zi] = nothing;
                        continue;
                    }

                    if (highestY == -1)
                    {
                        yPadding = yi - y;
                    }
                    highestY = yi;
                    
                    TileEntity te = world.getTileEntity(blockx, blocky, blockz);
                    /* handle special blocks */
                    if (temp.block == Blocks.mob_spawner)
                    {
                        temp.data = "MobSpawner:" + ((TileEntityMobSpawner) te).func_145881_a().getEntityNameToSpawn();
                    }
                    else if (te instanceof IInventory && !isIInventoryEmpty((IInventory)te))
                    {
                        IInventory inventory = (IInventory) te;
                        ArrayList<ItemStack> invItems = new ArrayList<ItemStack>();
                        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
                        {
                            if (inventory.getStackInSlot(slot) != null)
                            {
                                invItems.add(inventory.getStackInSlot(slot));
                            }
                        }
                        
                        temp.data = "IInventory;"+GameData.blockRegistry.getNameForObject(temp.block)+";";
                        for (ItemStack i : invItems)
                        {
                            String ident;
                            if (i.getItem() instanceof ItemBlock)
                            {
                                ident = GameData.blockRegistry.getNameForObject(((ItemBlock)i.getItem()).field_150939_a);
                            }
                            else
                            {
                                ident = GameData.itemRegistry.getNameForObject(i.getItem());
                            }
                            if (ident != null)
                            {
                                temp.data = temp.data.concat(ident+"#"+i.stackSize+"#"+i.getItemDamage()+"+");
                            }
                        }
                        
                        int iLastSep = temp.data.lastIndexOf("+");
                        if (iLastSep != -1)
                        {
                            temp.data = temp.data.substring(0, iLastSep)+"-"+temp.meta;
                        }
                        else
                        {
                            temp.data = temp.data+"-"+temp.meta;
                        }
                    }
                    else if (temp.block == Blocks.chest)
                    {
                        temp.data = "ChestGenHook:dungeonChest:5-"+temp.meta;
                    }
                    else if (temp.block == Blocks.command_block)
                    {
                        TileEntityCommandBlock tec = (TileEntityCommandBlock) te;
                        temp.data = "CommandBlock:" + tec.func_145993_a().func_145753_i() + ":" + tec.func_145993_a().getCommandSenderName();
                    }
                    else if (temp.block == Blocks.standing_sign)
                    {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("StandingSign:", tes) + "-" + temp.meta;
                    }
                    else if (temp.block == Blocks.wall_sign)
                    {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("WallSign:", tes) + "-" + temp.meta;
                    }
                    else if (temp.block == Blocks.skull)
                    {
                        TileEntitySkull tes = (TileEntitySkull) te;
                        int skulltype = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 0);
                        int rot = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 1);
                        String specialType = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 2);
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
            if (inventory.getStackInSlot(slot) != null)
            {
                return false;
            }
        }
        return true;
    }

    private String convertSignStrings(String prefix, TileEntitySign sign)
    {
        String a = sign.signText[0];
        if (a.equals("")) a = "null";
        String b = sign.signText[1];
        if (b.equals("")) b = "null";
        String c = sign.signText[2];
        if (c.equals("")) c = "null";
        String d = sign.signText[3];
        if (d.equals("")) d = "null";
        String result = prefix+a+":"+b+":"+c+":"+d;
        return result;
    }
    
    private void toFile(File file)
    {
        try
        {
            if (file.exists())
            {
                file.delete();
            }
            else
            {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            
            pw.println();
            pw.println("# Created by Ruins mod version "+RuinsMod.modversion+" Ingame Parser");
            pw.println("# authoring Player: "+player.getCommandSenderName());
            pw.println();
            
            pw.println("weight=1");
            pw.println("embed_into_distance=" + yPadding);
            pw.println("acceptable_target_blocks=stone,grass,dirt,sand,gravel,snow_layer,clay");
            pw.println("dimensions=" + layerData.size() + "," + xLength + "," + zLength);
            pw.println("allowable_overhang=0");
            pw.println("max_cut_in=2");
            pw.println("cut_in_buffer=1");
            pw.println("max_leveling=2");
            pw.println("leveling_buffer=1");
            pw.println("preserve_water=0");
            pw.println("preserve_lava=0");
            pw.println("preserve_plants=0");
            pw.println();

            int rulenum = 1;
            for (BlockData bd : usedBlocks)
            {
                pw.println("rule" + rulenum + "=0,100," + bd.toString());
                rulenum++;
            }

            pw.println();

            for (BlockData[][] layer : layerData)
            {
                pw.println("layer");

                for (int i = 0; i < layer.length; i++)
                {
                    /* have to invert this for some reason */
                    for (int j = 0, j2 = layer[0].length-1; j < layer[0].length; j++, j2--)
                    {
                        /*
                         * since 'nothing' is not contained, it returns -1 + 1 =
                         * 0, which is the default air rule
                         */
                        pw.print(usedBlocks.indexOf(layer[i][j2]) + 1);

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
            player.addChatMessage(new ChatComponentText("Something broke! See logfile for exception message and get it to AtomicStryker."));
        }
    }

    private class BlockData
    {
        Block block;
        int meta;
        String data;

        BlockData(Block b, int m, String d)
        {
            block = b;
            meta = m;
            data = d;
        }

        BlockData copy()
        {
            return new BlockData(block, meta, data);
        }

        boolean matchesBlock(World w, int x, int y, int z)
        {
            return w.getBlock(x, y, z) == block && meta == w.getBlockMetadata(x, y, z);
        }

        @Override
        public String toString()
        {
            return (data != null) ? data : GameData.blockRegistry.getNameForObject(block) + "-" + meta;
        }

        @Override
        public int hashCode()
        {
            if (data != null)
            {
                return data.hashCode();
            }
            return block.getUnlocalizedName().hashCode() & meta;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof BlockData)
            {
                BlockData b = (BlockData) o;
                if (b.data != null)
                {
                    if (data != null)
                    {
                        return b.data.equals(data);
                    }
                    return false;
                }
                return b.block == block && b.meta == meta;
            }
            return false;
        }
    }
}
