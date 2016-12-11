package atomicstryker.minions.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Minion control Item class. Nothing to see here really.
 * 
 * 
 * @author AtomicStryker
 */

public class ItemMastersStaff extends Item
{

    public ItemMastersStaff()
    {
        super();
        this.maxStackSize = 1;

        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase user, int ticksHeld)
    {
        if (world.isRemote && user instanceof EntityPlayer)
        {
            int ticksLeftFromMax = this.getMaxItemUseDuration(itemstack) - ticksHeld;
            float pointStrength = (float) ticksLeftFromMax / 20.0F;
            pointStrength = (pointStrength * pointStrength + pointStrength * 2.0F) / 3.0F;

            if (pointStrength > 1.0F)
            {
                // full power!
                MinionsCore.proxy.onMastersGloveRightClickHeld(itemstack, world, (EntityPlayer) user);
            }
            else
            {
                // shorter tap
                MinionsCore.proxy.onMastersGloveRightClick(itemstack, world, (EntityPlayer) user);
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack var1)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack var1)
    {
        return EnumAction.BLOCK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemStack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return TextFormatting.RED + super.getItemStackDisplayName(itemStack);
    }
}
