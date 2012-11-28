package atomicstryker.battletowers.common;

import java.io.*;
import java.util.*;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

import net.minecraft.src.*;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;

import atomicstryker.battletowers.client.ClientPacketHandler;

@Mod(modid = "BattleTowers", name = "Battle Towers", version = "1.2.4")
@NetworkMod(clientSideRequired = true, serverSideRequired = true,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_BT"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_BT"}, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class AS_BattleTowersCore
{
	private static Set<ChunkCoordinates> towerPositions;
	private static Set<AS_TowerDestroyer> towerDestroyers;
	public static double minDistanceBetweenTowers;
	public static int towerDestroyerEnabled;
	public static int itemGenerateAttemptsPerFloor;
	public static int chanceTowerIsUnderGround;
	
    @SidedProxy(clientSide = "atomicstryker.battletowers.client.ClientProxy", serverSide = "atomicstryker.battletowers.common.CommonProxy")
    public static CommonProxy proxy;
    
    public static TowerStageItemManager[] floorItemManagers = new TowerStageItemManager[10];
    public static Configuration configuration;
	
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        configuration = new Configuration(event.getSuggestedConfigurationFile(), false);
        loadForgeConfig();
        
        proxy.preInit();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        proxy.load();
        
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        
        EntityRegistry.registerModEntity(AS_EntityGolem.class, "AS_EntityGolem", 1, this, 25, 5, true);
        EntityRegistry.registerModEntity(AS_EntityGolemFireball.class, "AS_EntityGolemFireball", 2, this, 25, 5, true);
        
        towerPositions = new HashSet<ChunkCoordinates>();
        towerDestroyers = new HashSet<AS_TowerDestroyer>();
        
        GameRegistry.registerWorldGenerator(new WorldGenHandler());
    }
    
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        /* and this replaces modsLoaded(), all mods are loaded at this point, do inter-mod stuff here */
    }
    
    public void loadForgeConfig()
    {
        configuration.load();
        minDistanceBetweenTowers = Integer.parseInt(configuration.get("Main Options", "Minimum Distance between 2 BattleTowers", 196).value);
        towerDestroyerEnabled = Integer.parseInt(configuration.get("Main Options", "Tower Destroying Enabled", 1).value);
        itemGenerateAttemptsPerFloor = configuration.get("BattleTower Chest Items", "Item Generations per Floot", "7").getInt();
        chanceTowerIsUnderGround = configuration.get("Main Options", "chanceTowerIsUnderGround", 15, "Chance in percent a Battletower is underground.").getInt();
        
        // 280-0-50-6-5 sticks
        // 295-0-50-3-5 seeds
        // 5-0-50-6-5 planks
        // 83-0-50-3-5 sugarcane
        floorItemManagers[0] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 1", "280-0-50-6-5;295-0-50-3-5;5-0-50-6-5;83-0-50-3-5").value);
        
        // 274-0-25-1-1 stone pick
        // 275-0-25-1-1 stone axe
        // 50-0-75-3-3 torches
        // 77-0-50-2-2 stone button
        floorItemManagers[1] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 2", "274-0-25-1-1;275-0-25-1-1;50-0-75-3-3;77-0-50-2-2").value);
        
        // 281-0-50-2-4 wooden bowl
        // 263-0-75-4-4 coal
        // 287-0-50-5-5 string
        // 35-0-25-2-2 wool
        floorItemManagers[2] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 3", "281-0-50-2-4;263-0-75-4-4;287-0-50-5-5;35-0-25-2-2").value);
        
        // 20-0-50-3-3 glass
        // 288-0-25-4-4 feather
        // 297-0-50-2-2 bread
        // 260-0-75-2-2 apple
        floorItemManagers[3] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 4", "20-0-50-3-3;288-0-25-4-4;297-0-50-2-2;260-0-75-2-2").value);
        
        // 39-0-50-2-2 brown mushroom
        // 40-0-50-2-2 red mushroom
        // 6-0-75-3-3 saplings
        // 296-0-25-4-4 wheat
        floorItemManagers[4] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 5", "39-0-50-2-2;40-0-50-2-2;6-0-75-3-3;296-0-25-4-4").value);
        
        // 323-0-50-1-2 sign
        // 346-0-75-1-1 fishing rod
        // 361-0-25-2-2 pumpkin seeds
        // 362-0-25-3-3 Melon Seeds
        floorItemManagers[5] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 6", "323-0-0-50-1-2;346-0-75-1-1;361-0-25-2-2;362-0-25-3-3").value);
        
        // 267-0-25-1-1 iron sword
        // 289-0-25-3-3 gunpowder
        // 334-0-50-4-4 leather
        // 349-0-75-3-3 raw fish
        // 351-0-50-1-2 dye
        floorItemManagers[6] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 7", "267-0-25-1-1;289-0-25-3-3;334-0-50-4-4;349-0-75-3-3;351-0-50-1-2").value);
        
        // 302-0-25-1-1 chain helmet
        // 303-0-25-1-1 chain chestplate
        // 304-0-25-1-1 chain leggings
        // 305-0-25-1-1 chain boots
        floorItemManagers[7] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 8", "302-0-25-1-1;303-0-25-1-1;304-0-25-1-1;305-0-25-1-1").value);
        
        // 57-0-50-1-3 bookshelf
        // 123-0-25-2-2 redstone lamp
        // 111-0-75-3-3 Lily Plants
        // 379-0-25-1-1 brewing stand
        floorItemManagers[8] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Floor 9", "57-0-50-1-3;123-0-25-2-2;379-0-75-3-3;379-0-25-1-1").value);
        
        // 368-0-50-2-2 ender pearl
        // 264-0-50-2-2 diamond
        // 331-0-75-5-5 redstone dust
        // 266-0-75-8-8 gold ingot
        floorItemManagers[9] = new TowerStageItemManager(configuration.get("BattleTower Chest Items", "Top Floor", "368-0-50-2-2;264-0-50-2-2;331-0-75-5-5;266-0-75-8-8").value);

        configuration.save();
    }
    
    public static synchronized void onBattleTowerDestroyed(AS_TowerDestroyer td)
    {
        Packet3Chat packet = new Packet3Chat("A Battletower's Guardian has fallen! Without it's power, the Tower will collapse...");
        PacketDispatcher.sendPacketToAllAround(td.player.posX, td.player.posY, td.player.posZ, 100, td.player.worldObj.getWorldInfo().getDimension(), packet);
        towerDestroyers.add(td);
    }
	
	public static synchronized boolean canTowerSpawnAt(int xActual, int zActual)
	{
        for (ChunkCoordinates temp : towerPositions)
        {
            int diffX = temp.posX - xActual;
            int diffZ = temp.posZ - zActual;
            if (Math.sqrt(diffX*diffX + diffZ*diffZ) < minDistanceBetweenTowers)
            {
                return false;
            }
        }
        towerPositions.add(new ChunkCoordinates(xActual, 0, zActual));
        
        return true;
	}

    public static synchronized void setTowerSpawnFailedAt(int xActual, int zActual)
    {
        towerPositions.remove(new ChunkCoordinates(xActual, 0, zActual));
    }

	public static synchronized Set<AS_TowerDestroyer> getTowerDestroyers()
	{
		return AS_BattleTowersCore.towerDestroyers;
	}
}
