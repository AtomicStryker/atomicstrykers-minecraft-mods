package atomicstryker.ropesplus.common.arrows;

import java.util.*;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Confusion extends EntityArrow303
{
    private final double CONFUSION_EFFECT_SIZE = 6D;

    public EntityArrow303Confusion(World world)
    {
        super(world);
    }

    public EntityArrow303Confusion(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Confusing Arrow";
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

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        worldObj.playSoundAtEntity(this, "random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        confuse(this);
        return true;
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
        worldObj.playSoundAtEntity(this, "random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        confuse(entity);
        return true;
    }

    private void confuse(Entity entity)
    {
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.boundingBox.expand(CONFUSION_EFFECT_SIZE, CONFUSION_EFFECT_SIZE, CONFUSION_EFFECT_SIZE));
        ArrayList arraylist = new ArrayList();
        Iterator iterator = list.iterator();
        do
        {
            Entity entity1 = (Entity)iterator.next();
            if((entity1 instanceof EntityCreature) && entity1 != shooter)
            {
                arraylist.add((EntityCreature)entity1);
            }
        }
        while(iterator.hasNext());
        
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
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("tilecrack_12_0",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
}
