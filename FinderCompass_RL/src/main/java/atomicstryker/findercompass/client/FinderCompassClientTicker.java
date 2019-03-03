package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.block.Block;
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
import org.dimdev.rift.listener.client.ClientTickable;

import java.util.ArrayList;

public class FinderCompassClientTicker implements ClientTickable {

    public static FinderCompassClientTicker instance;

    private CompassSetting currentSetting;
    public FinderCompassLogic compassLogic;

    private final Minecraft mc;
    private Item COMPASS_ITEM_ID;
    private boolean repeat;
    private ArrayList<CompassSetting> settingList;
    private boolean loaded = false;

    public FinderCompassClientTicker() {
        instance = this;
        mc = Minecraft.getInstance();
        repeat = false;
        settingList = FinderCompassMod.instance.settingList;
        currentSetting = null;
    }

    public void onLoad() {
        COMPASS_ITEM_ID = Items.COMPASS;

        ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
        mesher.register(COMPASS_ITEM_ID, new ModelResourceLocation("compass", "inventory"));

        compassLogic = new FinderCompassLogic(mc);
        switchSetting();
    }

    @Override
    public void clientTick(Minecraft client) {

        if (client == null || client.player == null || client.player.world == null) {
            return;
        }

        if (!loaded) {
            loaded = true;
            onLoad();
        }

        if (client.player.getHeldItemMainhand().getItem() == COMPASS_ITEM_ID) {
            if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                if (!repeat) {
                    repeat = true;
                    switchSetting();
                    client.player.world.playSound(null, new BlockPos(client.player), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
                }
            } else {
                repeat = false;
            }
        }
        compassLogic.onTick();
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

    public void onFoundChunkCoordinates(BlockPos input, IBlockState blockState) {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(blockState);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
