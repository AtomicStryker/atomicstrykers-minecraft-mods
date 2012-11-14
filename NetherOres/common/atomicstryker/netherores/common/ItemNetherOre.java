package atomicstryker.netherores.common;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemNetherOre extends ItemBlock
{
	public ItemNetherOre(int i)
	{
		super(i);
		setHasSubtypes(true);
		setMaxDamage(0);
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public int getIconFromDamage(int i)
	{
		return Math.min(i, 7);
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		int md = itemstack.getItemDamage();
		if(md == 0) return "itemNetherCoal";
		if(md == 1) return "itemNetherDiamond";
		if(md == 2) return "itemNetherGold";
		if(md == 3) return "itemNetherIron";
		if(md == 4) return "itemNetherLapis";
		if(md == 5) return "itemNetherRedstone";
		if(md == 6) return "itemNetherCopper";
		
		return "itemNetherTin";
	}
	
	@Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
	    for (int i = 0; i <= 7; i++)
	    {
	        par3List.add(new ItemStack(par1, 1, i));
	    }
    }
}
