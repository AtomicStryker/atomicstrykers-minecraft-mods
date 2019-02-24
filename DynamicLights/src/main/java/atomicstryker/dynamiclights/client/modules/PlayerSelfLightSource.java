package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 
 * @author AtomicStryker
 *
 *         Offers Dynamic Light functionality to the Player Entity itself.
 *         Handheld Items and Armor can give off Light through this Module.
 * 
 *         With version 1.1.3 and later you can also use FMLIntercomms to use
 *         this and have the player shine light. Like so:
 * 
 *         FMLInterModComms.sendRuntimeMessage(sourceMod,
 *         "DynamicLights_thePlayer", "forceplayerlighton", "");
 *         FMLInterModComms.sendRuntimeMessage(sourceMod,
 *         "DynamicLights_thePlayer", "forceplayerlightoff", "");
 * 
 *         Note you have to track this yourself. Dynamic Lights will accept and
 *         obey, but not recover should you get stuck in the on or off state
 *         inside your own code. It will not revert to off on its own.
 *
 */
@Mod(PlayerSelfLightSource.MOD_ID)
@Mod.EventBusSubscriber(modid = PlayerSelfLightSource.MOD_ID, value = Dist.CLIENT)
public class PlayerSelfLightSource implements IDynamicLightSource
{
    static final String MOD_ID = "dynamiclights_theplayer";

    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;

    public boolean fmlOverrideEnable;

    public PlayerSelfLightSource()
    {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
    }

    public void onConfigLoad(ModConfig.ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == CLIENT_SPEC)
        {
            loadConfig();
        }
    }

    public static final PlayerSelfLightSource.ClientConfig CLIENT_CONFIG;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static List<? extends String> itemsList = new ArrayList<>();
    public static List<? extends String> notWaterProofList = new ArrayList<>();

    static
    {
        final Pair<PlayerSelfLightSource.ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PlayerSelfLightSource.ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    public static class ClientConfig
    {
        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemsList;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> notWaterProofList;

        ClientConfig(ForgeConfigSpec.Builder builder)
        {
            itemsList = builder.comment("Items that shine light").translation("forge.configgui.itemsList").defineList("itemsList", getDefaultLightItems(), i -> i instanceof String);
            notWaterProofList = builder.comment("Items that stop shining light when in water").translation("forge.configgui.notWaterProofList").defineList("notWaterProofList", Lists.newArrayList(),
                    i -> i instanceof String);
            builder.pop();
        }
    }

    private static List<String> getDefaultLightItems()
    {
        ItemStack torchStack = new ItemStack(Blocks.TORCH);
        List<String> output = new ArrayList<>();
        output.add(ItemConfigHelper.fromItemStack(torchStack));
        return output;
    }

    public static void loadConfig()
    {
        itemsList = CLIENT_CONFIG.itemsList.get();
        notWaterProofList = CLIENT_CONFIG.notWaterProofList.get();
        itemsMap = new ItemConfigHelper(itemsList);
        notWaterProofItems = new ItemConfigHelper(notWaterProofList);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (thePlayer != Minecraft.getInstance().player || lastWorld != thePlayer.world)
        {
            thePlayer = Minecraft.getInstance().player;
            if (thePlayer != null)
            {
                lastWorld = thePlayer.world;
            }
            else
            {
                lastWorld = null;
            }
        }

        if (thePlayer != null && thePlayer.isAlive() && !DynamicLights.globalLightsOff())
        {
            if (fmlOverrideEnable)
            {
                return;
            }
            int prevLight = lightLevel;

            ItemStack item = null;
            int main = getLightFromItemStack(thePlayer.getHeldItemMainhand());
            int off = getLightFromItemStack(thePlayer.getHeldItemOffhand());
            if (main >= off && main > 0)
            {
                item = thePlayer.getHeldItemMainhand();
                lightLevel = main;
            }
            else if (off >= main && off > 0)
            {
                item = thePlayer.getHeldItemOffhand();
                lightLevel = off;
            }
            else
            {
                lightLevel = 0;
            }
            // System.out.printf("Self light tick, main:%d, off:%d, light:%d, chosen
            // itemstack:%s\n", main, off, lightLevel, item);

            for (ItemStack armor : thePlayer.inventory.armorInventory)
            {
                lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
            }

            if (prevLight != 0 && lightLevel != prevLight)
            {
                lightLevel = 0;
            }
            else
            {
                if (thePlayer.isBurning())
                {
                    lightLevel = 15;
                }
                else
                {
                    if (checkPlayerWater(thePlayer) && notWaterProofItems.contains(item))
                    {
                        lightLevel = 0;
                        // System.out.printf("Self light tick, water blocked light!\n");
                        for (ItemStack armor : thePlayer.inventory.armorInventory)
                        {
                            if (!notWaterProofItems.contains(armor))
                            {
                                lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
                            }
                        }
                    }
                }
            }

            if (!enabled && lightLevel > 0)
            {
                enableLight();
            }
            else if (enabled && lightLevel < 1)
            {
                disableLight();
            }
        }

    }

    private boolean checkPlayerWater(EntityPlayer thePlayer)
    {
        if (thePlayer.isInWater())
        {
            int x = MathHelper.floor(thePlayer.posX + 0.5D);
            int y = MathHelper.floor(thePlayer.posY + thePlayer.getEyeHeight());
            int z = MathHelper.floor(thePlayer.posZ + 0.5D);
            IBlockState is = thePlayer.world.getBlockState(new BlockPos(x, y, z));
            return is.getMaterial().isLiquid();
        }
        return false;
    }

    private int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null && itemsMap.contains(stack))
        {
            return 15;
        }
        return 0;
    }

    private void enableLight()
    {
        DynamicLights.addLightSource(this);
        enabled = true;
    }

    private void disableLight()
    {
        if (!fmlOverrideEnable)
        {
            DynamicLights.removeLightSource(this);
            enabled = false;
        }
    }

    @Override
    public Entity getAttachmentEntity()
    {
        return thePlayer;
    }

    @Override
    public int getLightLevel()
    {
        return lightLevel;
    }

}
