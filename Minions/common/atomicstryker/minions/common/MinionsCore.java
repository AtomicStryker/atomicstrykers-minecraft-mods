package atomicstryker.minions.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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
import atomicstryker.ForgePacketWrapper;
import atomicstryker.astarpathing.AS_PathEntity;
import atomicstryker.astarpathing.AS_PathPoint;
import atomicstryker.astarpathing.AStarNode;
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
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
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
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "AS_Minions", name = "Minions", version = "1.6.4")
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
    public static ArrayList<EvilDeed> evilDoings = new ArrayList();
    
    private static float exhaustAmountSmall;
    private static float exhaustAmountBig;
    
	public static int masterStaffItemID = 2527;
    public static Item itemMastersStaff;
    
    public static HashSet<Integer> foundTreeBlocks = new HashSet<Integer>();
    public static HashSet<Integer> configWorthlessBlocks = new HashSet<Integer>();
    public static CopyOnWriteArrayList<Minion_Job_Manager> runningJobList = new CopyOnWriteArrayList<Minion_Job_Manager>();
    public static CopyOnWriteArrayList<Minion_Job_Manager> finishedJobList = new CopyOnWriteArrayList<Minion_Job_Manager>();
    public static Map<String, EntityMinion[]> masterNames = new HashMap<String, EntityMinion[]>();
    public static Map<String, Integer> masterCommits = new HashMap<String, Integer>();

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            cfg.load();
            masterStaffItemID = cfg.get(cfg.CATEGORY_ITEM, "masterStaffItemID", 2527).getInt();
            evilDeedXPCost = cfg.get(cfg.CATEGORY_GENERAL, "evilDeedXPCost", 2).getInt();
            minionsPerPlayer = cfg.get(cfg.CATEGORY_GENERAL, "minionsAmountPerPlayer", 4).getInt();
            
            cfg.get(cfg.CATEGORY_GENERAL, "FoodCostSmall", "1.5").comment = "Food cost per tick of casting lightning";
            exhaustAmountSmall = Float.valueOf(cfg.get(cfg.CATEGORY_GENERAL, "FoodCostSmall", "1.5").getString());
            cfg.get(cfg.CATEGORY_GENERAL, "FoodCostBig", "20").comment = "Food cost of summoning Minions and giving complex orders";
            exhaustAmountBig = Float.valueOf(cfg.get(cfg.CATEGORY_GENERAL, "FoodCostBig", "20").getString());
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
        
        proxy.preInit(event);
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        proxy.load(evt);
        
        MinecraftForge.EVENT_BUS.register(new MinionsChunkManager());
        
        EntityRegistry.registerModEntity(EntityMinion.class, "AS_EntityMinion", 1, this, 25, 5, true);        
        itemMastersStaff = (new ItemMastersStaff(masterStaffItemID).setUnlocalizedName("Master's Staff"));
        LanguageRegistry.instance().addName(itemMastersStaff, "Master's Staff");
        
        proxy.registerRenderInformation();
    }

    @PostInit
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        getViableTreeBlocks();
    }
    
    @ServerStarted
    public void serverStarted(FMLServerStartedEvent event)
    {
        //System.out.println("FML Minions Server Load!");
        
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        NetworkRegistry.instance().registerChannel(new ServerPacketHandler(), MinionsCore.getPacketChannel(), Side.SERVER);
    }
	
    public static void onTick()
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

        Iterator iter = runningJobList.iterator();
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
        Iterator iter = runningJobList.iterator();
        while (iter.hasNext())
        {
            temp = (Minion_Job_Manager) iter.next();
            if (temp != null && temp.masterName != null && temp.masterName.equals(name))
            {
                temp.onJobFinished();
            }
        }
    }

    public static void minionLoadRegister(EntityMinion ent)
    {        
        if (ent.masterUsername == null || ent.masterUsername.equals(""))
        {
            System.out.println("Loaded Minion without masterName, killing");
            ent.setDead();
            return;
        }

        System.out.println("Loaded Minion from NBT, re-registering master: "+ent.masterUsername);
        String mastername = ent.masterUsername;

        if (!masterNames.containsKey(mastername))
        {
            masterNames.put(mastername, null);
        }
        EntityMinion[] array = (EntityMinion[]) masterNames.get(mastername);
        if (array == null)
        {
            System.out.println("registering new key for "+mastername);
            array = new EntityMinion[1];
            array[0] = ent;
            masterNames.put(mastername, array);
        }
        else
        {
            for (EntityMinion m : array)
            {
                if (m.entityId == ent.entityId)
                {
                    System.out.println("Minion already loaded");
                    return;
                }
            }
            
            if (array.length >= minionsPerPlayer)
            {
                System.out.println("Adding a new minion too many for "+mastername+", killing it NOW");
                ent.setDead();
                return;
            }

            EntityMinion[] arrayplusone = new EntityMinion[array.length+1];
            int index = 0;
            while (index < array.length)
            {
                arrayplusone[index] = array[index];
                index++;
            }
            arrayplusone[array.length] = ent;
            masterNames.put(mastername, arrayplusone);
            System.out.println("adding additional minion for "+mastername+", now in array: "+arrayplusone.length);
        }
    }

    public static void onMasterAddedEvil(EntityPlayer player)
    {
        if (masterCommits.get(player.username) != null)
        {
            int commits = (Integer) masterCommits.get(player.username);
            commits++;

            if (commits == 4)
            {
            	proxy.playSoundAtEntity(player, "mod_minions.thegodshaverewardedyouroffering", 1.0F, 1.0F);
                // give master item to player
                player.inventory.addItemStackToInventory(new ItemStack(MinionsCore.itemMastersStaff.itemID, 1, 0));
            }
            else
            {
                masterCommits.put(player.username, commits);
                proxy.playSoundAtEntity(player, "mod_minions.thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
            }
        }
        else
        {
            masterCommits.put(player.username, 1);
            proxy.playSoundAtEntity(player, "mod_minions.thegodsarepleaseedwithyoursacrifice", 1.0F, 1.0F);
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
        EntityMinion[] array = (EntityMinion[]) masterNames.get(player.username);
        if (array != null)
        {
            return (array.length >= minionsPerPlayer);
        }
        return false;
    }
    
    public static void orderMinionToPickupEntity(EntityPlayer playerEnt, EntityLiving target)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;

            if (minions[i].riddenByEntity == null)
            {
                minions[i].targetEntityToGrab = (EntityLiving) target;
                minions[i].currentState = EnumMinionState.STALKING_TO_GRAB;
                proxy.sendSoundToClients(minions[i], "mod_minions.grabanimalorder");
                break;
            }
        }
    }
    
    public static void orderMinionToDrop(EntityPlayer playerEnt, EntityMinion minion)
    {
        if (minion.riddenByEntity != null)
        {
            proxy.sendSoundToClients(minion, "mod_minions.foryou");
            minion.riddenByEntity.mountEntity(null);
        }
        else if (minion.inventory.containsItems())
        {
            proxy.sendSoundToClients(minion, "mod_minions.foryou");
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
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        
        if (minions == null || minions.length < minionsPerPlayer)
        {
            int prevArraySize = (minions == null) ? 0 : minions.length;
            EntityMinion[] arrayplusone = new EntityMinion[prevArraySize+1];
            int index = 0;
            while (index < prevArraySize)
            {
                arrayplusone[index] = minions[index];
                index++;
            }
            arrayplusone[prevArraySize] = new EntityMinion(playerEnt.worldObj);
            arrayplusone[prevArraySize].setPosition(x, y+1, z);
            playerEnt.worldObj.spawnEntityInWorld(arrayplusone[prevArraySize]);
            arrayplusone[prevArraySize].setMaster(playerEnt);
            MinionsCore.proxy.sendSoundToClients(arrayplusone[prevArraySize], "mod_minions.minionspawn");

            masterNames.put(playerEnt.username, arrayplusone);
            //System.out.println("spawned missing minion for "+var3.username);
            return true;
        }
        
        //AS_EntityMinion[] readout = (AS_EntityMinion[]) masterNames.get(playerEnt.username);
        orderMinionsToMoveTo(playerEnt, x, y, z);
        return false;
    }
    
    public static void orderMinionsToChopTrees(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;
            minions[i].giveTask(null, true);
        }
        
        cancelRunningJobsForMaster(playerEnt.username);
        runningJobList.add(new Minion_Job_TreeHarvest(minions, x, y, z));
        proxy.sendSoundToClients(minions[0], "mod_minions.ordertreecutting");
    }
    
    public static void orderMinionsToDigStairWell(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;
            minions[i].giveTask(null, true);
        }
        
        // stairwell job
        cancelRunningJobsForMaster(playerEnt.username);
        runningJobList.add(new Minion_Job_DigMineStairwell(minions, x, y-1, z));
        proxy.sendSoundToClients(minions[0], "mod_minions.ordermineshaft");
    }
    
    public static void orderMinionsToDigStripMineShaft(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;
            minions[i].giveTask(null, true);
        }
        
        // strip mine job
        minions[0].master = playerEnt;
        runningJobList.add(new Minion_Job_StripMine(minions, x, y-1, z));
        proxy.sendSoundToClients(minions[0], "mod_minions.randomorder");
    }
    
    public static void orderMinionsToChestBlock(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        TileEntity chestOrInventoryBlock;
        if ((chestOrInventoryBlock = playerEnt.worldObj.getBlockTileEntity(x, y-1, z)) != null
                && chestOrInventoryBlock instanceof IInventory)
        {
            cancelRunningJobsForMaster(playerEnt.username);
            proxy.sendSoundToClients(minions[0], "mod_minions.randomorder");
            for (int i = 0; i < minions.length; i++)
            {
                minions[i].master = playerEnt;
                minions[i].giveTask(null, true);
                minions[i].returnChestOrInventory = (TileEntity) chestOrInventoryBlock;
                minions[i].currentState = EnumMinionState.RETURNING_GOODS;
            }
        }
    }
    
    public static void orderMinionsToMoveTo(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        cancelRunningJobsForMaster(playerEnt.username);
        proxy.sendSoundToClients(minions[0], "mod_minions.randomorder");
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;
            minions[i].giveTask(null, true);
            minions[i].currentState = EnumMinionState.IDLE;
            minions[i].orderMinionToMoveTo(x, y, z, false);
        }
    }
    
    public static void orderMinionsToMineOre(EntityPlayer playerEnt, int x, int y, int z)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        if (minions == null)
        {
            return;
        }
        
        if (isBlockValueable(playerEnt.worldObj.getBlockId(x, y-1, z)))
        {
            cancelRunningJobsForMaster(playerEnt.username);
            proxy.sendSoundToClients(minions[0], "mod_minions.randomorder");
            for (int i = 0; i < minions.length; i++)
            {
                minions[i].master = playerEnt;

                if (!minions[i].hasTask())
                {
                    minions[i].giveTask(new BlockTask_MineOreVein(null, minions[i], x, y-1, z));
                    break;
                }
            }
        }
    }
    
    public static void orderMinionsToFollow(EntityPlayer entPlayer)
    {
        cancelRunningJobsForMaster(entPlayer.username);
        
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(entPlayer.username);
        if (minions == null)
        {
            return;
        }
        
        proxy.sendSoundToClients(minions[0], "mod_minions.orderfollowplayer");
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = entPlayer;
            minions[i].giveTask(null, true);
            minions[i].currentState = EnumMinionState.FOLLOWING_PLAYER;
        }
    }
    
    public static void unSummonPlayersMinions(EntityPlayer playerEnt)
    {
    	EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
    	if (minions != null)
    	{
    		for (EntityMinion minion : minions)
    		{
    			minion.master = playerEnt;
    			minion.dropAllItemsToWorld();
    			minion.setDead();
    		}
    	}
    	masterNames.remove(playerEnt.username);
    	
    	Object[] toSend = { proxy.hasPlayerMinions(playerEnt) ? 1 : 0, hasAllMinions(playerEnt) ? 1 : 0 }; // HasMinions override call from server to client
    	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), (Player) playerEnt);
    }
    
    public static void orderMinionsToDigCustomSpace(EntityPlayer playerEnt, int x, int y, int z, int XZsize, int ySize)
    {
        EntityMinion[] minions = (EntityMinion[]) masterNames.get(playerEnt.username);
        for (int i = 0; i < minions.length; i++)
        {
            minions[i].master = playerEnt;
            minions[i].giveTask(null, true);
        }
        
        // custom dig job
        minions[0].master = playerEnt;
        runningJobList.add(new Minion_Job_DigByCoordinates(minions, x, y-1, z, XZsize, ySize));
        proxy.playSoundAtEntity(playerEnt, "mod_minions.randomorder", 1.0F, 1.0F);
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
        private final EnumSet tickTypes = EnumSet.of(TickType.WORLD);
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            // NOOP
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            onTick();
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
