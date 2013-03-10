package atomicstryker.magicyarn.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.astarpathing.AStarNode;

public class ItemMagicYarn extends Item
{
	private AStarNode origin = null;
	private AStarNode target = null;

	public ItemMagicYarn(int var1)
	{
		super(var1);
		this.setMaxDamage(64);
		this.setMaxStackSize(1);
	}
	
	@Override
    public void func_94581_a(IconRegister iconRegister)
    {
        iconIndex = iconRegister.func_94245_a("magicyarn:magicYarn");
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
    public String getItemDisplayName(ItemStack itemStack)
    {
        return "§E"+super.getItemDisplayName(itemStack);
    }
}
