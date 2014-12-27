package atomicstryker.petbat.common.batAI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import atomicstryker.petbat.common.EntityPetBat;

public class PetBatAIAttack extends EntityAIBase
{
    
    private final EntityPetBat petBat;
    private Entity entityTarget;
    private int attackTick;
    
    public PetBatAIAttack(EntityLivingBase bat)
    {
        petBat = (EntityPetBat) bat;
        attackTick = 0;
    }
    
    @Override
    public boolean shouldExecute()
    {
        if (petBat.getAttackTarget() != null
        && petBat.getAttackTarget().isEntityAlive())
        {
            entityTarget = petBat.getAttackTarget();
            return true;
        }
        else if (petBat.getFoodAttackTarget() != null
        && petBat.getFoodAttackTarget().isEntityAlive())
        {
            entityTarget = petBat.getFoodAttackTarget();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean continueExecuting()
    {
        if (entityTarget instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) entityTarget;
            if (entityTarget.getName().equals(petBat.getOwnerName()) && p.getHealth() < p.getMaxHealth()/2)
            {
                petBat.recallToOwner();
                return false;
            }
        }
        
        if (entityTarget != null && entityTarget.isEntityAlive())
        {
            return true;   
        }
        
        return super.continueExecuting();
    }
    
    @Override
    public void startExecuting()
    {
        super.startExecuting();
    }
    
    @Override
    public void resetTask()
    {
        entityTarget = null;
        attackTick = 0;
        super.resetTask();
    }
    
    @Override
    public void updateTask()
    {
        petBat.getLookHelper().setLookPositionWithEntity(entityTarget, 30.0F, 30.0F);

        attackTick = Math.max(attackTick - 1, 0);

        double maxReach = (double) (petBat.width * petBat.width * 5.0D);
        if (petBat.getDistanceSq(entityTarget.posX, entityTarget.getEntityBoundingBox().maxY, entityTarget.posZ) <= maxReach
                || (entityTarget.getEntityBoundingBox() != null && petBat.getEntityBoundingBox().intersectsWith(entityTarget.getEntityBoundingBox())))
        {
            if (entityTarget instanceof EntityItem)
            {
                if (attackTick == 0)
                {
                    attackTick = 40;
                }
                else if (attackTick == 1)
                {
                    entityTarget.setDead();
                    displayEatingEffects(((EntityItem) entityTarget).getEntityItem(), 16);
                    petBat.worldObj.playSoundAtEntity(petBat, "random.burp", 0.5F, petBat.getRNG().nextFloat() * 0.1F + 0.9F);
                    petBat.heal(18);
                    petBat.setFoodAttackTarget(null);
                }
                else if (attackTick % 3 == 0)
                {
                    displayEatingEffects(((EntityItem) entityTarget).getEntityItem(), 5);
                }
            }
            else
            {
                if (attackTick == 0)
                {
                    attackTick = 20;
                    petBat.attackEntityAsMob(entityTarget);

                    double xKnock = entityTarget.posX - petBat.posX;
                    double zKnock = entityTarget.posZ - petBat.posZ;
                    for (; xKnock * xKnock + zKnock * zKnock < 1.0E-4D; zKnock = (Math.random() - Math.random()) * 0.01D)
                    {
                        xKnock = (Math.random() - Math.random()) * 0.01D;
                    }
                    petBat.knockBack(entityTarget, 4, xKnock, zKnock);
                }
            }
        }
    }

    private void displayEatingEffects(ItemStack item, int power)
    {
        for (int var3 = 0; var3 < power; ++var3)
        {
            Vec3 var4 = new Vec3(((double)petBat.getRNG().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            var4.rotatePitch(-petBat.rotationPitch * (float)Math.PI / 180.0F);
            var4.rotateYaw(-petBat.rotationYaw * (float)Math.PI / 180.0F);
            Vec3 var5 = new Vec3(((double)petBat.getRNG().nextFloat() - 0.5D) * 0.3D, (double)(-petBat.getRNG().nextFloat()) * 0.6D - 0.3D, 0.6D);
            var5.rotatePitch(-petBat.rotationPitch * (float)Math.PI / 180.0F);
            var5.rotateYaw(-petBat.rotationYaw * (float)Math.PI / 180.0F);
            var5 = var5.addVector(petBat.posX, petBat.posY + (double)petBat.getEyeHeight(), petBat.posZ);
            petBat.worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, var5.xCoord, var5.yCoord, var5.zCoord, var4.xCoord, var4.yCoord + 0.05D, var4.zCoord, new int[] {Item.getIdFromItem(item.getItem()), item.getMetadata()});
        }

        petBat.worldObj.playSoundAtEntity(petBat, "random.eat", 0.5F + 0.5F * (float)petBat.getRNG().nextInt(2), (petBat.getRNG().nextFloat() - petBat.getRNG().nextFloat()) * 0.2F + 1.0F);
    }
    
}
