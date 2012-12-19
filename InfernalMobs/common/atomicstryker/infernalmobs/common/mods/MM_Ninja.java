package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ninja extends MobModifier
{
    public MM_Ninja(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Ninja";
    }
    
    public MM_Ninja(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Ninja";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        long time = System.currentTimeMillis();
        if (time > lastAbilityUse+coolDown
        && source.getEntity() != null
        && teleportToEntity(source.getEntity()))
        {
            lastAbilityUse = time;
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), damage);
            return super.onHurt(source, 0);
        }
        
        return super.onHurt(source, damage);
    }
    
    private boolean teleportToEntity(Entity par1Entity)
    {
        Vec3 vector = Vec3.createVectorHelper(mob.posX - par1Entity.posX, mob.boundingBox.minY + (double)(mob.height / 2.0F) - par1Entity.posY + (double)par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 8.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double)(mob.worldObj.rand.nextInt(16) - 4) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.zCoord * telDist;
        return teleportTo(destX, destY, destZ);
    }
    
    private boolean teleportTo(double destX, double destY, double destZ)
    {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        int blockID;

        if (mob.worldObj.blockExists(x, y, z))
        {
            boolean hitGround = false;            
            while (!hitGround && y < 96)
            {
                blockID = mob.worldObj.getBlockId(x, y + 1, z);

                if (blockID == 0 || !Block.blocksList[blockID].blockMaterial.blocksMovement())
                {
                    hitGround = true;
                }
                else
                {
                    ++mob.posY;
                    ++y;
                }
            }

            if (hitGround)
            {
                mob.setPosition(mob.posX, mob.posY, mob.posZ);
                
                mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "random.explode", 2.0F, (1.0F + (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
                mob.worldObj.spawnParticle("hugeexplosion", oldX, oldY, oldZ, 0D, 0D, 0D);
            }
            else
            {
                return false;
            }
        }
        return true;
    }
}
