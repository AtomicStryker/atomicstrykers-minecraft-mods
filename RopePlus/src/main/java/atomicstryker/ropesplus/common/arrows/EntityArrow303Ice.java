package atomicstryker.ropesplus.common.arrows;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityArrow303Ice extends EntityArrow303
{
    
    public EntityLivingBase victim;
    public float freezeFactor;
    public int freezeTimer;
    
    public EntityArrow303Ice(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Ice(World world, EntityPlayer ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "FrostArrow";
        craftingResults = 4;
        tip = Items.snowball;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:icearrow";
    }

    @Override
    public boolean onHitTarget(EntityLivingBase entity)
    {
        if (victim != null)
        {
            return false;
        }
        

        @SuppressWarnings("unchecked")
        List<Entity> list = (List<Entity>)worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.getBoundingBox().expand(3D, 3D, 3D));
        for (Entity e : list)
        {
            if (e instanceof EntityArrow303Ice)
            {
                EntityArrow303Ice entityarrow303ice = (EntityArrow303Ice) e;
                if (entityarrow303ice.victim == e)
                {
                    entityarrow303ice.freezeTimer += getFreezeTimer((EntityLivingBase) e);
                    entityarrow303ice.setDead();
                    return super.onHitTarget(entity);
                }
            }
        }
        
        freezeMob(entity);
        return super.onHitTarget(entity);
    }

    private int getFreezeTimer(EntityLivingBase EntityLivingBase)
    {
        return ((EntityLivingBase instanceof EntityPlayer) ? 5 : 10 * 20);
    }

    private void freezeMob(EntityLivingBase EntityLivingBase)
    {
        victim = EntityLivingBase;
        freezeFactor = ((EntityLivingBase instanceof EntityPlayer) ? 0.5F : 0.1F);
        freezeTimer = getFreezeTimer(EntityLivingBase);
        motionX = motionY = motionZ = 0.0D;
    }

    private void unfreezeMob()
    {
        victim = null;
    }

    @Override
    public void setDead()
    {
        if (victim != null)
        {
            unfreezeMob();
        }
        super.setDead();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (victim != null)
        {
            if (victim.isDead || victim.deathTime > 0)
            {
                setDead();
                return;
            }
            isDead = false;
            inGround = false;
            posX = victim.posX;
            posY = victim.getBoundingBox().minY + (double) victim.height * 0.5D;
            posZ = victim.posZ;
            setPosition(posX, posY, posZ);
            victim.motionX *= freezeFactor;
            victim.motionY *= freezeFactor;
            victim.motionZ *= freezeFactor;
            freezeTimer--;
            if (freezeTimer <= 0)
            {
                setDead();
            }
        }
    }

    @Override
    public boolean onHitBlock(int curX, int curY, int curZ)
    {
        for (int iX = curX - 1; iX <= curX + 1; iX++)
        {
            for (int iY = curY - 1; iY <= curY + 1; iY++)
            {
                for (int iZ = curZ - 1; iZ <= curZ + 1; iZ++)
                {
                    BlockPos pos = new BlockPos(iX, iY, iZ);
                    IBlockState state = worldObj.getBlockState(pos);
                    Block b = state.getBlock();
                    if (b.getMaterial() == Material.water && b.getMetaFromState(state) == 0)
                    {
                        worldObj.setBlockState(new BlockPos(iX,  iY,  iZ),  Blocks.ice.getStateFromMeta( 0));
                        continue;
                    }
                    if (b.getMaterial() == Material.lava && b.getMetaFromState(state) == 0)
                    {
                        worldObj.setBlockState(new BlockPos(iX,  iY,  iZ),  Blocks.cobblestone.getStateFromMeta( 0));
                        continue;
                    }
                    if (b == Blocks.fire)
                    {
                        worldObj.setBlockState(new BlockPos(iX,  iY,  iZ),  Blocks.air.getStateFromMeta( 0));
                        continue;
                    }
                    if (b == Blocks.torch)
                    {
                        b.onEntityCollidedWithBlock(worldObj, pos, this);
                        worldObj.setBlockState(new BlockPos(iX,  iY,  iZ),  Blocks.air.getStateFromMeta( 0));
                    }
                }
            }
        }

        return super.onHitBlock(curX, curY, curZ);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.SNOWBALL,
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Ice entityarrow = new EntityArrow303Ice(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }

}
