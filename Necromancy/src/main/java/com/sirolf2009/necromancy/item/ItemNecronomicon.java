package com.sirolf2009.necromancy.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.achievement.AchievementNecromancy;
import com.sirolf2009.necromancy.block.BlockNecromancy;

public class ItemNecronomicon extends Item
{

    public ItemNecronomicon()
    {
        super();
        setCreativeTab(Necromancy.tabNecromancy);
        setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
    }

    @Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int xPos, int yPos, int zPos, int par7, float par8, float par9, float par10)
    {
        if (par3World.getBlock(xPos, yPos, zPos) == Blocks.planks)
        {
            if (par3World.getBlock(xPos + 1, yPos, zPos) == Blocks.cobblestone && par3World.getBlock(xPos + 2, yPos, zPos) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, BlockNecromancy.altar, 3, 0);
                par3World.setBlock(xPos + 1, yPos, zPos, BlockNecromancy.altarBlock, 3, 0);
                par3World.setBlock(xPos + 2, yPos, zPos, BlockNecromancy.altarBlock, 3, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                return true;
            }
            if (par3World.getBlock(xPos - 1, yPos, zPos) == Blocks.cobblestone && par3World.getBlock(xPos - 2, yPos, zPos) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, BlockNecromancy.altar, 1, 0);
                par3World.setBlock(xPos - 1, yPos, zPos, BlockNecromancy.altarBlock, 1, 0);
                par3World.setBlock(xPos - 2, yPos, zPos, BlockNecromancy.altarBlock, 1, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                return true;
            }
            if (par3World.getBlock(xPos, yPos, zPos + 1) == Blocks.cobblestone && par3World.getBlock(xPos, yPos, zPos + 2) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, BlockNecromancy.altar, 0, 0);
                par3World.setBlock(xPos, yPos, zPos + 1, BlockNecromancy.altarBlock, 0, 0);
                par3World.setBlock(xPos, yPos, zPos + 2, BlockNecromancy.altarBlock, 0, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                return true;
            }
            if (par3World.getBlock(xPos, yPos, zPos - 1) == Blocks.cobblestone && par3World.getBlock(xPos, yPos, zPos - 2) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, BlockNecromancy.altar, 2, 0);
                par3World.setBlock(xPos, yPos, zPos - 1, BlockNecromancy.altarBlock, 2, 0);
                par3World.setBlock(xPos, yPos, zPos - 2, BlockNecromancy.altarBlock, 2, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    public double page = 0;
}
