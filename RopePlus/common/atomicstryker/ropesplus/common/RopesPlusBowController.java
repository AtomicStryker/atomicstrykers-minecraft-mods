package atomicstryker.ropesplus.common;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;

public class RopesPlusBowController
{
    private static HashMap<EntityPlayer, ItemStack> vanillaBows = new HashMap<EntityPlayer, ItemStack>();
    
    @ForgeSubscribe
    public void onArrowNock(ArrowNockEvent event)
    {
        if (event.entityPlayer.getCurrentEquippedItem().getItem().shiftedIndex != RopesPlusCore.bowRopesPlus.shiftedIndex)
        {
            ItemStack selected = event.entityPlayer.inventory.mainInventory[RopesPlusCore.instance.selectedSlot(event.entityPlayer)];
            if (selected != null
            && selected.getItem() instanceof ItemArrow303
            && ((ItemArrow303)selected.getItem()).arrow.tip != Item.flint)
            {
                vanillaBows.put(event.entityPlayer, event.entityPlayer.getCurrentEquippedItem());
                ItemStack replacementBow = new ItemStack(RopesPlusCore.bowRopesPlus);
                event.result = replacementBow;
                event.entityPlayer.setItemInUse(replacementBow, replacementBow.getMaxItemUseDuration());
                event.setCanceled(true);
            }
        }
    }
    
    public static ItemStack getVanillaBowForPlayer(EntityPlayer player)
    {
        return vanillaBows.get(player);
    }
}
