package com.sirolf2009.necromancy.entity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.model.ModelBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIControlledByPlayer;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.ISaddleAble;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.client.model.ModelMinion;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;

import cpw.mods.fml.common.FMLCommonHandler;

public class EntityMinion extends EntityTameable
{

    private boolean isAgressive;
    private BaseAttributeMap attributeMap;

    public EntityMinion(World par1World, BodyPart[][] bodypart, String owner)
    {
        this(par1World);
        setBodyParts(bodypart);
        setTamed(true);
        setOwner(owner);
    }

    public EntityMinion(World par1World)
    {
        super(par1World);
        getNavigator().setAvoidsWater(true);
        this.getNavigator().setAvoidsWater(true);
        setSize(0.6F, 1.8F);
        ticksExisted = 0;
        tasks.addTask(0, aiMinion);
        tasks.addTask(1, aiSit);
        tasks.addTask(2, aicontrolledByPlayer);
        tasks.addTask(4, new EntityAITempt(this, 0.3F, ItemGeneric.getItemStackFromName("Brain on a Stick").getItem(), false));
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        dataWatcher.addObject(20, "UNDEFINED");
        dataWatcher.addObject(21, "UNDEFINED");
        dataWatcher.addObject(22, "UNDEFINED");
        dataWatcher.addObject(23, "UNDEFINED");
        dataWatcher.addObject(24, "UNDEFINED");
        dataWatcher.addObject(25, Byte.valueOf((byte) 0));
        dataWatcher.addObject(26, Byte.valueOf((byte) 0));
        onBodyChange();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(Double.MAX_VALUE);
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage);
    }

    public void updateAttributes()
    {
        if (getBodyParts().length > 0)
        {
            attributeMap = new ServersideAttributeMap();
            attributeMap.getAttributeInstance(SharedMonsterAttributes.maxHealth);
            attributeMap.getAttributeInstance(SharedMonsterAttributes.followRange);
            attributeMap.getAttributeInstance(SharedMonsterAttributes.knockbackResistance);
            attributeMap.getAttributeInstance(SharedMonsterAttributes.movementSpeed);
            attributeMap.getAttributeInstance(SharedMonsterAttributes.attackDamage);
            if (head != null && head.length > 0 && head[0] != null)
            {
                head[0].attributes = new ServersideAttributeMap();
                head[0].entity.setAttributes(this, BodyPartLocation.Head);
                combineAttributes(head[0].attributes);
            }
            if (torso != null && torso.length > 0 && torso[0] != null)
            {
                torso[0].attributes = new ServersideAttributeMap();
                torso[0].entity.setAttributes(this, BodyPartLocation.Torso);
                combineAttributes(torso[0].attributes);
            }
            if (armLeft != null && armLeft.length > 0 && armLeft[0] != null)
            {
                armLeft[0].attributes = new ServersideAttributeMap();
                armLeft[0].entity.setAttributes(this, BodyPartLocation.ArmLeft);
                combineAttributes(armLeft[0].attributes);
            }
            if (armRight != null && armRight.length > 0 && armRight[0] != null)
            {
                armRight[0].attributes = new ServersideAttributeMap();
                armRight[0].entity.setAttributes(this, BodyPartLocation.ArmRight);
                combineAttributes(armRight[0].attributes);
            }
            if (leg != null && leg.length > 0 && leg[0] != null)
            {
                leg[0].attributes = new ServersideAttributeMap();
                leg[0].entity.setAttributes(this, BodyPartLocation.Legs);
                combineAttributes(leg[0].attributes);
            }
            setHealth((float) (getHealth() > getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue() ? getEntityAttribute(
                    SharedMonsterAttributes.maxHealth).getBaseValue() : getHealth()));
        }
    }

    @SuppressWarnings("unchecked")
    public void combineAttributes(BaseAttributeMap map)
    {
        Iterator<ModifiableAttributeInstance> itr = map.getAllAttributes().iterator();
        while (itr.hasNext())
        {
            ModifiableAttributeInstance incrementAttribute = itr.next();
            double oldValue =
                    getAttributeMap().getAttributeInstanceByName(incrementAttribute.getAttribute().getAttributeUnlocalizedName()).getBaseValue();
            double increment = incrementAttribute.getBaseValue();
            getAttributeMap().getAttributeInstanceByName(incrementAttribute.getAttribute().getAttributeUnlocalizedName()).setBaseValue(
                    oldValue + increment);
        }
    }

    @Override
    public BaseAttributeMap getAttributeMap()
    {
        if (this.attributeMap == null)
        {
            this.attributeMap = new ServersideAttributeMap();
        }
        return this.attributeMap;
    }

    public void dataWatcherUpdate()
    {
        if (getBodyPartsNames()[0] != "UNDEFINED")
        {
            dataWatcher.updateObject(20, getBodyPartsNames()[0]);
        }
        if (getBodyPartsNames()[1] != "UNDEFINED")
        {
            dataWatcher.updateObject(21, getBodyPartsNames()[1]);
        }
        if (getBodyPartsNames()[2] != "UNDEFINED")
        {
            dataWatcher.updateObject(22, getBodyPartsNames()[2]);
        }
        if (getBodyPartsNames()[3] != "UNDEFINED")
        {
            dataWatcher.updateObject(23, getBodyPartsNames()[3]);
        }
        if (getBodyPartsNames()[4] != "UNDEFINED")
        {
            dataWatcher.updateObject(24, getBodyPartsNames()[4]);
        }
        setSaddled(getSaddled());
        setAltarMob(isAltarMob());
    }

    private void updateBodyParts()
    {
        head = getBodyPartFromlocation(BodyPartLocation.Head, dataWatcher.getWatchableObjectString(20));
        torso = getBodyPartFromlocation(BodyPartLocation.Torso, dataWatcher.getWatchableObjectString(21));
        armLeft = getBodyPartFromlocation(BodyPartLocation.ArmLeft, dataWatcher.getWatchableObjectString(22));
        armRight = getBodyPartFromlocation(BodyPartLocation.ArmRight, dataWatcher.getWatchableObjectString(23));
        leg = getBodyPartFromlocation(BodyPartLocation.Legs, legType = dataWatcher.getWatchableObjectString(24));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        updateBodyParts();
        par1NBTTagCompound.setString("head", getBodyPartsNames()[0]);
        par1NBTTagCompound.setString("body", getBodyPartsNames()[1]);
        par1NBTTagCompound.setString("armLeft", getBodyPartsNames()[2]);
        par1NBTTagCompound.setString("armRight", getBodyPartsNames()[3]);
        par1NBTTagCompound.setString("leg", getBodyPartsNames()[4]);
        par1NBTTagCompound.setBoolean("Saddle", getSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        head = getBodyPartFromlocation(BodyPartLocation.Head, par1NBTTagCompound.getString("head"));
        torso = getBodyPartFromlocation(BodyPartLocation.Torso, par1NBTTagCompound.getString("body"));
        armLeft = getBodyPartFromlocation(BodyPartLocation.ArmLeft, par1NBTTagCompound.getString("armLeft"));
        armRight = getBodyPartFromlocation(BodyPartLocation.ArmRight, par1NBTTagCompound.getString("armRight"));
        leg = getBodyPartFromlocation(BodyPartLocation.Legs, par1NBTTagCompound.getString("leg"));
        setSaddled(par1NBTTagCompound.getBoolean("Saddle"));
        dataWatcherUpdate();
        updateAttributes();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        if (getOwner() != null)
            return par1Entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) getOwner()), 8);
        else
            return par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), 8);
    }

    @Override
    public boolean canBeSteered()
    {
        ItemStack var1 = ((EntityPlayer) riddenByEntity).getHeldItem();
        return var1 != null && var1 == ItemGeneric.getItemStackFromName("Brain on a Stick");
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        List<?> list = worldObj.selectEntitiesWithinAABB(EntityPlayer.class, boundingBox.expand(10D, 4.0D, 10D), null);
        Iterator<?> itr = list.iterator();
        while (itr.hasNext())
        {
            Object obj = itr.next();
            if (obj instanceof EntityPlayer)
            {
                if (getOwner() != null)
                {
                    NBTTagCompound nbt = getOwner().getEntityData();
                    isAgressive = nbt.getBoolean("aggressive");
                    if (nbt.getString(((EntityPlayer) obj).getCommandSenderName()).equals("enemy"))
                    {
                        setAttackTarget((EntityPlayer) obj);
                    }
                    else if (nbt.getString(((EntityPlayer) obj).getCommandSenderName()) == "" && isAgressive)
                    {
                        setAttackTarget((EntityPlayer) obj);
                    }
                }
            }
        }
        if (rand.nextInt(1000) == 0 || ticksExisted == 1)
        {
            if (!worldObj.isRemote)
            {
                dataWatcherUpdate();
            }
            else
            {
                updateBodyParts();
            }
        }
        if (ticksExisted == 1)
        {
            model.updateModel(this, true);
        }
        if (head == null)
        {
            this.setDead();
        }
    }

    public static BodyPart[] getBodyPartFromlocation(BodyPartLocation location, String name)
    {
        NecroEntityBase mob;
        if ((mob = NecroEntityRegistry.registeredEntities.get(name)) != null)
        {
            if (location == BodyPartLocation.Head)
                return mob.head == null ? mob.updateParts(ModelMinion.instance).head : mob.head;
            if (location == BodyPartLocation.Torso)
                return mob.torso == null ? mob.updateParts(ModelMinion.instance).torso : mob.torso;
            if (location == BodyPartLocation.ArmLeft)
                return mob.armLeft == null ? mob.updateParts(ModelMinion.instance).armLeft : mob.armLeft;
            if (location == BodyPartLocation.ArmRight)
                return mob.armRight == null ? mob.updateParts(ModelMinion.instance).armRight : mob.armRight;
            if (location == BodyPartLocation.Legs)
                return mob.legs == null ? mob.updateParts(ModelMinion.instance).legs : mob.legs;
        }
        else if (name != "UNDEFINED")
        {
            System.err.println(location + " " + name + " not found!");
        }
        return null;
    }

    @Override
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        if (!getSaddled() && !worldObj.isRemote && par1EntityPlayer.getHeldItem() != null && par1EntityPlayer.getHeldItem().getItem() == Items.saddle)
        {
            NecroEntityBase mob;
            if (torso != null && torso[0] != null && (mob = NecroEntityRegistry.registeredEntities.get(torso[0].name)) != null
                    && mob instanceof ISaddleAble)
            {
                setSaddled(true);
                if (!par1EntityPlayer.capabilities.isCreativeMode)
                {
                    par1EntityPlayer.inventory.consumeInventoryItem(Items.saddle);
                }
                return true;
            }
            return false;
        }
        if (this.getSaddled() && !worldObj.isRemote && (riddenByEntity == null || riddenByEntity == par1EntityPlayer)
                && (par1EntityPlayer.isSneaking() || riddenByEntity == par1EntityPlayer))
        {
            ISaddleAble mob = (ISaddleAble) NecroEntityRegistry.registeredEntities.get(torso[0].name);
            par1EntityPlayer.mountEntity(this);
            if (riddenByEntity != null)
            {
                float lowestPoint = 0;
                float highestPoint = 0;
                for (Object model : leg[0].cubeList)
                {
                    ModelBox cube = (ModelBox) model;
                    if (cube.posY1 < lowestPoint)
                    {
                        lowestPoint = cube.posY1;
                    }
                    if (cube.posY2 > highestPoint)
                    {
                        highestPoint = cube.posY1;
                    }
                }
                riddenByEntity.height = mob.riderHeight() + (highestPoint - lowestPoint);
            }
            return true;
        }
        if (riddenByEntity == null && par1EntityPlayer.getCommandSenderName().equalsIgnoreCase(this.getOwnerName()) && !worldObj.isRemote)
        {
            aiSit.setSitting(!this.isSitting());
            isJumping = false;
            this.setPathToEntity((PathEntity) null);
            if (FMLCommonHandler.instance().getSide() != cpw.mods.fml.relauncher.Side.SERVER)
            {
                ClientProxy.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Minion is " + (isSitting() ? "walking" : "sitting")));
            }
        }
        else if (riddenByEntity == null && !par1EntityPlayer.getCommandSenderName().equalsIgnoreCase(this.getOwnerName())
                && FMLCommonHandler.instance().getSide() == cpw.mods.fml.relauncher.Side.CLIENT && !worldObj.isRemote)
        {
            ClientProxy.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("<Minion> I obey only " + getOwnerName()));
        }
        return true;
    }

    public boolean getSaddled()
    {
        return (dataWatcher.getWatchableObjectByte(25) & 1) != 0;
    }

    public void setSaddled(boolean par1)
    {
        if (par1)
        {
            dataWatcher.updateObject(25, Byte.valueOf((byte) 1));
        }
        else
        {
            dataWatcher.updateObject(25, Byte.valueOf((byte) 0));
        }
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    public void setBodyPart(BodyPartLocation location, BodyPart[] bodypart)
    {
        if (location == BodyPartLocation.Head)
        {
            head = bodypart;
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso = bodypart;
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            armLeft = bodypart;
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight = bodypart;
        }
        else if (location == BodyPartLocation.Legs)
        {
            leg = bodypart;
        }
        else
        {
            System.err.println("Trying to set an impossible body part!");
        }
        dataWatcherUpdate();
    }

    @Override
    public String toString()
    {
        return String.format(
                "%s[\'%s\'/%d, l=\'%s\', x=%.1f, y=%.1f, z=%.1f, head=\'%s\', torso=\'%s\', armLeft=\'%s\', armRight=\'%s\', legs=\'%s\']",
                new Object[] { this.getClass().getSimpleName(), this.getEntityString(), Integer.valueOf(getEntityId()),
                        worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), Double.valueOf(posX), Double.valueOf(posY),
                        Double.valueOf(posZ), getBodyPartsNames()[0], getBodyPartsNames()[1], getBodyPartsNames()[2], getBodyPartsNames()[3],
                        getBodyPartsNames()[4] });
    }

    public String[] getBodyPartsNames()
    {
        String list[] =
                { head != null && head.length > 0 && head[0] != null ? head[0].name : "UNDEFINED",
                        torso != null && torso.length > 0 && torso[0] != null ? torso[0].name : "UNDEFINED",
                        armLeft != null && armLeft.length > 0 && armLeft[0] != null ? armLeft[0].name : "UNDEFINED",
                        armRight != null && armRight.length > 0 && armRight[0] != null ? armRight[0].name : "UNDEFINED",
                        leg != null && leg.length > 0 && leg[0] != null ? leg[0].name : "UNDEFINED", };
        return list;
    }

    public BodyPart[][] getBodyParts()
    {
        BodyPart[][] list = { head, torso, armLeft, armRight, leg };
        return list;
    }

    public void onBodyChange()
    {
        if (model != null)
        {
            model.updateModel(this, isAltarMob);
        }
    }

    public ModelMinion getModel()
    {
        return model;
    }

    public void setModel(ModelMinion model)
    {
        this.model = model;
    }

    public EntityAnimal spawnBabyAnimal(EntityAnimal var1)
    {
        return null;
    }

    public boolean isAltarMob()
    {
        return isAltarMob;
    }

    public void setAltarMob(boolean isAltarMob)
    {
        this.isAltarMob = isAltarMob;
        if (isAltarMob)
        {
            dataWatcher.updateObject(26, Byte.valueOf((byte) 1));
        }
        else
        {
            dataWatcher.updateObject(26, Byte.valueOf((byte) 0));
        }
    }

    public void setBodyParts(BodyPart[][] bodypart)
    {
        head = bodypart[0];
        torso = bodypart[1];
        armLeft = bodypart[2];
        armRight = bodypart[3];
        leg = bodypart[4];
        dataWatcherUpdate();
    }

    public void setAltar(TileEntityAltar tileEntityAltar)
    {
        altar = tileEntityAltar;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return null;
    }

    @Override
    protected String getLivingSound()
    {
        return "mob." + aiMinion.getSound(this) + ".say";
    }

    @Override
    protected String getHurtSound()
    {
        return "mob." + aiMinion.getSound(this) + ".hurt";
    }

    @Override
    protected String getDeathSound()
    {
        return "mob." + aiMinion.getSound(this) + ".death";
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        getOwner().getEntityData().setInteger("minions", getOwner().getEntityData().getInteger("minions") - 1);
    }

    protected String legType = "";
    protected BodyPart[] head, torso, armLeft, armRight, leg;
    protected ModelMinion model = new ModelMinion();
    private boolean isAltarMob = false;
    private EntityAIMinion aiMinion = new EntityAIMinion(this);
    private final EntityAIControlledByPlayer aicontrolledByPlayer = new EntityAIControlledByPlayer(this, 0.8F);
    public TileEntityAltar altar;

    @Override
    public EntityLivingBase getOwner()
    {
        return worldObj.getPlayerEntityByName(getOwnerName());
    }
}
