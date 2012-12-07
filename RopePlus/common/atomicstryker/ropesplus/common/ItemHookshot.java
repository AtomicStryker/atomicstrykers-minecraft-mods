package atomicstryker.ropesplus.common;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumAction;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ItemHookshot extends Item
{

    public ItemHookshot(int i)
    {
        super(i);
        maxStackSize = 1;
        setTextureFile("/atomicstryker/ropesplus/client/ropesPlusItems.png");
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
    
    /**
     * called when the player releases the use item button.
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int heldTicks)
    {
        int ticksLeftToCharge = getMaxItemUseDuration(itemstack) - heldTicks;
        float chargeRatio = (float)ticksLeftToCharge / 20.0F;
        
        boolean hasAmmo = entityplayer.inventory.hasItem(RopesPlusCore.itemHookShotCartridge.shiftedIndex);
        
        if (chargeRatio < 0.5)
        {
            if(world.isRemote)
            {
                if (!RopesPlusCore.proxy.getHasClientRopeOut() && hasAmmo)
                {
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
                }
                else
                {
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
                }
                
                RopesPlusCore.proxy.setShouldHookShotPull(false);
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
                        double guessY = entityplayer.prevPosY + (entityplayer.posY - entityplayer.prevPosY) + 1.62D - (double)entityplayer.yOffset;
                        double guessZ = entityplayer.prevPosZ + (entityplayer.posZ - entityplayer.prevPosZ);
                        Vec3 playerVec = world.getWorldVec3Pool().getVecFromPool(guessX, guessY, guessZ);
                        float yawCos = MathHelper.cos(-guessYaw * 0.017453292F - (float)Math.PI);
                        float yawSin = MathHelper.sin(-guessYaw * 0.017453292F - (float)Math.PI);
                        float rotCos = -MathHelper.cos(-guessRot * 0.017453292F);
                        float viewY = MathHelper.sin(-guessRot * 0.017453292F);
                        float viewX = yawSin * rotCos;
                        float viewZ = yawCos * rotCos;
                        double traceDistance = Settings_RopePlus.maxHookShotRopeLength;
                        Vec3 aimVec = playerVec.addVector((double)viewX * traceDistance, (double)viewY * traceDistance, (double)viewZ * traceDistance);
                        MovingObjectPosition target = world.rayTraceBlocks_do_do(playerVec, aimVec, false, false);
                        
                        if (target != null)
                        {
                            if (target.typeOfHit == EnumMovingObjectType.TILE)
                            {
                                EntityFreeFormRope ropeEnt = new EntityFreeFormRope(world);
                                ropeEnt.setStartCoordinates(entityplayer.posX, entityplayer.posY+0.5D, entityplayer.posZ);
                                ropeEnt.setEndCoordinates(entityplayer.posX, entityplayer.posY+0.5D, entityplayer.posZ);
                                ropeEnt.setEndBlock(target.blockX, target.blockY, target.blockZ);
                                ropeEnt.setShooter(entityplayer);
                                world.spawnEntityInWorld(ropeEnt);
                                
                                RopesPlusCore.instance.setPlayerRope(entityplayer, ropeEnt);
                                
                                Object[] toSend = {ropeEnt.entityId, target.blockX, target.blockY, target.blockZ};
                                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 4, toSend), (Player) entityplayer);
                                world.playSoundAtEntity(entityplayer, "hookshotfire", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
                                entityplayer.inventory.consumeInventoryItem(RopesPlusCore.itemHookShotCartridge.shiftedIndex);
                            }
                        }
                        else
                        {
                            RopesPlusCore.instance.setPlayerRope(entityplayer, null);
                            entityplayer.sendChatToPlayer("No target for Hookshot");
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
        else if (!world.isRemote
                && RopesPlusCore.instance.getPlayerRope(entityplayer) != null
                && RopesPlusCore.instance.getPlayerRope(entityplayer).isEntityAlive())
        {
            // activate hook pull on clientside
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 5, null), (Player) entityplayer);
            world.playSoundAtEntity(entityplayer, "hookshotpull", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        entityplayer.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
        return itemstack;
    }
    
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }
    
}
