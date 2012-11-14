package atomicstryker.ropesplus.common.arrows;

import java.util.*;

import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.Settings_RopePlus;
import atomicstryker.ropesplus.common.TileEntityRope;
import net.minecraft.src.*;

public class EntityArrow303Rope extends EntityArrow303
{

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

    public EntityArrow303Rope(World world)
    {
        super(world);
    }

    public EntityArrow303Rope(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHit()
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, RopesPlusCore.blockRopeCentralPos.blockID))
        {
        	setDead();
			RopesPlusCore.onRopeArrowHit(this.worldObj, placeCoords[0], placeCoords[1], placeCoords[2]);
        }
		else if(tryToPlaceBlock2(RopesPlusCore.blockRopeWallPos.blockID))
		{
            TileEntityRope newent = new TileEntityRope(worldObj, placeCoords[0], placeCoords[1], placeCoords[2], 32);
            RopesPlusCore.addRopeToArray(newent);
            
            setDead();
		}

        return true;
    }
	
    public boolean tryToPlaceBlock2(int i)
    {
        int j = MathHelper.floor_double(posX);
        int k = MathHelper.floor_double(posY);
        int l = MathHelper.floor_double(posZ);
        boolean flag = false;
        int ai[][] = candidates;
        int arrayLength = ai.length;
        int index = 0;
        
		double d5 = motionX;
		double d6 = motionZ;
		byte byte0 = ((byte)(d5 <= 0.0D ? -1 : 1));
		byte byte1 = ((byte)(d6 <= 0.0D ? -1 : 1));
		byte targetMeta = 0;
		int blockSide = 0;

		boolean flag2 = false;
		if(Math.abs(d5) > Math.abs(d6))
		{
			flag2 = true;
			if(byte0 > 0)
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
		else if(Math.abs(d5) <= Math.abs(d6))
		{
			flag2 = true;
			if(byte1 > 0)
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
            if(index >= arrayLength)
            {
                break;
            }
            int ai1[] = ai[index];
            int i2 = ai1[0];
            int j2 = ai1[1];
            int k2 = ai1[2];
            if(worldObj.canPlaceEntityOnSide(i, j + i2, k + j2, l + k2, true, blockSide, (Entity)null))
            {
                j += i2;
                k += j2;
                l += k2;
                flag = true;
                break;
            }
            index++;
        } while(true);
        if(!flag)
        {
            return false;
        }
        
		if(flag2)
		{
			if(!worldObj.isRemote)
			{
				worldObj.setBlockWithNotify(j, k, l, i);
				worldObj.setBlockMetadataWithNotify(j, k, l, targetMeta);
			}
			
			placeCoords[0] = j;
			placeCoords[1] = k;
			placeCoords[2] = l;
			return true;
		}
		
		return false;
    }
}
