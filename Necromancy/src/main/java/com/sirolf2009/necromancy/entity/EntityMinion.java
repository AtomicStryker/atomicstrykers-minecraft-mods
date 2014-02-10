package com.sirolf2009.necromancy.entity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.model.ModelBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIControlledByPlayer;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
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

import cpw.mods.fml.common.FMLCommonHandler;

public class EntityMinion extends EntityTameable
{

    protected String legType = "";
    protected BodyPart[] head, torso, armLeft, armRight, leg;
    protected ModelMinion model;
    private boolean isAgressive;
    
    public EntityMinion(World par1World)
    {
        super(par1World);
        model = new ModelMinion();
        getNavigator().setAvoidsWater(true);
        setSize(0.6F, 1.8F);
        ticksExisted = 0;
        
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, aiSit);
        tasks.addTask(3, new EntityAIControlledByPlayer(this, 0.8F));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, 0.3F, true));
        tasks.addTask(5, new EntityAIWander(this, 0.3F));
        tasks.addTask(6, new EntityAITempt(this, 0.3F, ItemGeneric.getItemStackFromName("Brain on a Stick").getItem(), false));
        tasks.addTask(7, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));        
        
        model.updateModel(this, false);
    }
    
    public EntityMinion(World par1World, BodyPart[][] bodypart, String owner)
    {
        this(par1World);
        setBodyParts(bodypart);
        setTamed(true);
        setOwner(owner);
    }
    
    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(20, "UNDEFINED");
        dataWatcher.addObject(21, "UNDEFINED");
        dataWatcher.addObject(22, "UNDEFINED");
        dataWatcher.addObject(23, "UNDEFINED");
        dataWatcher.addObject(24, "UNDEFINED");
        dataWatcher.addObject(25, Byte.valueOf((byte) 0));
        dataWatcher.addObject(26, Byte.valueOf((byte) 0));
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1D);
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0D);
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0D);
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0D);
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.1D);
    }
    
    /**
     * Called upon spawning the Necro Entity (from Worldload or TileEntity)
     */
    public void updateAttributes()
    {
        if (getBodyParts().length > 0)
        {
            if (head != null && head.length > 0 && head[0] != null)
            {
                head[0].entity.setAttributes(this, BodyPartLocation.Head);
            }
            if (torso != null && torso.length > 0 && torso[0] != null)
            {
                torso[0].entity.setAttributes(this, BodyPartLocation.Torso);
            }
            if (armLeft != null && armLeft.length > 0 && armLeft[0] != null)
            {
                armLeft[0].entity.setAttributes(this, BodyPartLocation.ArmLeft);
            }
            if (armRight != null && armRight.length > 0 && armRight[0] != null)
            {
                armRight[0].entity.setAttributes(this, BodyPartLocation.ArmRight);
            }
            if (leg != null && leg.length > 0 && leg[0] != null)
            {
                leg[0].entity.setAttributes(this, BodyPartLocation.Legs);
            }
            
            setHealth((float) (getHealth() > getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue() ? getEntityAttribute(
                    SharedMonsterAttributes.maxHealth).getAttributeValue() : getHealth()));
            
            System.out.println("Necromancy Minion spawned, final movementSpeed:" + getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            System.out.println("Necromancy Minion spawned, final health:" + getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue());
            System.out.println("Necromancy Minion spawned, final damage:" + getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
        }
    }
    
    /**
     * Sends the bodypart data to the dataWatcher system
     */
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
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        float damage = (float) getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).getAttributeValue();
        if (getOwner() != null)
            return par1Entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) getOwner()), damage);
        else
            return par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
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
                    else if (nbt.getString(((EntityPlayer) obj).getCommandSenderName()).equals("") && isAgressive)
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

    private BodyPart[] getBodyPartFromlocation(BodyPartLocation location, String name)
    {
        NecroEntityBase mob;
        if ((mob = NecroEntityRegistry.registeredEntities.get(name)) != null)
        {
            if (location == BodyPartLocation.Head)
                return mob.head == null ? mob.updateParts(model).head : mob.head;
            if (location == BodyPartLocation.Torso)
                return mob.torso == null ? mob.updateParts(model).torso : mob.torso;
            if (location == BodyPartLocation.ArmLeft)
                return mob.armLeft == null ? mob.updateParts(model).armLeft : mob.armLeft;
            if (location == BodyPartLocation.ArmRight)
                return mob.armRight == null ? mob.updateParts(model).armRight : mob.armRight;
            if (location == BodyPartLocation.Legs)
                return mob.legs == null ? mob.updateParts(model).legs : mob.legs;
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
    
    /**
     * @return saddle state from datawatcher
     */
    public boolean getSaddled()
    {
        return (dataWatcher.getWatchableObjectByte(25) & 1) != 0;
    }
    
    /**
     * updates dataWatcher with saddle state
     */
    public void setSaddled(boolean saddled)
    {
        if (saddled)
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
    
    /**
     * Used by the TileEntity to specify entity body parts
     */
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
    
    /**
     * builds an array of the current body part names
     */
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
    
    /**
     * builds an array of the bodypart instances
     */
    public BodyPart[][] getBodyParts()
    {
        BodyPart[][] list = { head, torso, armLeft, armRight, leg };
        return list;
    }
    
    /**
     * model instance getter
     */
    public ModelMinion getModel()
    {
        return model;
    }

    /**
     * model instance setter
     */
    public void setModel(ModelMinion model)
    {
        this.model = model;
    }
    
    /**
     * setter helper for body parts
     */
    public void setBodyParts(BodyPart[][] bodypart)
    {
        head = bodypart[0];
        torso = bodypart[1];
        armLeft = bodypart[2];
        armRight = bodypart[3];
        leg = bodypart[4];
        dataWatcherUpdate();
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return null;
    }

    @Override
    protected String getLivingSound()
    {
        return "mob." + getBodyPartsNames()[0] + ".say";
    }

    @Override
    protected String getHurtSound()
    {
        return "mob." + getBodyPartsNames()[0] + ".hurt";
    }

    @Override
    protected String getDeathSound()
    {
        return "mob." + getBodyPartsNames()[0] + ".death";
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        getOwner().getEntityData().setInteger("minions", getOwner().getEntityData().getInteger("minions") - 1);
    }

    @Override
    public EntityLivingBase getOwner()
    {
        return worldObj.getPlayerEntityByName(getOwnerName());
    }
}
