package com.sirolf2009.necromancy.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.entity.EntityNecromancy;

public class ItemSpawner extends Item
{

    public int[] entityIDs = { EntityNecromancy.IsaacID, EntityNecromancy.TeddyID };

    public ItemSpawner()
    {
        super();
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
    {
        if (par3World.isRemote)
            return true;
        else
        {
            Block i1 = par3World.getBlock(par4, par5, par6);
            par4 += Facing.offsetsXForSide[par7];
            par5 += Facing.offsetsYForSide[par7];
            par6 += Facing.offsetsZForSide[par7];
            double d0 = 0.0D;

            if (par7 == 1 && i1.getRenderType() == 11)
            {
                d0 = 0.5D;
            }

            Entity entity = spawnCreature(par3World, entityIDs[0], par4 + 0.5D, par5 + d0, par6 + 0.5D);

            if (entity != null)
            {
                if (!par2EntityPlayer.capabilities.isCreativeMode)
                {
                    --par1ItemStack.stackSize;
                }
            }

            return true;
        }
    }

    /**
     * Spawns the creature specified by the egg's type in the location specified
     * by the last three parameters. Parameters: world, entityID, x, y, z.
     */
    public static Entity spawnCreature(World par0World, int par1, double par2, double par4, double par6)
    {
        Entity entity = null;

        for (int j = 0; j < 1; ++j)
        {
            entity = EntityList.createEntityByID(par1, par0World);

            if (entity != null && entity instanceof EntityLiving)
            {
                EntityLiving entityliving = (EntityLiving) entity;
                entity.setLocationAndAngles(par2, par4, par6, MathHelper.wrapAngleTo180_float(par0World.rand.nextFloat() * 360.0F), 0.0F);
                entityliving.rotationYawHead = entityliving.rotationYaw;
                entityliving.renderYawOffset = entityliving.rotationYaw;
                entityliving.onSpawnWithEgg(null);
                par0World.spawnEntityInWorld(entity);
                entityliving.playLivingSound();
            }
        }

        return entity;
    }

    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
        itemIcon = IIconRegister.registerIcon("necromancy:soulheart");
    }

}
