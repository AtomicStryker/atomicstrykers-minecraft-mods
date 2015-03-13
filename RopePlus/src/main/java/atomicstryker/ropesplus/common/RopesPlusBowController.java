package atomicstryker.ropesplus.common;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;

public class RopesPlusBowController
{
    private static HashMap<String, ItemStack> vanillaBows = new HashMap<String, ItemStack>();
    
    @SubscribeEvent
    public void onArrowNock(ArrowNockEvent event)
    {
        final EntityPlayer player = event.entityPlayer;
        final ItemStack firingBow = player.getCurrentEquippedItem();
        if (!Settings_RopePlus.disableBowHook
        && firingBow.getItem() != RopesPlusCore.instance.bowRopesPlus)
        {
            int slot = RopesPlusCore.instance.selectedSlot(player);
            if (slot != -1)
            {
                final ItemStack selected = player.inventory.mainInventory[slot];
                if (selected != null
                && selected.getItem() instanceof ItemArrow303
                && ((ItemArrow303)selected.getItem()).arrow.tip != Items.flint)
                {
                    vanillaBows.put(player.getName(), firingBow.copy());
                    final ItemStack replacementBow = new ItemStack(RopesPlusCore.instance.bowRopesPlus);
                    event.result = replacementBow;
                    player.setItemInUse(replacementBow, replacementBow.getMaxItemUseDuration());
                    event.setCanceled(true);
                }
            }
        }
    }
    
    public static ItemStack getVanillaBowForPlayer(EntityPlayer player)
    {
        return vanillaBows.get(player.getName());
    }
}
