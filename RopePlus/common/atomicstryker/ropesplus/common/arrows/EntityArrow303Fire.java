package atomicstryker.ropesplus.common.arrows;

import java.util.Random;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Fire extends EntityArrow303
{

    public EntityArrow303Fire(World world)
    {
        super(world);
    }

    public EntityArrow303Fire(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public void entityInit()
    {
        super.entityInit();
        name = "FiArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowFire;
        tip = Item.coal;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 4;
    }

    public boolean onHit()
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, 51))
        {
        	setDead();
        }
        return true;
    }

    public boolean onHitTarget(Entity entity)
    {
    	entity.setFire(300/20);
        return true;
    }

    public void tickFlying()
    {
        super.tickFlying();
    }
}
