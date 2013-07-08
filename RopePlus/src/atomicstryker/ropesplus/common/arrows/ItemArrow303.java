package atomicstryker.ropesplus.common.arrows;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemArrow303 extends Item
{
    
	public EntityArrow303 arrow;

    public ItemArrow303(int i, EntityArrow303 entityarrow303)
    {
        super(i);
        arrow = entityarrow303;
        setUnlocalizedName("arrow");
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon(arrow.getIcon());
    }
    
}
