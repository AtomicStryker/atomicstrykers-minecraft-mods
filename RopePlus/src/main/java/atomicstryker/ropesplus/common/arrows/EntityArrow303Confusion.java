package atomicstryker.ropesplus.common.arrows;

import java.util.ArrayList;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityArrow303Confusion extends EntityArrow303
{
    private final double CONFUSION_EFFECT_SIZE = 6D;

    public EntityArrow303Confusion(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Confusion(World world, EntityPlayer ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "ConfusingArrow";
        craftingResults = 4;
        tip = Blocks.sand;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:confusionarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        worldObj.playSoundAtEntity(this, "random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        confuse(this);
        return true;
    }

    @Override
    public boolean onHitTarget(EntityLivingBase entity)
    {
        confuse(entity);
        return super.onHitTarget(entity);
    }

    private void confuse(Entity entity)
    {
        Object[] ents = worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.getBoundingBox().expand(CONFUSION_EFFECT_SIZE, CONFUSION_EFFECT_SIZE, CONFUSION_EFFECT_SIZE)).toArray();
        ArrayList<EntityCreature> hitList = new ArrayList<EntityCreature>();
        for (Object o : ents)
        {
            if (o instanceof EntityCreature && o != shooter)
            {
                hitList.add((EntityCreature)o);
            }
        }
        
        if(hitList.size() < 2)
        {
            return;
        }
        
        for(int i = 0; i < hitList.size(); i++)
        {
            EntityCreature creatureA = hitList.get(i);
            EntityCreature creatureB = hitList.get(i != 0 ? i - 1 : hitList.size() - 1);
            creatureA.attackEntityFrom(DamageSource.causeMobDamage(creatureB), 0);
            creatureB.attackEntityFrom(DamageSource.causeMobDamage(creatureA), 0);
        }

        setDead();
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Confusion entityarrow = new EntityArrow303Confusion(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
