package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassConfig;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.ArrayList;

public class FinderCompassClientTicker {

    public static FinderCompassClientTicker instance;
    private final Minecraft mc;
    public FinderCompassLogic compassLogic;
    private CompassSetting currentSetting;
    private Item COMPASS_ITEM_ID;
    private boolean repeat;
    private ArrayList<CompassSetting> settingList;

    public FinderCompassClientTicker() {
        mc = Minecraft.getInstance();
        repeat = false;
        settingList = FinderCompassMod.instance.settingList;
        currentSetting = null;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onLoad() {
        COMPASS_ITEM_ID = Items.COMPASS;

        ItemModelMesher mesher = mc.getItemRenderer().getItemModelMesher();
        mesher.register(COMPASS_ITEM_ID, new ModelResourceLocation("compass", "inventory"));

        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick) {
        if (tick.phase == Phase.END && compassLogic != null) {
            if (tick.player.getHeldItemMainhand().getItem() == COMPASS_ITEM_ID) {
                if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                    if (!repeat) {
                        repeat = true;
                        switchSetting();
                        tick.player.world.playSound(null, new BlockPos(tick.player), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
                    }
                } else {
                    repeat = false;
                }
            }
            compassLogic.onTick();
        }
    }

    public CompassSetting getCurrentSetting() {
        return currentSetting;
    }

    public void switchSetting() {
        if (settingList.isEmpty()) {
            return;
        }

        int nextIndex;
        if (currentSetting == null) {
            nextIndex = 0;
        } else {
            currentSetting.onDisableThisConfig();

            nextIndex = settingList.indexOf(currentSetting) + 1;
            if (nextIndex >= settingList.size()) {
                nextIndex = 0;
            }
        }

        currentSetting = settingList.get(nextIndex);

        if (mc.world != null) {
            mc.world.playSound(null, new BlockPos(mc.player), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Finder Compass Mode: " + currentSetting.getName()));
        }
    }

    /**
     * Used by the server packet to override the clientside config
     */
    public void inputOverrideConfig(String json) {
        settingList.clear();
        CompassConfig compassConfig = GsonConfig.loadConfigFromString(CompassConfig.class, json);
        FinderCompassMod.instance.loadSettingListFromConfig(compassConfig);
        mc.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentTranslation("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(BlockPos input, IBlockState blockState) {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(blockState);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
