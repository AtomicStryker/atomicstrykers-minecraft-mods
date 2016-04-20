package atomicstryker.magicyarn.common;

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

public class ItemMagicYarn extends Item
{
    
	protected ItemMagicYarn()
	{
		super();
        maxStackSize = 1;
        setMaxDamage(0);
	}

	@Override
	public boolean shouldRotateAroundWhenRendering()
	{
		return true;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase user, int timeLeft)
	{
		int var5 = this.getMaxItemUseDuration(itemstack) - timeLeft;
		float var6 = (float)var5 / 20.0F;
		var6 = (var6 * var6 + var6 * 2.0F) / 3.0F;

		if (user instanceof EntityPlayer)
		{
			MagicYarn.proxy.onPlayerUsedYarn(world, (EntityPlayer) user, var6);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand)
	{
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack var1)
	{
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack var1)
	{
		return EnumAction.BOW;
	}
	
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return TextFormatting.GOLD+super.getItemStackDisplayName(itemStack);
    }
}
