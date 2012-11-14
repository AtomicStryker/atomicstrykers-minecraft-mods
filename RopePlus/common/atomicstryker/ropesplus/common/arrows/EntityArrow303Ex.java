package atomicstryker.ropesplus.common.arrows;

import java.util.Random;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Ex extends EntityArrow303
{

    public void subscreen()
    {
    }

    public void setupConfig()
    {
    }

    public void entityInit()
    {
        super.entityInit();
        name = "ExArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowExplosion;
        tip = Item.gunpowder;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 3;
    }

    public EntityArrow303Ex(World world)
    {
        super(world);
    }

    public EntityArrow303Ex(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHit()
    {
        worldObj.createExplosion(((Entity) (shooter != null ? ((Entity) (shooter)) : ((Entity) (this)))), posX, posY, posZ, 2.0F, true);
        setDead();
        return true;
    }

    public void tickFlying() {
        super.tickFlying();
    }
}
