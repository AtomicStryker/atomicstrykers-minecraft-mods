package com.sirolf2009.necromancy.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBucket;

public class ItemBucketBlood extends ItemBucket
{
    public ItemBucketBlood(Block b)
    {
        super(b);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("necromancy:bucketblood");
    }
}
