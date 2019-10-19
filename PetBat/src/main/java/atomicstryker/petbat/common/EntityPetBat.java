package atomicstryker.petbat.common;

import atomicstryker.petbat.common.batAI.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityPetBat extends CreatureEntity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Byte> BAT_FLAGS = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> IS_STAYING = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> BAT_XP = EntityDataManager.createKey(EntityPetBat.class, DataSerializers.VARINT);
    private UUID ownerUUID;

    private PlayerEntity owner;
    private ItemEntity foodAttackTarget;
    private boolean fluteOut;
    private boolean isRecalled;
    private int lastOwnerX;
    private int lastOwnerY;
    private int lastOwnerZ;
    private BlockPos hangSpot;
    private long nextInteractPossibleTime;

    public EntityPetBat(World par1World) {
        super(PetBatMod.instance().batEntityType, par1World);
        setIsBatHanging(false);
        ownerUUID = null;
        lastOwnerX = lastOwnerY = lastOwnerZ = 0;
        hangSpot = null;
        fluteOut = false;
        isRecalled = false;
        setCustomName(new StringTextComponent("Battus Genericus"));
        setCustomNameVisible(true);
        nextInteractPossibleTime = System.currentTimeMillis();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PetBatAIAttack(this));
        this.goalSelector.addGoal(2, new PetBatAIFlying(this));
        this.goalSelector.addGoal(3, new PetBatAIFindSittingSpot(this));
        this.targetSelector.addGoal(1, new PetBatAIOwnerAttacked(this));
        this.targetSelector.addGoal(2, new PetBatAIOwnerAttacks(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeString(ownerUUID == null ? "null" : ownerUUID.toString());
        data.writeString(getCustomNameSafe().getUnformattedComponentText());
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        String uid = data.readString(32767);
        if (!"null".equals(uid)) {
            ownerUUID = UUID.fromString(uid);
        } else {
            ownerUUID = null;
        }
        String petName = data.readString(32767);
        setCustomName(new TranslationTextComponent(petName));
    }


    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(BAT_FLAGS, (byte) 0);
        dataManager.register(IS_STAYING, (byte) 0);
        dataManager.register(BAT_XP, 0);
    }

    public void setNames(UUID ownerId, String petName) {
        this.ownerUUID = ownerId;
        setCustomName(new TranslationTextComponent(petName));
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public Team getTeam() {
        if (ownerUUID != null) {
            return world.getScoreboard().getPlayersTeam(ownerUUID.toString());
        }
        return super.getTeam();
    }

    public PlayerEntity getOwnerEntity() {
        return owner;
    }

    public void setOwnerEntity(PlayerEntity playerEntityByName) {
        owner = playerEntityByName;
    }

    public void updateOwnerCoords() {
        lastOwnerX = (int) (owner.posX + 0.5D);
        lastOwnerY = (int) (owner.posY + 0.5D);
        lastOwnerZ = (int) (owner.posZ + 0.5D);
    }

    public int getLastOwnerX() {
        return lastOwnerX;
    }

    public int getLastOwnerY() {
        return lastOwnerY;
    }

    public int getLastOwnerZ() {
        return lastOwnerZ;
    }

    public ItemEntity getFoodAttackTarget() {
        return foodAttackTarget;
    }

    public void setFoodAttackTarget(ItemEntity target) {
        foodAttackTarget = target;
    }

    public BlockPos getHangingSpot() {
        return hangSpot;
    }

    public void setHangingSpot(BlockPos coords) {
        hangSpot = coords;
    }

    public boolean getHasTarget() {
        return getAttackTarget() != null && getAttackTarget().isAlive() || getFoodAttackTarget() != null && getFoodAttackTarget().isAlive();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.equals(DamageSource.IN_WALL)) {
            return true;
        }
        if (!world.isRemote) {
            if (getIsBatHanging()) {
                setIsBatHanging(false);
            }

            // if hit by owner
            if (source.getTrueSource() != null && source.getTrueSource().getUniqueID().equals(getOwnerUUID())) {
                // and in combat with something else
                if (source.getTrueSource() != getAttackTarget()) {
                    // ignore the hit
                    return true;
                }
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    public void recallToOwner() {
        isRecalled = true;
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        if (!player.world.isRemote() && getIsBatHanging() && player.getUniqueID().equals(ownerUUID)) {
            long time = System.currentTimeMillis();
            if (time >= nextInteractPossibleTime) {
                nextInteractPossibleTime = time + 1000L;
                setIsBatStaying(!getIsBatStaying());
                player.sendMessage(
                        new TranslationTextComponent(getCustomNameSafe().getUnformattedComponentText() + ": " + (getIsBatStaying() ? I18n.format("translation.PetBat:staying") : I18n.format("translation.PetBat:notstaying"))));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        int level = getBatLevel();
        int damage = 1 + level;

        float prevHealth = 0;
        LivingEntity livingTarget = null;
        if (target instanceof LivingEntity) {
            livingTarget = (LivingEntity) target;
            prevHealth = livingTarget.getHealth();
        }

        boolean result = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (result) {
            if (livingTarget != null) {
                float damageDealt = prevHealth - livingTarget.getHealth();
                if (damageDealt > 0) {
                    addBatExperience((int) Math.max(1, damageDealt));
                    if (level > 2) {
                        heal(Math.max(damageDealt / 3, 1));
                    }
                }
            } else {
                addBatExperience(damage);
                if (level > 2) {
                    heal(Math.max(damage / 3, 1));
                }
            }
        }

        return result;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    public void setDeadWithoutRecall() {
        remove();
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (this.owner != null && !world.isRemote) {
            setHealth(1);
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != ItemStack.EMPTY) {
                PetBatMod.instance().removeFluteFromPlayer(owner, getCustomNameSafe().getUnformattedComponentText());
                if (owner.getHealth() > 0 && owner.inventory.addItemStackToInventory(batstack)) {
                    world.playSound(null, new BlockPos(owner), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.HOSTILE, 1F, 1F);
                } else {
                    world.playSound(null, new BlockPos(owner), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.HOSTILE, 1F, 1F);
                    world.addEntity(new ItemEntity(world, owner.posX, owner.posY, owner.posZ, batstack));
                }
            }
        }

        super.onDeath(cause);
        remove();
    }

    private ITextComponent getCustomNameSafe() {
        ITextComponent result = getCustomName();
        if (result != null) {
            return result;
        }
        return new StringTextComponent("Nameless Bat");
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PetBatMod.soundIdle;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PetBatMod.soundHit;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PetBatMod.soundDeath;
    }

    @Override
    public void setPortal(BlockPos b) {
        // Nope
    }

    public boolean getIsBatHanging() {
        return (this.dataManager.get(BAT_FLAGS) & 1) != 0;
    }

    public void setIsBatHanging(boolean par1) {
        setHangingSpot(null);

        byte var2 = this.dataManager.get(BAT_FLAGS);

        if (par1) {
            dataManager.set(BAT_FLAGS, (byte) (var2 | 1));
        } else {
            dataManager.set(BAT_FLAGS, (byte) (var2 & -2));
        }
    }

    /**
     * Bat levels up with all damage it inflicts in combat.
     *
     * @param xp one experience point for every point of damage inflicted
     */
    private void addBatExperience(int xp) {
        if (!world.isRemote) {
            PetBatMod.LOGGER.debug("bat {} earned xp: {}, is now: {}", getCustomNameSafe().getUnformattedComponentText(), xp, getBatExperience() + xp);
            setBatExperience(getBatExperience() + xp);
        }
    }

    public int getBatExperience() {
        return dataManager.get(BAT_XP);
    }

    public void setBatExperience(int value) {
        dataManager.set(BAT_XP, value);
        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16d + (2 * PetBatMod.instance().getLevelFromExperience(value)));
    }

    public boolean getIsBatStaying() {
        return dataManager.get(IS_STAYING) != 0;
    }

    public void setIsBatStaying(boolean cond) {
        dataManager.set(IS_STAYING, (byte) (cond ? 1 : 0));
    }

    public int getBatLevel() {
        return PetBatMod.instance().getLevelFromExperience(getBatExperience());
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();

        checkOwnerFlute();

        if (this.getIsBatHanging()) {
            this.setMotion(0, 0, 0);
            this.posY = (double) MathHelper.floor(this.posY) + 1.0D - (double) this.getHeight();
        } else {
            double newY = getMotion().y;
            newY *= 0.6D;
            setMotion(getMotion().x, newY, getMotion().z);
        }

        if (isRecalled) {
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != ItemStack.EMPTY && owner != null) {
                ItemStack flute = PetBatMod.instance().removeFluteFromPlayer(owner, getCustomNameSafe().getUnformattedComponentText());
                if (owner.inventory.addItemStackToInventory(batstack)) {
                    world.playSound(null, new BlockPos(owner), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.HOSTILE, 1F, 1F);
                    setDeadWithoutRecall();
                } else {
                    owner.inventory.addItemStackToInventory(flute);
                }
            }
        }
    }

    private void checkOwnerFlute() {
        if (!fluteOut && owner != null && !world.isRemote) {
            boolean found = false;
            final Item fluteItem = PetBatMod.instance().itemBatFlute;
            for (ItemStack inventoryItem : owner.inventory.mainInventory) {
                if (inventoryItem.getItem() == fluteItem && inventoryItem.getTag() != null) {
                    if (inventoryItem.getTag().getString("batName").equals(getCustomNameSafe().getUnformattedComponentText())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                ItemStack newflute = new ItemStack(fluteItem, 1, null);
                newflute.getOrCreateTag().putString("batName", getCustomNameSafe().getUnformattedComponentText());
                if (owner.inventory.addItemStackToInventory(newflute)) {
                    fluteOut = true;
                }
            }
        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        this.dataManager.set(BAT_FLAGS, nbt.getByte("BatFlags"));
        dataManager.set(BAT_XP, nbt.getInt("BatXP"));
        String uid = nbt.getString("ownerUUID");
        if (!uid.equals("null")) {
            this.ownerUUID = UUID.fromString(uid);
        } else {
            ownerUUID = null;
        }
        lastOwnerX = nbt.getInt("lastOwnerX");
        lastOwnerY = nbt.getInt("lastOwnerY");
        lastOwnerZ = nbt.getInt("lastOwnerZ");
    }

    @Override
    public CompoundNBT writeWithoutTypeId(CompoundNBT nbt) {
        super.writeWithoutTypeId(nbt);
        nbt.putByte("BatFlags", this.dataManager.get(BAT_FLAGS));
        nbt.putInt("BatXP", getBatExperience());
        nbt.putString("ownerUUID", ownerUUID == null ? "null" : ownerUUID.toString());
        nbt.putInt("lastOwnerX", lastOwnerX);
        nbt.putInt("lastOwnerY", lastOwnerY);
        nbt.putInt("lastOwnerZ", lastOwnerZ);
        return nbt;
    }

}
