package atomicstryker.minions.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import atomicstryker.minions.client.ClientProxy.ClientPacketHandler;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.jobmanager.BlockTask_MineOreVein;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigByCoordinates;
import atomicstryker.minions.common.jobmanager.Minion_Job_DigMineStairwell;
import atomicstryker.minions.common.jobmanager.Minion_Job_Manager;
import atomicstryker.minions.common.jobmanager.Minion_Job_StripMine;
import atomicstryker.minions.common.jobmanager.Minion_Job_TreeHarvest;
import atomicstryker.minions.common.network.ForgePacketWrapper;
import atomicstryker.minions.common.network.PacketDispatcher;
import atomicstryker.minions.common.network.PacketDispatcher.IPacketHandler;
import atomicstryker.minions.common.network.PacketDispatcher.WrappedPacket;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;


@Mod(modid = "AS_Minions", name = "Minions", version = "1.8.4")
public class MinionsCore
{
    @SidedProxy(clientSide = "atomicstryker.minions.client.ClientProxy", serverSide = "atomicstryker.minions.common.CommonProxy")
    public static CommonProxy proxy;
    
    @Instance(value = "AS_Minions")
    public static MinionsCore instance;
	
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
    public final LinkedList<Minion_Job_Manager> runningJobList;
    public final LinkedList<Minion_Job_Manager> finishedJobList;
    
    private final HashMap<String, ArrayList<EntityMinion>> minionMap;
    private boolean minionMapLock;
    
    private boolean debugMode;
    
    private EvilCommitCount commitStorage;
    
    public MinionsCore()
    {
        evilDoings = new ArrayList<EvilDeed>();
        foundTreeBlocks = new HashSet<Block>();
        configWorthlessBlocks = new HashSet<Block>();
        runningJobList = new LinkedList<Minion_Job_Manager>();
        finishedJobList = new LinkedList<Minion_Job_Manager>();
        minionMap = new HashMap<String, ArrayList<EntityMinion>>();
        minionMapLock = false;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        PacketDispatcher.init("AS_M", new ClientPacketHandler(), new ServerPacketHandler());
        
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            cfg.load();
            evilDeedXPCost = cfg.get(Configuration.CATEGORY_GENERAL, "evilDeedXPCost", 2).getInt();
            minionsPerPlayer = cfg.get(Configuration.CATEGORY_GENERAL, "minionsAmountPerPlayer", 4).getInt();
            
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").comment = "Food cost per tick of casting lightning";
            exhaustAmountSmall = Float.valueOf(cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostSmall", "1.5").getString());
            cfg.get(Configuration.CATEGORY_GENERAL, "FoodCostBig", "20").comment = "Food cost of summoning Minions and giving complex orders";
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
        
        itemMastersStaff = (new ItemMastersStaff()).setUnlocalizedName("masterstaff");;
        GameRegistry.registerItem(itemMastersStaff, "masterstaff");
        
        MinecraftForge.EVENT_BUS.register(this);
        EntityRegistry.registerModEntity(EntityMinion.class, "AS_EntityMinion", 1, this, 25, 5, true);
        
        proxy.preInit(event);
        
        FMLCommonHandler.instance().bus().register(this);
        
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
        if (!event.world.isRemote && event.entity instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) event.entity;
            Object[] toSend = {MinionsCore.instance.evilDeedXPCost};
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend), p);
            
            MinionsCore.instance.prepareMinionHolder(p.getGameProfile().getName());
            Object[] toSend2 = {MinionsCore.instance.hasPlayerMinions(p) ? 1 : 0, MinionsCore.instance.hasAllMinions(p) ? 1 : 0};
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend2), p);
        }
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        getViableTreeBlocks();
    }
	
    public void onTick(World world)
    {
        if (!hasBooted)
        {
            if (System.currentTimeMillis() > firstBootTime + 3000L)
            {
                hasBooted = true;
                commitStorage = (EvilCommitCount) world.perWorldStorage.loadData(EvilCommitCount.class, "minionsCommits");
                if (commitStorage == null)
                {
                    commitStorage = new EvilCommitCount("minionsCommits");
                    world.perWorldStorage.setData("minionsCommits", commitStorage);
                }
            }
        }
        
        Iterator<Minion_Job_Manager> iter = runningJobList.iterator();
        while (iter.hasNext())
        {
            if (finishedJobList.contains(iter))
            {
                debugPrint("Now removing finished Job: "+iter);
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
	
    public boolean isBlockValueable(Block blockID)
    {
        if (blockID == Blocks.air
        || blockID == Blocks.dirt
        || blockID == Blocks.grass
        || blockID == Blocks.stone
        || blockID == Blocks.cobblestone
        || blockID == Blocks.gravel
        || blockID == Blocks.sand
        || blockID == Blocks.leaves
        || blockID == Blocks.obsidian
        || blockID == Blocks.bedrock
        || blockID == Blocks.stone_brick_stairs
        || blockID == Blocks.netherrack
        || blockID == Blocks.soul_sand
        || blockID == Blocks.snow
        || configWorthlessBlocks.contains(blockID))
        {
            return false;
        }
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private void getViableTreeBlocks()
    {
        for (String s : (Set<String>)GameData.blockRegistry.getKeys())
        {
            Block iter = GameData.blockRegistry.getObject(s);
            if (iter instanceof BlockLog || iter.getLocalizedName().contains("log"))
            {
                debugPrint("Minions found viable TreeBlock: "+iter);
                foundTreeBlocks.add(iter);
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
        long wait = System.currentTimeMillis()+1000l;
        while (minionMapLock)
        {
            if (System.currentTimeMillis() > wait)
            {
                throw new ConcurrentModificationException("Minions: minionMapLock is hanging");
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
        EntityMinion[] minions = getMinionsForMasterName(p.getCommandSenderName());
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
            return (EntityMinion[]) prepareMinionHolder(n).toArray();
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

    public void onMasterAddedEvil(EntityPlayer player)
    {
        if (commitStorage.masterCommits.get(player.getGameProfile().getName()) != null)
        {
            int commits = commitStorage.masterCommits.get(player.getGameProfile().getName());
            commits++;

            if (commits % 4 == 0)
            {
                // check existing staff
                if (!player.inventory.hasItem(itemMastersStaff))
                {
                    proxy.playSoundAtEntity(player, "minions:thegodshaverewardedyouroffering", 1.0F, 1.0F);
                    // give master item to player
                    player.inventory.addItemStackToInventory(new ItemStack(itemMastersStaff, 1, 0));
                }
            }
            else
            {
                commitStorage.masterCommits.put(player.getGameProfile().getName(), commits);
                proxy.playSoundAtEntity(player, "minions:thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
            }
        }
        else
        {
            commitStorage.masterCommits.put(player.getGameProfile().getName(), 1);
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
            offerMinionToMap(minion, playerEnt.getGameProfile().getName());
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
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
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
            if (!minion.isStripMining)
            {
                for (Minion_Job_Manager mgr : runningJobList)
                {
                    mgr.setWorkerFree(minion);
                }
                
                runningJobList.add(new Minion_Job_StripMine(minion, x, y-1, z));
                proxy.sendSoundToClients(minions[0], "minions:randomorder");
                break;
            }
        }
    }
    
    public void orderMinionsToChestBlock(EntityPlayer playerEnt, boolean sneaking, int x, int y, int z)
    { 
        TileEntity chestOrInventoryBlock;
        if ((chestOrInventoryBlock = playerEnt.worldObj.getTileEntity(x, y-1, z)) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            if (!sneaking)
            {
                cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
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
        
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
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
        if (isBlockValueable(playerEnt.worldObj.getBlock(x, y-1, z)))
        {
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
            
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
        cancelRunningJobsForMaster(playerEnt.getGameProfile().getName());
        
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
        System.out.println("Minions: unSummonPlayersMinions called by "+playerEnt);
        for (EntityMinion minion : getMinionsForMaster(playerEnt))
        {
            System.out.println("Minions: Killing "+minion);
            minion.setDead();
        }
    	
        Object[] toSend = { Integer.valueOf(0), Integer.valueOf(0) };
    	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), playerEnt);
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
                        if (lineString.startsWith("registerBlockIDasTreeBlock:"))
                        {
                            Block id = GameData.blockRegistry.getObject(lineString.substring(lineString.indexOf(":")+1));
                            if (id != Blocks.air)
                            {
                                foundTreeBlocks.add(id);
                                System.out.println("Config: registered additional tree block ID "+id);
                            }
                        }
                        else if (lineString.startsWith("registerBlockIDasWorthlessBlock:"))
                        {
                            Block id = GameData.blockRegistry.getObject(lineString.substring(lineString.indexOf(":")+1));
                            if (id != Blocks.air)
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
    
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (!tick.world.isRemote && tick.phase == Phase.END)
        {
            onTick(tick.world);
        }
    }
    
    public class ServerPacketHandler implements IPacketHandler
    {
        @Override
        public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
        {
            MinionsServer.onPacketData(packetType, packet, player);
        }
    }

    public final static String getPacketChannel()
    {
        return "AS_Minions";
    }
    
    public void debugPrint(String s)
    {
        if (debugMode)
        {
            System.out.println(s);
        }
    }
    
    private class EvilCommitCount extends WorldSavedData
    {
        protected final HashMap<String, Integer> masterCommits;

        public EvilCommitCount(String par1Str)
        {
            super(par1Str);
            masterCommits = new HashMap<String, Integer>();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readFromNBT(NBTTagCompound tags)
        {
            for(String s : (Set<String>)tags.func_150296_c())
            {
                masterCommits.put(s, tags.getInteger(s));
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound tags)
        {
            for (String s : masterCommits.keySet())
            {
                tags.setInteger(s, masterCommits.get(s));
            }
        }
    }
    
    private class ChunkLoaderCallback implements LoadingCallback
    {
        @Override
        public void ticketsLoaded(List<Ticket> tickets, World world)
        {
            for (Ticket t : tickets)
            {
                System.out.println("Minions Chunkloader ticketsLoaded, getEntity(): "+t.getEntity());
                for (ChunkCoordIntPair c : t.getChunkList())
                {
                    // just load the chunk. The minion entities inside will re-register their own tickets
                    ForgeChunkManager.fetchDormantChunk(ChunkCoordIntPair.chunkXZ2Int(c.chunkXPos, c.chunkZPos), world);
                }
                // get rid of these old tickets
                ForgeChunkManager.releaseTicket(t);
            }
        }
    }
    
}
