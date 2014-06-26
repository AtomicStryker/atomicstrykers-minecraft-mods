package atomicstryker.ropesplus.common;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.network.HookshotPacket;
import atomicstryker.ropesplus.common.network.HookshotPullPacket;

public class ItemHookshot extends Item
{

    public ItemHookshot()
    {
        super();
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("ropesplus:hookshot");
    }

    @Override
    public boolean isFull3D()
    {
        return true;
    }

    @Override
    public boolean shouldRotateAroundWhenRendering()
    {
        return true;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int heldTicks)
    {
        int ticksLeftToCharge = getMaxItemUseDuration(itemstack) - heldTicks;
        float chargeRatio = (float) ticksLeftToCharge / 20.0F;

        boolean hasAmmo = entityplayer.inventory.hasItem(RopesPlusCore.instance.itemHookShotCartridge) || entityplayer.capabilities.isCreativeMode;

        if (chargeRatio < 0.5)
        {
            if (world.isRemote)
            {
                if (!RopesPlusCore.proxy.getHasClientRopeOut() && hasAmmo)
                {
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
                }
                else
                {
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
                }

                RopesPlusCore.proxy.setShouldRopeChangeState(0f);
                entityplayer.swingItem();
            }
            else
            {
                if (RopesPlusCore.instance.getPlayerRope(entityplayer) == null)
                {
                    if (hasAmmo)
                    {
                        float guessRot = entityplayer.prevRotationPitch + (entityplayer.rotationPitch - entityplayer.prevRotationPitch);
                        float guessYaw = entityplayer.prevRotationYaw + (entityplayer.rotationYaw - entityplayer.prevRotationYaw);
                        double guessX = entityplayer.prevPosX + (entityplayer.posX - entityplayer.prevPosX);
                        double guessY = entityplayer.prevPosY + (entityplayer.posY - entityplayer.prevPosY) + 1.62D - (double) entityplayer.yOffset;
                        double guessZ = entityplayer.prevPosZ + (entityplayer.posZ - entityplayer.prevPosZ);
                        final Vec3 playerVec = Vec3.createVectorHelper(guessX, guessY, guessZ);
                        float yawCos = MathHelper.cos(-guessYaw * 0.017453292F - (float) Math.PI);
                        float yawSin = MathHelper.sin(-guessYaw * 0.017453292F - (float) Math.PI);
                        float rotCos = -MathHelper.cos(-guessRot * 0.017453292F);
                        float viewY = MathHelper.sin(-guessRot * 0.017453292F);
                        float viewX = yawSin * rotCos;
                        float viewZ = yawCos * rotCos;
                        double traceDistance = Settings_RopePlus.maxHookShotRopeLength;
                        final Vec3 aimVec =
                                playerVec.addVector((double) viewX * traceDistance, (double) viewY * traceDistance, (double) viewZ * traceDistance);
                        final MovingObjectPosition target = world.func_147447_a(playerVec, aimVec, false, false, false);

                        if (target != null)
                        {
                            if (target.typeOfHit == MovingObjectType.BLOCK)
                            {
                                EntityFreeFormRope ropeEnt = new EntityFreeFormRope(world);
                                ropeEnt.setStartCoordinates(entityplayer.posX, entityplayer.posY + 0.5D, entityplayer.posZ);
                                ropeEnt.setEndCoordinates(entityplayer.posX, entityplayer.posY + 0.5D, entityplayer.posZ);
                                ropeEnt.setEndBlock(target.blockX, target.blockY, target.blockZ);
                                ropeEnt.setShooter(entityplayer);
                                world.spawnEntityInWorld(ropeEnt);

                                RopesPlusCore.instance.setPlayerRope(entityplayer, ropeEnt);
                                RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new HookshotPacket(ropeEnt.getEntityId(), target.blockX,
                                        target.blockY, target.blockZ), (EntityPlayerMP) entityplayer);

                                entityplayer.worldObj.playSoundAtEntity(entityplayer, "ropesplus:hookshotfire", 1.0F,
                                        1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));

                                if (!entityplayer.capabilities.isCreativeMode)
                                {
                                    entityplayer.inventory.consumeInventoryItem(RopesPlusCore.instance.itemHookShotCartridge);
                                }
                            }
                        }
                        else
                        {
                            RopesPlusCore.instance.setPlayerRope(entityplayer, null);
                            entityplayer.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal("hookshotnotarget")));
                        }
                    }
                    else
                    {
                        world.playSoundAtEntity(entityplayer, "random.click", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
                    }
                }
                else
                {
                    if (RopesPlusCore.instance.getPlayerRope(entityplayer) != null)
                    {
                        RopesPlusCore.instance.getPlayerRope(entityplayer).setDead();
                    }
                    RopesPlusCore.instance.setPlayerRope(entityplayer, null);
                    world.playSoundAtEntity(entityplayer, "random.bowhit", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
                }
                entityplayer.swingItem();
            }
        }
        else if (!world.isRemote && RopesPlusCore.instance.getPlayerRope(entityplayer) != null
                && RopesPlusCore.instance.getPlayerRope(entityplayer).isEntityAlive())
        {
            // activate hook pull on clientside
            RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new HookshotPullPacket("server", -1), (EntityPlayerMP) entityplayer);
            world.playSoundAtEntity(entityplayer, "ropesplus:hookshotpull", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        entityplayer.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
        return itemstack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.GOLD + super.getItemStackDisplayName(itemStack);
    }

}
