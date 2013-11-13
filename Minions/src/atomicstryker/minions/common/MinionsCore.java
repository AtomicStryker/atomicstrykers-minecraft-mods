package atomicstryker.minions.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
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
import atomicstryker.minions.common.jobmanager.BlockTask_MineOreVein;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigByCoordinates;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigMineStairwell;
import atomicstryker.minions.common.jobmanager.Minion_Job_Manager;
import atomicstryker.minions.common.jobmanager.Minion_Job_StripMine;
import atomicstryker.minions.common.jobmanager.Minion_Job_TreeHarvest;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
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


@Mod(modid = "AS_Minions", name = "Minions", version = "1.7.6")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, connectionHandler = ConnectionHandler.class)
public class MinionsCore
{
    @SidedProxy(clientSide = "atomicstryker.minions.client.ClientProxy", serverSide = "atomicstryker.minions.common.CommonProxy")
    public static CommonProxy proxy;
    
    @Instance(value = "AS_Minions")
    public static MinionsCore instance;
    
	private long time = System.currentTimeMillis();
	
	private long firstBootTime = System.currentTimeMillis();
	private boolean hasBooted;
	
    public int evilDeedXPCost = 2;
    public int minionsPerPlayer = 4;
    public final ArrayList<EvilDeed> evilDoings;
    
    private float exhaustAmountSmall;
    private float exhaustAmountBig;
    
	public int masterStaffItemID = 2527;
    public Item itemMastersStaff;
    
    public int secondsWithoutMasterDespawn;
    public double minionFollowRange = 30d;
    
    public final HashSet<Integer> foundTreeBlocks;
    public final HashSet<Integer> configWorthlessBlocks;
    public final LinkedList<Minion_Job_Manager> runningJobList;
    public final LinkedList<Minion_Job_Manager> finishedJobList;
    public final HashMap<String, Integer> masterCommits;
    private final HashMap<String, ArrayList<EntityMinion>> minionMap;
    private boolean minionMapLock;
    
    private final MinionsChunkManager chunkLoader;
    
    public MinionsCore()
    {
        evilDoings = new ArrayList<EvilDeed>();
        foundTreeBlocks = new HashSet<Integer>();
        configWorthlessBlocks = new HashSet<Integer>();
        runningJobList = new LinkedList<Minion_Job_Manager>();
        finishedJobList = new LinkedList<Minion_Job_Manager>();
        masterCommits = new HashMap<String, Integer>();
        minionMap = new HashMap<String, ArrayList<EntityMinion>>();
        minionMapLock = false;
        chunkLoader = new MinionsChunkManager();
    }

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
            minionFollowRange = cfg.get(Configuration.CATEGORY_GENERAL, "minionFollowRange", 30d, "Distance to be used by MC follower pathing. MC default is 12. High values may impede performance").getDouble(30d);
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
        
        MinecraftForge.EVENT_BUS.register(chunkLoader);
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
        getMinionMap().clear();
        minionMapLock = false;
        masterCommits.clear();
        chunkLoader.onWorldUnloaded();
    }
	
    public void onTick(World world)
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
            
            chunkLoader.updateLoadedChunks();
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
	
    public boolean isBlockValueable(int blockID)
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

    public boolean isBlockIDViableTreeBlock(int ID)
    {
        return foundTreeBlocks.contains(ID);
    }
    
    public boolean hasPlayerWillPower(EntityPlayer player)
    {
        return player.getFoodStats().getFoodLevel() > 3 || player.capabilities.isCreativeMode;
    }
    
    public void exhaustPlayerSmall(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            player.getFoodStats().addExhaustion(exhaustAmountSmall);
        }
    }
    
    public void exhaustPlayerBig(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            player.getFoodStats().addExhaustion(exhaustAmountBig);
        }
    }
    
    public void onJobHasFinished(Minion_Job_Manager input)
    {
        if (!finishedJobList.contains(input))
        {
            finishedJobList.add(input);
        }
    }

    private void cancelRunningJobsForMaster(String name)
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
    
    private HashMap<String, ArrayList<EntityMinion>> getMinionMap()
    {
        while (minionMapLock) {}
        minionMapLock = true;
        return minionMap;
    }
    
    public void prepareMinionHolder(String username)
    {
        if (getMinionMap().get(username) == null)
        {
            minionMapLock = false;
            System.out.println("New Minionlist prepared for user "+username);
            getMinionMap().put(username, new ArrayList<EntityMinion>());
        }
        minionMapLock = false;
    }
    
    /**
     * Gets Collection associated with Username containing registered Minions
     * Creates one if none exists and returns the empty new one
     */
    public EntityMinion[] getMinionsForMaster(EntityPlayer p)
    {
        EntityMinion[] minions = getMinionsForMasterName(p.username);
        minionMapLock = false;
        for (EntityMinion m : minions)
        {
            m.master = p;
        }
        return minions;
    }
    
    private EntityMinion[] getMinionsForMasterName(String n)
    {
        List<EntityMinion> l = getMinionMap().get(n);
        Iterator<EntityMinion> iter = l.iterator();
        while (iter.hasNext())
        {
            if (iter.next().isDead)
            {
                iter.remove();
            }
        }
        return l.toArray(new EntityMinion[l.size()]);
    }
    
    private void offerMinionToMap(EntityMinion m, String masterName)
    {
        getMinionMap().get(masterName).add(m);
        minionMapLock = false;
    }

    public void minionLoadRegister(EntityMinion ent)
    {        
        if (ent.getMasterUserName().equals("undef"))
        {
            System.out.println("Loaded Minion without masterName, killing");
            ent.setDead();
            return;
        }

        System.out.println("Loaded Minion from NBT, re-registering master: "+ent.getMasterUserName());
        String mastername = ent.getMasterUserName();
        
        prepareMinionHolder(mastername);

        EntityMinion[] minions = getMinionsForMasterName(mastername);
        minionMapLock = false;
        if (minions.length >= minionsPerPlayer)
        {
            System.out.println("Added a new minion too many for "+mastername+", killing it NOW");
            ent.setDead();
            return;
        }
        else
        {
            offerMinionToMap(ent, mastername);
            chunkLoader.registerChunkLoaderEntity(ent);
        }
        System.out.println("added additional minion for "+mastername+", now registered: "+minions.length);
    }

    public void onMasterAddedEvil(EntityPlayer player)
    {
        if (masterCommits.get(player.username) != null)
        {
            int commits = (Integer) masterCommits.get(player.username);
            commits++;

            if (commits == 4)
            {
            	proxy.playSoundAtEntity(player, "minions:thegodshaverewardedyouroffering", 1.0F, 1.0F);
                // give master item to player
                player.inventory.addItemStackToInventory(new ItemStack(itemMastersStaff.itemID, 1, 0));
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
    
    public boolean hasPlayerMinions(EntityPlayer player)
    {
        return getMinionsForMaster(player).length > 0;
    }
    
    public boolean hasAllMinions(EntityPlayer player)
    {
        return getMinionsForMaster(player).length >= minionsPerPlayer;
    }
    
    public void orderMinionToPickupEntity(EntityPlayer playerEnt, EntityLivingBase target)
    {
        for (EntityMinion minion : getMinionsForMaster(playerEnt))
        {
            if (minion.riddenByEntity == null)
            {
                minion.targetEntityToGrab = (EntityLivingBase) target;
                proxy.sendSoundToClients(minion, "minions:grabanimalorder");
                break;
            }
        }
    }
    
    public void orderMinionToDrop(EntityPlayer playerEnt, EntityMinion minion)
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
    public boolean spawnMinionsForPlayer(EntityPlayer playerEnt, int x, int y, int z)
    {   
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        if (minions.length < minionsPerPlayer)
        {
            final EntityMinion minion = new EntityMinion(playerEnt.worldObj, playerEnt);
            minion.setPosition(x, y+1, z);
            playerEnt.worldObj.spawnEntityInWorld(minion);
            MinionsCore.proxy.sendSoundToClients(minion, "minions:minionspawn");
            offerMinionToMap(minion, playerEnt.username);
            //System.out.println("spawned missing minion for "+var3.username);
            return true;
        }
        
        //AS_EntityMinion[] readout = (AS_EntityMinion[]) masterNames.get(playerEnt.username);
        orderMinionsToMoveTo(playerEnt, x, y, z);
        return false;
    }
    
    public void orderMinionsToChopTrees(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        for (EntityMinion minion : minions)
        {
            minion.giveTask(null, true);
        }
        
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.length > 0)
        {
            runningJobList.add(new Minion_Job_TreeHarvest(Lists.newArrayList(minions), x, y, z));
            proxy.sendSoundToClients(minions[0], "minions:ordertreecutting");
        }
    }
    
    public void orderMinionsToDigStairWell(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        for (EntityMinion minion : minions)
        {
            minion.giveTask(null, true);
        }
        
        // stairwell job
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.length > 0)
        {
            runningJobList.add(new Minion_Job_DigMineStairwell(Lists.newArrayList(minions), x, y-1, z));
            proxy.sendSoundToClients(minions[0], "minions:ordermineshaft");
        }        
    }
    
    public void orderMinionsToDigStripMineShaft(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        for (EntityMinion minion : minions)
        {
            minion.giveTask(null, true);
        }
        
        // strip mine job
        if (minions.length > 0)
        {
            runningJobList.add(new Minion_Job_StripMine(Lists.newArrayList(minions), x, y-1, z));
            proxy.sendSoundToClients(minions[0], "minions:randomorder");
        }
    }
    
    public void orderMinionsToChestBlock(EntityPlayer playerEnt, boolean sneaking, int x, int y, int z)
    { 
        TileEntity chestOrInventoryBlock;
        if ((chestOrInventoryBlock = playerEnt.worldObj.getBlockTileEntity(x, y-1, z)) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            if (!sneaking)
            {
                cancelRunningJobsForMaster(playerEnt.username);
            }
            
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            if (minions.length > 0)
            {
                proxy.sendSoundToClients(minions[0], "minions:randomorder");
                for (EntityMinion minion : minions)
                {
                    if (!sneaking)
                    {
                        minion.giveTask(null, true);
                        minion.returningGoods = true;
                    }
                    minion.returnChestOrInventory = (TileEntity) chestOrInventoryBlock;
                }
            }
        }
    }
    
    public void orderMinionsToMoveTo(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        
        cancelRunningJobsForMaster(playerEnt.username);
        
        if (minions.length > 0)
        {
            proxy.sendSoundToClients(minions[0], "minions:randomorder");
            for (EntityMinion minion : minions)
            {
                minion.giveTask(null, true);
                minion.orderMinionToMoveTo(x, y, z, false);
            }
        }
    }
    
    public void orderMinionsToMineOre(EntityPlayer playerEnt, int x, int y, int z)
    {        
        if (isBlockValueable(playerEnt.worldObj.getBlockId(x, y-1, z)))
        {
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            cancelRunningJobsForMaster(playerEnt.username);
            
            if (minions.length > 0)
            {
                proxy.sendSoundToClients(minions[0], "minions:randomorder");
                for (EntityMinion minion : minions)
                {
                    if (!minion.hasTask())
                    {
                        minion.giveTask(new BlockTask_MineOreVein(null, minion, x, y-1, z), true);
                        break;
                    }
                }
            }
        }
    }
    
    public void orderMinionsToFollow(EntityPlayer playerEnt)
    {
        cancelRunningJobsForMaster(playerEnt.username);
        
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        if (minions.length > 0)
        {
            proxy.sendSoundToClients(minions[0], "minions:orderfollowplayer");
            for (EntityMinion minion : minions)
            {
                minion.giveTask(null, true);
                minion.followingMaster = true;
            }
        }
    }
    
    public void unSummonPlayersMinions(EntityPlayer playerEnt)
    {
        for (EntityMinion minion : getMinionsForMaster(playerEnt))
        {
    			minion.setDead();
    	}
    	
    	Object[] toSend = { proxy.hasPlayerMinions(playerEnt) ? 1 : 0, hasAllMinions(playerEnt) ? 1 : 0 }; // HasMinions override call from server to client
    	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), (Player) playerEnt);
    }
    
    public void orderMinionsToDigCustomSpace(EntityPlayer playerEnt, int x, int y, int z, int XZsize, int ySize)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        for (EntityMinion minion : minions)
        {
            minion.giveTask(null, true);
        }
        
        // custom dig job
        runningJobList.add(new Minion_Job_DigByCoordinates(Lists.newArrayList(minions), x, y-1, z, XZsize, ySize));
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

    public final static String getPacketChannel()
    {
        return "AS_Minions";
    }
    
}
