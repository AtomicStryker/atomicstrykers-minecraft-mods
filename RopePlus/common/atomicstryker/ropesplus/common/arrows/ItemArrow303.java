package atomicstryker.ropesplus.common.arrows;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemArrow303 extends Item
{
    
	public EntityArrow303 arrow;
	private String icon;

    public ItemArrow303(int i, EntityArrow303 entityarrow303)
    {
        super(i);
        arrow = entityarrow303;
        setUnlocalizedName("arrow");
    }
    
    @Override
    public void func_94581_a(IconRegister iconRegister)
    {
        iconIndex = iconRegister.func_94245_a(arrow.getIcon());
    }
    
}
