package atomicstryker.powerconverters.common;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemPowerConverter extends ItemBlock
{
	public ItemPowerConverter(int i)
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
		return 0;
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		int md = itemstack.getItemDamage();
		if(md == 0) return "engineGeneratorLV";
		if(md == 1) return "engineGeneratorMV";
		if(md == 2) return "engineGeneratorHV";
		if(md == 3) return "oilFabricator";
		if(md == 4) return "energyLink";
		if(md == 5) return "lavaFabricator";
		if(md == 6) return "geoMk2";
		if(md == 7) return "waterStrainer";
		return "waterStrainer";
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
