package atomicstryker.ropesplus.common.arrows;

import java.util.*;

import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.Settings_RopePlus;
import atomicstryker.ropesplus.common.TileEntityRope;
import net.minecraft.src.*;

public class EntityArrow303Rope extends EntityArrow303
{

    public EntityArrow303Rope(World world)
    {
        super(world);
    }

    public EntityArrow303Rope(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "RopeArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowRope;
        tip = RopesPlusCore.blockRopeCentralPos;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 9;
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, RopesPlusCore.blockRopeCentralPos.blockID))
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
        int blockID = RopesPlusCore.blockRopeWallPos.blockID;
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
            if(worldObj.canPlaceEntityOnSide(blockID, x + ix, y + iy, z + iz, true, blockSide, (Entity)null))
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
            worldObj.setBlockWithNotify(x, y, z, blockID);
            worldObj.setBlockMetadataWithNotify(x, y, z, targetMeta);
        }

        placeCoords[0] = x;
        placeCoords[1] = y;
        placeCoords[2] = z;
        return true;
    }
}
