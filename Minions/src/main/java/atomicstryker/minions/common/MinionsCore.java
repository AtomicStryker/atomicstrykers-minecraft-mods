package atomicstryker.minions.common;

import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.jobmanager.*;
import atomicstryker.minions.common.network.*;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


@Mod(modid = "minions", name = "Minions", version = "1.9.8")
public class MinionsCore
{
    @SidedProxy(clientSide = "atomicstryker.minions.client.ClientProxy", serverSide = "atomicstryker.minions.common.CommonProxy")
    public static IProxy proxy;
    
    @Instance(value = "minions")
    public static MinionsCore instance;
    
    public NetworkHelper networkHelper;
	
	private long firstBootTime = System.currentTimeMillis();
	private boolean hasBooted;
	
    public int evilDeedXPCost = 2;
    public int minionsPerPlayer = 4;
    public final ArrayList<EvilDeed> evilDoings;
    
    private float exhaustAmountSmall;
    private float exhaustAmountBig;
    
    public Item itemMastersStaff;
    
    public int secondsWithoutMasterDespawn;
    public double minionFollowRange = 30d;
    
    public final HashSet<Block> foundTreeBlocks;
    public final HashSet<Block> configWorthlessBlocks;
    public final ConcurrentLinkedQueue<Minion_Job_Manager> runningJobList;
    
    private final HashMap<String, ArrayList<EntityMinion>> minionMap;
    private boolean minionMapLock;
    
    private static boolean debugMode;
    
    private EvilCommitCount commitStorage;
    private final ArrayList<FutureDeedEvent> delayedDeedEvents;
    
    public MinionsCore()
    {
        evilDoings = new ArrayList<EvilDeed>();
        foundTreeBlocks = new HashSet<Block>();
        configWorthlessBlocks = new HashSet<Block>();
        runningJobList = new ConcurrentLinkedQueue<Minion_Job_Manager>();
        minionMap = new HashMap<String, ArrayList<EntityMinion>>();
        minionMapLock = false;
        delayedDeedEvents = new ArrayList<FutureDeedEvent>();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        networkHelper =
                new NetworkHelper("AS_M", AssignChestPacket.class, ChopTreesPacket.class, CustomDigPacket.class, DigOreVeinPacket.class,
                        DigStairwellPacket.class, DropAllPacket.class, EvilDeedPacket.class, FollowPacket.class, HasMinionsPacket.class,
                        HaxPacket.class, LightningPacket.class, MinionMountPacket.class, MinionSpawnPacket.class, MovetoPacket.class,
                        PickupEntPacket.class, RequestXPSettingPacket.class, SoundPacket.class, StripminePacket.class, UnsummonPacket.class);
        
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            cfg.load();
            evilDeedXPCost = cfg.get(Configuration.CATEGORY_GENERAL, "evilDeedXPCost", 2).getInt();
            minionsPerPlayer = cfg.get(Configuration.CATEGORY_GENERAL, "minionsAmountPerPlayer", 4).getInt();
            
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").setComment("Food cost per tick of casting lightning");
            exhaustAmountSmall = Float.valueOf(cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").getString());
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostBig", "20").setComment("Food cost of summoning Minions and giving complex orders");
            exhaustAmountBig = Float.valueOf(cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostBig", "20").getString());
            
            secondsWithoutMasterDespawn = cfg.get(Configuration.CATEGORY_GENERAL, "automaticDespawnDelay", 300, "Time in seconds after which a Minion without a Master ingame despawns").getInt();
            minionFollowRange = cfg.get(Configuration.CATEGORY_GENERAL, "minionFollowRange", 30d, "Distance to be used by MC follower pathing. MC default is 12. High values may impede performance").getDouble(30d);
            
            debugMode = cfg.get(Configuration.CATEGORY_GENERAL, "debugMode", false, "Enables debug printing. LOTS OF IT.").getBoolean(false);
        }
        catch (Exception e)
        {
            System.err.println("Minions has a problem loading it's configuration!");
        }
        finally
        {
            cfg.save();
        }
        
        String configpath = event.getSuggestedConfigurationFile().getAbsolutePath();
        configpath = configpath.replaceFirst(".cfg", "_Advanced.cfg");
        initializeSettingsFile(new File(configpath));
        
        itemMastersStaff = (new ItemMastersStaff()).setUnlocalizedName("masterstaff");
        GameRegistry.register(itemMastersStaff);
        
        MinecraftForge.EVENT_BUS.register(this);
        EntityRegistry.registerModEntity(EntityMinion.class, "AS_EntityMinion", 1, this, 25, 5, true);
        
        proxy.preInit(event);
        
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkLoaderCallback());
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.load(evt);
        
        proxy.registerRenderInformation();
    }
    
    @SubscribeEvent
    public void onEntityJoinsWorld(EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP)
        {
            EntityPlayerMP p = (EntityPlayerMP) event.getEntity();
            networkHelper.sendPacketToPlayer(new RequestXPSettingPacket(evilDeedXPCost), p);
            
            MinionsCore.instance.prepareMinionHolder(p.getGameProfile().getName());
            networkHelper.sendPacketToPlayer(new HasMinionsPacket(MinionsCore.instance.hasPlayerMinions(p) ? 1 : 0, MinionsCore.instance.hasAllMinions(p) ? 1 : 0), p);
        }
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        getViableTreeBlocks();
    }
    
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (!event.getWorld().isRemote)
        {
            runningJobList.clear();
            getMinionMap().clear();
            hasBooted = false;
            commitStorage = null;
            firstBootTime = System.currentTimeMillis();
        }
    }
	
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (!tick.world.isRemote && tick.phase == Phase.END)
        {
            if (!hasBooted)
            {
                if (System.currentTimeMillis() > firstBootTime + 3000L)
                {
                    hasBooted = true;
                    commitStorage = (EvilCommitCount) tick.world.getPerWorldStorage().loadData(EvilCommitCount.class, "minionsCommits");
                    debugPrint("Minions loaded evil commit storage: "+commitStorage);
                    if (commitStorage == null)
                    {
                        commitStorage = new EvilCommitCount("minionsCommits");
                        tick.world.getPerWorldStorage().setData("minionsCommits", commitStorage);
                        debugPrint("Minions stored new evil commit storage: "+commitStorage);
                    }
                }
            }
            
            for (Iterator <Minion_Job_Manager> iter = runningJobList.iterator(); iter.hasNext();)
            {
                if (iter.next().onJobUpdateTick())
                {
                    iter.remove();
                }
            }
            
            ChickenLightningBolt.update();
            
            for (Iterator <FutureDeedEvent> iter = delayedDeedEvents.iterator(); iter.hasNext();)
            {
                FutureDeedEvent e = iter.next();
                if (System.currentTimeMillis() >= e.targetTime)
                {
                    sendSoundToClients(e.player, e.sound);
                    if (e.staff)
                    {
                        e.player.inventory.addItemStackToInventory(new ItemStack(itemMastersStaff, 1, 0));
                    }
                    iter.remove();
                }
            }
        }
    }
	
    public boolean isBlockValueable(Block blockID)
    {

        return !(blockID == Blocks.AIR
                || blockID == Blocks.DIRT
                || blockID == Blocks.GRASS
                || blockID == Blocks.STONE
                || blockID == Blocks.COBBLESTONE
                || blockID == Blocks.GRAVEL
                || blockID == Blocks.SAND
                || blockID == Blocks.LEAVES
                || blockID == Blocks.OBSIDIAN
                || blockID == Blocks.BEDROCK
                || blockID == Blocks.STONE_BRICK_STAIRS
                || blockID == Blocks.NETHERRACK
                || blockID == Blocks.SOUL_SAND
                || blockID == Blocks.SNOW
                || configWorthlessBlocks.contains(blockID));

    }
    
    private void getViableTreeBlocks()
    {
        Iterator<Block> iterator = Block.REGISTRY.iterator();
        while (iterator.hasNext())
        {
            Block iter = iterator.next();
            if (iter instanceof BlockLog || iter.getLocalizedName().contains("log"))
            {
                debugPrint("Minions found viable TreeBlock: "+iter);
                foundTreeBlocks.add(iter);
            }
        }
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

    private void cancelRunningJobsForMaster(String name)
    {
        for (Minion_Job_Manager temp : runningJobList)
        {
            if (name.equals(temp.masterName))
            {
                temp.onJobFinished();
            }
        }
    }
    
    private HashMap<String, ArrayList<EntityMinion>> getMinionMap()
    {
        long wait = System.currentTimeMillis()+333l;
        while (minionMapLock)
        {
            if (System.currentTimeMillis() > wait)
            {
                System.out.println("Minions: minionMapLock was hanging");
                return minionMap;
            }
            Thread.yield();
        }        
        minionMapLock = true;
        return minionMap;
    }
    
    public ArrayList<EntityMinion> prepareMinionHolder(String username)
    {
        HashMap<String, ArrayList<EntityMinion>> map = getMinionMap();
        ArrayList<EntityMinion> list = map.get(username);
        if (list == null)
        {
            System.out.println("New Minionlist prepared for user "+username);
            list = new ArrayList<EntityMinion>();
            map.put(username, list);
        }
        minionMapLock = false;
        return list;
    }
    
    public EntityMinion[] getMinionsForMaster(EntityPlayer p)
    {
        EntityMinion[] minions = getMinionsForMasterName(p.getName());
        minionMapLock = false;
        for (EntityMinion m : minions)
        {
            m.master = p;
        }
        return minions;
    }
    
    private EntityMinion[] getMinionsForMasterName(String n)
    {
        HashMap<String, ArrayList<EntityMinion>> map = getMinionMap();
        ArrayList<EntityMinion> l = map.get(n);
        if (l == null)
        {
            System.out.println("Minions got faulty request for username "+n+", no Minionlist prepared?!");
            minionMapLock = false;
            
            l = prepareMinionHolder(n);
            return new EntityMinion[0];
        }
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
        ArrayList<EntityMinion> l = getMinionMap().get(masterName);
        if (l == null)
        {
            System.out.println("Minions got faulty push for username "+masterName+", no Minionlist prepared?!");
            minionMapLock = false;
            l = prepareMinionHolder(masterName);
        }
        l.add(m);
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
        }
        System.out.println("added additional minion for "+mastername+", now registered: "+minions.length);
    }
    
    public void onMasterAddedEvil(EntityPlayer player, int soundLength)
    {
        FutureDeedEvent event = new FutureDeedEvent();
        
        event.player = player;
        event.targetTime = System.currentTimeMillis() + (soundLength*1000l);
        event.staff = false;
        event.sound = "minions:thegodsarepleaseedwithyoursacrifice";
        if (commitStorage.masterCommits.get(player.getGameProfile().getName()) != null)
        {
            int commits = commitStorage.masterCommits.get(player.getGameProfile().getName());
            commits++;
            if (commits >= 4)
            {
                event.sound = "minions:thegodshaverewardedyouroffering";
                event.staff = true;
                commits -= 4;
            }
            commitStorage.masterCommits.put(player.getGameProfile().getName(), commits);
        }
        else
        {
            commitStorage.masterCommits.put(player.getGameProfile().getName(), 1);
        }
        delayedDeedEvents.add(event);
        commitStorage.markDirty();
    }
    
    private class FutureDeedEvent
    {
        public long targetTime;
        public String sound;
        public EntityPlayer player;
        public boolean staff;
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
            if (minion.getPassengers().isEmpty())
            {
                minion.targetEntityToGrab = target;
                sendSoundToClients(minion, "minions:grabanimalorder");
                break;
            }
        }
    }
    
    public void orderMinionToDrop(EntityPlayer playerEnt, EntityMinion minion)
    {
        MinionsCore.debugPrint("Minion got drop order: "+minion);
        if (!minion.getPassengers().isEmpty())
        {
            sendSoundToClients(minion, "minions:foryou");
            minion.removePassengers();
        }
        else if (minion.inventory.containsItems())
        {
            sendSoundToClients(minion, "minions:foryou");
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
            sendSoundToClients(minion, "minions:minionspawn");
            offerMinionToMap(minion, playerEnt.getGameProfile().getName());
            minion.currentTarget = new BlockPos(x, y, z);
            //System.out.println("spawned missing minion for "+var3.getGameProfile().getName());
            return true;
        }
        
        //AS_EntityMinion[] readout = (AS_EntityMinion[]) masterNames.get(playerEnt.getGameProfile().getName());
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
        
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
        if (minions.length > 0)
        {
            runningJobList.add(new Minion_Job_TreeHarvest(Lists.newArrayList(minions), x, y, z));
            sendSoundToClients(minions[0], "minions:ordertreecutting");
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
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
        if (minions.length > 0)
        {
            runningJobList.add(new Minion_Job_DigMineStairwell(Lists.newArrayList(minions), x, y-1, z));
            sendSoundToClients(minions[0], "minions:ordermineshaft");
        }        
    }
    
    public void orderMinionsToDigStripMineShaft(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        for (EntityMinion minion : minions)
        {
            if (!minion.isStripMining)
            {
                for (Minion_Job_Manager mgr : runningJobList)
                {
                    mgr.setWorkerFree(minion);
                }
                
                runningJobList.add(new Minion_Job_StripMine(minion, x, y-1, z));
                sendSoundToClients(minions[0], "minions:randomorder");
                break;
            }
        }
    }
    
    public void orderMinionsToChestBlock(EntityPlayer playerEnt, boolean sneaking, int x, int y, int z)
    { 
        TileEntity chestOrInventoryBlock;
        if ((chestOrInventoryBlock = playerEnt.worldObj.getTileEntity(new BlockPos(x, y-1, z))) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            if (!sneaking)
            {
                cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
            }
            
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            if (minions.length > 0)
            {
                sendSoundToClients(minions[0], "minions:randomorder");
                for (EntityMinion minion : minions)
                {
                    if (!sneaking)
                    {
                        minion.giveTask(null, true);
                        minion.returningGoods = true;
                    }
                    minion.returnChestOrInventory = chestOrInventoryBlock;
                }
            }
        }
    }
    
    public void orderMinionsToMoveTo(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
        if (minions.length > 0)
        {
            sendSoundToClients(minions[0], "minions:randomorder");
            for (EntityMinion minion : minions)
            {
                minion.giveTask(null, true);
                minion.orderMinionToMoveTo(x, y, z, false);
            }
        }
    }
    
    public void orderMinionsToMineOre(EntityPlayer playerEnt, int x, int y, int z)
    {        
        if (isBlockValueable(playerEnt.worldObj.getBlockState(new BlockPos(x, y-1, z)).getBlock()))
        {
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
            
            if (minions.length > 0)
            {
                sendSoundToClients(minions[0], "minions:randomorder");
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
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
        EntityMinion[] minions = getMinionsForMaster(playerEnt);
        if (minions.length > 0)
        {
            sendSoundToClients(minions[0], "minions:orderfollowplayer");
            for (EntityMinion minion : minions)
            {
                minion.giveTask(null, true);
                minion.followingMaster = true;
            }
        }
    }
    
    public void unSummonPlayersMinions(EntityPlayerMP playerEnt)
    {
        System.out.println("Minions: unSummonPlayersMinions called by "+playerEnt);
        for (EntityMinion minion : getMinionsForMaster(playerEnt))
        {
            System.out.println("Minions: Killing "+minion);
            minion.setDead();
        }
    	
        networkHelper.sendPacketToPlayer(new HasMinionsPacket(0, 0), playerEnt);
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
        sendSoundToClients(playerEnt, "minions:randomorder");
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
                        if (lineString.startsWith("registerBlockIDasTreeBlock:"))
                        {
                            Block id = Block.REGISTRY.getObject(new ResourceLocation(lineString.substring(lineString.indexOf(":")+1)));
                            if (id != Blocks.AIR)
                            {
                                foundTreeBlocks.add(id);
                                System.out.println("Config: registered additional tree block ID "+id);
                            }
                        }
                        else if (lineString.startsWith("registerBlockIDasWorthlessBlock:"))
                        {
                            Block id = Block.REGISTRY.getObject(new ResourceLocation(lineString.substring(lineString.indexOf(":")+1)));
                            if (id != Blocks.AIR)
                            {
                                configWorthlessBlocks.add(id);
                                System.out.println("Config: registered additional worthless block ID "+id);
                            }
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
    
    public void sendSoundToClients(Entity ent, String resource)
    {
        MinionsCore.debugPrint("sendSoundToClients "+ent+", "+resource);
        SoundEvent sound = new SoundEvent(new ResourceLocation(resource));
        SoundCategory category = SoundCategory.AMBIENT;
        if (!ent.worldObj.isRemote && FMLCommonHandler.instance().getMinecraftServerInstance() != null)
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendToAllNearExcept(null, ent.posX, ent.posY, ent.posZ, 16D, ent.dimension,
                            new SPacketSoundEffect(sound, category, ent.posX, ent.posY, ent.posZ, 1f, 1f));
        }
    }
    
    public static void debugPrint(String s)
    {
        if (debugMode)
        {
            System.out.println(s);
        }
    }
    
    public static class EvilCommitCount extends WorldSavedData
    {
        protected final HashMap<String, Integer> masterCommits;

        public EvilCommitCount(String par1Str)
        {
            super(par1Str);
            masterCommits = new HashMap<String, Integer>();
        }

        @Override
        public void readFromNBT(NBTTagCompound tags)
        {
            debugPrint("EvilCommitCount readFromNBT");
            for(String s : (Set<String>)tags.getKeySet())
            {
                debugPrint("loaded master "+s+" with commitcount "+tags.getInteger(s));
                masterCommits.put(s, tags.getInteger(s));
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound tags)
        {
            debugPrint("EvilCommitCount writeToNBT");
            for (String s : masterCommits.keySet())
            {
                tags.setInteger(s, masterCommits.get(s));
            }
        }
    }
    
    public static class ChunkLoaderCallback implements LoadingCallback
    {
		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world)
		{
            for (Ticket t : tickets)
            {
                for (ChunkCoordIntPair c : t.getChunkList())
                {
                    // just load the chunk. The minion entities inside will re-register their own tickets
                    // note: im 99% sure forge does this by itself. but whatevers
                    ForgeChunkManager.fetchDormantChunk(ChunkCoordIntPair.chunkXZ2Int(c.chunkXPos, c.chunkZPos), world);
                }
                // get rid of these old tickets
                ForgeChunkManager.releaseTicket(t);
            }
		}
    }
    
}
