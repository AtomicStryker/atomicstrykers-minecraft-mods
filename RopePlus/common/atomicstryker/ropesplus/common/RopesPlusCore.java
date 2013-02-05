package atomicstryker.ropesplus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.ropesplus.client.ClientPacketHandler;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Confusion;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Dirt;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ex;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Fire;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Grass;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ice;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Laser;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Rope;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Slime;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Torch;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Warp;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "RopesPlus", name = "Ropes+", version = "1.3.6")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
connectionHandler = ConnectionHandler.class,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_Ropes"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_Ropes"}, packetHandler = ServerPacketHandler.class))
public class RopesPlusCore
{
    @SidedProxy(clientSide = "atomicstryker.ropesplus.client.ClientProxy", serverSide = "atomicstryker.ropesplus.common.CommonProxy")
    public static IProxy proxy;
    
	public final static Class coreArrowClasses[] =
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
			EntityArrow303Rope.class
	};
	
	public static List<EntityArrow303> arrows;
	public static Item bowRopesPlus;
	public static List<ItemArrow303> arrowItems;
	public static RopesPlusCore instance;
	
    public static Block blockRopeCentralPos;
    public static List ropeEntArray;
    private static List<int[]> ropePosArray;
    
    public static Block blockRopeWallPos;
    public static Block blockGrapplingHook;
    public static Item itemGrapplingHook;
    private static HashMap<EntityPlayer, EntityGrapplingHook> grapplingHookMap;
    
    /* Arrow Selection Maps */
    private static HashMap<EntityPlayer, Integer> selectedSlotMap;
    private HashMap<EntityPlayer, Boolean> cycledMap;
    
    public static Item itemHookShot;
    public static Block blockZipLineAnchor;
    public static Item itemHookShotCartridge;
    
    private HashMap<EntityPlayer, EntityFreeFormRope> playerRopeMap;
	
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	instance = this;
    	arrows = new ArrayList();
    	arrowItems = new ArrayList();
    	ropeEntArray = new ArrayList();
    	ropePosArray = new ArrayList();
    	grapplingHookMap = new HashMap<EntityPlayer, EntityGrapplingHook>();
    	selectedSlotMap = new HashMap<EntityPlayer, Integer>();
    	cycledMap = new HashMap<EntityPlayer, Boolean>();
    	playerRopeMap = new HashMap<EntityPlayer, EntityFreeFormRope>();
    	
        Settings_RopePlus.InitSettings(event.getSuggestedConfigurationFile());
        proxy.loadConfig(Settings_RopePlus.config);
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        blockRopeCentralPos = (new BlockRopeCenter(Settings_RopePlus.blockIdRopeDJRoslin, Settings_RopePlus.ropeTexture)).setHardness(0.3F).setBlockName("blockRopeCentral");

        itemGrapplingHook = new ItemGrapplingHook(Settings_RopePlus.itemIdGrapplingHook).setIconIndex(13).setItemName("itemGrapplingHook");
        blockRopeWallPos = (new BlockRopeWall(Settings_RopePlus.blockIdRope, Settings_RopePlus.ropeTexture)).setHardness(0.5F).setStepSound(Block.soundClothFootstep).setBlockName("blockRope");
        blockGrapplingHook = (new BlockGrapplingHook(Settings_RopePlus.blockIdGrapplingHook, 0)).setHardness(0.0F).setStepSound(Block.soundMetalFootstep).setBlockName("blockGrHk");
        
        blockZipLineAnchor = new BlockZipLineAnchor(Settings_RopePlus.blockIdZipLineAnchor, 3).setHardness(0.3F).setBlockName("blockZiplineAnchor");
        
        itemHookShot = new ItemHookshot(Settings_RopePlus.itemIdHookShot).setIconIndex(17).setItemName("itemHookshot");
        itemHookShotCartridge = new Item(Settings_RopePlus.itemIdHookshotCartridge).setIconIndex(18).setTextureFile("/atomicstryker/ropesplus/client/ropesPlusItems.png").setItemName("HookshotCartridge");
        
        GameRegistry.registerBlock(blockGrapplingHook, "blockGrHk");
        GameRegistry.registerBlock(blockRopeWallPos, "blockRope");
        GameRegistry.registerBlock(blockRopeCentralPos, "blockRopeCentral");
        GameRegistry.registerBlock(blockZipLineAnchor, "blockZiplineAnchor");
        GameRegistry.registerTileEntity(TileEntityZipLineAnchor.class, "TileEntityZipLineAnchor");
        
        ItemStack ropeCentral = new ItemStack(blockRopeCentralPos, 6);
        GameRegistry.addRecipe(ropeCentral, new Object[] {" # ", " # ", " # ", Character.valueOf('#'), Item.silk});
        
        ItemStack stackGrHk = new ItemStack(itemGrapplingHook, 1);
        GameRegistry.addRecipe(stackGrHk, new Object[] {" X ", " # ", " # ", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), Item.ingotIron});
        
        ItemStack stackHookShot = new ItemStack(itemHookShot, 1);
        GameRegistry.addRecipe(stackHookShot, new Object[] {"AXA", "A#A", "AYA", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), itemGrapplingHook, Character.valueOf('Y'), Block.pistonBase,  Character.valueOf('A'), Item.ingotIron});
        
        ItemStack stackZipAnchor = new ItemStack(blockZipLineAnchor, 1);
        GameRegistry.addRecipe(stackZipAnchor, new Object[] {" # ", " # ", " X ", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), Item.ingotIron});
        
        ItemStack stackCartridge = new ItemStack(itemHookShotCartridge, 4);
        GameRegistry.addRecipe(stackCartridge, new Object[] {" # ", "#X#", " # ", Character.valueOf('#'), Item.paper, Character.valueOf('X'), Item.gunpowder});
        
        LanguageRegistry.instance().addName(blockRopeCentralPos, "Rope");
        LanguageRegistry.instance().addName(blockRopeWallPos, "GrHk Rope");
        LanguageRegistry.instance().addName(blockGrapplingHook, "Grappling Hook");
        LanguageRegistry.instance().addName(stackHookShot, "Hookshot");
        LanguageRegistry.instance().addName(itemGrapplingHook, "Grappling Hook");
        LanguageRegistry.instance().addName(blockZipLineAnchor, "Zipline Anchor");
        LanguageRegistry.instance().addName(stackZipAnchor, "Zipline Anchor");
        LanguageRegistry.instance().addName(itemHookShotCartridge, "Hookshot Cartridge");
        
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        
        EntityRegistry.registerModEntity(EntityGrapplingHook.class, "GrapplingHook", 1, this, 25, 5, true);
        EntityRegistry.registerModEntity(EntityFreeFormRope.class, "FreeFormRope", 2, this, 75, 5, false);
        
        bowRopesPlus = new ItemBowRopesPlus(Settings_RopePlus.itemIdRopesPlusBow).setIconCoord(5, 1).setItemName("bowRopesPlus");
        LanguageRegistry.instance().addName(bowRopesPlus, "RopesPlusBow");
        
        for(Class c : coreArrowClasses)
        {
            addArrowToRegister(constructArrowInstance(c));
        }
        arrows.add(new EntityArrow303(null));
        
        MinecraftForge.EVENT_BUS.register(new RopesPlusBowController());
        
        proxy.load();
    }
    
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        int index = 3;
        EntityArrow303 entArrow303 = null;
        Configuration c = Settings_RopePlus.config;
        c.load();
        for(Iterator iter = RopesPlusCore.arrows.iterator(); iter.hasNext();)
        {
            entArrow303 = (EntityArrow303)iter.next();
            makeItem(entArrow303, c);
            String name = (new StringBuilder()).append(entArrow303.name).append("303").toString();
            
            EntityRegistry.registerModEntity(entArrow303.getClass(), name, index, this, 25, 5, true);
            index++;
            
            //System.out.println("registered "+name+" as Networked Entity for ALL TIME!!!");
        }
        c.save();
    }
    
    @ServerStarted
    public void serverStarted(FMLServerStartedEvent event)
    {
        // cant think of anything yet
    }

	private static void makeItem(EntityArrow303 entityarrow303, Configuration config)
	{
	    ItemArrow303 item = null;
		if(entityarrow303.itemId == Item.arrow.itemID)
		{
		    /*
		     * dont replace vanilla arrow thank you
			 */
		}
		else if (Item.arrow != null && config.get("Arrows Enabled", entityarrow303.name, true).getBoolean(true))
		{
			item = (ItemArrow303) (new ItemArrow303(entityarrow303.itemId - 256, entityarrow303)).setItemName(entityarrow303.name);
			ItemStack craftedStack = new ItemStack(entityarrow303.itemId, entityarrow303.craftingResults, 0);
			
			GameRegistry.addRecipe(craftedStack, new Object[] {
				"X", "#", "Y", Character.valueOf('X'), entityarrow303.tip, Character.valueOf('#'), Item.stick, Character.valueOf('Y'), Item.feather
			});
			
			entityarrow303.configuredDamage = config.get("ArrowConfig", "Damage "+entityarrow303.name, "4").getInt();
			entityarrow303.craftingResults = config.get("ArrowConfig", "CraftedStackSize "+entityarrow303.name, "4").getInt();

			arrowItems.add(item);
			LanguageRegistry.instance().addName(item, entityarrow303.name);
			item.setIconIndex(entityarrow303.getArrowIconIndex());
		}
	}

	public static EntityArrow303 constructArrowInstance(Class class1)
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
		for(Iterator iterator = arrowItems.iterator(); iterator.hasNext();)
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
