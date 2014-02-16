package com.sirolf2009.necromancy.tileentity;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;

import org.apache.logging.log4j.Level;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.achievement.AchievementNecromancy;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.item.ItemGeneric;

import cpw.mods.fml.common.FMLLog;

public class TileEntityAltar extends TileEntity implements IInventory
{

    private ItemStack altarItemStacks[] = new ItemStack[7];
    private final EntityMinion minion;
    private ItemStack bodyPartsNew[];
    private ItemStack bodyPartsOld[];

    public TileEntityAltar()
    {
        minion = new EntityMinion(worldObj);
        bodyPartsNew = new ItemStack[5];
        bodyPartsOld = new ItemStack[5];
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    // altar stays rendered when the TE isnt onscreen. thanks AbrarSyed
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    /**
     * Called by BlockAltar
     */
    public void spawn(EntityPlayer user)
    {
        if (!worldObj.isRemote)
        {
            if (Necromancy.instance.maxSpawn != -1 && user.getEntityData().getInteger("minions") >= Necromancy.instance.maxSpawn)
            {
                user.addChatMessage(new ChatComponentText("<Death> Mortal fool! Thou shan't never grow that strong."));
                Entity thunder = new EntityLightningBolt(worldObj, xCoord, yCoord, zCoord);
                worldObj.spawnEntityInWorld(thunder);
            }
            else
            {
                BodyPart[][] types = new BodyPart[5][];
                ItemStack head = getStackInSlot(2);
                ItemStack body = getStackInSlot(3);
                ItemStack leg = getStackInSlot(4);
                ItemStack armRight = getStackInSlot(5);
                ItemStack armLeft = getStackInSlot(6);
                if (head != null && head.getItem() != null)
                {
                    types[0] = getBodyPart(head, false);
                }
                else
                {
                    types[0] = new BodyPart[] {};
                }
                if (body != null)
                {
                    types[1] = getBodyPart(body, false);
                }
                else
                {
                    types[1] = new BodyPart[] {};
                }
                if (armLeft != null)
                {
                    types[2] = getBodyPart(armLeft, false);
                }
                else
                {
                    types[2] = new BodyPart[] {};
                }
                if (armRight != null)
                {
                    types[3] = getBodyPart(armRight, true);
                }
                else
                {
                    types[3] = new BodyPart[] {};
                }
                if (leg != null)
                {
                    types[4] = getBodyPart(leg, false);
                }
                else
                {
                    types[4] = new BodyPart[] {};
                }
                EntityMinion minionSpawned = new EntityMinion(worldObj, types, user.getCommandSenderName());
                minionSpawned.setPosition(xCoord, yCoord + 1, zCoord);
                minionSpawned.calculateAttributes();
                worldObj.spawnEntityInWorld(minionSpawned);
                Necromancy.loggerNecromancy.info(minionSpawned.toString());
                user.addStat(AchievementNecromancy.SpawnAchieve, 1);
                user.addChatMessage(new ChatComponentText("<Minion> Thy command?"));
                minionSpawned.dataWatcherUpdate();
                minionSpawned.getModel().updateModel(minionSpawned, true);
                if (!user.capabilities.isCreativeMode)
                {
                    for (int x = 0; x < 7; x++)
                    {
                        decrStackSize(x, 1);
                    }
                }
                user.getEntityData().setInteger("minions", user.getEntityData().getInteger("minions") + 1);
                bodyPartsOld = null;
                user.addStat(AchievementNecromancy.SpawnAchieve, 1);
                FMLLog.getLogger().log(Level.INFO, TileEntityAltar.class + "    " + minionSpawned);
            }
        }
    }

    /**
     * called by BlockAltar
     */
    public boolean canSpawn()
    {
        if (getStackInSlot(0) == null || getStackInSlot(0).getItem() != ItemGeneric.getItemStackFromName("Jar of Blood").getItem())
            return false;
        if (getStackInSlot(1) == null || !soulCheck())
            return false;
        return true;
    }

    private BodyPart[] getBodyPart(ItemStack stack, boolean isRightArm)
    {
        Iterator<NecroEntityBase> itr = NecroEntityRegistry.registeredEntities.values().iterator();
        while (itr.hasNext())
        {
            NecroEntityBase mob = itr.next();
            if (mob.headItem != null && stack.isItemEqual(mob.headItem))
                return mob.head == null ? mob.updateParts(minion.getModel()).head : mob.head;
            if (mob.torsoItem != null && stack.isItemEqual(mob.torsoItem))
                return mob.torso == null ? mob.updateParts(minion.getModel()).torso : mob.torso;
            if (mob.armItem != null && stack.isItemEqual(mob.armItem))
                if (isRightArm)
                    return mob.armRight == null ? mob.updateParts(minion.getModel()).armRight : mob.armRight;
                else
                    return mob.armLeft == null ? mob.updateParts(minion.getModel()).armLeft : mob.armLeft;
            if (mob.legItem != null && stack.isItemEqual(mob.legItem))
                return mob.legs == null ? mob.updateParts(minion.getModel()).legs : mob.legs;
        }
        return null;
    }

    /**
     * called by TileEntityAltarRenderer
     */
    public boolean hasAltarChanged()
    {
        bodyPartsNew[0] = getStackInSlot(2);
        bodyPartsNew[1] = getStackInSlot(3);
        bodyPartsNew[2] = getStackInSlot(4);
        bodyPartsNew[3] = getStackInSlot(5);
        bodyPartsNew[4] = getStackInSlot(6);

        for (int i = 0; i < 5; i++)
            if (bodyPartsNew[i] != null && bodyPartsOld[i] != null)
            {
                if (!bodyPartsNew[i].isItemEqual(bodyPartsOld[i]))
                {
                    bodyPartsOld = bodyPartsNew.clone();
                    return true;
                }
            }
            else if (bodyPartsNew[i] != null || bodyPartsOld[i] != null && !(bodyPartsNew[i] == null && bodyPartsOld[i] == null))
            {
                bodyPartsOld = bodyPartsNew.clone();
                return true;
            }

        bodyPartsOld = bodyPartsNew.clone();
        return false;
    }

    /**
     * builds the current Entity from the pieces
     */
    public EntityMinion getPreviewEntity()
    {
        if (hasAltarChanged())
        {
            ItemStack head = getStackInSlot(2);
            ItemStack body = getStackInSlot(3);
            ItemStack leg = getStackInSlot(4);
            ItemStack armRight = getStackInSlot(5);
            ItemStack armLeft = getStackInSlot(6);
            if (head != null && head.getItem() != null && isLegalCombo("head", head))
            {
                minion.setBodyPart(BodyPartLocation.Head, getBodyPart(head, false));
            }
            else
            {
                minion.setBodyPart(BodyPartLocation.Head, new BodyPart[] {});
            }
            if (body != null && isLegalCombo("body", body))
            {
                minion.setBodyPart(BodyPartLocation.Torso, getBodyPart(body, false));
            }
            else
            {
                minion.setBodyPart(BodyPartLocation.Torso, new BodyPart[] {});
            }
            if (leg != null && isLegalCombo("leg", leg))
            {
                minion.setBodyPart(BodyPartLocation.Legs, getBodyPart(leg, false));
            }
            else
            {
                minion.setBodyPart(BodyPartLocation.Legs, new BodyPart[] {});
            }
            if (armLeft != null && isLegalCombo("arm", armLeft))
            {
                minion.setBodyPart(BodyPartLocation.ArmLeft, getBodyPart(armLeft, false));
            }
            else
            {
                minion.setBodyPart(BodyPartLocation.ArmLeft, new BodyPart[] {});
            }
            if (armRight != null && isLegalCombo("arm", armRight))
            {
                minion.setBodyPart(BodyPartLocation.ArmRight, getBodyPart(armRight, true));
            }
            else
            {
                minion.setBodyPart(BodyPartLocation.ArmRight, new BodyPart[] {});
            }
        }
        return minion;
    }

    private boolean isLegalCombo(String location, ItemStack stack)
    {
        Iterator<NecroEntityBase> itr = NecroEntityRegistry.registeredEntities.values().iterator();
        while (itr.hasNext() && stack != null)
        {
            NecroEntityBase mob = itr.next();
            if (location.equals("head") && mob.hasHead && stack.isItemEqual(mob.headItem))
                return true;
            if (location.equals("body") && mob.hasTorso && stack.isItemEqual(mob.torsoItem))
                return true;
            if (location.equals("arm") && mob.hasArms && stack.isItemEqual(mob.armItem))
                return true;
            if (location.equals("leg") && mob.hasLegs && stack.isItemEqual(mob.legItem))
                return true;
        }
        return false;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        NBTTagList var2 = par1NBTTagCompound.getTagList("Items", 10);
        altarItemStacks = new ItemStack[getSizeInventory()];
        for (int var3 = 0; var3 < var2.tagCount(); var3++)
        {
            NBTTagCompound var4 = (NBTTagCompound) var2.getCompoundTagAt(var3);
            byte var5 = var4.getByte("Slot");
            if (var5 >= 0 && var5 < altarItemStacks.length)
            {
                altarItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
        getPreviewEntity();
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        NBTTagList var2 = new NBTTagList();
        for (int var3 = 0; var3 < altarItemStacks.length; var3++)
        {
            if (altarItemStacks[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte) var3);
                altarItemStacks[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }
        par1NBTTagCompound.setTag("Items", var2);
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2)
    {
        if (altarItemStacks[var1] != null)
        {
            ItemStack var3;
            if (altarItemStacks[var1].stackSize <= var2)
            {
                var3 = altarItemStacks[var1];
                altarItemStacks[var1] = null;
                return var3;
            }
            var3 = altarItemStacks[var1].splitStack(var2);
            if (altarItemStacks[var1].stackSize == 0)
            {
                altarItemStacks[var1] = null;
            }
            return var3;
        }
        else
            return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1)
    {
        if (altarItemStacks[var1] != null)
        {
            ItemStack var2 = altarItemStacks[var1];
            altarItemStacks[var1] = null;
            return var2;
        }
        else
            return null;
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2)
    {
        altarItemStacks[var1] = var2;
        if (var2 != null && var2.stackSize > getInventoryStackLimit())
        {
            var2.stackSize = getInventoryStackLimit();
        }
    }
    
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbttagcompound);
    }

    @Override
    public String getInventoryName()
    {
        return "Altar";
    }

    @Override
    public int getSizeInventory()
    {
        return altarItemStacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int var1)
    {
        return altarItemStacks[var1];
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    private boolean soulCheck()
    {
        return getStackInSlot(1).getItem() == ItemGeneric.getItemStackFromName("Soul in a Jar").getItem();
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return false;
    }
}
