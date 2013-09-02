package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

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

    public EntityArrow303Ex(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
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
        icon = "ropesplus:exarrow";
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
            
            if (ent instanceof EntityLivingBase)
            {
                ((EntityLivingBase)ent).setArrowCountInEntity(((EntityLivingBase)ent).getArrowCountInEntity() + 1);
            }
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
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Ex entityarrow = new EntityArrow303Ex(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
