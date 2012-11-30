package atomicstryker.powerconverters.common;

import ic2.api.Items;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.Property;
import atomicstryker.powerconverters.client.ClientPacketHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "PowerConverters", name = "Power Converters", version = "1.4.5R1.4.0", dependencies = "required-after:IC2;required-after:BuildCraft|Energy")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "PowerConverters" }, packetHandler = ClientPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class PowerConverterCore
{
    public static String terrainTexture = "/atomicstryker/powerconverters/client/sprites/terrain_0.png";
    public static String itemTexture = "/atomicstryker/powerconverters/client/sprites/items_0.png";

    public static Block powerConverterBlock;

    public static Item jetpackFuellerItem;

    public static Property powerConverterBlockId;
    public static Property jetpackFuellerItemId;

    public static int textureOffsetEngineGeneratorLV = 6;
    public static int textureOffsetEngineGeneratorMV = 12;
    public static int textureOffsetEngineGeneratorHV = 18;
    public static int textureOffsetOilFabricator = 24;
    public static int textureOffsetEnergyLinkDisconnected = 30;
    public static int textureOffsetEnergyLinkConnected = 36;
    public static int textureOffsetLavaFabricator = 42;
    public static int textureOffsetGeomk2OffDisconnected = 48;
    public static int textureOffsetGeomk2OffConnected = 54;
    public static int textureOffsetGeomk2OnDisconnected = 60;
    public static int textureOffsetGeomk2OnConnected = 66;
    public static int textureOffsetWaterStrainerOffDisconnected = 72;
    public static int textureOffsetWaterStrainerOffConnected = 78;
    public static int textureOffsetWaterStrainerOnDisconnected = 84;
    public static int textureOffsetWaterStrainerOnConnected = 90;

    public static int bcToICScaleNumerator;
    public static int bcToICScaleDenominator;
    public static int icToBCScaleNumerator;
    public static int icToBCScaleDenominator;
    public static int oilUnitCostInEU;
    public static int lavaUnitCostInEU;
    public static int euProducedPerLavaUnit;
    public static int euProducedPerWaterUnit;
    public static int waterConsumedPerOutput;
    public static int jetpackFuelRefilledPerFuelUnit;
    public static int fuelCanDamageValue;

    public static int euPerSecondLava;
    public static int euPerSecondWater;

    public static boolean enableFuelConversion;

    public static boolean enableEngineGenerator;
    public static boolean enableEnergyLink;
    public static boolean enableLavaFab;
    public static boolean enableOilFab;
    public static boolean enableGeoMk2;
    public static boolean enableWaterStrainer;

    public static boolean enableJetpackFueller;

    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        Configuration c = new Configuration(evt.getSuggestedConfigurationFile());
        c.load();
        powerConverterBlockId = c.getBlock("ID.PowerConverter", 190);
        jetpackFuellerItemId = c.getItem("ID.JetpackFueller", 17900);

        Property bcToICScaleNumeratorProperty = c.get(c.CATEGORY_GENERAL, "Scale.BCtoIC.Numerator", 5);
        Property bcToICScaleDenominatorProperty = c.get(c.CATEGORY_GENERAL, "Scale.BCtoIC.Denominator", 2);
        bcToICScaleDenominatorProperty.comment = "This property and Numerator set the ratio for power conversion. By default, going off the value of a piece of coal, one BC MJ is worth 2.5 IC2 EUs.";

        Property icToBCScaleNumeratorProperty = c.get(c.CATEGORY_GENERAL, "Scale.ICtoBC.Numerator", 2);
        Property icToBCScaleDenominatorProperty = c.get(c.CATEGORY_GENERAL, "Scale.ICtoBC.Denominator", 5);
        icToBCScaleDenominatorProperty.comment = "This by default is 2/5, the inverse of the BC to IC scale. Note that the Energy Link block has a currently unfixed bug which will add ~10% loss on top of this ratio.";

        Property euPerSecondLavaProperty = c.get(c.CATEGORY_GENERAL, "Rate.GeoMk2EUPerTick", 20);
        euPerSecondLavaProperty.comment = "The EU/t output of the Geothermal Mk. 2.";
        Property euPerSecondWaterProperty = c.get(c.CATEGORY_GENERAL, "Rate.WaterStrainerEUPerTick", 2);
        euPerSecondWaterProperty.comment = "The EU/t output of the Water Strainer.";

        Property oilCostEUProperty = c.get(c.CATEGORY_GENERAL, "Scale.OilCostInEU", 50);
        oilCostEUProperty.comment = "One oil bucket is worth 20,000 BC MJ; there are 1000 units per bucket. Using the above ratio of 2.5 EUs per MJ, one 20 MJ unit is worth 50 EUs.";
        Property lavaCostEUProperty = c.get(c.CATEGORY_GENERAL, "Scale.LavaCostInEU", 50);
        lavaCostEUProperty.comment = "One lava bucket is worth 20,000 BC MJ; there are 1000 units per bucket. Using the above ratio of 2.5 EUs per MJ, one 20 MJ unit is worth 50 EUs. Note that lava is worth less (20EU per unit) in IC2 than in BC.";
        Property euProducedPerLavaUnitProperty = c.get(c.CATEGORY_GENERAL, "Scale.EUGeneratedPerLavaUnit", 50);
        euProducedPerLavaUnitProperty.comment = "See comments on the lava unit cost property. This number should probably match that one, but this is for how much power the geo mk2 produces.";
        Property waterConsumedPerOutputProperty = c.get(c.CATEGORY_GENERAL, "Scale.WaterConsumedPerTick", 2);
        waterConsumedPerOutputProperty.comment = "Combines with Scale.EUGeneratedPerWaterOutput for the Water Strainer. This determines how much water is used per tick, to enable greater water consumption as you cannot go lower than 1 EU per water unit without going to 0. Note that the water strainer will only consume a constant amount of water per tick, so this will also throttle its output.";
        Property euProducedPerWaterOutputProperty = c.get(c.CATEGORY_GENERAL, "Scale.EUGeneratedPerWaterOutput", 1);
        euProducedPerWaterOutputProperty.comment = "IC2's water generator produces 1000 EU per water bucket, or 1 EU per water unit. BC has no equivalent.";
        Property jetpackFuelRefilledPerFuelUnitProperty = c.get(c.CATEGORY_GENERAL, "Scale.JetpackFuelFilledPerFuelUnit", 468);
        jetpackFuelRefilledPerFuelUnitProperty.comment = "A jetpack is fully fuelled by 6 coalfuel cells, which are 4,000 EUs each, or 24000 EUs total. The Jetpack has 18,000 fuel units. Each unit is worth 1.33333... EUs. Each unit of fuel is worth 625 EUs. Thus, each unit of BC fuel is worth 468.75 (ish) jetpack fuel units, or 468 rounded down.";

        Property enableJetpackFuellingItemProperty = c.get(c.CATEGORY_GENERAL, "Enable.JetpackFueller", true);
        Property enableEngineGeneratorProperty = c.get(c.CATEGORY_GENERAL, "Enable.EngineGenerator", true);
        Property enableEnergyLinkProperty = c.get(c.CATEGORY_GENERAL, "Enable.EnergyLink", true);
        Property enableLavaFabProperty = c.get(c.CATEGORY_GENERAL, "Enable.LavaFabricator", true);
        Property enableOilFabProperty = c.get(c.CATEGORY_GENERAL, "Enable.OilFabricator", true);
        Property enableGeoMk2Property = c.get(c.CATEGORY_GENERAL, "Enable.GeothermalMk2", false);
        Property enableWaterStrainerProperty = c.get(c.CATEGORY_GENERAL, "Enable.WaterStrainer", true);

        Property enableFuelConversionCraftingProperty = c.get(c.CATEGORY_GENERAL, "Enable.FuelConversionCrafting", true);
        enableFuelConversionCraftingProperty.comment = "If true, you can craft a BC fuel bucket + IC empty fuel can into a filled IC fuel can. The reverse is not provided, as it would be a massive gain of energy.";
        Property fuelConversionValueProperty = c.get(c.CATEGORY_GENERAL, "Scale.CraftedFuelCanValue", 16000);
        fuelConversionValueProperty.comment = "The value of a fuel can crafted from BC fuel buckets. There are 5 EUs per each unit of this setting. Note that as this is stored in the can's damage value, the maximum setting is 32767; settings above 16000 seem to not function correctly with IC2.";
        c.save();

        bcToICScaleNumerator = Integer.parseInt(bcToICScaleNumeratorProperty.value);
        bcToICScaleDenominator = Integer.parseInt(bcToICScaleDenominatorProperty.value);
        icToBCScaleNumerator = Integer.parseInt(icToBCScaleNumeratorProperty.value);
        icToBCScaleDenominator = Integer.parseInt(icToBCScaleDenominatorProperty.value);
        oilUnitCostInEU = Integer.parseInt(oilCostEUProperty.value);
        lavaUnitCostInEU = Integer.parseInt(lavaCostEUProperty.value);
        euProducedPerLavaUnit = Integer.parseInt(euProducedPerLavaUnitProperty.value);
        euProducedPerWaterUnit = Integer.parseInt(euProducedPerWaterOutputProperty.value);
        waterConsumedPerOutput = Integer.parseInt(waterConsumedPerOutputProperty.value);
        jetpackFuelRefilledPerFuelUnit = Integer.parseInt(jetpackFuelRefilledPerFuelUnitProperty.value);
        fuelCanDamageValue = Integer.parseInt(fuelConversionValueProperty.value);

        euPerSecondLava = Integer.parseInt(euPerSecondLavaProperty.value);
        euPerSecondWater = Integer.parseInt(euPerSecondWaterProperty.value);

        enableFuelConversion = Boolean.parseBoolean(enableFuelConversionCraftingProperty.value);

        enableJetpackFueller = Boolean.parseBoolean(enableJetpackFuellingItemProperty.value);
        enableEngineGenerator = Boolean.parseBoolean(enableEngineGeneratorProperty.value);
        enableEnergyLink = Boolean.parseBoolean(enableEnergyLinkProperty.value);
        enableLavaFab = Boolean.parseBoolean(enableLavaFabProperty.value);
        enableOilFab = Boolean.parseBoolean(enableOilFabProperty.value);
        enableGeoMk2 = Boolean.parseBoolean(enableGeoMk2Property.value);
        enableWaterStrainer = Boolean.parseBoolean(enableWaterStrainerProperty.value);
    }

    @Init
    public void load(FMLInitializationEvent evt)
    {
        powerConverterBlock = new BlockPowerConverter(Integer.parseInt(powerConverterBlockId.value));

        GameRegistry.registerBlock(powerConverterBlock, ItemPowerConverter.class);

        jetpackFuellerItem = new ItemJetpackFueller(Integer.parseInt(jetpackFuellerItemId.value));

        GameRegistry.registerTileEntity(TileEntityEngineGenerator.class, "EngineGenerator");
        GameRegistry.registerTileEntity(TileEntityOilFabricator.class, "OilFabricator");
        GameRegistry.registerTileEntity(TileEntityEnergyLink.class, "EnergyLink");
        GameRegistry.registerTileEntity(TileEntityLavaFabricator.class, "LavaFabricator");
        GameRegistry.registerTileEntity(TileEntityGeoMk2.class, "GeothermalMk2");
        GameRegistry.registerTileEntity(TileEntityWaterStrainer.class, "WaterStrainer");
    }

    @PostInit
    public void afterModsLoaded(FMLPostInitializationEvent evt)
    {
        // Engine generators
        if (enableEngineGenerator)
        {
            // LV engine generator
            GameRegistry.addRecipe(
                    new ItemStack(powerConverterBlock, 1, 0),
                    new Object[] { "GEG", "RSR", "GDG", Character.valueOf('E'), new ItemStack(BuildCraftEnergy.engineBlock, 1, 0), Character.valueOf('S'), Items.getItem("lvTransformer"),
                            Character.valueOf('G'), Item.ingotGold, Character.valueOf('R'), Item.redstone, Character.valueOf('D'), BuildCraftCore.ironGearItem });

            // MV engine generator
            GameRegistry.addRecipe(
                    new ItemStack(powerConverterBlock, 1, 1),
                    new Object[] { "GEG", "RSR", "GDG", Character.valueOf('E'), new ItemStack(BuildCraftEnergy.engineBlock, 1, 1), Character.valueOf('S'), Items.getItem("mvTransformer"),
                            Character.valueOf('G'), Item.ingotGold, Character.valueOf('R'), Item.redstone, Character.valueOf('D'), BuildCraftCore.goldGearItem });

            // HV engine generator
            GameRegistry.addRecipe(
                    new ItemStack(powerConverterBlock, 1, 2),
                    new Object[] { "GEG", "RSR", "GDG", Character.valueOf('E'), new ItemStack(BuildCraftEnergy.engineBlock, 1, 2), Character.valueOf('S'), Items.getItem("hvTransformer"),
                            Character.valueOf('G'), Item.ingotGold, Character.valueOf('R'), Item.redstone, Character.valueOf('D'), BuildCraftCore.diamondGearItem });
        }

        // Oil fabricator
        if (enableOilFab)
        {
            GameRegistry.addRecipe(new ItemStack(powerConverterBlock, 1, 3), new Object[] { "LDL", "ATA", "LML", Character.valueOf('L'), Items.getItem("advancedAlloy"), Character.valueOf('D'),
                    BuildCraftCore.diamondGearItem, Character.valueOf('T'), Block.tnt, Character.valueOf('A'), BuildCraftFactory.tankBlock, Character.valueOf('M'), Items.getItem("massFabricator"), });
        }

        // Energy link
        if (enableEnergyLink)
        {
            GameRegistry.addRecipe(new ItemStack(powerConverterBlock, 1, 4), new Object[] { "ARA", "CRP", "GRG", Character.valueOf('A'), Items.getItem("advancedAlloy"), Character.valueOf('G'),
                    BuildCraftCore.goldGearItem, Character.valueOf('C'), Items.getItem("insulatedCopperCableItem"), Character.valueOf('P'), BuildCraftTransport.pipePowerWood, Character.valueOf('R'),
                    Item.redstone });
        }

        // Lava fabricator
        if (enableLavaFab)
        {
            GameRegistry.addRecipe(new ItemStack(powerConverterBlock, 1, 5),
                    new Object[] { "LDL", "ATA", "LML", Character.valueOf('L'), Items.getItem("advancedAlloy"), Character.valueOf('D'), BuildCraftCore.goldGearItem, Character.valueOf('T'),
                            Block.stoneOvenIdle, Character.valueOf('A'), BuildCraftFactory.tankBlock, Character.valueOf('M'), Items.getItem("massFabricator"), });
        }

        // Water strainer
        if (enableWaterStrainer)
        {
            GameRegistry.addRecipe(new ItemStack(powerConverterBlock, 1, 7),
                    new Object[] { "TWP", Character.valueOf('T'), BuildCraftFactory.tankBlock, Character.valueOf('W'), Items.getItem("waterMill"), Character.valueOf('P'),
                            BuildCraftTransport.pipeLiquidsIron });
        }
        // Geothermal MK2 -- ALREADY IN IC2 1.64
        if (enableGeoMk2)
        {
            GameRegistry.addShapelessRecipe(new ItemStack(powerConverterBlock, 1, 6), new Object[] { Items.getItem("geothermalGenerator"), BuildCraftFactory.tankBlock });
        }

        if (enableJetpackFueller)
        {
            GameRegistry.addRecipe(new ItemStack(jetpackFuellerItem),
                    new Object[] { "WRS", Character.valueOf('W'), BuildCraftTransport.pipeLiquidsWood, Character.valueOf('R'), Items.getItem("rubber"), Character.valueOf('S'), Item.stick });
        }
        if (enableFuelConversion)
        {
            ItemStack newFuelCan = Items.getItem("filledFuelCan").copy();
            newFuelCan.setItemDamage(fuelCanDamageValue);
            GameRegistry.addShapelessRecipe(newFuelCan, new Object[] { Items.getItem("fuelCan"), BuildCraftEnergy.bucketFuel });
        }

        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 0), "Engine Generator (LV)");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 1), "Engine Generator (MV)");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 2), "Engine Generator (HV)");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 3), "Oil Fabricator");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 4), "Energy Link");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 5), "Lava Fabricator");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 6), "Geothermal Generator Mk. 2");
        LanguageRegistry.instance().addName(new ItemStack(powerConverterBlock, 1, 7), "Water Strainer");

        LanguageRegistry.instance().addName(PowerConverterCore.jetpackFuellerItem, "Jetpack Fueller");
    }

    public static ForgeDirection getOrientationFromSide(int side)
    {
        if (side == 0)
            return ForgeDirection.DOWN;
        if (side == 1)
            return ForgeDirection.UP;
        if (side == 2)
            return ForgeDirection.WEST;
        if (side == 3)
            return ForgeDirection.EAST;
        if (side == 4)
            return ForgeDirection.SOUTH;
        if (side == 5)
            return ForgeDirection.NORTH;
        return ForgeDirection.UNKNOWN;
    }

    public static void sendTEStoredLiquidPacket(TileEntityLiquidGenerator te, int liquidStored)
    {
        Object[] input = {te.xCoord, te.yCoord, te.zCoord, liquidStored};
        PacketDispatcher.sendPacketToAllInDimension(ForgePacketWrapper.createPacket("PowerConverters", 1, input), te.worldObj.getWorldInfo().getDimension());
    }
}
