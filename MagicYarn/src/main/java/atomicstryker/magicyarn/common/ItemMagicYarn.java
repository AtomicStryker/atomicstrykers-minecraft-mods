package atomicstryker.magicyarn.common;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemMagicYarn extends Item
{
	public ItemMagicYarn()
	{
		super();
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("magicyarn:magicYarn");
    }

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public boolean shouldRotateAroundWhenRendering()
	{
		return true;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int useTime)
	{	    
		int var5 = this.getMaxItemUseDuration(itemstack) - useTime;
		float var6 = (float)var5 / 20.0F;
		var6 = (var6 * var6 + var6 * 2.0F) / 3.0F;
		
		MagicYarn.proxy.onPlayerUsedYarn(world, player, var6);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3)
	{
		var3.setItemInUse(var1, this.getMaxItemUseDuration(var1));
		return var1;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack var1)
	{
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack var1)
	{
		return EnumAction.bow;
	}
	
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.GOLD+super.getItemStackDisplayName(itemStack);
    }
}
