package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.TileEntityRope;

public class EntityArrow303Rope extends EntityArrow303
{

    public EntityArrow303Rope(World world)
    {
        super(world);
    }

    public EntityArrow303Rope(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Rope Arrow";
        craftingResults = 1;
        tip = RopesPlusCore.blockRopeCentralPos;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:ropearrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, RopesPlusCore.blockRopeCentralPos))
        {
        	setDead();
			RopesPlusCore.onRopeArrowHit(this.worldObj, placeCoords[0], placeCoords[1], placeCoords[2]);
        }
		else if(tryToPlaceWallRope())
		{
            TileEntityRope newent = new TileEntityRope(worldObj, placeCoords[0], placeCoords[1], placeCoords[2], 32);
            RopesPlusCore.addRopeToArray(newent);
            
            setDead();
		}

        return super.onHitBlock(x, y, z);
    }
	
    private boolean tryToPlaceWallRope()
    {
        Block blockID = RopesPlusCore.blockRopeWallPos;
        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(posY);
        int z = MathHelper.floor_double(posZ);
        boolean canPlace = false;
        int arrayLength = candidates.length;
        int index = 0;
		byte targetMeta = 0;
		int blockSide = 0;

		if(Math.abs(motionX) > Math.abs(motionZ))
		{
			if(motionX > 0)
			{
				blockSide = 4;
				targetMeta = 8;
			}
			else
			{
				blockSide = 5;
				targetMeta = 2;
			}
		}
		else if(Math.abs(motionX) <= Math.abs(motionZ))
		{
			if(motionZ > 0)
			{
				blockSide = 1;
				targetMeta = 1;
			}
			else
			{
				blockSide = 3;
				targetMeta = 4;
			}
		}
        
        do
        {
            int coords[] = candidates[index];
            int ix = coords[0];
            int iy = coords[1];
            int iz = coords[2];
            if(worldObj.func_147472_a(blockID, x + ix, y + iy, z + iz, true, blockSide, null, item))
            {
                x += ix;
                y += iy;
                z += iz;
                canPlace = true;
                break;
            }
            index++;
        }
        while(index < arrayLength);
        
        if(!canPlace)
        {
            return false;
        }
        
        if(!worldObj.isRemote)
        {
            worldObj.func_147465_d(x, y, z, blockID, targetMeta, 3);
        }

        placeCoords[0] = x;
        placeCoords[1] = y;
        placeCoords[2] = z;
        return true;
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Rope entityarrow = new EntityArrow303Rope(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
