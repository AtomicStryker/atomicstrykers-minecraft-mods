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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void playerLoginToServer(ClientPlayerNetworkEvent.LoggingIn evt) {
        // client starting point, also local servers
        FinderCompassMod.instance.initIfNeeded();
    }

    public void onLoad() {
        COMPASS_ITEM_ID = Items.COMPASS;

        mc = Minecraft.getInstance();

        // in case we have our own compass renderer? but as of 1.19 no we do not
        if (COMPASS_ITEM_ID != Items.COMPASS) {
            ItemModelShaper mesher = mc.getItemRenderer().getItemModelShaper();
            mesher.register(COMPASS_ITEM_ID, ModelResourceLocation.vanilla("compass", "inventory"));
        }

        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post tick) {
        if (compassLogic != null) {
            if (tick.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() == COMPASS_ITEM_ID) {
                if (mc.options.keyAttack.isDown()) {
                    if (!repeat) {
                        repeat = true;
                        switchSetting();
                        tick.getEntity().level().playSound(null, new BlockPos(tick.getEntity().getOnPos()), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, 0.6F);
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
            mc.level.playSound(mc.player, new BlockPos(mc.player.getOnPos()), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, 0.6F);
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
