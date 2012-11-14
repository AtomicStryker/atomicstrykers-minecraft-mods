package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Grass extends EntityArrow303
{

    public void entityInit()
    {
        super.entityInit();
        name = "GrassArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowGrass;
        tip = Item.seeds;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 5;
    }

    public EntityArrow303Grass(World world)
    {
        super(world);
    }

    public EntityArrow303Grass(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHit()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY);
        int k = MathHelper.floor_double(posZ);
        for(int l = i - 1; l <= i + 1; l++)
        {
            for(int i1 = j - 1; i1 <= j + 1; i1++)
            {
                for(int j1 = k - 1; j1 <= k + 1; j1++)
                {
                    int k1 = worldObj.getBlockId(l, i1, j1);
                    if(k1 == 3)
                    {
                        worldObj.setBlockWithNotify(l, i1, j1, 2);
                        setDead();
                        continue;
                    }
                    if(k1 == 4)
                    {
                        worldObj.setBlockWithNotify(l, i1, j1, 48);
                        setDead();
                        continue;
                    }
                    if(k1 == 60 && i1 != 127 && worldObj.getBlockId(l, i1 + 1, j1) == 0)
                    {
                        worldObj.setBlockWithNotify(l, i1 + 1, j1, 59);
                        setDead();
                    }
                }

            }

        }

        return true;
    }

    public void tickFlying()
    {
        super.tickFlying();
    }
}
