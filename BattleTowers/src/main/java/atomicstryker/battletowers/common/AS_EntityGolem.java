package atomicstryker.battletowers.common;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class AS_EntityGolem extends EntityMob implements IEntityAdditionalSpawnData
{

    private final static DataParameter<Boolean> AWAKE = EntityDataManager.<Boolean> createKey(AS_EntityGolem.class, DataSerializers.BOOLEAN);

    private int rageCounter;
    private int explosionAttack;
    private int towerID;
    private int drops;
    private int attackCounter;
    private int noTargetCountdown;

    private int towerX = -1;
    private int towerY = -1;
    private int towerZ = -1;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AS_EntityGolem(World world)
    {
        super(world);
        this.setSize(1.1F, 4.0F);
        rotationYaw = 0.0F;
        rageCounter = 0;
        explosionAttack = 0;
        isImmuneToFire = true;
        drops = 1;
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
        attackCounter = 0;

        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0d); // max
                                                                                     // health
        setHealth(getMaxHealth());

        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0d); // attack
                                                                                      // damage
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3d); // movespeed

        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAttackMelee(this, 1.0d, true));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true, true));
    }

    public AS_EntityGolem(World world, int i)
    {
        this(world);
        towerID = i;
        this.updateGolemType();
    }

    @Override
    protected void updateAITasks()
    {
        if (!this.getIsDormant())
        {
            super.updateAITasks();
        }
    }

    @Override
    public void onLivingUpdate()
    {
        if (!this.getIsDormant())
        {
            super.onLivingUpdate();
        }
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.towerID);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        towerID = data.readInt();
        this.updateGolemType();
    }

    private void updateGolemType()
    {
        drops = 5 + towerID;
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0f + towerID);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150f + 50f * towerID);
        setHealth(getMaxHealth());
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(AWAKE, false); // dormant dataManager
    }

    public void setDormant()
    {
        if (!world.isRemote && !isDead && getHealth() > 0F)
        {
            dataManager.set(AWAKE, false);
        }
    }

    public void setAwake()
    {
        if (!world.isRemote)
        {
            if (getIsDormant())
            {
                noTargetCountdown = 90;
                world.playSound(null, getPosition(), AS_BattleTowersCore.soundGolemAwaken, SoundCategory.HOSTILE, getSoundVolume() * 2.0F,
                        ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
            }

            dataManager.set(AWAKE, true);
        }
    }

    public boolean getIsDormant()
    {
        return dataManager.get(AWAKE) == false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float amount)
    {
        if (damageSource.getTrueSource() != null && damageSource.getTrueSource() instanceof EntityLivingBase)
        {
            setAwake();
            setAttackTarget((EntityLivingBase) damageSource.getTrueSource());
        }

        return super.attackEntityFrom(damageSource, amount);
    }

    @Override
    public void setDead()
    {
        super.setDead();
    }

    @Override
    public void onDeath(DamageSource var1)
    {
        super.onDeath(var1);
        setDormant();
        Entity entity = var1.getTrueSource();

        if (scoreValue > 0 && entity != null)
        {
            entity.awardKillScore(this, scoreValue, var1);
        }
        if (!world.isRemote)
        {
            int i = drops;
            for (int j = 0; j < i; j++)
            {
                entityDropItem(new ItemStack(Items.DIAMOND, 1), 0f);
                entityDropItem(new ItemStack(Items.REDSTONE, 1), 0f);
            }

            i = rand.nextInt(4) + 8;
            for (int k = 0; k < i; k++)
            {
                entityDropItem(new ItemStack(Blocks.CLAY, 1), 0f);
            }
            if (getAttackTarget() != null && (AS_BattleTowersCore.instance.towerDestroyerEnabled != 0) && towerY > 50 && posY > 0)
            {
                AS_BattleTowersCore.onBattleTowerDestroyed(new AS_TowerDestroyer(world, new BlockPos(towerX, towerY, towerZ), System.currentTimeMillis(), getAttackTarget()));
            }
        }
        world.setEntityState(this, (byte) 3);
    }

    @Override
    public void knockBack(Entity entity, float i, double d, double d1)
    {
        // moveSpeed = 0.25F + (float)((double)(450 - health) / 1750D);
        if (rand.nextInt(5) == 0)
        {
            motionX *= 1.5D;
            motionZ *= 1.5D;
            motionY += 0.60000002384185791D;
        }
        // rageCounter = 150;
    }

    @Override
    public void onUpdate()
    {
        if ((this.isDead || this.getHealth() <= 0F || this.posY < 0))
        {
            this.setDead();
            this.setDormant();
            super.onUpdate();
            return;
        }

        if (!this.getIsDormant())
        {
            if (getAttackTarget() == null || !getAttackTarget().isEntityAlive())
            {
                noTargetCountdown--;
                if (noTargetCountdown < 1)
                {
                    setHealth(getMaxHealth());
                    rageCounter = 125;
                    explosionAttack = 0;

                    if (this.onGround)
                    {
                        this.setDormant();
                    }
                }
            }
            else if (rageCounter <= 0 && explosionAttack == 0)
            {
                world.playSound(null, getPosition(), AS_BattleTowersCore.soundGolemSpecial, SoundCategory.HOSTILE, getSoundVolume() * 2.0F,
                        ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
                motionY += 0.9D;
                explosionAttack = 1;
            }
            else if ((rageCounter <= -30 || onGround) && explosionAttack == 1)
            {
                if (getHealth() <= getMaxHealth() / 2) // getEntityHealth
                {
                    setHealth(getHealth() + 20);
                }

                if (!world.isRemote && (this.posY - getAttackTarget().posY) > 0.3D)
                {
                    if (AS_BattleTowersCore.instance.noGolemExplosions)
                    {
                        getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 3.5f);
                    }
                    else
                    {
                        world.createExplosion(this, posX, posY - 0.3D, posZ, 4F, true);
                    }
                }
                rageCounter = 125;
                explosionAttack = 0;
            }
            noTargetCountdown = 90;
        }

        checkForVictim();
        super.onUpdate();
    }

    private void checkForVictim()
    {
        if (this.getIsDormant())
        {
            if (!world.isRemote)
            {
                EntityPlayer entityplayer = world.getClosestPlayerToEntity(this, 6D);
                if (entityplayer != null && canEntityBeSeen(entityplayer))
                {
                    setAwake();
                    setAttackTarget(entityplayer);
                    rageCounter = 175;
                }
            }
        }
        else if (getAttackTarget() != null)
        {
            if (towerY == -1)
            {
                towerX = (int) posX;
                towerY = (int) posY;
                towerZ = (int) posZ;
            }

            boolean targetNearby = (getAttackTarget().getDistanceSq(this) < 6F * 6F);

            if (!targetNearby || explosionAttack == 1 || (((this.posY - getAttackTarget().posY) > 0.3D)))
            {
                rageCounter -= 2;
                // System.out.println("Golem losing patience: "+rageCounter);
            }
            else
            {
                rageCounter = 175;
            }

            if (attackCounter == 10)
            {
                world.playSound(null, getPosition(), AS_BattleTowersCore.soundGolemCharge, SoundCategory.HOSTILE, getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }
            attackCounter++;
            if (attackCounter >= 20)
            {
                conjureFireBall();
                attackCounter = -40;
            }
        }
        else if (attackCounter > 0)
        {
            attackCounter--;
        }
    }

    private void conjureFireBall()
    {
        if (!world.isRemote)
        {
            double diffX = getAttackTarget().posX - posX;
            double diffY = (getAttackTarget().getEntityBoundingBox().minY + (double) (getAttackTarget().height / 2.0F)) - (posY + height * 0.8D);
            double diffZ = getAttackTarget().posZ - posZ;

            renderYawOffset = rotationYaw = (-(float) Math.atan2(diffX, diffZ) * 180F) / (float) Math.PI;

            world.playSound(null, getPosition(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            AS_EntityGolemFireball entityfireball = new AS_EntityGolemFireball(world, this, diffX, diffY, diffZ);
            Vec3d vec3d = getLook(1.0F);
            entityfireball.posX = posX + vec3d.x * 2D;
            entityfireball.posY = posY + (height * 0.8) + vec3d.y * 0.5D;
            entityfireball.posZ = posZ + vec3d.z * 2D;
            world.spawnEntity(entityfireball);
        }
    }

    @Override
    protected boolean canDespawn()
    {
        return this.isDead;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        if (towerY == -1)
        {
            towerX = (int) posX;
            towerY = (int) posY;
            towerZ = (int) posZ;
        }

        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte("isDormant", (byte) (this.getIsDormant() ? 1 : 0));
        nbttagcompound.setByte("hasexplosionAttacked", (byte) explosionAttack);
        nbttagcompound.setByte("rageCounter", (byte) rageCounter);
        nbttagcompound.setByte("Drops", (byte) drops);
        nbttagcompound.setInteger("towerX", towerX);
        nbttagcompound.setInteger("towerY", towerY);
        nbttagcompound.setInteger("towerZ", towerZ);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if ((nbttagcompound.getByte("isDormant") & 0xff) == 1)
        {
            this.setDormant();
        }
        else
        {
            this.setAwake();
        }
        explosionAttack = nbttagcompound.getByte("hasexplosionAttacked") & 0xff;
        rageCounter = nbttagcompound.getByte("rageCounter") & 0xff;
        drops = nbttagcompound.getByte("Drops") & 0xff;

        towerX = nbttagcompound.getInteger("towerX");
        towerY = nbttagcompound.getInteger("towerY");
        towerZ = nbttagcompound.getInteger("towerZ");
    }

    @Override
    public boolean attackEntityAsMob(Entity entity)
    {
        if (onGround)
        {
            double d = entity.posX - posX;
            double d1 = entity.posZ - posZ;
            float f1 = MathHelper.sqrt(d * d + d1 * d1);
            motionX = (d / (double) f1) * 0.5D * 0.20000000192092895D + motionX * 0.20000000098023224D;
            motionZ = (d1 / (double) f1) * 0.5D * 0.10000000192092896D + motionZ * 0.20000000098023224D;
        }

        return super.attackEntityAsMob(entity);
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        if (!this.getIsDormant())
        {
            return AS_BattleTowersCore.soundGolem;
        }
        else
        {
            return SoundEvents.AMBIENT_CAVE;
        }
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return AS_BattleTowersCore.soundGolemHurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return AS_BattleTowersCore.soundGolemDeath;
    }

    @Override
    protected Item getDropItem()
    {
        return Items.PAPER;
    }

}
