package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

public class EntityArrow303Warp extends EntityArrow303
{
    
    public EntityArrow303Warp(World world)
    {
        super(world);
    }

    public EntityArrow303Warp(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Warp Arrow";
        craftingResults = 4;
        itemId = Settings_RopePlus.itemIdArrowWarp;
        tip = Block.obsidian;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 12;
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
    
}
