package com.sirolf2009.necromancy.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ItemBucketBlood extends ItemBucket
{
    
    private Block blood;

    public ItemBucketBlood(Block b)
    {
        super(b);
        blood = b;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event)
    {
        Block target = event.world.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ);
        System.out.println("Necromancy onFillBucket, target "+target);
        if (target == blood)
        {
            event.world.setBlock(event.target.blockX, event.target.blockY, event.target.blockZ, Blocks.air);
            event.result = new ItemStack(this);
            event.setResult(Result.ALLOW);
        }
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("necromancy:bucketblood");
    }

}
