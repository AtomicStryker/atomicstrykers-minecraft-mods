package atomicstryker.ropesplus.common.arrows;

import java.util.Random;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Ex extends EntityArrow303
{
    private boolean isCharged;
    private int ticksCharged;
    private Entity entStuckIn;
    
    public EntityArrow303Ex(World world)
    {
        super(world);
        isCharged = false;
        entStuckIn = null;
    }

    public EntityArrow303Ex(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
        isCharged = false;
        entStuckIn = null;
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Exploding Arrow";
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

    @Override
    public boolean onHitTarget(Entity ent)
    {
        if (!isCharged)
        {
            isCharged = true;
            worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
            entStuckIn = ent;
            
            // deal minimal damage to provoke a reaction
            if (shooter != null)
            {
                ent.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) shooter), 1);
            }
            
            // ((EntityLiving)ent).setArrowCountInEntity(((EntityLiving)ent).getArrowCountInEntity() + 1);
            ((EntityLiving)ent).func_85034_r(((EntityLiving)ent).func_85035_bI() + 1);
            setSize(0, 0);
        }
        return false;
    }
    
    @Override
    public void tickInGround()
    {
        if (!isCharged)
        {
            isCharged = true;
            worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        }
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        if (entStuckIn != null)
        {
            this.setPosition(entStuckIn.posX, entStuckIn.posY, entStuckIn.posZ);
        }
        
        if (ticksCharged++ > 60)
        {
            if (!worldObj.isRemote)
            {
                worldObj.createExplosion(((Entity) (shooter != null ? ((Entity) (shooter)) : ((Entity) (this)))), posX, posY, posZ, 2.0F, true);
            }
            setDead();
        }
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("smoke",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
}
