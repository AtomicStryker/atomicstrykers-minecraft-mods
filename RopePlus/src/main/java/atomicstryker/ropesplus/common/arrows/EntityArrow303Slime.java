package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Slime extends EntityArrow303
{
    
    public EntityArrow303Slime(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Slime(World world, EntityLivingBase ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "SlimeArrow";
        craftingResults = 1;
        tip = Items.slime_ball;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:slimearrow";
    }

    private EntityLivingBase makeMob()
    {
        EntitySlime entityslime = new EntitySlime(worldObj);
        entityslime.heal(1 << rand.nextInt(4));
        return entityslime;
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if (!worldObj.isRemote)
        {
            EntityLivingBase entityLivingBase = makeMob();
            entityLivingBase.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            if(worldObj.spawnEntityInWorld(entityLivingBase))
            {
                ((EntitySlime)entityLivingBase).spawnExplosionParticle();
                setDead();
            }   
        }
        return super.onHitBlock(x, y, z);
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
        EntityLivingBase entityLivingBase = makeMob();
        entityLivingBase.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        if(worldObj.spawnEntityInWorld(entityLivingBase))
        {
            ((EntitySlime)entityLivingBase).spawnExplosionParticle();
        }
        return super.onHitTarget(entity);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("slime",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Slime entityarrow = new EntityArrow303Slime(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
