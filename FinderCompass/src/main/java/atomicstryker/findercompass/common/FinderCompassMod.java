package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.CompassSetting;
import atomicstryker.findercompass.client.FinderCompassClient;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import atomicstryker.findercompass.common.network.NetworkHelper;
import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
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
    public static ISidedProxy proxy = DistExecutor.safeRunForDist(() -> FinderCompassClient::new, () -> FinderCompassServer::new);
    public CompassConfig compassConfig;
    public ArrayList<CompassSetting> settingList;

    public NetworkHelper networkHelper;

    public FinderCompassMod() {
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        networkHelper = new NetworkHelper("findercompass", HandshakePacket.class, FeatureSearchPacket.class);
    }

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent evt) {
        // dedicated server starting point
        initIfNeeded();
    }

    /**
     * called either by serverStarted or by FinderCompassClientTicker.playerLoginToServer
     */
    public void initIfNeeded() {
        if (FinderCompassClientTicker.instance == null) {
            compassConfig = createDefaultConfig();
            try {
                compassConfig = GsonConfig.loadConfigWithDefault(CompassConfig.class, new File(proxy.getMcFolder() + File.separator + "config" + File.separator, "findercompass.cfg"), compassConfig);
                loadSettingListFromConfig(compassConfig);
                // FinderCompassClientTicker.instance is set in clientside commonSetup
                proxy.commonSetup();
            } catch (IOException e) {
                LOGGER.error("IOException parsing config", e);
            }
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
        LOGGER.info("Server sending Finder Compass Handshake to player {}", event.getPlayer().getDisplayName());
        networkHelper.sendPacketToPlayer(new HandshakePacket("server", GsonConfig.jsonFromConfig(compassConfig)), (ServerPlayer) event.getPlayer());
    }

    private String getStringFromBlockState(BlockState blockState) {

        Map<String, String> blockMap = new HashMap<>();

        blockMap.put("block", ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString());
        for (Property<?> property : blockState.getBlock().getStateDefinition().getProperties()) {
            blockMap.put(property.getName(), blockState.getValue(property).toString());
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
        BlockState reconstructedState = block.defaultBlockState();
        for (Property<?> property : block.getStateDefinition().getProperties()) {
            reconstructedState = setValueHelper(reconstructedState, property, property.getName(), blockMap.get(property.getName()));
        }
        return reconstructedState;
    }

    private <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S blockState, Property<T> property, String propertyName, String valueString) {
        Optional<T> optional = property.getValue(valueString);
        if (optional.isPresent()) {
            return (blockState.setValue(property, optional.get()));
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
                BlockState state = Blocks.GOLD_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(245, 245, 0, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(245, 245, 0, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.IRON_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(112, 112, 112, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(112, 112, 112, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.COAL_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(51, 26, 0, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(51, 26, 0, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.COPPER_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(141, 83, 46, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(141, 83, 46, 15, 1, -64, 0, 0));
            }

            workingManMineables.setNeedles(needleMap);
            workingManMineables.setFeatureNeedle("village");
            needleSetList.add(workingManMineables);
        }

        {
            CompassConfig.NeedleSet shinyStones = new CompassConfig.NeedleSet();
            shinyStones.setName("Shiny Stones");
            Map<String, int[]> needleMap = new HashMap<>();

            {
                BlockState state = Blocks.DIAMOND_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(51, 255, 204, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(51, 255, 204, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.LAPIS_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(55, 70, 220, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(55, 70, 220, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.REDSTONE_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(255, 125, 155, 15, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(255, 125, 155, 15, 1, -64, 0, 0));
            }

            {
                BlockState state = Blocks.EMERALD_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(26, 255, 26, 7, 1, 0, 384, 0));
            }

            {
                BlockState state = Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(26, 255, 26, 7, 1, -64, 0, 0));
            }

            shinyStones.setNeedles(needleMap);
            shinyStones.setFeatureNeedle("stronghold");
            needleSetList.add(shinyStones);
        }

        {
            CompassConfig.NeedleSet netherDelights = new CompassConfig.NeedleSet();
            netherDelights.setName("Nether Delights");
            Map<String, int[]> needleMap = new HashMap<>();

            {
                // nether_gold_ore
                BlockState state = Blocks.NETHER_GOLD_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(245, 245, 0, 15, 1, 1, 256, 0));
            }

            {
                // ancient_debris
                BlockState state = Blocks.ANCIENT_DEBRIS.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(51, 255, 204, 15, 1, 1, 16, 0));
            }

            {
                BlockState state = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
                String string = getStringFromBlockState(state);
                needleMap.put(string, getSettingIntArray(55, 70, 220, 15, 1, 1, 256, 0));
            }

            netherDelights.setNeedles(needleMap);
            netherDelights.setFeatureNeedle("fortress");
            needleSetList.add(netherDelights);
        }

        compassConfig.setNeedles(needleSetList);

        compassConfig.setNeedleWidthOfScreenWidth(0.01D);
        compassConfig.setNeedleHeightOfScreenHeight(0.10D);
        compassConfig.setOnScreenPositionHeight(0.5D);
        compassConfig.setOnScreenPositionWidth(0.5D);
        compassConfig.setMustHoldCompassInHandToBeActive(true);

        return compassConfig;
    }

    /**
     * @param colorRed             rgb red part
     * @param colorGreen           rgb green part
     * @param colorBlue            rgb blue part
     * @param xzSearchRange        block finder checks this range horizontally from player position
     * @param ySearchRange         block finder checks the player XYZ block plus this value up- and downwards, note: 1 means blocks in the ceiling above are not checked, but the floor below is, as the ceiling is 2 blocks away from the players feet
     * @param minY                 minimum world height to be searched
     * @param maxY                 maximum world height to be searched
     * @param boolSearchEvery15Sec if 1, the block finder only executes once every 15 seconds (long range searches)
     * @return array of ints used in the config file
     */
    private int[] getSettingIntArray(int colorRed, int colorGreen, int colorBlue, int xzSearchRange, int ySearchRange, int minY, int maxY, int boolSearchEvery15Sec) {
        return new int[]{colorRed, colorGreen, colorBlue, xzSearchRange, ySearchRange, minY, maxY, boolSearchEvery15Sec};
    }

    /**
     * wrap MC structure search mechanic for finder compass
     */
    public BlockPos findLevelStructure(ServerLevel level, BlockPos searchPosition, String featureId) {
        // EnderEyeItem is useful for looking up how map structures work if they change
        TagKey<Structure> configuredStructureFeatureTagKey = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(featureId));
        return level.findNearestMapStructure(configuredStructureFeatureTagKey, searchPosition, FeatureSearchPacket.SEARCH_RADIUS, false);
    }

}
