package atomicstryker.ropesplus.common.arrows;

import java.util.*;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Confusion extends EntityArrow303
{

    public EntityArrow303Confusion(World world)
    {
        super(world);
    }

    public EntityArrow303Confusion(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public void entityInit()
    {
        super.entityInit();
        name = "ConfusionArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowConfusion;
        tip = Block.sand;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 0;
    }

    public boolean onHitBlock()
    {
        confuse(this);
        return true;
    }

    public boolean onHitTarget(Entity entity)
    {
        confuse(entity);
        return true;
    }

    public void confuse(Entity entity)
    {
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.boundingBox.expand(radius, radius, radius));
        ArrayList arraylist = new ArrayList();
        Iterator iterator = list.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Entity entity1 = (Entity)iterator.next();
            if((entity1 instanceof EntityCreature) && entity1 != shooter)
            {
                arraylist.add((EntityCreature)entity1);
            }
        } while(true);
        if(arraylist.size() < 2)
        {
            return;
        }
        for(int i = 0; i < arraylist.size(); i++)
        {
            EntityCreature entitycreature = (EntityCreature)arraylist.get(i);
            EntityCreature entitycreature1 = (EntityCreature)arraylist.get(i != 0 ? i - 1 : arraylist.size() - 1);
            entitycreature.attackEntityFrom(DamageSource.causeMobDamage(entitycreature1), 0);
            entitycreature1.setTarget(entitycreature);
        }

        setDead();
    }

    public void tickFlying()
    {
        super.tickFlying();
    }

    public static double radius = 6D;

}
