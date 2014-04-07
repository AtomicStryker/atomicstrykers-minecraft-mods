package atomicstryker.ropesplus.common.arrows;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Laser extends EntityArrow303
{
    
    private final String sound = "damage.fallbig";
    
    public boolean pierced;
    public Set<Entity> piercedMobs;
    
    public EntityArrow303Laser(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Laser(World world, EntityPlayer ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "PenetratingArrow";
        craftingResults = 4;
        tip = Items.redstone;
        curvature = 0.0F;
        slowdown = 1.3F;
        precision = 0.0F;
        speed = 2.0F;
        pierced = false;
        piercedMobs = new HashSet<Entity>();
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:laserarrow";
    }

    @Override
    public boolean onHitTarget(EntityLivingBase entity)
    {
        causeArrowDamage(entity);
        pierced = true;
        piercedMobs.add(entity);
        target = null;
        worldObj.playSoundAtEntity(this, sound, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
        return false;
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        if (inTileBlockID.getLightOpacity() == 255) // isTransparent
        {
            if (pierced)
            {
                setDead();
            }
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean canTarget(Entity entity)
    {
        return !piercedMobs.contains(entity) && super.canTarget(entity);
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Laser entityarrow = new EntityArrow303Laser(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }

}
