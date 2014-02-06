package com.sirolf2009.necromancy.item;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class ItemBucketBlood extends ItemBucket
{

    public ItemBucketBlood(Block b)
    {
        super(b);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (movingobjectposition == null)
            return item;
        else
        {
            FillBucketEvent event = new FillBucketEvent(player, item, world, movingobjectposition);
            if (MinecraftForge.EVENT_BUS.post(event))
                return item;

            if (event.getResult() == Result.ALLOW)
            {
                if (player.capabilities.isCreativeMode)
                    return item;

                if (--item.stackSize <= 0)
                    return event.result;

                if (!player.inventory.addItemStackToInventory(event.result))
                {
                    player.dropItem(event.result.getItem(), event.result.stackSize);
                }

                return item;
            }

            if (movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
            {
                int x = movingobjectposition.blockX;
                int y = movingobjectposition.blockY;
                int z = movingobjectposition.blockZ;

                if (!world.canMineBlock(player, x, y, z))
                    return item;

                if (movingobjectposition.sideHit == 0)
                {
                    --y;
                }

                if (movingobjectposition.sideHit == 1)
                {
                    ++y;
                }

                if (movingobjectposition.sideHit == 2)
                {
                    --z;
                }

                if (movingobjectposition.sideHit == 3)
                {
                    ++z;
                }

                if (movingobjectposition.sideHit == 4)
                {
                    --x;
                }

                if (movingobjectposition.sideHit == 5)
                {
                    ++x;
                }

                if (!player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, item))
                    return item;

                if (this.tryPlaceContainedLiquid(world, x, y, z) && !player.capabilities.isCreativeMode)
                    return new ItemStack(Items.bucket);

            }

            return item;
        }
    }

    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
        itemIcon = IIconRegister.registerIcon("necromancy:bucketblood");
    }

}
