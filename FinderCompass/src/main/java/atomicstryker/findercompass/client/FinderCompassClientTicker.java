package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassConfig;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class FinderCompassClientTicker {

    public static FinderCompassClientTicker instance;
    private Minecraft mc;
    public FinderCompassLogic compassLogic;
    private CompassSetting currentSetting;
    private Item COMPASS_ITEM_ID;
    private boolean repeat;
    private ArrayList<CompassSetting> settingList;

    public FinderCompassClientTicker() {
        instance = this;
        repeat = false;
        currentSetting = null;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onLoad() {
        COMPASS_ITEM_ID = Items.COMPASS;

        mc = Minecraft.getInstance();
        ItemModelMesher mesher = mc.getItemRenderer().getItemModelMesher();
        mesher.register(COMPASS_ITEM_ID, new ModelResourceLocation("compass", "inventory"));

        settingList = FinderCompassMod.instance.settingList;
        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && compassLogic != null) {
            if (tick.player.getHeldItemMainhand().getItem() == COMPASS_ITEM_ID) {
                if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                    if (!repeat) {
                        repeat = true;
                        switchSetting();
                        tick.player.world.playSound(null, new BlockPos(tick.player.getPositionVec()), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
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
        FinderCompassLogic.hasFeature = false;

        if (mc.world != null) {
            mc.world.playSound(null, new BlockPos(mc.player.getPositionVec()), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            mc.ingameGUI.getChatGUI().printChatMessage(new TranslationTextComponent("Finder Compass Mode: " + currentSetting.getName()));
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
                new TranslationTextComponent("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(BlockPos input, BlockState blockState) {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(blockState);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
