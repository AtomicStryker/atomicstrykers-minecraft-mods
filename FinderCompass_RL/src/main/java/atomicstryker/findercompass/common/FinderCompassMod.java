package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.CompassSetting;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.BootstrapListener;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class FinderCompassMod implements BootstrapListener {

    private static final Logger LOGGER = LogManager.getLogger();

    public static FinderCompassMod instance;

    public ArrayList<CompassSetting> settingList;

    public FinderCompassMod() {
        instance = this;
    }

    @Override
    public void afterVanillaBootstrap() {

        CompassConfig compassConfig = createDefaultConfig();

        try {
            GsonConfig.loadConfigWithDefault(CompassConfig.class, new File(Minecraft.getInstance().gameDir, "\\config\\findercompass.cfg"), compassConfig);

            settingList = new ArrayList<>();
            for (CompassConfig.NeedleSet needleSet : compassConfig.getNeedles()) {
                CompassSetting setting = new CompassSetting(needleSet.getName());

                for (Map.Entry<String, int[]> blockEntry : needleSet.getNeedles().entrySet()) {

                    IBlockState state = getBlockStateFromString(blockEntry.getKey());
                    if (state != null) {
                        CompassTargetData data = new CompassTargetData(state);
                        setting.getCustomNeedles().put(data, blockEntry.getValue());
                        setting.setHasStrongholdNeedle(false);
                        LOGGER.info("{}: parsed blockstate {} for colors {}", needleSet.getName(), state, blockEntry.getValue());
                    } else {
                        LOGGER.error("Could not identify block for input {}", blockEntry.getKey());
                    }
                }

                settingList.add(setting);
            }
        } catch (IOException e) {
            LOGGER.error("IOException parsing config", e);
        }
    }

    private String getStringFromBlockState(IBlockState blockState) {

        Map<String, String> blockMap = new HashMap<>();
        blockMap.put("block", IRegistry.BLOCK.getKey(blockState.getBlock()).toString());
        for (IProperty<?> property : blockState.getBlock().getStateContainer().getProperties()) {
            blockMap.put(property.getName(), blockState.get(property).toString());
        }
        Gson gson = new Gson();
        return gson.toJson(blockMap);
    }

    private IBlockState getBlockStateFromString(String json) {
        Gson gson = new Gson();
        Map<String, String> blockMap = gson.fromJson(json, HashMap.class);
        String resourceAsString = blockMap.get("block");
        Block block = IRegistry.BLOCK.get(new ResourceLocation(resourceAsString));
        if (block == null) {
            return null;
        }
        IBlockState reconstructedState = block.getDefaultState();
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
                IBlockState state = Blocks.GOLD_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{245, 245, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                IBlockState state = Blocks.IRON_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{245, 245, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                IBlockState state = Blocks.COAL_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{51, 26, 0, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            workingManMineables.setNeedles(needleMap);
            needleSetList.add(workingManMineables);
        }

        {
            CompassConfig.NeedleSet shinyStones = new CompassConfig.NeedleSet();
            shinyStones.setName("Shiny Stones");
            Map<String, int[]> needleMap = new HashMap<>();

            {
                IBlockState state = Blocks.DIAMOND_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{51, 255, 204, 15, 1, 1, 16, 0};
                needleMap.put(string, setting);
            }

            {
                IBlockState state = Blocks.LAPIS_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{55, 70, 220, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                IBlockState state = Blocks.REDSTONE_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{255, 125, 155, 15, 1, 1, 100, 0};
                needleMap.put(string, setting);
            }

            {
                IBlockState state = Blocks.EMERALD_ORE.getDefaultState();
                String string = getStringFromBlockState(state);
                int[] setting = new int[]{26, 255, 26, 7, 1, 4, 31, 0};
                needleMap.put(string, setting);
            }

            shinyStones.setNeedles(needleMap);
            needleSetList.add(shinyStones);
        }

        compassConfig.setNeedles(needleSetList);
        return compassConfig;
    }
}
