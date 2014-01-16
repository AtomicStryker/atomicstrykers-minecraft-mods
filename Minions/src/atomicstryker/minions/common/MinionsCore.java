package atomicstryker.minions.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
import cpw.mods.fml.common.registry.LanguageRegistry;


@Mod(modid = "AS_Minions", name = "Minions", version = "1.7.9")
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
    
    public Item itemMastersStaff;
    
    public int secondsWithoutMasterDespawn;
    public double minionFollowRange = 30d;
    
    public final HashSet<Block> foundTreeBlocks;
    public final HashSet<Block> configWorthlessBlocks;
    public final LinkedList<Minion_Job_Manager> runningJobList;
    public final LinkedList<Minion_Job_Manager> finishedJobList;
    public final HashMap<String, Integer> masterCommits;
    private final HashMap<String, ArrayList<EntityMinion>> minionMap;
    private boolean minionMapLock;
    
    private final MinionsChunkManager chunkLoader;
    
    public MinionsCore()
    {
        evilDoings = new ArrayList<EvilDeed>();
        foundTreeBlocks = new HashSet<Block>();
        configWorthlessBlocks = new HashSet<Block>();
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
        
        MinecraftForge.EVENT_BUS.register(chunkLoader);
        MinecraftForge.EVENT_BUS.register(this);
        EntityRegistry.registerModEntity(EntityMinion.class, "AS_EntityMinion", 1, this, 25, 5, true);
        
        proxy.preInit(event);
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            FMLCommonHandler.instance().bus().register(this);
        }
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.load(evt);
        
        LanguageRegistry.instance().addStringLocalization("item.masterstaff.name", "en_US", "Master's Staff");
        
        proxy.registerRenderInformation();
    }
    
    @SubscribeEvent
    public void onEntityJoinsWorld(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) event.entity;
            Object[] toSend = {MinionsCore.instance.evilDeedXPCost};
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend), p);
            
            MinionsCore.instance.prepareMinionHolder(p.func_146103_bH().getName());
            Object[] toSend2 = {MinionsCore.instance.hasPlayerMinions(p) ? 1 : 0, MinionsCore.instance.hasAllMinions(p) ? 1 : 0};
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend2), p);
        }
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        getViableTreeBlocks();
    }
    
    public void onMinionUnloaded(EntityMinion entityMinion)
    {
        Iterator<ArrayList<EntityMinion>> iterLists = getMinionMap().values().iterator();
        while (iterLists.hasNext())
        {
            ArrayList<EntityMinion> list = iterLists.next();
            Iterator<EntityMinion> iterMinions = list.iterator();
            while (iterMinions.hasNext())
            {
                if (iterMinions.next() == entityMinion)
                {
                    iterMinions.remove();
                    if (list.isEmpty())
                    {
                        iterLists.remove();
                    }
                    minionMapLock = false;
                    return;
                }
            }
        }
        minionMapLock = false;
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
        for (String s : (Set<String>)GameData.blockRegistry.func_148742_b())
        {
            Block iter = GameData.blockRegistry.getObject(s);
            if (iter instanceof BlockLog || iter.func_149732_F().contains("log"))
            {
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
        while (minionMapLock) {}
        minionMapLock = true;
        return minionMap;
    }
    
    public void prepareMinionHolder(String username)
    {
        System.out.println("New Minionlist prepared for user "+username);
        getMinionMap().put(username, new ArrayList<EntityMinion>());
        minionMapLock = false;
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
        ArrayList<EntityMinion> l = getMinionMap().get(n);
        if (l == null)
        {
            System.out.println("Minions got faulty request for username "+n+", no Minionlist prepared?!");
            prepareMinionHolder(n);
            return (EntityMinion[]) getMinionMap().get(n).toArray();
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
            prepareMinionHolder(masterName);
            l = getMinionMap().get(masterName);
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
            chunkLoader.registerChunkLoaderEntity(ent);
        }
        System.out.println("added additional minion for "+mastername+", now registered: "+minions.length);
    }

    public void onMasterAddedEvil(EntityPlayer player)
    {
        if (masterCommits.get(player.func_146103_bH().getName()) != null)
        {
            int commits = (Integer) masterCommits.get(player.func_146103_bH().getName());
            commits++;

            if (commits == 4)
            {
            	proxy.playSoundAtEntity(player, "minions:thegodshaverewardedyouroffering", 1.0F, 1.0F);
                // give master item to player
                player.inventory.addItemStackToInventory(new ItemStack(itemMastersStaff, 1, 0));
            }
            else
            {
                masterCommits.put(player.func_146103_bH().getName(), commits);
                proxy.playSoundAtEntity(player, "minions:thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
            }
        }
        else
        {
            masterCommits.put(player.func_146103_bH().getName(), 1);
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
            offerMinionToMap(minion, playerEnt.func_146103_bH().getName());
            //System.out.println("spawned missing minion for "+var3.func_146103_bH().getName());
            return true;
        }
        
        //AS_EntityMinion[] readout = (AS_EntityMinion[]) masterNames.get(playerEnt.func_146103_bH().getName());
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
        
        cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
        
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
        cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
        
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
        if ((chestOrInventoryBlock = playerEnt.worldObj.func_147438_o(x, y-1, z)) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            if (!sneaking)
            {
                cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
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
        
        cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
        
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
        if (isBlockValueable(playerEnt.worldObj.func_147439_a(x, y-1, z)))
        {
            EntityMinion[] minions = getMinionsForMaster(playerEnt);
            cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
            
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
        cancelRunningJobsForMaster(playerEnt.func_146103_bH().getName());
        
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
                        if (lineString.startsWith("registerBlockIDasTreeBlock"))
                        {
                            String[] stringArray = lineString.split(":");
                            Block id = GameData.blockRegistry.getObject(stringArray[1]);
                            if (id != Blocks.air)
                            {
                                foundTreeBlocks.add(id);
                                System.out.println("Config: registered additional tree block ID "+id);
                            }
                        }
                        else if (lineString.startsWith("registerBlockIDasWorthlessBlock"))
                        {
                            String[] stringArray = lineString.split(":");
                            Block id = GameData.blockRegistry.getObject(stringArray[1]);
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
        if (tick.phase == Phase.END)
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
    
}
