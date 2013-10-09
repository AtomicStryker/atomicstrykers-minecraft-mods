package atomicstryker.minions.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.entity.EnumMinionState;
import atomicstryker.minions.common.jobmanager.BlockTask_MineOreVein;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigByCoordinates;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigMineStairwell;
import atomicstryker.minions.common.jobmanager.Minion_Job_Manager;
import atomicstryker.minions.common.jobmanager.Minion_Job_StripMine;
import atomicstryker.minions.common.jobmanager.Minion_Job_TreeHarvest;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;


@Mod(modid = "AS_Minions", name = "Minions", version = "1.7.2")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, connectionHandler = ConnectionHandler.class)
public class MinionsCore
{
    @SidedProxy(clientSide = "atomicstryker.minions.client.ClientProxy", serverSide = "atomicstryker.minions.common.CommonProxy")
    public static CommonProxy proxy;
    
	private static long time = System.currentTimeMillis();
	
	private static long firstBootTime = System.currentTimeMillis();
	private static boolean hasBooted;
	
    public static int evilDeedXPCost = 2;
    public static int minionsPerPlayer = 4;
    public static ArrayList<EvilDeed> evilDoings = new ArrayList<EvilDeed>();
    
    private static float exhaustAmountSmall;
    private static float exhaustAmountBig;
    
	public static int masterStaffItemID = 2527;
    public static Item itemMastersStaff;
    
    public static HashSet<Integer> foundTreeBlocks = new HashSet<Integer>();
    public static HashSet<Integer> configWorthlessBlocks = new HashSet<Integer>();
    public static CopyOnWriteArrayList<Minion_Job_Manager> runningJobList = new CopyOnWriteArrayList<Minion_Job_Manager>();
    public static CopyOnWriteArrayList<Minion_Job_Manager> finishedJobList = new CopyOnWriteArrayList<Minion_Job_Manager>();
    public static HashMap<String, Vector<EntityMinion>> masterNames = new HashMap<String, Vector<EntityMinion>>();
    public static HashMap<String, Integer> masterCommits = new HashMap<String, Integer>();
    
    public static int secondsWithoutMasterDespawn;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            cfg.load();
            masterStaffItemID = cfg.get(Configuration.CATEGORY_ITEM, "masterStaffItemID", 2527).getInt();
            evilDeedXPCost = cfg.get(Configuration.CATEGORY_GENERAL, "evilDeedXPCost", 2).getInt();
            minionsPerPlayer = cfg.get(Configuration.CATEGORY_GENERAL, "minionsAmountPerPlayer", 4).getInt();
            
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").comment = "Food cost per tick of casting lightning";
            exhaustAmountSmall = Float.valueOf(cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").getString());
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostBig", "20").comment = "Food cost of summoning Minions and giving complex orders";
            exhaustAmountBig = Float.valueOf(cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostBig", "20").getString());
            
            secondsWithoutMasterDespawn = cfg.get(Configuration.CATEGORY_GENERAL, "automaticDespawnDelay", 300, "Time in seconds after which a Minion without a Master ingame despawns").getInt();
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "Minions has a problem loading it's configuration!");
        }
        finally
        {
            cfg.save();
        }
        
        String configpath = event.getSuggestedConfigurationFile().getAbsolutePath();
        configpath = configpath.replaceFirst(".cfg", "_Advanced.cfg");
        initializeSettingsFile(new File(configpath));
        
        itemMastersStaff = (new ItemMastersStaff(masterStaffItemID)).setUnlocalizedName("masterstaff");;
        GameRegistry.registerItem(itemMastersStaff, "masterstaff");
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.load(evt);
        
        MinecraftForge.EVENT_BUS.register(new MinionsChunkManager());
        MinecraftForge.EVENT_BUS.register(this);
        
        EntityRegistry.registerModEntity(EntityMinion.class, "AS_EntityMinion", 1, this, 25, 5, true);
        
        LanguageRegistry.instance().addStringLocalization("item.masterstaff.name", "en_US", "Master's Staff");
        
        proxy.registerRenderInformation();
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        getViableTreeBlocks();
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        //System.out.println("FML Minions Server Load!");
        
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        NetworkRegistry.instance().registerChannel(new ServerPacketHandler(), MinionsCore.getPacketChannel(), Side.SERVER);
    }
    
    @ForgeSubscribe
    public void onWorldUnLoad(WorldEvent.Unload event)
    {
        System.out.println("Minions detected World unload");
        finishedJobList.clear();
        runningJobList.clear();
        masterNames.clear();
        masterCommits.clear();
        MinionsChunkManager.onWorldUnloaded();
    }
	
    public static void onTick(World world)
    {
        if (!hasBooted)
        {
            if (System.currentTimeMillis() > firstBootTime + 10000L)
            {
                hasBooted = true;
            }
        }
        else if (System.currentTimeMillis() > time)
        {
            time = System.currentTimeMillis() + 1000L;
            
            MinionsChunkManager.updateLoadedChunks();
        }

        Iterator<Minion_Job_Manager> iter = runningJobList.iterator();
        while (iter.hasNext())
        {
            if (finishedJobList.contains(iter))
            {
                finishedJobList.remove(iter);
                runningJobList.remove(iter);
            }
            else
            {
                ((Minion_Job_Manager) iter.next()).onJobUpdateTick();
            }
        }        
        ChickenLightningBolt.update();
    }
	
    public static boolean isBlockValueable(int blockID)
    {
        if (blockID == 0
        || blockID == Block.dirt.blockID
        || blockID == Block.grass.blockID
        || blockID == Block.stone.blockID
        || blockID == Block.cobblestone.blockID
        || blockID == Block.gravel.blockID
        || blockID == Block.sand.blockID
        || blockID == Block.leaves.blockID
        || blockID == Block.obsidian.blockID
        || blockID == Block.bedrock.blockID
        || blockID == Block.stairsCobblestone.blockID
        || blockID == Block.netherrack.blockID
        || blockID == Block.slowSand.blockID
        || blockID == Block.snow.blockID
        || configWorthlessBlocks.contains(blockID))
        {
            return false;
        }
        
        return true;
    }
    
    private void getViableTreeBlocks()
    {       
        for (Block iter : Block.blocksList)
        {
            if (iter != null && iter.getUnlocalizedName() != null && (iter instanceof BlockLog || iter.getUnlocalizedName().contains("log")))
            {
                foundTreeBlocks.add(iter.blockID);
            }
        }
    }

    public static boolean isBlockIDViableTreeBlock(int ID)
    {
        return foundTreeBlocks.contains(ID);
    }
    
    public static boolean hasPlayerWillPower(EntityPlayer player)
    {
        return player.getFoodStats().getFoodLevel() > 3 || player.capabilities.isCreativeMode;
    }
    
    public static void exhaustPlayerSmall(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            player.getFoodStats().addExhaustion(exhaustAmountSmall);
        }
    }
    
    public static void exhaustPlayerBig(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            player.getFoodStats().addExhaustion(exhaustAmountBig);
        }
    }
    
    public static void onJobHasFinished(Minion_Job_Manager input)
    {
        if (!finishedJobList.contains(input))
        {
            finishedJobList.add(input);
        }
    }

    public static void cancelRunningJobsForMaster(String name)
    {
        Minion_Job_Manager temp;
        Iterator<Minion_Job_Manager> iter = runningJobList.iterator();
        while (iter.hasNext())
        {
            temp = (Minion_Job_Manager) iter.next();
            if (temp != null && temp.masterName != null && temp.masterName.equals(name))
            {
                temp.onJobFinished();
            }
        }
    }
    
    /**
     * Gets HashSet associated with Username containing registered Minions
     * Creates one if none exists and returns the empty new one
     */
    private static Vector<EntityMinion> getMinionsForMaster(String name)
    {
        Vector<EntityMinion> set = masterNames.get(name);
        if (set == null)
        {
            set = new Vector<EntityMinion>(minionsPerPlayer);
            masterNames.put(name, set);
        }
        return set;
    }
    
    public static void unregisterMinion(EntityMinion ent)
    {
        for (Entry<String, Vector<EntityMinion>> entry : masterNames.entrySet())
        {
            entry.getValue().remove(ent);
        }
    }

    public static void minionLoadRegister(EntityMinion ent)
    {        
        if (ent.getMasterUserName().equals("undef"))
        {
            System.out.println("Loaded Minion without masterName, killing");
            ent.setDead();
            return;
        }

        System.out.println("Loaded Minion from NBT, re-registering master: "+ent.getMasterUserName());
        String mastername = ent.getMasterUserName();

        Vector<EntityMinion> minions = getMinionsForMaster(mastername);
        if (minions.size() >= minionsPerPlayer)
        {
            System.out.println("Added a new minion too many for "+mastername+", killing it NOW");
            ent.setDead();
            return;
        }
        else if (!minions.add(ent))
        {
            System.out.println("Minion was already loaded, error happening here");
        }
        System.out.println("added additional minion for "+mastername+", now registered: "+minions.size());
    }

    public static void onMasterAddedEvil(EntityPlayer player)
    {
        if (masterCommits.get(player.username) != null)
        {
            int commits = (Integer) masterCommits.get(player.username);
            commits++;

            if (commits == 4)
            {
            	proxy.playSoundAtEntity(player, "minions:thegodshaverewardedyouroffering", 1.0F, 1.0F);
                // give master item to player
                player.inventory.addItemStackToInventory(new ItemStack(MinionsCore.itemMastersStaff.itemID, 1, 0));
            }
            else
            {
                masterCommits.put(player.username, commits);
                proxy.playSoundAtEntity(player, "minions:thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
            }
        }
        else
        {
            masterCommits.put(player.username, 1);
            proxy.playSoundAtEntity(player, "minions:thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
        }
    }
    
    public static Entity findEntityByID(World world, int ID)
    {
        return world.getEntityByID(ID);
    }
    
    public static boolean hasPlayerMinions(EntityPlayer player)
    {
        return proxy.hasPlayerMinions(player);
    }
    
    public static boolean hasAllMinions(EntityPlayer player)
    {
        return getMinionsForMaster(player.username).size() >= minionsPerPlayer;
    }
    
    public static void orderMinionToPickupEntity(EntityPlayer playerEnt, EntityLivingBase target)
    {        
        for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
        {
            minion.master = playerEnt;

            if (minion.riddenByEntity == null)
            {
                minion.targetEntityToGrab = (EntityLivingBase) target;
                minion.currentState = EnumMinionState.STALKING_TO_GRAB;
                proxy.sendSoundToClients(minion, "minions:grabanimalorder");
                break;
            }
        }
    }
    
    public static void orderMinionToDrop(EntityPlayer playerEnt, EntityMinion minion)
    {
        if (minion.riddenByEntity != null)
        {
            proxy.sendSoundToClients(minion, "minions:foryou");
            minion.riddenByEntity.mountEntity(null);
        }
        else if (minion.inventory.containsItems())
        {
            proxy.sendSoundToClients(minion, "minions:foryou");
            minion.dropAllItemsToWorld();
        }
    }
    
    /**
     * Tries to spawn additional Minions for a player, checks first if he needs any
     * 
     * @param playerEnt Player calling the spawning
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @return true if a Minion was actually spawned, false otherwise
     */
    public static boolean spawnMinionsForPlayer(EntityPlayer playerEnt, int x, int y, int z)
    {   
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        if (minions.size() < minionsPerPlayer)
        {

            final EntityMinion minion = new EntityMinion(playerEnt.worldObj, playerEnt);
            minion.setPosition(x, y+1, z);
            playerEnt.worldObj.spawnEntityInWorld(minion);
            MinionsCore.proxy.sendSoundToClients(minion, "minions:minionspawn");
            minions.add(minion);
            //System.out.println("spawned missing minion for "+var3.username);
            return true;
        }
        
        //AS_EntityMinion[] readout = (AS_EntityMinion[]) masterNames.get(playerEnt.username);
        orderMinionsToMoveTo(playerEnt, x, y, z);
        return false;
    }
    
    public static void orderMinionsToChopTrees(EntityPlayer playerEnt, int x, int y, int z)
    {
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        for (EntityMinion minion : minions)
        {
            minion.master = playerEnt;
            minion.giveTask(null, true);
        }
        
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.size() > 0)
        {
            runningJobList.add(new Minion_Job_TreeHarvest(minions, x, y, z));
            proxy.sendSoundToClients(getMinionsForMaster(playerEnt.username).iterator().next(), "minions:ordertreecutting");
        }
    }
    
    public static void orderMinionsToDigStairWell(EntityPlayer playerEnt, int x, int y, int z)
    {
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        for (EntityMinion minion : minions)
        {
            minion.master = playerEnt;
            minion.giveTask(null, true);
        }
        
        // stairwell job
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.size() > 0)
        {
            runningJobList.add(new Minion_Job_DigMineStairwell(minions, x, y-1, z));
            proxy.sendSoundToClients(minions.firstElement(), "minions:ordermineshaft");
        }        
    }
    
    public static void orderMinionsToDigStripMineShaft(EntityPlayer playerEnt, int x, int y, int z)
    {
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
        {
            minion.master = playerEnt;
            minion.giveTask(null, true);
        }
        
        // strip mine job
        if (minions.size() > 0)
        {
            runningJobList.add(new Minion_Job_StripMine(minions, x, y-1, z));
            proxy.sendSoundToClients(minions.firstElement(), "minions:randomorder");
        }
    }
    
    public static void orderMinionsToChestBlock(EntityPlayer playerEnt, boolean sneaking, int x, int y, int z)
    { 
        TileEntity chestOrInventoryBlock;
        if ((chestOrInventoryBlock = playerEnt.worldObj.getBlockTileEntity(x, y-1, z)) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            if (!sneaking)
            {
                cancelRunningJobsForMaster(playerEnt.username);
            }
            
            Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
            if (minions.size() > 0)
            {
                proxy.sendSoundToClients(minions.firstElement(), "minions:randomorder");
                for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
                {
                    minion.master = playerEnt;
                    if (!sneaking)
                    {
                        minion.giveTask(null, true);
                        minion.currentState = EnumMinionState.RETURNING_GOODS;
                    }
                    minion.returnChestOrInventory = (TileEntity) chestOrInventoryBlock;
                }
            }
        }
    }
    
    public static void orderMinionsToMoveTo(EntityPlayer playerEnt, int x, int y, int z)
    {
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.size() > 0)
        {
            proxy.sendSoundToClients(minions.firstElement(), "minions:randomorder");
            for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
            {
                minion.master = playerEnt;
                minion.giveTask(null, true);
                minion.currentState = EnumMinionState.IDLE;
                minion.orderMinionToMoveTo(x, y, z, false);
            }
        }
    }
    
    public static void orderMinionsToMineOre(EntityPlayer playerEnt, int x, int y, int z)
    {        
        if (isBlockValueable(playerEnt.worldObj.getBlockId(x, y-1, z)))
        {
            Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
            cancelRunningJobsForMaster(playerEnt.username);
            
            if (minions.size() > 0)
            {
                proxy.sendSoundToClients(minions.firstElement(), "minions:randomorder");
                for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
                {
                    if (!minion.hasTask())
                    {
                        minion.giveTask(new BlockTask_MineOreVein(null, minion, x, y-1, z));
                        break;
                    }
                }
            }
        }
    }
    
    public static void orderMinionsToFollow(EntityPlayer playerEnt)
    {
        cancelRunningJobsForMaster(playerEnt.username);
        
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        if (minions.size() > 0)
        {
            proxy.sendSoundToClients(minions.firstElement(), "minions:orderfollowplayer");
            for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
            {
                minion.master = playerEnt;
                minion.giveTask(null, true);
                minion.currentState = EnumMinionState.FOLLOWING_PLAYER;
            }
        }
    }
    
    public static void unSummonPlayersMinions(EntityPlayer playerEnt)
    {
        for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
        {
    			minion.master = playerEnt;
    			minion.dropAllItemsToWorld();
    			minion.setDead();
    	}
    	masterNames.remove(playerEnt.username);
    	
    	Object[] toSend = { proxy.hasPlayerMinions(playerEnt) ? 1 : 0, hasAllMinions(playerEnt) ? 1 : 0 }; // HasMinions override call from server to client
    	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), (Player) playerEnt);
    }
    
    public static void orderMinionsToDigCustomSpace(EntityPlayer playerEnt, int x, int y, int z, int XZsize, int ySize)
    {
        Vector<EntityMinion> minions = getMinionsForMaster(playerEnt.username);
        for (EntityMinion minion : getMinionsForMaster(playerEnt.username))
        {
            minion.master = playerEnt;
            minion.giveTask(null, true);
        }
        
        // custom dig job
        runningJobList.add(new Minion_Job_DigByCoordinates(minions, x, y-1, z, XZsize, ySize));
        proxy.playSoundAtEntity(playerEnt, "minions:randomorder", 1.0F, 1.0F);
    }
    
    private void initializeSettingsFile(File settingsFile)
    {
        try
        {
            if (settingsFile.exists())
            {
                System.out.println(settingsFile.getPath()+" found and opened");
                BufferedReader var1 = new BufferedReader(new FileReader(settingsFile));

                String lineString;
                while ((lineString = var1.readLine()) != null)
                {
                    if (!lineString.startsWith("//"))
                    {
                        lineString = lineString.trim();
                        if (lineString.startsWith("registerBlockIDasTreeBlock"))
                        {
                            String[] stringArray = lineString.split(":");
                            int id = Integer.parseInt(stringArray[1]);
                            foundTreeBlocks.add(id);
                            System.out.println("Config: registered additional tree block ID "+id);
                        }
                        else if (lineString.startsWith("registerBlockIDasWorthlessBlock"))
                        {
                            String[] stringArray = lineString.split(":");
                            int id = Integer.parseInt(stringArray[1]);
                            configWorthlessBlocks.add(id);
                            System.out.println("Config: registered additional worthless block ID "+id);
                        }
                        else
                        {
                            String[] stringArray = lineString.split(":");

                            EvilDeed deed = new EvilDeed(stringArray[0], stringArray[1], Integer.parseInt(stringArray[2]));
                            evilDoings.add(deed);
                        }
                    }
                }

                var1.close();
                
                System.out.println(settingsFile.getPath()+" parsed, registered "+evilDoings.size()+" evil Deeds.");
            }
            else
            {
                System.out.println("Could not open "+settingsFile.getPath()+", you suck");
            }
        }
        catch (Exception var6)
        {
            System.out.println("EXCEPTION BufferedReader: " + var6);
        }
    }
    
    public class ServerTickHandler implements ITickHandler
    {
        private final EnumSet<TickType> tickTypes = EnumSet.of(TickType.WORLD);
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            // NOOP
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            onTick((World)tickData[0]);
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

        @Override
        public String getLabel()
        {
            return "MinionsTickServer";
        }
    }
    
    public class ServerPacketHandler implements IPacketHandler
    {
        @Override
        public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
        {
            MinionsServer.onPacketData(manager, packet, player);
        }
    }

    public static String getPacketChannel()
    {
        return "AS_Minions";
    }
}
