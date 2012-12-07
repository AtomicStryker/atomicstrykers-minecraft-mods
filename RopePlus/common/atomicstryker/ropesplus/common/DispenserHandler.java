package atomicstryker.ropesplus.common;

import java.util.Iterator;
import java.util.Random;

import atomicstryker.ropesplus.common.arrows.EntityArrow303;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import cpw.mods.fml.common.IDispenseHandler;
import cpw.mods.fml.common.IDispenserHandler;

public class DispenserHandler implements IDispenserHandler
{

	@Override
    public int dispense(int x, int y, int z, int xVelocity, int zVelocity, World world, ItemStack item, Random random, double entX, double entY, double entZ)
    {
        for(Iterator iterator = RopesPlusCore.arrows.iterator(); iterator.hasNext();)
        {
            EntityArrow303 entityarrow303 = (EntityArrow303)iterator.next();
            if(entityarrow303.itemId == item.itemID)
            {
                EntityArrow303 arrow = entityarrow303.newArrow(world);
                arrow.setPosition(x, y, z);
                arrow.setThrowableHeading(xVelocity, 0.10000000000000001D, zVelocity, 1.1F, 6F);
                world.spawnEntityInWorld(arrow);
                world.playSoundEffect(x, y, z, "random.bow", 1.0F, 1.2F);
                return 1;
            }
        }
        
        return -1;
	}

}
