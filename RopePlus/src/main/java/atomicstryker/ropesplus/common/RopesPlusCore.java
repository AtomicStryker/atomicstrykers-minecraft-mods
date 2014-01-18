package atomicstryker.ropesplus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.ropesplus.client.ClientPacketHandler;
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
import atomicstryker.ropesplus.common.network.ForgePacketWrapper;
import atomicstryker.ropesplus.common.network.PacketDispatcher;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "RopesPlus", name = "Ropes+", version = "1.5.1")
public class RopesPlusCore
{
    @SidedProxy(clientSide = "atomicstryker.ropesplus.client.ClientProxy", serverSide = "atomicstryker.ropesplus.common.CommonProxy")
    public static IProxy proxy;
    
	public final static Class<?> coreArrowClasses[] =
	{
			EntityArrow303Dirt.class,
			EntityArrow303Ex.class,
			EntityArrow303Fire.class,
			EntityArrow303Grass.class,
			EntityArrow303Ice.class,
			EntityArrow303Laser.class,
			EntityArrow303Slime.class,
			EntityArrow303Torch.class, 
			EntityArrow303Warp.class,
			EntityArrow303Confusion.class,
			EntityArrow303Rope.class,
			EntityArrow303RedStoneTorch.class
	};
	
	public static List<EntityArrow303> arrows;
	public static Item bowRopesPlus;
	public static List<ItemArrow303> arrowItems;
	public static RopesPlusCore instance;
	
    public static Block blockRopeCentralPos;
    public static List<Object> ropeEntArray;
    private static List<int[]> ropePosArray;
    
    public static Block blockRopeWallPos;
    public static Block blockGrapplingHook;
    public static Item itemGrapplingHook;
    private static HashMap<EntityPlayer, EntityGrapplingHook> grapplingHookMap;
    
    /* Arrow Selection Maps */
    private static HashMap<EntityPlayer, Integer> selectedSlotMap;
    public static Item itemHookShot;
    public static Block blockZipLineAnchor;
    public static Item itemHookShotCartridge;
    
    private HashMap<EntityPlayer, EntityFreeFormRope> playerRopeMap;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	instance = this;
    	arrows = new ArrayList<EntityArrow303>();
    	arrowItems = new ArrayList<ItemArrow303>();
    	ropeEntArray = new ArrayList<Object>();
    	ropePosArray = new ArrayList<int[]>();
    	grapplingHookMap = new HashMap<EntityPlayer, EntityGrapplingHook>();
    	selectedSlotMap = new HashMap<EntityPlayer, Integer>();
    	new HashMap<EntityPlayer, Boolean>();
    	playerRopeMap = new HashMap<EntityPlayer, EntityFreeFormRope>();
    	
        Settings_RopePlus.InitSettings(event.getSuggestedConfigurationFile());
        proxy.loadConfig(Settings_RopePlus.config);
        
        blockRopeCentralPos = (new BlockRopeCenter().func_149711_c(0.3F).func_149663_c("blockRopeCentral"));
        itemGrapplingHook = new ItemGrapplingHook().setUnlocalizedName("itemGrapplingHook");
        blockRopeWallPos = (new BlockRopeWall().func_149711_c(0.5F).func_149672_a(Block.field_149775_l).func_149663_c("blockRope"));
        blockGrapplingHook = (new BlockGrapplingHook()).func_149711_c(0.0F).func_149672_a(Block.field_149788_p).func_149663_c("blockGrHk");
        blockZipLineAnchor = new BlockZipLineAnchor().func_149711_c(0.3F).func_149663_c("blockZiplineAnchor");
        itemHookShot = new ItemHookshot().setUnlocalizedName("itemHookshot");
        itemHookShotCartridge = new ItemHookShotCartridge().setUnlocalizedName("HookshotCartridge");
        bowRopesPlus = new ItemBowRopesPlus().setUnlocalizedName("bowRopesPlus");
        
        for(Class<?> c : coreArrowClasses)
        {
            addArrowToRegister(constructArrowInstance(c));
        }
        arrows.add(new EntityArrow303(null));
        
        int index = 3;
        EntityArrow303 entArrow303 = null;
        Configuration c = Settings_RopePlus.config;
        c.load();
        Item i;
        for(Iterator<EntityArrow303> iter = RopesPlusCore.arrows.iterator(); iter.hasNext();)
        {
            entArrow303 = (EntityArrow303)iter.next();
            String name = (new StringBuilder()).append(entArrow303.name).append("303").toString();
            i = makeItem(entArrow303, c);
            if (i != null)
            {
                i.setCreativeTab(CreativeTabs.tabCombat);
                GameRegistry.registerItem(i, name);
                entArrow303.itemId = i;
                BlockDispenser.field_149943_a.putObject(i, entArrow303.getDispenserBehaviour());
            }
            EntityRegistry.registerModEntity(entArrow303.getClass(), name, index, this, 25, 5, true);
            index++;
            
            //System.out.println("registered "+name+" as Networked Entity for ALL TIME!!!");
        }
        c.save();
        
        GameRegistry.registerBlock(blockGrapplingHook, "blockGrHk");
        GameRegistry.registerItem(itemGrapplingHook, "itemGrapplingHook", "RopesPlus");
        GameRegistry.registerBlock(blockRopeWallPos, "blockRope");
        GameRegistry.registerBlock(blockRopeCentralPos, "blockRopeCentral");
        GameRegistry.registerBlock(blockZipLineAnchor, "blockZiplineAnchor");
        GameRegistry.registerItem(itemHookShot, "itemHookshot", "RopesPlus");
        GameRegistry.registerItem(itemHookShotCartridge, "HookshotCartridge", "RopesPlus");
        GameRegistry.registerItem(bowRopesPlus, "bowRopesPlus", "RopesPlus");
        GameRegistry.registerTileEntity(TileEntityZipLineAnchor.class, "TileEntityZipLineAnchor");
        
        PacketDispatcher.init("AS_RP", new ClientPacketHandler(), new ServerPacketHandler());
        
        MinecraftForge.EVENT_BUS.register(new RopesPlusBowController());
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {        
        ItemStack ropeCentral = new ItemStack(blockRopeCentralPos, 6);
        GameRegistry.addRecipe(ropeCentral, new Object[] {" # ", " # ", " # ", Character.valueOf('#'), Items.string});
        
        ItemStack stackGrHk = new ItemStack(itemGrapplingHook, 1);
        GameRegistry.addRecipe(stackGrHk, new Object[] {" X ", " # ", " # ", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), Items.iron_ingot});
        
        ItemStack stackHookShot = new ItemStack(itemHookShot, 1);
        GameRegistry.addRecipe(stackHookShot, new Object[] {"AXA", "A#A", "AYA", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), itemGrapplingHook, Character.valueOf('Y'), Blocks.piston,  Character.valueOf('A'), Items.iron_ingot});
        
        ItemStack stackZipAnchor = new ItemStack(blockZipLineAnchor, 1);
        GameRegistry.addRecipe(stackZipAnchor, new Object[] {" # ", " # ", " X ", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), Items.iron_ingot});
        
        ItemStack stackCartridge = new ItemStack(itemHookShotCartridge, 4);
        GameRegistry.addRecipe(stackCartridge, new Object[] {" # ", "#X#", " # ", Character.valueOf('#'), Items.paper, Character.valueOf('X'), Items.gunpowder});
        
		LanguageRegistry.addName(blockRopeCentralPos, "Rope");
		LanguageRegistry.addName(blockRopeWallPos, "GrHk Rope");
		LanguageRegistry.addName(blockGrapplingHook, "Grappling Hook");
		LanguageRegistry.addName(stackHookShot, "Hookshot");
		LanguageRegistry.addName(itemGrapplingHook, "Grappling Hook");
		LanguageRegistry.addName(blockZipLineAnchor, "Zipline Anchor");
		LanguageRegistry.addName(stackZipAnchor, "Zipline Anchor");
		LanguageRegistry.addName(itemHookShotCartridge, "Hookshot Cartridge");
        
        EntityRegistry.registerModEntity(EntityGrapplingHook.class, "GrapplingHook", 1, this, 25, 5, true);
        EntityRegistry.registerModEntity(EntityFreeFormRope.class, "FreeFormRope", 2, this, 75, 5, false);
        
		LanguageRegistry.addName(bowRopesPlus, "RopesPlusBow");
        
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
        Object[] input = { Settings_RopePlus.disableBowHook };
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("RopesPlus", 8, input), event.player);
    }

	private Item makeItem(EntityArrow303 entityarrow303, Configuration config)
	{
	    ItemArrow303 item = null;
		if(entityarrow303.itemId == Items.arrow)
		{
		    /*
		     * dont replace vanilla arrow thank you
			 */
		}
		else if (Items.arrow != null && config.get("Arrows Enabled", entityarrow303.name, true).getBoolean(true))
		{
            entityarrow303.configuredDamage = config.get("ArrowConfig", "Damage "+entityarrow303.name, "4").getInt();
            entityarrow303.craftingResults = config.get("ArrowConfig", "CraftedStackSize "+entityarrow303.name, "4").getInt();
		    
			item = (ItemArrow303) (new ItemArrow303(entityarrow303)).setUnlocalizedName(entityarrow303.name);
			ItemStack craftedStack = new ItemStack(entityarrow303.itemId, entityarrow303.craftingResults, 0);
			
			GameRegistry.addRecipe(craftedStack, new Object[] {
				"X", "#", "Y", Character.valueOf('X'), entityarrow303.tip, Character.valueOf('#'), Items.stick, Character.valueOf('Y'), Items.feather
			});

			arrowItems.add(item);
			LanguageRegistry.instance();
			LanguageRegistry.addName(item, entityarrow303.name);
		}
		return item;
	}

	public static EntityArrow303 constructArrowInstance(Class<?> class1)
	{
		try
		{
			return (EntityArrow303)class1.getConstructor(new Class[] {World.class}).newInstance(new Object[] {(World)null});
		}
		catch(Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}

	public static void addArrowToRegister(EntityArrow303 entityarrow303)
	{
		arrows.add(entityarrow303);
	}
	
	public static Item getArrowItemByTip(Object desiredtip)
	{
		for(Iterator<ItemArrow303> iterator = arrowItems.iterator(); iterator.hasNext();)
		{
			ItemArrow303 itemarrow303 = (ItemArrow303)iterator.next();
			if(itemarrow303.arrow.tip == desiredtip)
			{
				return (Item)itemarrow303;
			}
		}

		return null;
	}
	
	public static void onRopeArrowHit(World world, int x, int y, int z)
	{
		int[] coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		addCoordsToRopeArray(coords);
		
		BlockRopePseudoEnt newent = new BlockRopePseudoEnt(world, x, y, z, 31);
		addRopeToArray(newent);
	}
	
	public static void addRopeToArray(BlockRopePseudoEnt ent)
	{
		ropeEntArray.add(ent);
	}
	
	public static void addRopeToArray(TileEntityRope newent)
	{
		ropeEntArray.add(newent);
	}
	
	public static void addCoordsToRopeArray(int[] coords)
	{
		ropePosArray.add(coords);
	}
	
	public static void removeCoordsFromRopeArray(int[] coords)
	{
		ropePosArray.remove(coords);
	}
	
	public static int[] areCoordsArrowRope(int i, int j, int k)
	{
		for(int w = 0; w < ropePosArray.size(); w++)
		{
			int[] coords = (int[])ropePosArray.get(w);
			
			if (i == coords[0] && j == coords[1] && k == coords[2])
			{
				return coords;
			}
		}
		return null;
	}
    
    public static int selectedSlot(EntityPlayer p)
    {
        if (selectedSlotMap.get(p) == null)
        {
            return 0;
        }
        
        return selectedSlotMap.get(p);
    }
    
    public static void setselectedSlot(EntityPlayer p, int i)
    {
        selectedSlotMap.put(p, i);      
    }
    
    public static HashMap<EntityPlayer, EntityGrapplingHook> getGrapplingHookMap()
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
