package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Dirt extends EntityArrow303
{

    public void subscreen()
    {
    }

    public void setupConfig()
    {
    }

    public void entityInit()
    {
        super.entityInit();
        name = "DirtArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowDirt;
        tip = Block.dirt;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 1;
    }

    public EntityArrow303Dirt(World world)
    {
        super(world);
    }

    public EntityArrow303Dirt(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHit()
    {
        spawnDirt();
        return true;
    }

    public boolean onHitBlock()
    {
    	setDead();
        return true;
    }

    public void spawnDirt()
    {

        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY);
        int k = MathHelper.floor_double(posZ);
        for(int l = -0; l <= 0; l++)
        {
            for(int i1 = -0; i1 <= 0; i1++)
            {
                for(int j1 = -0; j1 <= 0; j1++)
                {
                    int k1 = i + l;
                    int l1 = j + j1;
                    int i2 = k + i1;

                    if(Math.abs(l) != 0 && Math.abs(j1) != 0 && Math.abs(i1) != 0)
                    {
                        continue;
                    }
                    if(!worldObj.canPlaceEntityOnSide(defaultBlockId, k1, l1, i2, true, Block.dirt.blockID, (Entity)null))
                    {
                        continue;
                    }
                    worldObj.setBlockWithNotify(k1, l1, i2, Block.dirt.blockID);
                    break;
                }

            }

        }

    }

    public void tickFlying()
    {
        super.tickFlying();
    }

    public static final int defaultBlockId;

    static 
    {
        defaultBlockId = Block.dirt.blockID;
    }
}
