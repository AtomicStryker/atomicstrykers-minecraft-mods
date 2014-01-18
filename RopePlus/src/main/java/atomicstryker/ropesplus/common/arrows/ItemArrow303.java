package atomicstryker.ropesplus.common.arrows;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemArrow303 extends Item
{
    
	public EntityArrow303 arrow;

    public ItemArrow303(EntityArrow303 entityarrow303)
    {
        super();
        arrow = entityarrow303;
        setUnlocalizedName("arrow");
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon(arrow.getIcon());
    }
    
}
