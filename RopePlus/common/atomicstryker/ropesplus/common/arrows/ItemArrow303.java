package atomicstryker.ropesplus.common.arrows;

import net.minecraft.src.Item;

public class ItemArrow303 extends Item
{
	public EntityArrow303 arrow;

    public ItemArrow303(int i, EntityArrow303 entityarrow303)
    {
        super(i);
        arrow = entityarrow303;
        this.setTextureFile("/atomicstryker/ropesplus/client/ropesPlusItems.png");
    }
}
