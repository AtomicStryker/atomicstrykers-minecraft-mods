package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassConfig;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;

public class FinderCompassClientTicker {

    public static FinderCompassClientTicker instance;
    private Minecraft mc;
    public FinderCompassLogic compassLogic;
    private CompassSetting currentSetting;
    private Item COMPASS_ITEM_ID;
    private boolean repeat;

    public FinderCompassClientTicker() {
        instance = this;
        repeat = false;
        currentSetting = null;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void playerLoginToServer(ClientPlayerNetworkEvent.LoggedInEvent evt) {
        // client starting point, also local servers
        FinderCompassMod.instance.initIfNeeded();
    }

    public void onLoad() {
        COMPASS_ITEM_ID = Items.COMPASS;

        mc = Minecraft.getInstance();
        ItemModelShaper mesher = mc.getItemRenderer().getItemModelShaper();
        mesher.register(COMPASS_ITEM_ID, new ModelResourceLocation("compass", "inventory"));

        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && compassLogic != null) {
            if (tick.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == COMPASS_ITEM_ID) {
                if (mc.options.keyAttack.isDown()) {
                    if (!repeat) {
                        repeat = true;
                        switchSetting();
                        tick.player.level.playSound(null, new BlockPos(tick.player.getOnPos()), SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
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
        if (getSettingsList().isEmpty()) {
            return;
        }

        int nextIndex;
        if (currentSetting == null) {
            nextIndex = 0;
        } else {
            currentSetting.onDisableThisConfig();

            nextIndex = getSettingsList().indexOf(currentSetting) + 1;
            if (nextIndex >= getSettingsList().size()) {
                nextIndex = 0;
            }
        }

        currentSetting = getSettingsList().get(nextIndex);
        FinderCompassLogic.hasFeature = false;

        if (mc.level != null) {
            mc.level.playSound(null, new BlockPos(mc.player.getOnPos()), SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
            mc.gui.getChat().addMessage(Component.literal("Finder Compass Mode: " + currentSetting.getName()));
        }
    }

    /**
     * Used by the server packet to override the clientside config
     */
    public void inputOverrideConfig(String json) {
        getSettingsList().clear();
        FinderCompassMod.LOGGER.info("inputting Finder Compass config from serverside: {}", json);
        CompassConfig compassConfig = GsonConfig.loadConfigFromString(CompassConfig.class, json);
        FinderCompassMod.instance.loadSettingListFromConfig(compassConfig);
        mc.gui.getChat().addMessage(Component.literal("Finder Compass server config loaded; " + getSettingsList().size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(BlockPos input, BlockState blockState) {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(blockState);
        currentSetting.getNewFoundTargets().put(key, input);
    }

    private ArrayList<CompassSetting> getSettingsList() {
        return FinderCompassMod.instance.settingList;
    }
}
