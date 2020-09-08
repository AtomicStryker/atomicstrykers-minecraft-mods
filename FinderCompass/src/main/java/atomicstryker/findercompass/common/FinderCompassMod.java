package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.CompassSetting;
import atomicstryker.findercompass.client.FinderCompassClient;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import atomicstryker.findercompass.common.network.NetworkHelper;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod(FinderCompassMod.MOD_ID)
@Mod.EventBusSubscriber(modid = FinderCompassMod.MOD_ID)
public class FinderCompassMod {

    public static final String MOD_ID = "findercompass";

    public static final Logger LOGGER = LogManager.getLogger();

    public static FinderCompassMod instance;
    public static ISidedProxy proxy = DistExecutor.runForDist(() -> () -> new FinderCompassClient(), () -> () -> new FinderCompassServer());
    public CompassConfig compassConfig;
    public ArrayList<CompassSetting> settingList;

    public NetworkHelper networkHelper;

    public FinderCompassMod() {
        instance = this;
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        networkHelper = new NetworkHelper("findercompass", HandshakePacket.class, FeatureSearchPacket.class);
    }

    public void preInit(FMLCommonSetupEvent evt) {
        compassConfig = createDefaultConfig();
        try {
            compassConfig = GsonConfig.loadConfigWithDefault(CompassConfig.class, new File(proxy.getMcFolder() + File.separator + "config" + File.separator, "findercompass.cfg"), compassConfig);
            loadSettingListFromConfig(compassConfig);
        } catch (IOException e) {
            LOGGER.error("IOException parsing config", e);
        }
    }

    public void loadSettingListFromConfig(CompassConfig input) {
        compassConfig = input;
        settingList = new ArrayList<>();
        for (CompassConfig.NeedleSet needleSet : compassConfig.getNeedles()) {
            CompassSetting setting = new CompassSetting(needleSet.getName(), needleSet.getFeatureNeedle());
            for (Map.Entry<String, int[]> blockEntry : needleSet.getNeedles().entrySet()) {
                BlockState state = getBlockStateFromString(blockEntry.getKey());
                if (state != null) {
                    CompassTargetData data = new CompassTargetData(state);
                    setting.getCustomNeedles().put(data, blockEntry.getValue());
                    LOGGER.info("{}: parsed blockstate {} for colors {}", needleSet.getName(), state, blockEntry.getValue());
                } else {
                    LOGGER.error("Could not identify block for input {}", blockEntry.getKey());
                }
            }
            settingList.add(setting);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        networkHelper.sendPacketToPlayer(new HandshakePacket("server", GsonConfig.jsonFromConfig(compassConfig)), (ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public void clientSetup(TickEvent.ClientTickEvent evt) {
        // hopefully this only executes on client?
        if (FinderCompassClientTicker.instance == null) {
            FinderCompassClientTicker.instance = new FinderCompassClientTicker();
            FinderCompassClientTicker.instance.onLoad();
            FinderCompassClientTicker.instance.switchSetting();
        }
    }

    private String getStringFromBlockState(BlockState blockState) {

        Map<String, String> blockMap = new HashMap<>();

        blockMap.put("block", ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString());
        for (IProperty<?> property : blockState.getBlock().getStateContainer().getProperties()) {
            blockMap.put(property.getName(), blockState.get(property).toString());
        }
        Gson gson = new Gson();
        return gson.toJson(blockMap);
    }

    private BlockState getBlockStateFromString(String json) {
        Gson gson = new Gson();
        Map<String, String> blockMap = gson.fromJson(json, HashMap.class);
        String resourceAsString = blockMap.get("block");
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(resourceAsString));
        if (block == null) {
            return null;
        }
        BlockState reconstructedState = block.getDefaultState();
        for (IProperty<?> property : block.getStateContainer().getProperties()) {
            reconstructedState = setValueHelper(reconstructedState, property, property.getName(), blockMap.get(property.getName()));
        }
        return reconstructedState;
    }

    private <S extends IStateHolder<S>, T extends Comparable<T>> S setValueHelper(S blockState, IProperty<T> property, String propertyName, String valueString) {
        Optional<T> optional = property.parseValue(valueString);
        if (optional.isPresent()) {
            return (blockState.with(property, optional.get()));
        } else {
            LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", propertyName, valueString, blockState.toString());
            return blockState;
        }
    }

    private CompassConfig createDefaultConfig() {
        CompassConfig compassConfig = new CompassConfig();

        List<CompassConfig.NeedleSet> needleSetList = new ArrayList<>();

        {
            CompassConfig.NeedleSet workingManMineables = new CompassConfig.NeedleSet();
            workingManMineables.setName("Working Man's Mineables");
            Map<String, int[]> needleMap = new HashMap<>();

            {
                BlockState state = Blocks.GOLD_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{245, 245, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                BlockState state = Blocks.IRON_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{245, 245, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                BlockState state = Blocks.COAL_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{51, 26, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            workingManMineables.setNeedles(needleMap);
            workingManMineables.setFeatureNeedle("Village");
            needleSetList.add(workingManMineables);
        }

        {
            CompassConfig.NeedleSet shinyStones = new CompassConfig.NeedleSet();
            shinyStones.setName("Shiny Stones");
            Map<String, int[]> needleMap = new HashMap<>();

            {
                BlockState state = Blocks.DIAMOND_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{51, 255, 204, 15, 1, 1, 16, 0};
                needleMap.put(string, setting);
            }

            {
                BlockState state = Blocks.LAPIS_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{55, 70, 220, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                BlockState state = Blocks.REDSTONE_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{255, 125, 155, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                BlockState state = Blocks.EMERALD_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{26, 255, 26, 7, 1, 4, 31, 0};
                needleMap.put(string, setting);
            }

            shinyStones.setNeedles(needleMap);
            shinyStones.setFeatureNeedle("Stronghold");
            needleSetList.add(shinyStones);
        }

        compassConfig.setNeedles(needleSetList);
        return compassConfig;
    }

}
