package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Warp extends EntityArrow303
{
    
    public EntityArrow303Warp(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Warp(World world, EntityLivingBase ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "WarpArrow";
        craftingResults = 4;
        tip = Blocks.obsidian;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:warparrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(shooter != null && y > 8)
        {
            shooter.worldObj.playSoundAtEntity(shooter, "portal.trigger", 1.0F, 1.0F);
        	shooter.setPositionAndUpdate(x, y+2, z);
        	shooter.fallDistance = 0.0F;
        	setDead();
        }
        return true;
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("portal",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Fire entityarrow = new EntityArrow303Fire(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
