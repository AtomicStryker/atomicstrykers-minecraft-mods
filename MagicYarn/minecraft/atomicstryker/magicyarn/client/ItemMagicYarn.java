package atomicstryker.magicyarn.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
	    if (!world.isRemote) return;
	    
		int var5 = this.getMaxItemUseDuration(itemstack) - useTime;
		float var6 = (float)var5 / 20.0F;
		var6 = (var6 * var6 + var6 * 2.0F) / 3.0F;

		if(var6 > 2.5F)
		{
			var6 = 2.5F;
		}

		if(var6 < 2.5F)
		{
			if(origin == null)
			{		
				origin = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0);
				System.out.println("Magic Yarn Origin set to ["+origin.x+"|"+origin.y+"|"+origin.z+"]");
				world.playSound(player.posX, player.posY, player.posZ, "random.orb", 1.0F, 1.0F, false);
				MagicYarn.showPath = false;
			}
			else
			{
				if (target == null && MagicYarn.path == null)
				{					
					target = new AStarNode((int)Math.floor(player.posX), (int)player.posY-1, (int)Math.floor(player.posZ), 0);
					System.out.println("Magic Yarn Target: ["+target.x+"|"+target.y+"|"+target.z+"]");

					AStarPath.getPath(origin, target, false, (var6 < 0.5F));
					MagicYarn.showPath = true;
				}
				else
				{
					boolean soundplayed = false;
					if (MagicYarn.path != null)
					{
						target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0);
						for (int i = MagicYarn.path.size()-1; i != 0; i--)
						{
							if (((AStarNode) MagicYarn.path.get(i)).equals(target))
							{
								System.out.println("Magic Yarn being cut shorter!");
								world.playSound(player.posX, player.posY, player.posZ, "random.break", 1.0F, 1.0F, false);
								soundplayed = true;
								while (i >= 0)
								{
									MagicYarn.path.remove(i);
									i--;
								}
								break;
							}
						}
					}
					
					target = null;
					MagicYarn.inputPath(null, true);
					AStarPath.stopPathSearch();
					System.out.println("Magic Yarn Target nulled");
					if (!soundplayed)
					{
						world.playSound(player.posX, player.posY, player.posZ, "random.pop", 1.0F, 1.0F, false);
					}
					MagicYarn.showPath = false;
				}
			}
		}
		else
		{
			if(origin != null)
			{
				origin = null;
				target = null;
				MagicYarn.inputPath(null, true);
				MagicYarn.lastPath = null;
				AStarPath.stopPathSearch();
				System.out.println("Magic Yarn Origin nulled");
				world.playSound(player.posX, player.posY, player.posZ, "random.fizz", 1.0F, 1.0F, false);
				MagicYarn.showPath = false;
			}
		}
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
	public EnumAction getItemUseAction(ItemStack var1) {
		return EnumAction.bow;
	}
}
