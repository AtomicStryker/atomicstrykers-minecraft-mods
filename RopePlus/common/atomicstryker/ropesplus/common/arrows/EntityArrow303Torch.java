// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;


// Referenced classes of package net.minecraft.src:
//            EntityArrow303, Block, ItemStack, Entity, 
//            World, EntityLiving

public class EntityArrow303Torch extends EntityArrow303
{

    public void entityInit()
    {
        super.entityInit();
        name = "TorchArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowTorch;
        tip = Block.torchWood;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 11;
    }

    public EntityArrow303Torch(World world)
    {
        super(world);
    }

    public EntityArrow303Torch(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHit()
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, 50))
        {
        	setDead();
        }
        return true;
    }

    public boolean onHitTarget(Entity entity)
    {
    	entity.setFire(300/20);
        return true;
    }
}
