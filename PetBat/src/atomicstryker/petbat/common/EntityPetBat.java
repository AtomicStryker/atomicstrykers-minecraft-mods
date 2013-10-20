package atomicstryker.petbat.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.petbat.common.batAI.PetBatAIAttack;
import atomicstryker.petbat.common.batAI.PetBatAIFindSittingSpot;
import atomicstryker.petbat.common.batAI.PetBatAIFlying;
import atomicstryker.petbat.common.batAI.PetBatAIOwnerAttacked;
import atomicstryker.petbat.common.batAI.PetBatAIOwnerAttacks;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPetBat extends EntityCreature implements IEntityAdditionalSpawnData
{    
    private String ownerName;
    private String petName;
    private EntityPlayer owner;
    private EntityItem foodAttackTarget;
    
    private int lastOwnerX;
    private int lastOwnerY;
    private int lastOwnerZ;
    
    private ChunkCoordinates hangSpot;

    public EntityPetBat(World par1World)
    {
        super(par1World);
        setSize(0.5F, 0.9F);
        setIsBatHanging(false);
        ownerName = "";
        petName = "";
        lastOwnerX = lastOwnerY = lastOwnerZ = 0;
        hangSpot = null;
        
        tasks.addTask(1, new PetBatAIAttack(this));
        tasks.addTask(2, new PetBatAIFlying(this));
        tasks.addTask(3, new PetBatAIFindSittingSpot(this));
        targetTasks.addTask(1, new PetBatAIOwnerAttacked(this));
        targetTasks.addTask(2, new PetBatAIOwnerAttacks(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }
    
    @Override
    public void writeSpawnData(ByteArrayDataOutput data)
    {
        data.writeUTF(ownerName);
        data.writeUTF(petName);
    }

    @Override
    public void readSpawnData(ByteArrayDataInput data)
    {
        ownerName = data.readUTF();
        petName = data.readUTF();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, new Byte((byte)0));
        dataWatcher.addObject(17, new Integer((int)0));
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
    
    /**
     * Used by PetBat Renderer to display Bat Name
     */
    public String getDisplayName()
    {
        return petName;
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
        lastOwnerX = (int) (owner.posX+0.5D);
        lastOwnerY = (int) (owner.posY+0.5D);
        lastOwnerZ = (int) (owner.posZ+0.5D);
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
    
    public void setHangingSpot(ChunkCoordinates coords)
    {
        hangSpot = coords;
    }
    
    public ChunkCoordinates getHangingSpot()
    {
        return hangSpot;
    }
    
    public boolean getHasTarget()
    {
        if (getAttackTarget() != null && getAttackTarget().isEntityAlive())
        {
            return true;
        }
        if (getFoodAttackTarget() != null && getFoodAttackTarget().isEntityAlive())
        {
            return true;
        }
        return false;
    }
    
    /**
     * Sets the active target the Task system uses for tracking
     */
    @Override
    public void setAttackTarget(EntityLivingBase par1EntityLiving)
    {
        super.setAttackTarget(par1EntityLiving);
    }
    
    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.equals(DamageSource.inWall))
        {
            return true;
        }
        
        if (!this.worldObj.isRemote)
        {
            if (getIsBatHanging())
            {
                setIsBatHanging(false);
            }
            
            if (source.getEntity() != null
            && source.getEntity() == owner)
            {
                ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
                if (batstack != null)
                {
                    if (owner.inventory.addItemStackToInventory(batstack))
                    {
                        worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                        setDeadWithoutRecall();
                        return true;
                    }
                }
            }
        }

        return super.attackEntityFrom(source, amount);
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
        
        if (livingTarget != null && result)
        {
            float damageDealt = prevHealth - livingTarget.getHealth();
            if (damageDealt > 0)
            {
                addBatExperience((int) damageDealt);
                
                if (level > 2)
                {
                    this.heal(Math.max(damageDealt/3, 1));
                }
            }
        }
        return result;
    }
    
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     * return true when interaction is accepted, otherwise it will act as if there was no Entity you clicked.
     */
    @Override
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        // ItemStack stack = par1EntityPlayer.inventory.getCurrentItem();
        // TODO think up some food/healing interaction here?
        return super.interact(par1EntityPlayer);
    }
    
    @Override
    public void handleHealthUpdate(byte par1)
    {
        super.handleHealthUpdate(par1);
    }
    
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }
    
    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
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
                if (owner.inventory.addItemStackToInventory(batstack))
                {
                    worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                }
                else
                {
                    worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, batstack));
                }
            }
        }
        
        super.setDead();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound()
    {
        return "mob.bat.idle";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound()
    {
        return "mob.bat.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound()
    {
        return "mob.bat.death";
    }
    
    @Override
    public void setInPortal()
    {
        //Nope
    }

    public boolean getIsBatHanging()
    {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setIsBatHanging(boolean par1)
    {
        setHangingSpot(null);
        
        byte var2 = this.dataWatcher.getWatchableObjectByte(16);

        if (par1)
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(var2 | 1)));
        }
        else
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(var2 & -2)));
        }
    }
    
    /**
     * Bat levels up with all damage it inflicts in combat.
     * @param xp one experience point for every point of damage inflicted
     */
    private void addBatExperience(int xp)
    {
        if (!worldObj.isRemote)
        {
            setBatExperience(Integer.valueOf(getBatExperience()+xp));
        }
    }
    
    public int getBatExperience()
    {
        return dataWatcher.getWatchableObjectInt(17);
    }
    
    public void setBatExperience(int value)
    {
        dataWatcher.updateObject(17, value);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(16d + (2*PetBatMod.instance().getLevelFromExperience(value)));
    }
    
    public int getBatLevel()
    {
        return PetBatMod.instance().getLevelFromExperience(getBatExperience());
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    @Override
    protected boolean isAIEnabled()
    {
        return true;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (this.getIsBatHanging())
        {
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.posY = (double)MathHelper.floor_double(this.posY) + 1.0D - (double)this.height;
        }
        else
        {
            this.motionY *= 0.6D;
        }
    }

    /**
     * CLIENTSIDE ONLY AI
     */
    @Override
    protected void updateAITasks()
    {
        super.updateAITasks();
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    @Override
    protected void fall(float par1) {}

    /**
     * Takes in the distance the entity has fallen this tick and whether its on the ground to update the fall distance
     * and deal fall damage if landing on the ground.  Args: distanceFallenThisTick, onGround
     */
    @Override
    protected void updateFallState(double par1, boolean par3) {}

    @Override
    public boolean doesEntityNotTriggerPressurePlate()
    {
        return true;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.dataWatcher.updateObject(16, Byte.valueOf(nbt.getByte("BatFlags")));
        dataWatcher.updateObject(17, Integer.valueOf(nbt.getInteger("BatXP")));
        this.ownerName = nbt.getString("ownerName");
        this.petName = nbt.getString("petName");
        lastOwnerX = nbt.getInteger("lastOwnerX");
        lastOwnerY = nbt.getInteger("lastOwnerY");
        lastOwnerZ = nbt.getInteger("lastOwnerZ");
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setByte("BatFlags", this.dataWatcher.getWatchableObjectByte(16));
        nbt.setInteger("BatXP", getBatExperience());
        nbt.setString("ownerName", this.ownerName);
        nbt.setString("petName", this.petName);
        nbt.setInteger("lastOwnerX", lastOwnerX);
        nbt.setInteger("lastOwnerY", lastOwnerY);
        nbt.setInteger("lastOwnerZ", lastOwnerZ);
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    @Override
    public boolean getCanSpawnHere()
    {
        return super.getCanSpawnHere();
    }
    
    @Override
    public String getEntityName()
    {
        return "Pet Bat";
    }
    
    public boolean glister;
    public void setGlistering(boolean set)
    {
        glister = set;
    }
    
}
