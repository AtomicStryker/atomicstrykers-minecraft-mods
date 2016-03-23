package atomicstryker.petbat.common;

import atomicstryker.petbat.common.batAI.PetBatAIAttack;
import atomicstryker.petbat.common.batAI.PetBatAIFindSittingSpot;
import atomicstryker.petbat.common.batAI.PetBatAIFlying;
import atomicstryker.petbat.common.batAI.PetBatAIOwnerAttacked;
import atomicstryker.petbat.common.batAI.PetBatAIOwnerAttacks;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPetBat extends EntityCreature implements IEntityAdditionalSpawnData
{
    private String ownerName;
    private String petName;
    private EntityPlayer owner;
    private EntityItem foodAttackTarget;
    private boolean fluteOut;
    private boolean isRecalled;

    private int lastOwnerX;
    private int lastOwnerY;
    private int lastOwnerZ;

    private BlockPos hangSpot;

    private static final DataParameter<Byte> BAT_FLAGS = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> IS_STAYING = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> BAT_XP = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.VARINT);

    public EntityPetBat(World par1World)
    {
        super(par1World);
        setSize(0.5F, 0.9F);
        setIsBatHanging(false);
        ownerName = "";
        petName = "";
        lastOwnerX = lastOwnerY = lastOwnerZ = 0;
        hangSpot = null;
        fluteOut = false;
        isRecalled = false;

        tasks.addTask(1, new PetBatAIAttack(this));
        tasks.addTask(2, new PetBatAIFlying(this));
        tasks.addTask(3, new PetBatAIFindSittingSpot(this));
        targetTasks.addTask(1, new PetBatAIOwnerAttacked(this));
        targetTasks.addTask(2, new PetBatAIOwnerAttacks(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        ByteBufUtils.writeUTF8String(data, ownerName);
        ByteBufUtils.writeUTF8String(data, petName);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        ownerName = ByteBufUtils.readUTF8String(data);
        petName = ByteBufUtils.readUTF8String(data);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.register(BAT_FLAGS, (byte) 0);
        dataWatcher.register(IS_STAYING, (byte) 0);
        dataWatcher.register(BAT_XP, 0);
    }

    public void setNames(String ownerName, String petName)
    {
        this.ownerName = ownerName;
        this.petName = petName;
    }

    public String getOwnerName()
    {
        return ownerName;
    }
    
    @Override
    public Team getTeam()
    {
    	return worldObj.getScoreboard().getPlayersTeam(ownerName);
    }
    
    /**
     * Used by PetBat Renderer to display Bat Name
     */
    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation(petName);
    }

    public EntityPlayer getOwnerEntity()
    {
        return owner;
    }

    public void setOwnerEntity(EntityPlayer playerEntityByName)
    {
        owner = playerEntityByName;
    }

    public void updateOwnerCoords()
    {
        lastOwnerX = (int) (owner.posX + 0.5D);
        lastOwnerY = (int) (owner.posY + 0.5D);
        lastOwnerZ = (int) (owner.posZ + 0.5D);
    }

    public int getLastOwnerX()
    {
        return lastOwnerX;
    }

    public int getLastOwnerY()
    {
        return lastOwnerY;
    }

    public int getLastOwnerZ()
    {
        return lastOwnerZ;
    }

    public void setFoodAttackTarget(EntityItem target)
    {
        foodAttackTarget = target;
    }

    public EntityItem getFoodAttackTarget()
    {
        return foodAttackTarget;
    }

    public void setHangingSpot(BlockPos coords)
    {
        hangSpot = coords;
    }

    public BlockPos getHangingSpot()
    {
        return hangSpot;
    }

    public boolean getHasTarget()
    {
        return getAttackTarget() != null && getAttackTarget().isEntityAlive() || getFoodAttackTarget() != null && getFoodAttackTarget().isEntityAlive();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.equals(DamageSource.inWall))
        {
            return true;
        }
        if (!worldObj.isRemote)
        {
            if (getIsBatHanging())
            {
                setIsBatHanging(false);
            }
            
            // if hit by owner
            if (source.getEntity() != null && source.getEntity().getName().equals(getOwnerName()))
            {
                // and in combat with something else
                if (source.getEntity() != getAttackTarget())
                {
                    // ignore the hit
                    return true;
                }
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    public void recallToOwner()
    {
        isRecalled = true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (getIsBatHanging() && player.getName().equals(ownerName))
        {
            setIsBatStaying(!getIsBatStaying());
            player.addChatMessage(new TextComponentTranslation(petName + ": " + 
            (getIsBatStaying() ? I18n.translateToLocal("translation.PetBat:staying")
                    : I18n.translateToLocal("translation.PetBat:notstaying"))));
            return true;
        }
        return false;
    }

    @Override
    public boolean attackEntityAsMob(Entity target)
    {
        int level = getBatLevel();
        int damage = 1 + level;

        float prevHealth = 0;
        EntityLivingBase livingTarget = null;
        if (target instanceof EntityLivingBase)
        {
            livingTarget = (EntityLivingBase) target;
            prevHealth = livingTarget.getHealth();
        }

        boolean result = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (result)
        {
            if (livingTarget != null)
            {
                float damageDealt = prevHealth - livingTarget.getHealth();
                if (damageDealt > 0)
                {
                    addBatExperience((int) Math.max(1, damageDealt));
                    if (level > 2)
                    {
                        heal(Math.max(damageDealt / 3, 1));
                    }
                }
            }
            else
            {
                addBatExperience(damage);
                if (level > 2)
                {
                    heal(Math.max(damage / 3, 1));
                }
            }
        }

        return result;
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    public void setDeadWithoutRecall()
    {
        super.setDead();
    }

    @Override
    public void setDead()
    {
        if (this.owner != null && !worldObj.isRemote)
        {
            setHealth(1);
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != null)
            {
                PetBatMod.instance().removeFluteFromPlayer(owner, petName);
                if (owner.getHealth() > 0 && owner.inventory.addItemStackToInventory(batstack))
                {
                    worldObj.playSound(null, new BlockPos(owner), SoundEvents.entity_slime_attack, SoundCategory.HOSTILE, 1F, 1F);
                }
                else
                {
                    worldObj.playSound(null, new BlockPos(owner), SoundEvents.entity_slime_attack, SoundCategory.HOSTILE, 1F, 1F);
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, owner.posX, owner.posY, owner.posZ, batstack));
                }
            }
        }

        super.setDead();
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.entity_bat_ambient;
    }

    @Override
    protected SoundEvent getHurtSound()
    {
        return SoundEvents.entity_bat_hurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.entity_bat_death;
    }

    @Override
    public void setPortal(BlockPos b)
    {
        // Nope
    }

    public boolean getIsBatHanging()
    {
        return (this.dataWatcher.get(BAT_FLAGS) & 1) != 0;
    }

    public void setIsBatHanging(boolean par1)
    {
        setHangingSpot(null);

        byte var2 = this.dataWatcher.get(BAT_FLAGS);

        if (par1)
        {
            dataWatcher.set(BAT_FLAGS, (byte) (var2 | 1));
        }
        else
        {
            dataWatcher.set(BAT_FLAGS, (byte) (var2 & -2));
        }
    }

    /**
     * Bat levels up with all damage it inflicts in combat.
     * 
     * @param xp
     *            one experience point for every point of damage inflicted
     */
    private void addBatExperience(int xp)
    {
        if (!worldObj.isRemote)
        {
            setBatExperience(getBatExperience() + xp);
        }
    }

    public int getBatExperience()
    {
        return dataWatcher.get(BAT_XP);
    }

    public void setBatExperience(int value)
    {
        dataWatcher.set(BAT_XP, value);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16d + (2 * PetBatMod.instance().getLevelFromExperience(value)));
    }

    public boolean getIsBatStaying()
    {
        return dataWatcher.get(IS_STAYING) != 0;
    }

    public void setIsBatStaying(boolean cond)
    {
        dataWatcher.set(IS_STAYING, (byte) (cond ? 1 : 0));
    }

    public int getBatLevel()
    {
        return PetBatMod.instance().getLevelFromExperience(getBatExperience());
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        checkOwnerFlute();

        if (this.getIsBatHanging())
        {
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.posY = (double) MathHelper.floor_double(this.posY) + 1.0D - (double) this.height;
        }
        else
        {
            this.motionY *= 0.6D;
        }

        if (isRecalled)
        {
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != null && owner != null)
            {
                ItemStack flute = PetBatMod.instance().removeFluteFromPlayer(owner, petName);
                if (owner.inventory.addItemStackToInventory(batstack))
                {
                    worldObj.playSound(null, new BlockPos(owner), SoundEvents.entity_slime_attack, SoundCategory.HOSTILE, 1F, 1F);
                    setDeadWithoutRecall();
                }
                else
                {
                    owner.inventory.addItemStackToInventory(flute);
                }
            }
        }
    }

    private void checkOwnerFlute()
    {
        if (!fluteOut && owner != null && !worldObj.isRemote)
        {
            boolean found = false;
            final Item fluteItem = PetBatMod.instance().itemBatFlute;
            for (ItemStack inventoryItem : owner.inventory.mainInventory)
            {
                if (inventoryItem != null && inventoryItem.getItem() == fluteItem && inventoryItem.getTagCompound() != null)
                {
                    if (inventoryItem.getTagCompound().getString("batName").equals(petName))
                    {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
            {
                ItemStack newflute = new ItemStack(fluteItem, 1, 0);
                newflute.setTagCompound(new NBTTagCompound());
                newflute.getTagCompound().setString("batName", petName);
                if (owner.inventory.addItemStackToInventory(newflute))
                {
                    fluteOut = true;
                }
            }
        }
    }

    @Override
    protected void updateAITasks()
    {
        super.updateAITasks();
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public void fall(float distance, float damageMultiplier)
    {
    }
    
    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
    {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate()
    {
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.dataWatcher.set(BAT_FLAGS, nbt.getByte("BatFlags"));
        dataWatcher.set(BAT_XP, nbt.getInteger("BatXP"));
        this.ownerName = nbt.getString("ownerName");
        this.petName = nbt.getString("petName");
        lastOwnerX = nbt.getInteger("lastOwnerX");
        lastOwnerY = nbt.getInteger("lastOwnerY");
        lastOwnerZ = nbt.getInteger("lastOwnerZ");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setByte("BatFlags", this.dataWatcher.get(BAT_FLAGS));
        nbt.setInteger("BatXP", getBatExperience());
        nbt.setString("ownerName", this.ownerName);
        nbt.setString("petName", this.petName);
        nbt.setInteger("lastOwnerX", lastOwnerX);
        nbt.setInteger("lastOwnerY", lastOwnerY);
        nbt.setInteger("lastOwnerZ", lastOwnerZ);
    }

    @Override
    public boolean getCanSpawnHere()
    {
        return super.getCanSpawnHere();
    }

    @Override
    public String getName()
    {
        return petName;
    }

    public boolean glister;

    public void setGlistering(boolean set)
    {
        glister = set;
    }

}
