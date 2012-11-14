package atomicstryker.minefactoryreloaded.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.Block;
import net.minecraft.src.BlockMushroom;
import net.minecraft.src.BlockStem;
import net.minecraft.src.EntityCaveSpider;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityPig;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntitySheep;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntitySquid;
import net.minecraft.src.EntityWitch;
import net.minecraft.src.EntityZombie;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import atomicstryker.minefactoryreloaded.client.ClientPacketHandler;
import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryFertilizable;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;
import atomicstryker.minefactoryreloaded.common.api.IFactoryPlantable;
import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.blocks.BlockConveyor;
import atomicstryker.minefactoryreloaded.common.blocks.BlockFactoryMachine;
import atomicstryker.minefactoryreloaded.common.blocks.BlockRailCargoDropoff;
import atomicstryker.minefactoryreloaded.common.blocks.BlockRailCargoPickup;
import atomicstryker.minefactoryreloaded.common.blocks.BlockRailPassengerDropoff;
import atomicstryker.minefactoryreloaded.common.blocks.BlockRailPassengerPickup;
import atomicstryker.minefactoryreloaded.common.core.IMFRProxy;
import atomicstryker.minefactoryreloaded.common.core.Util;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableCocoa;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableGiantMushroom;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableNetherWart;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableSapling;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableStemPlants;
import atomicstryker.minefactoryreloaded.common.farmables.FertilizableCropPlant;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableCocoa;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableCropPlant;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableNetherWart;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableStandard;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableStemPlant;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableVine;
import atomicstryker.minefactoryreloaded.common.farmables.HarvestableWood;
import atomicstryker.minefactoryreloaded.common.farmables.PlantableCocoa;
import atomicstryker.minefactoryreloaded.common.farmables.PlantableNetherWart;
import atomicstryker.minefactoryreloaded.common.farmables.PlantableStandard;
import atomicstryker.minefactoryreloaded.common.farmables.PlantableCropPlant;
import atomicstryker.minefactoryreloaded.common.farmables.RanchableChicken;
import atomicstryker.minefactoryreloaded.common.farmables.RanchableCow;
import atomicstryker.minefactoryreloaded.common.farmables.RanchableMooshroom;
import atomicstryker.minefactoryreloaded.common.farmables.RanchableStandard;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityBlockBreaker;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityCollector;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityConveyor;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFertilizer;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFisher;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityHarvester;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityPlanter;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityVet;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityWeather;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.LiquidData;
import buildcraft.api.liquids.LiquidManager;
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
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MFReloaded", name = "Minefactory Reloaded", version = "1.4.4R1.5.9", dependencies = "after:BuildCraft|Core")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "MFReloaded" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "MFReloaded" }, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class MineFactoryReloadedCore
{
    public static final String TEXTURE_FOLDER = "/atomicstryker/minefactoryreloaded/client/textures/";
    public static final String terrainTexture = TEXTURE_FOLDER+"terrain_0.png";
    public static final String itemTexture = TEXTURE_FOLDER+"items_0.png";
    
    public static PowerSystem powerSystem;

    public static Block machineBlock;
    public static Block conveyorBlock;

    public static Block passengerRailDropoffBlock;
    public static Block passengerRailPickupBlock;
    public static Block cargoRailDropoffBlock;
    public static Block cargoRailPickupBlock;

    public static Item steelIngotItem;
    public static Item factoryHammerItem;
    public static Item milkItem;

    public static Item machineItem;

    public static int conveyorTexture;
    public static int conveyorOffTexture;
    public static int conveyorStillOffTexture;
    public static int harvesterAnimatedTexture;
    public static int rancherAnimatedTexture;
    public static int steelSideTexture;
    public static int planterSaplingTexture;
    public static int planterCactusTexture;
    public static int planterSugarTexture;
    public static int planterMushroomTexture;
    public static int steelHoleTexture;
    public static int passengerRailPickupTexture;
    public static int passengerRailDropoffTexture;
    public static int cargoRailPickupTexture;
    public static int cargoRailDropoffTexture;
    public static int fisherBucketTexture;
    public static int fisherFishTexture;
    public static int harvesterSideTexture;
    public static int rancherSideTexture;
    public static int fertilizerBackTexture;
    public static int fertilizerSideTexture;
    public static int vetSideTexture;
    public static int collectorSideTexture;
    public static int weatherTopTexture;
    public static int weatherSnowSideTexture;
    public static int blockBreakerAnimatedTexture;
    public static int fertilizerAnimatedTexture;
    public static int vetAnimatedTexture;
    public static int blockBreakerSideTexture;

    public static int factoryHammerTexture;
    public static int steelIngotTexture;
    public static int milkTexture;

    @SidedProxy(clientSide = "atomicstryker.minefactoryreloaded.client.ClientProxy", serverSide = "atomicstryker.minefactoryreloaded.common.CommonProxy")
    public static IMFRProxy proxy;

    public static Map<MineFactoryReloadedCore.Machine, Integer> machineMetadataMappings;

    // Config
    public static Property machineBlockId;
    public static Property conveyorBlockId;
    public static Property passengerPickupRailBlockId;
    public static Property passengerDropoffRailBlockId;
    public static Property cargoPickupRailBlockId;
    public static Property cargoDropoffRailBlockId;

    public static Property steelIngotItemId;
    public static Property hammerItemId;
    public static Property milkItemId;

    public static Property animateBlockFaces;
    public static Property animationTileSize;
    public static Property treeSearchMaxVertical;
    public static Property treeSearchMaxHorizontal;
    public static Property passengerRailSearchMaxVertical;
    public static Property passengerRailSearchMaxHorizontal;
    public static Property verticalHarvestSearchMaxVertical;
    public static Property playSounds;
    public static Property machinesCanDropInChests;
    public static Property rancherInjuresAnimals;
    public static Property harvesterHarvestsSmallMushrooms;

    public static Property powerSystemProperty;
    public static Property enableSteelCraftingProperty;

    private static MineFactoryReloadedCore instance;

    public static MineFactoryReloadedCore instance()
    {
        return instance;
    }

    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        loadConfig(evt.getSuggestedConfigurationFile());
    }

    @Init
    public void load(FMLInitializationEvent evt)
    {
        instance = this;

        machineMetadataMappings = new HashMap<Machine, Integer>();
        machineMetadataMappings.put(Machine.Planter, 0);
        machineMetadataMappings.put(Machine.Fisher, 1);
        machineMetadataMappings.put(Machine.Harvester, 2);
        machineMetadataMappings.put(Machine.Rancher, 3);
        machineMetadataMappings.put(Machine.Fertilizer, 4);
        machineMetadataMappings.put(Machine.Vet, 5);
        machineMetadataMappings.put(Machine.Collector, 6);
        machineMetadataMappings.put(Machine.Breaker, 7);
        machineMetadataMappings.put(Machine.Weather, 8);

        setupTextures();

        passengerRailPickupBlock = new BlockRailPassengerPickup(Util.getInt(passengerPickupRailBlockId), passengerRailPickupTexture);
        passengerRailDropoffBlock = new BlockRailPassengerDropoff(Util.getInt(passengerDropoffRailBlockId), passengerRailDropoffTexture);
        cargoRailDropoffBlock = new BlockRailCargoDropoff(Util.getInt(cargoDropoffRailBlockId), cargoRailDropoffTexture);
        cargoRailPickupBlock = new BlockRailCargoPickup(Util.getInt(cargoPickupRailBlockId), cargoRailPickupTexture);

        conveyorBlock = new BlockConveyor(Util.getInt(conveyorBlockId), conveyorTexture);

        machineBlock = new BlockFactoryMachine(Util.getInt(machineBlockId), 0);

        steelIngotItem = (new ItemFactory(Util.getInt(steelIngotItemId))).setIconIndex(steelIngotTexture).setItemName("steelIngot");
        factoryHammerItem = (new ItemFactory(Util.getInt(hammerItemId))).setIconIndex(factoryHammerTexture).setItemName("factoryWrench").setMaxStackSize(1);
        milkItem = (new ItemFactory(Util.getInt(milkItemId))).setIconIndex(milkTexture).setItemName("milkItem");

        GameRegistry.registerBlock(machineBlock, ItemFactoryMachine.class);
        GameRegistry.registerBlock(conveyorBlock);
        GameRegistry.registerBlock(passengerRailPickupBlock);
        GameRegistry.registerBlock(passengerRailDropoffBlock);
        GameRegistry.registerBlock(cargoRailDropoffBlock);
        GameRegistry.registerBlock(cargoRailPickupBlock);

        GameRegistry.registerTileEntity(TileEntityFisher.class, "factoryFisher");
        GameRegistry.registerTileEntity(TileEntityPlanter.class, "factoryPlanter");
        GameRegistry.registerTileEntity(TileEntityHarvester.class, "factoryHarvester");
        GameRegistry.registerTileEntity(TileEntityRancher.class, "factoryRancher");
        GameRegistry.registerTileEntity(TileEntityFertilizer.class, "factoryFertilizer");
        GameRegistry.registerTileEntity(TileEntityConveyor.class, "factoryConveyor");
        GameRegistry.registerTileEntity(TileEntityVet.class, "factoryVet");
        GameRegistry.registerTileEntity(TileEntityCollector.class, "factoryItemCollector");
        GameRegistry.registerTileEntity(TileEntityBlockBreaker.class, "factoryBlockBreaker");
        GameRegistry.registerTileEntity(TileEntityWeather.class, "factoryWeather");

        if (Util.getBool(enableSteelCraftingProperty))
        {
            GameRegistry.addRecipe(new ItemStack(steelIngotItem, 5), new Object[] { " C ", "CIC", " C ", Character.valueOf('C'), Item.coal, Character.valueOf('I'), Item.ingotIron });
        }

        MinecraftForge.EVENT_BUS.register(instance);
        OreDictionary.registerOre("ingotRefinedIron", new ItemStack(steelIngotItem));

        registerFarmables();
        
        proxy.load();
    }
    
    @PostInit
    public void afterModsLoaded(FMLPostInitializationEvent evt)
    {
        LiquidManager.liquids.add(new LiquidData(milkItem.shiftedIndex, Item.bucketMilk.shiftedIndex, Item.bucketMilk));
    }
    
    @ServerStarted
    public void serverStarted(FMLServerStartedEvent event)
    {
        
    }

    private void registerRecipes(ItemStack steelIngot)
    {
        GameRegistry.addRecipe(new ItemStack(conveyorBlock, 1), new Object[] { "R", "S", Character.valueOf('R'), Item.redstone, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Fertilizer)),
                new Object[] { "SSS", "SPS", "SSS", Character.valueOf('P'), Block.sapling, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Fertilizer)), new Object[] { "SSS", "SPS", "SSS", Character.valueOf('P'),
                new ItemStack(Block.sapling, 1, 1), Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Fertilizer)), new Object[] { "SSS", "SPS", "SSS", Character.valueOf('P'),
                new ItemStack(Block.sapling, 1, 2), Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Harvester)),
                new Object[] { "SSS", "SXS", "SSS", Character.valueOf('X'), Item.axeSteel, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Harvester)),
                new Object[] { "SSS", "SWS", "SSS", Character.valueOf('W'), Item.swordSteel, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Planter)),
                new Object[] { "SSS", "SHS", "SSS", Character.valueOf('H'), Item.hoeSteel, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Rancher)),
                new Object[] { "SSS", "SHS", "SSS", Character.valueOf('H'), Item.shears, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Fisher)),
                new Object[] { "SSS", "SFS", "SSS", Character.valueOf('F'), Item.fishingRod, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Collector)),
                new Object[] { "SSS", "SCS", "SSS", Character.valueOf('C'), Block.chest, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Vet)), new Object[] { "SSS", "SBS", "SSS", Character.valueOf('B'), Item.bread,
                Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Breaker)),
                new Object[] { "SSS", "SPS", "SSS", Character.valueOf('P'), Item.pickaxeSteel, Character.valueOf('S'), steelIngot });
        GameRegistry.addRecipe(new ItemStack(machineBlock, 1, machineMetadataMappings.get(Machine.Weather)),
                new Object[] { "SSS", "SBS", "SSS", Character.valueOf('B'), Item.bucketEmpty, Character.valueOf('S'), steelIngot });

        GameRegistry.addRecipe(new ItemStack(cargoRailPickupBlock, 2),
                new Object[] { " C ", "SDS", "SSS", Character.valueOf('C'), Block.chest, Character.valueOf('S'), steelIngot, Character.valueOf('D'), Block.railDetector });
        GameRegistry.addRecipe(new ItemStack(cargoRailDropoffBlock, 2),
                new Object[] { "SSS", "SDS", " C ", Character.valueOf('C'), Block.chest, Character.valueOf('S'), steelIngot, Character.valueOf('D'), Block.railDetector });
        GameRegistry.addRecipe(new ItemStack(passengerRailPickupBlock, 3),
                new Object[] { " L ", "SDS", "SSS", Character.valueOf('L'), Block.blockLapis, Character.valueOf('S'), steelIngot, Character.valueOf('D'), Block.railDetector });
        GameRegistry.addRecipe(new ItemStack(passengerRailDropoffBlock, 3),
                new Object[] { "SSS", "SDS", " L ", Character.valueOf('L'), Block.blockLapis, Character.valueOf('S'), steelIngot, Character.valueOf('D'), Block.railDetector });

        GameRegistry.addRecipe(new ItemStack(factoryHammerItem, 1), new Object[] { "SSS", " T ", " T ", Character.valueOf('S'), steelIngotItem, Character.valueOf('T'), Item.stick });
    }

    private void registerFarmables()
    {
        registerPlantable(new PlantableStandard(Block.sapling.blockID, Block.sapling.blockID));
        registerPlantable(new PlantableStandard(Item.reed.shiftedIndex, Block.reed.blockID));
        registerPlantable(new PlantableStandard(Block.cactus.blockID, Block.cactus.blockID));
        registerPlantable(new PlantableStandard(Item.pumpkinSeeds.shiftedIndex, Block.pumpkinStem.blockID));
        registerPlantable(new PlantableStandard(Item.melonSeeds.shiftedIndex, Block.melonStem.blockID));
        registerPlantable(new PlantableStandard(Block.mushroomBrown.blockID, Block.mushroomBrown.blockID));
        registerPlantable(new PlantableStandard(Block.mushroomRed.blockID, Block.mushroomRed.blockID));
        registerPlantable(new PlantableCropPlant(Item.seeds.shiftedIndex, Block.crops.blockID));
        registerPlantable(new PlantableCropPlant(Item.carrot.shiftedIndex, Block.carrot.blockID));
        registerPlantable(new PlantableCropPlant(Item.potatoe.shiftedIndex, Block.potatoe.blockID));
        registerPlantable(new PlantableNetherWart());
        registerPlantable(new PlantableCocoa());

        registerHarvestable(new HarvestableWood());
        registerHarvestable(new HarvestableStandard(Block.leaves.blockID, HarvestType.TreeLeaf));
        registerHarvestable(new HarvestableStandard(Block.reed.blockID, HarvestType.LeaveBottom));
        registerHarvestable(new HarvestableStandard(Block.cactus.blockID, HarvestType.LeaveBottom));
        registerHarvestable(new HarvestableStandard(Block.plantRed.blockID, HarvestType.Normal));
        registerHarvestable(new HarvestableStandard(Block.plantYellow.blockID, HarvestType.Normal));
        registerHarvestable(new HarvestableStandard(Block.tallGrass.blockID, HarvestType.Normal));
        registerHarvestable(new HarvestableStandard(Block.mushroomCapBrown.blockID, HarvestType.Tree));
        registerHarvestable(new HarvestableStandard(Block.mushroomCapRed.blockID, HarvestType.Tree));
        registerHarvestable(new HarvestableStemPlant(Block.pumpkin.blockID, HarvestType.Normal));
        registerHarvestable(new HarvestableStemPlant(Block.melon.blockID, HarvestType.Normal));
        registerHarvestable(new HarvestableCropPlant(Block.crops.blockID));
        registerHarvestable(new HarvestableCropPlant(Block.carrot.blockID));
        registerHarvestable(new HarvestableCropPlant(Block.potatoe.blockID));
        registerHarvestable(new HarvestableVine());
        registerHarvestable(new HarvestableNetherWart());
        if (Util.getBool(harvesterHarvestsSmallMushrooms))
        {
            registerHarvestable(new HarvestableStandard(Block.mushroomBrown.blockID, HarvestType.Normal));
            registerHarvestable(new HarvestableStandard(Block.mushroomRed.blockID, HarvestType.Normal));
        }
        registerHarvestable(new HarvestableCocoa());

        registerFertilizable(new FertilizableSapling());
        registerFertilizable(new FertilizableCropPlant(Block.crops.blockID));
        registerFertilizable(new FertilizableCropPlant(Block.carrot.blockID));
        registerFertilizable(new FertilizableCropPlant(Block.potatoe.blockID));
        registerFertilizable(new FertilizableGiantMushroom(Block.mushroomBrown.blockID));
        registerFertilizable(new FertilizableGiantMushroom(Block.mushroomRed.blockID));
        registerFertilizable(new FertilizableStemPlants(Block.pumpkinStem.blockID));
        registerFertilizable(new FertilizableStemPlants(Block.melonStem.blockID));
        registerFertilizable(new FertilizableNetherWart());
        registerFertilizable(new FertilizableCocoa());

        registerFertilizerItem(Item.dyePowder.shiftedIndex);

        registerRanchable(new RanchableChicken());
        registerRanchable(new RanchableCow());
        registerRanchable(new RanchableStandard(EntityPig.class, new ItemStack(Item.porkRaw), 45, 1, 40));
        registerRanchable(new RanchableStandard(EntitySheep.class, new ItemStack(Block.cloth), 30, 1, 40));
        registerRanchable(new RanchableStandard(EntitySlime.class, new ItemStack(Item.slimeBall), 25, 1, 30));
        registerRanchable(new RanchableStandard(EntitySquid.class, new ItemStack(Item.dyePowder), 10, 1, 40));
        registerRanchable(new RanchableMooshroom());
        
        registerRanchable(new RanchableStandard(EntityCreeper.class, new ItemStack(Item.gunpowder), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntityPigZombie.class, new ItemStack(Item.porkCooked), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntitySpider.class, new ItemStack(Item.silk), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntityCaveSpider.class, new ItemStack(Item.silk), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntitySkeleton.class, new ItemStack(Item.bone), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntityWitch.class, new ItemStack(Item.spiderEye), 100, 2, 100));
        registerRanchable(new RanchableStandard(EntityZombie.class, new ItemStack(Item.rottenFlesh), 100, 2, 100));
    }

    private static void setupTextures()
    {
        factoryHammerTexture = 0;
        steelIngotTexture = 2;
        milkTexture = 29;

        // 0 bottom 1 top 2 east 3 west 4 north 5 south
        cargoRailDropoffTexture = 0;
        cargoRailPickupTexture = 1;
        passengerRailDropoffTexture = 2;
        passengerRailPickupTexture = 3;
        steelSideTexture = 4;
        steelHoleTexture = 5;
        planterCactusTexture = 6;
        planterMushroomTexture = 7;
        planterSaplingTexture = 8;
        planterSugarTexture = 9;
        conveyorTexture = 10;
        conveyorOffTexture = 11;
        harvesterAnimatedTexture = 12;
        rancherAnimatedTexture = 13;
        fisherBucketTexture = 14;
        fisherFishTexture = 15;
        harvesterSideTexture = 16;
        rancherSideTexture = 17;
        fertilizerBackTexture = 18;
        fertilizerSideTexture = 19;
        conveyorStillOffTexture = 20;
        vetSideTexture = 21;
        collectorSideTexture = 22;
        weatherTopTexture = 23;
        weatherSnowSideTexture = 24;
        blockBreakerAnimatedTexture = 25;
        fertilizerAnimatedTexture = 26;
        vetAnimatedTexture = 27;
        blockBreakerSideTexture = 28;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][1] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][2] = planterCactusTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][3] = planterMushroomTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][4] = planterSaplingTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Planter)][5] = planterSugarTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][0] = rancherAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][1] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][2] = fisherBucketTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][3] = fisherBucketTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][4] = fisherFishTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fisher)][5] = fisherFishTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][5] = harvesterAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][4] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][2] = harvesterSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Harvester)][3] = harvesterSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][5] = rancherAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][4] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][2] = rancherSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Rancher)][3] = rancherSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][5] = fertilizerAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][4] = fertilizerBackTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][2] = fertilizerSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Fertilizer)][3] = fertilizerSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][5] = vetAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][4] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][2] = vetSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Vet)][3] = vetSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][5] = collectorSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][4] = collectorSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][2] = collectorSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Collector)][3] = collectorSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][0] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][1] = steelSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][5] = blockBreakerAnimatedTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][4] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][2] = blockBreakerSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Breaker)][3] = blockBreakerSideTexture;

        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][0] = steelHoleTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][1] = weatherTopTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][2] = fisherBucketTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][3] = fisherBucketTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][4] = weatherSnowSideTexture;
        BlockFactoryMachine.textures[machineMetadataMappings.get(Machine.Weather)][5] = weatherSnowSideTexture;
    }

    private static void loadConfig(File configFile)
    {
        Configuration c = new Configuration(configFile);
        c.load();
        machineBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.MachineBlock", 145);
        conveyorBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.ConveyorBlock", 146);
        passengerPickupRailBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.PassengerRailPickupBlock", 147);
        passengerDropoffRailBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.PassengerRailDropoffBlock", 148);
        cargoPickupRailBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.CargoRailPickupBlock", 149);
        cargoDropoffRailBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.CargoRailDropoffBlock", 150);

        steelIngotItemId = c.getItem(Configuration.CATEGORY_ITEM, "ID.SteelIngot", 986);
        hammerItemId = c.getItem(Configuration.CATEGORY_ITEM, "ID.Hammer", 987);
        milkItemId = c.getItem(Configuration.CATEGORY_ITEM, "ID.Milk", 988);

        animateBlockFaces = c.get(Configuration.CATEGORY_GENERAL, "AnimateBlockFaces", true);
        animateBlockFaces.comment = "Set to false to disable animation of harvester, rancher, conveyor, etc. This may be required if using certain mods that affect rendering.";
        playSounds = c.get(Configuration.CATEGORY_GENERAL, "PlaySounds", true);
        playSounds.comment = "Set to false to disable the harvester's sound when a block is harvested.";
        harvesterHarvestsSmallMushrooms = c.get(Configuration.CATEGORY_GENERAL, "HarvesterHarvestsSmallMushrooms", false);
        harvesterHarvestsSmallMushrooms.comment = "Set to true to enable old-style mushroom farms (but will prevent giant mushrooms from working correctly as the small ones will be harvested immediately)";
        rancherInjuresAnimals = c.get(Configuration.CATEGORY_GENERAL, "RancherInjuresAnimals", true);
        rancherInjuresAnimals.comment = "If false, the rancher will never injure animals. Intended for those who want to play in a (pseudo-)creative style.";
        machinesCanDropInChests = c.get(Configuration.CATEGORY_GENERAL, "MachinesCanDropInChests", true);
        machinesCanDropInChests.comment = "Set to false to disable machines placing items into chests adjacent to them";

        treeSearchMaxHorizontal = c.get(Configuration.CATEGORY_GENERAL, "SearchDistance.TreeMaxHoriztonal", 5);
        treeSearchMaxHorizontal.comment = "When searching for parts of a tree, how far out to the sides (radius) to search";
        treeSearchMaxVertical = c.get(Configuration.CATEGORY_GENERAL, "SearchDistance.TreeMaxVertical", 15);
        treeSearchMaxVertical.comment = "When searching for parts of a tree, how far up to search";
        verticalHarvestSearchMaxVertical = c.get(Configuration.CATEGORY_GENERAL, "SearchDistance.StackingBlockMaxVertical", 3);
        verticalHarvestSearchMaxVertical.comment = "How far upward to search for members of \"stacking\" blocks, like cactus and sugarcane";
        passengerRailSearchMaxVertical = c.get(Configuration.CATEGORY_GENERAL, "SearchDistance.PassengerRailMaxVertical", 2);
        passengerRailSearchMaxVertical.comment = "When searching for players or dropoff locations, how far up to search";
        passengerRailSearchMaxHorizontal = c.get(Configuration.CATEGORY_GENERAL, "SearchDistance.PassengerRailMaxHorizontal", 3);
        passengerRailSearchMaxHorizontal.comment = "When searching for players or dropoff locations, how far out to the sides (radius) to search";

        powerSystemProperty = c.get(Configuration.CATEGORY_GENERAL, "PowerSystem", "redstone");
        powerSystemProperty.comment = "Set whether MFR will run off classic alternating redstone of BuildCraft's power system. Values other than \"redstone\" or \"buildcraft\" will cause the system to revert to redstone mode";
        if (powerSystemProperty.value.toLowerCase().equals("buildcraft"))
        {
            powerSystem = PowerSystem.BuildCraft;
        }
        else
        {
            powerSystem = PowerSystem.Redstone;
            powerSystemProperty.value = "redstone";
        }

        enableSteelCraftingProperty = c.get(Configuration.CATEGORY_GENERAL, "EnableSteelCrafting", true);
        enableSteelCraftingProperty.comment = "Set to false to disable steel crafting. This is provided in case another mod provides a way to get steel ignots via the ore dictionary (like IC2) and the built in recipe becomes unbalanced because of it.";

        c.save();
    }

    public static void registerPlantable(IFactoryPlantable plantable)
    {
        TileEntityPlanter.registerPlantable(plantable);
    }

    public static void registerHarvestable(IFactoryHarvestable harvestable)
    {
        TileEntityHarvester.registerHarvestable(harvestable);
    }

    public static void registerFertilizable(IFactoryFertilizable fertilizable)
    {
        TileEntityFertilizer.registerFertilizable(fertilizable);
    }

    public static void registerFertilizerItem(int itemId)
    {
        TileEntityFertilizer.registerFertilizerItem(itemId);
    }

    public static void registerRanchable(IFactoryRanchable ranchable)
    {
        TileEntityRancher.registerRanchable(ranchable);
    }

    public enum PowerSystem
    {
        Redstone, BuildCraft
    }

    public enum Machine
    {
        Planter, Fisher, Harvester, Fertilizer, Rancher, Vet, Collector, Breaker, Weather
    }

    @ForgeSubscribe
    public void registerOre(OreRegisterEvent event)
    {
        if (event.Name.equals("ingotRefinedIron"))
        {
            registerRecipes(event.Ore);
        }
    }

    public void onRotatedTileEntity(TileEntityFactory te, Orientations direction)
    {
        Object[] toSend = { te.xCoord, te.yCoord, te.zCoord, direction.ordinal() };
        PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, 50, te.worldObj.getWorldInfo().getDimension(), PacketWrapper.createPacket("MFReloaded", 1, toSend));
    }
    
    public boolean fertilizeGiantMushroom(World world, int x, int y, int z)
    {
        int blockId = world.getBlockId(x, y, z);
        return ((BlockMushroom)Block.blocksList[blockId]).fertilizeMushroom(world, x, y, z, world.rand);
    }
    
    public void fertilizeStemPlant(World world, int x, int y, int z)
    {
        int blockId = world.getBlockId(x, y, z);
        ((BlockStem)Block.blocksList[blockId]).fertilizeStem(world, x, y, z);
    }
}
