package atomicstryker.ropesplus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Confusion;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Dirt;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ex;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Fire;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Grass;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ice;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Laser;
import atomicstryker.ropesplus.common.arrows.EntityArrow303RedStoneTorch;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Rope;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Slime;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Torch;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Warp;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import atomicstryker.ropesplus.common.network.ArrowChoicePacket;
import atomicstryker.ropesplus.common.network.GrapplingHookPacket;
import atomicstryker.ropesplus.common.network.HookshotPacket;
import atomicstryker.ropesplus.common.network.HookshotPullPacket;
import atomicstryker.ropesplus.common.network.NetworkHelper;
import atomicstryker.ropesplus.common.network.RopeBowSettingPacket;
import atomicstryker.ropesplus.common.network.SoundPacket;
import atomicstryker.ropesplus.common.network.ZiplinePacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "RopesPlus", name = "Ropes+", version = "1.6.3")
public class RopesPlusCore
{
    @SidedProxy(clientSide = "atomicstryker.ropesplus.client.ClientProxy", serverSide = "atomicstryker.ropesplus.common.CommonProxy")
    public static IProxy proxy;

    @Instance("RopesPlus")
    public static RopesPlusCore instance;

    public NetworkHelper networkHelper;

    public final static Class<?> coreArrowClasses[] = { EntityArrow303Dirt.class, EntityArrow303Ex.class, EntityArrow303Fire.class,
            EntityArrow303Grass.class, EntityArrow303Ice.class, EntityArrow303Laser.class, EntityArrow303Slime.class, EntityArrow303Torch.class,
            EntityArrow303Warp.class, EntityArrow303Confusion.class, EntityArrow303Rope.class, EntityArrow303RedStoneTorch.class };

    private List<EntityArrow303> arrows;
    public Item bowRopesPlus;
    public List<ItemArrow303> arrowItems;

    public Block blockRope;
    public List<Object> ropeEntArray;
    private List<int[]> ropePosArray;

    public Block blockRopeWall;
    public Block blockGrapplingHook;
    public Item itemGrapplingHook;

    public Item itemHookShot;
    public Block blockZipLineAnchor;
    public Item itemHookShotCartridge;

    private HashMap<EntityPlayer, EntityGrapplingHook> grapplingHookMap;
    private HashMap<EntityPlayer, EntityFreeFormRope> playerRopeMap;
    private HashMap<EntityPlayer, Integer> selectedSlotMap;

    private boolean hookShotEnabled;
    private boolean zipLinesEnabled;
    private boolean grapplingEnabled;

    @SuppressWarnings("unchecked")
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        arrows = new ArrayList<EntityArrow303>();
        arrowItems = new ArrayList<ItemArrow303>();
        ropeEntArray = new ArrayList<Object>();
        ropePosArray = new ArrayList<int[]>();
        grapplingHookMap = new HashMap<EntityPlayer, EntityGrapplingHook>();
        playerRopeMap = new HashMap<EntityPlayer, EntityFreeFormRope>();
        selectedSlotMap = new HashMap<EntityPlayer, Integer>();

        Settings_RopePlus.InitSettings(event.getSuggestedConfigurationFile());
        proxy.loadConfig(Settings_RopePlus.config);

        blockRope = (new BlockRope().setHardness(0.3F).setBlockName("blockRopeCenter"));
        itemGrapplingHook = new ItemGrapplingHook().setUnlocalizedName("itemGrapplingHook");
        blockRopeWall = (new BlockRopeWall().setHardness(0.5F).setStepSound(Block.soundTypeCloth).setBlockName("blockRopeWall"));
        blockGrapplingHook = (new BlockGrapplingHook()).setHardness(0.0F).setStepSound(Block.soundTypeMetal).setBlockName("blockGrHk");
        blockZipLineAnchor = new BlockZipLineAnchor().setHardness(0.3F).setBlockName("blockZiplineAnchor");
        itemHookShot = new ItemHookshot().setUnlocalizedName("itemHookshot");
        itemHookShotCartridge = new ItemHookShotCartridge().setUnlocalizedName("HookshotCartridge");
        bowRopesPlus = new ItemBowRopesPlus().setUnlocalizedName("bowRopesPlus");

        EntityArrow303 baseArrow = new EntityArrow303(null);
        for (Class<?> c : coreArrowClasses)
        {
            arrows.add(constructArrowInstance(c));
        }
        //arrows.add(baseArrow);

        int index = 3;
        Configuration c = Settings_RopePlus.config;
        c.load();
        Item i;
        for (EntityArrow303 entArrow303 : arrows)
        {
            String name = entArrow303.name;
            i = makeItem(entArrow303, c, baseArrow);
            if (i != null)
            {
                i.setCreativeTab(CreativeTabs.tabCombat);
                GameRegistry.registerItem(i, name);
                entArrow303.itemId = i;
                BlockDispenser.dispenseBehaviorRegistry.putObject(i, entArrow303.getDispenserBehaviour());
            }
            EntityRegistry.registerModEntity(entArrow303.getClass(), name, index, this, 25, 5, true);
            index++;

            // System.out.println("registered "+name+" as Networked Entity for ALL TIME!!!");
        }

        hookShotEnabled = c.get(Configuration.CATEGORY_GENERAL, "Hook Shot enabled", true).getBoolean(true);
        zipLinesEnabled = c.get(Configuration.CATEGORY_GENERAL, "Ziplines enabled", true).getBoolean(true);
        grapplingEnabled = c.get(Configuration.CATEGORY_GENERAL, "Grappling Hook enabled", true).getBoolean(true);

        c.save();

        if (grapplingEnabled)
        {
            GameRegistry.registerBlock(blockGrapplingHook, "blockGrHk");
            GameRegistry.registerItem(itemGrapplingHook, "itemGrapplingHook", "RopesPlus");
        }

        GameRegistry.registerBlock(blockRopeWall, "blockRope");
        GameRegistry.registerBlock(blockRope, "blockRopeCentral");

        if (hookShotEnabled)
        {
            if (zipLinesEnabled)
            {
                GameRegistry.registerBlock(blockZipLineAnchor, "blockZiplineAnchor");
                GameRegistry.registerTileEntity(TileEntityZipLineAnchor.class, "TileEntityZipLineAnchor");
            }
            GameRegistry.registerItem(itemHookShot, "itemHookshot", "RopesPlus");
            GameRegistry.registerItem(itemHookShotCartridge, "HookshotCartridge", "RopesPlus");
        }
        GameRegistry.registerItem(bowRopesPlus, "bowRopesPlus", "RopesPlus");

        networkHelper =
                new NetworkHelper("AS_RP", ArrowChoicePacket.class, GrapplingHookPacket.class, HookshotPacket.class, HookshotPullPacket.class,
                        RopeBowSettingPacket.class, SoundPacket.class, ZiplinePacket.class);

        MinecraftForge.EVENT_BUS.register(new RopesPlusBowController());
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        for (EntityArrow303 entArrow303 : arrows)
        {
            ItemStack craftedStack = new ItemStack(entArrow303.itemId, entArrow303.craftingResults, 0);
            GameRegistry.addRecipe(craftedStack, new Object[] { "X", "#", "Y", Character.valueOf('X'), entArrow303.tip, Character.valueOf('#'),
                    Items.stick, Character.valueOf('Y'), Items.feather });
        }

        ItemStack ropeCentral = new ItemStack(blockRope, 6);
        GameRegistry.addRecipe(ropeCentral, new Object[] { " # ", " # ", " # ", Character.valueOf('#'), Items.string });

        ItemStack stackGrHk = new ItemStack(itemGrapplingHook, 1);
        GameRegistry.addRecipe(stackGrHk, new Object[] { " X ", " # ", " # ", Character.valueOf('#'), blockRope, Character.valueOf('X'),
                Items.iron_ingot });

        ItemStack stackHookShot = new ItemStack(itemHookShot, 1);
        GameRegistry.addRecipe(stackHookShot, new Object[] { "AXA", "A#A", "AYA", Character.valueOf('#'), blockRope, Character.valueOf('X'),
                itemGrapplingHook, Character.valueOf('Y'), Blocks.piston, Character.valueOf('A'), Items.iron_ingot });

        ItemStack stackZipAnchor = new ItemStack(blockZipLineAnchor, 1);
        GameRegistry.addRecipe(stackZipAnchor, new Object[] { " # ", " # ", " X ", Character.valueOf('#'), blockRope, Character.valueOf('X'),
                Items.iron_ingot });

        ItemStack stackCartridge = new ItemStack(itemHookShotCartridge, 4);
        GameRegistry.addRecipe(stackCartridge, new Object[] { " # ", "#X#", " # ", Character.valueOf('#'), Items.paper, Character.valueOf('X'),
                Items.gunpowder });

        EntityRegistry.registerModEntity(EntityGrapplingHook.class, "GrapplingHook", 1, this, 25, 5, true);
        EntityRegistry.registerModEntity(EntityFreeFormRope.class, "FreeFormRope", 2, this, 75, 5, false);

        proxy.load();
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        // NOOP
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        // NOOP
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        networkHelper.sendPacketToPlayer(new RopeBowSettingPacket(Settings_RopePlus.disableBowHook), (EntityPlayerMP) event.player);
    }

    private Item makeItem(EntityArrow303 entityarrow303, Configuration config, EntityArrow303 baseArrow)
    {
        ItemArrow303 item = null;
        if (entityarrow303 == baseArrow)
        {
            /*
             * dont replace vanilla arrow thank you
             */
        }
        else if (config.get("Arrows Enabled", entityarrow303.name, true).getBoolean(true))
        {
            entityarrow303.damage = config.get("ArrowConfig", "Damage " + entityarrow303.name, "2", "base arrow damage independent from crits/motion").getInt();
            entityarrow303.craftingResults = config.get("ArrowConfig", "CraftedStackSize " + entityarrow303.name, "4").getInt();
            item = (ItemArrow303) (new ItemArrow303(entityarrow303)).setUnlocalizedName(entityarrow303.name);
            arrowItems.add(item);
        }
        return item;
    }

    private EntityArrow303 constructArrowInstance(Class<?> clazz)
    {
        try
        {
            return (EntityArrow303) clazz.getConstructor(new Class[] { World.class }).newInstance(new Object[] { (World) null });
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }
    
    public EntityProjectileBase getArrowTemplate(EntityProjectileBase copy)
    {
        for (EntityArrow303 a : arrows)
        {
            if (a.item.isItemEqual(copy.item))
            {
                return a;
            }
        }
        return null;
    }

    public Item getArrowItemByTip(Object desiredtip)
    {
        for (Iterator<ItemArrow303> iterator = arrowItems.iterator(); iterator.hasNext();)
        {
            ItemArrow303 itemarrow303 = (ItemArrow303) iterator.next();
            if (itemarrow303.arrow.tip == desiredtip)
            {
                return (Item) itemarrow303;
            }
        }

        return null;
    }

    public void onRopeArrowHit(World world, int x, int y, int z)
    {
        int[] coords = new int[3];
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        addCoordsToRopeArray(coords);

        BlockRopePseudoEnt newent = new BlockRopePseudoEnt(world, x, y, z, 31);
        addRopeToArray(newent);
    }

    public void addRopeToArray(BlockRopePseudoEnt ent)
    {
        ropeEntArray.add(ent);
    }

    public void addRopeToArray(TileEntityRope newent)
    {
        ropeEntArray.add(newent);
    }

    public void addCoordsToRopeArray(int[] coords)
    {
        ropePosArray.add(coords);
    }

    public void removeCoordsFromRopeArray(int[] coords)
    {
        ropePosArray.remove(coords);
    }

    public int[] areCoordsArrowRope(int i, int j, int k)
    {
        for (int w = 0; w < ropePosArray.size(); w++)
        {
            int[] coords = (int[]) ropePosArray.get(w);

            if (i == coords[0] && j == coords[1] && k == coords[2])
            {
                return coords;
            }
        }
        return null;
    }

    public int selectedSlot(EntityPlayer p)
    {
        if (selectedSlotMap.get(p) == null)
        {
            return -1;
        }

        return selectedSlotMap.get(p);
    }

    public void setselectedSlot(EntityPlayer p, int i)
    {
        selectedSlotMap.put(p, i);
    }

    public HashMap<EntityPlayer, EntityGrapplingHook> getGrapplingHookMap()
    {
        return grapplingHookMap;
    }

    public EntityFreeFormRope getPlayerRope(EntityPlayer p)
    {
        return playerRopeMap.get(p);
    }

    public void setPlayerRope(EntityPlayer p, EntityFreeFormRope r)
    {
        if (r == null)
        {
            playerRopeMap.remove(p);
        }
        else
        {
            playerRopeMap.put(p, r);
        }
    }

}
