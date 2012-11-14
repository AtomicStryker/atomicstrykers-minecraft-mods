package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Warp extends EntityArrow303
{

    public void entityInit()
    {
        super.entityInit();
        name = "WarpArrow";
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

    public EntityArrow303Warp(World world)
    {
        super(world);
    }

    public EntityArrow303Warp(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
        world.playSoundAtEntity(this, "portal.trigger", 1.0F, 1.0F);
    }

    public void tickFlying()
    {
        super.tickFlying();
    }

    public boolean onHit()
    {
        if(shooter != null)
        {
        	shooter.setPositionAndUpdate(this.posX, this.posY, this.posZ);
        	setDead();
        }
        return true;
    }

    private boolean isSolid(int i, int j, int k)
    {
        int l = worldObj.getBlockId(i, j, k);
        return l != 0 && Block.blocksList[l].renderAsNormalBlock();
    }

    private boolean isSolid(double d, double d1, double d2)
    {
        return isSolid(MathHelper.floor_double(d), MathHelper.floor_double(d1), MathHelper.floor_double(d2));
    }
}
