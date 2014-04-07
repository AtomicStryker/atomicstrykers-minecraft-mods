package com.sirolf2009.necromancy.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.achievement.AchievementNecromancy;
import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;

public class ItemNecronomicon extends Item
{
    
    public ItemNecronomicon()
    {
        super();
        setCreativeTab(Necromancy.tabNecromancy);
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int xPos, int yPos, int zPos, int par7, float par8, float par9, float par10)
    {
        if (par3World.getBlock(xPos, yPos, zPos) == Blocks.planks)
        {
            if (par3World.getBlock(xPos + 1, yPos, zPos) == Blocks.cobblestone && par3World.getBlock(xPos + 2, yPos, zPos) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, RegistryBlocksNecromancy.altar, 3, 0);
                par3World.setBlock(xPos + 1, yPos, zPos, RegistryBlocksNecromancy.altarBlock, 3, 0);
                par3World.setBlock(xPos + 2, yPos, zPos, RegistryBlocksNecromancy.altarBlock, 3, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                --par1ItemStack.stackSize;
                return true;
            }
            if (par3World.getBlock(xPos - 1, yPos, zPos) == Blocks.cobblestone && par3World.getBlock(xPos - 2, yPos, zPos) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, RegistryBlocksNecromancy.altar, 1, 0);
                par3World.setBlock(xPos - 1, yPos, zPos, RegistryBlocksNecromancy.altarBlock, 1, 0);
                par3World.setBlock(xPos - 2, yPos, zPos, RegistryBlocksNecromancy.altarBlock, 1, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                --par1ItemStack.stackSize;
                return true;
            }
            if (par3World.getBlock(xPos, yPos, zPos + 1) == Blocks.cobblestone && par3World.getBlock(xPos, yPos, zPos + 2) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, RegistryBlocksNecromancy.altar, 0, 0);
                par3World.setBlock(xPos, yPos, zPos + 1, RegistryBlocksNecromancy.altarBlock, 0, 0);
                par3World.setBlock(xPos, yPos, zPos + 2, RegistryBlocksNecromancy.altarBlock, 0, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                --par1ItemStack.stackSize;
                return true;
            }
            if (par3World.getBlock(xPos, yPos, zPos - 1) == Blocks.cobblestone && par3World.getBlock(xPos, yPos, zPos - 2) == Blocks.cobblestone)
            {
                par3World.setBlock(xPos, yPos, zPos, RegistryBlocksNecromancy.altar, 2, 0);
                par3World.setBlock(xPos, yPos, zPos - 1, RegistryBlocksNecromancy.altarBlock, 2, 0);
                par3World.setBlock(xPos, yPos, zPos - 2, RegistryBlocksNecromancy.altarBlock, 2, 0);
                par2EntityPlayer.addStat(AchievementNecromancy.AltarAchieve, 1);
                --par1ItemStack.stackSize;
                return true;
            }
        }

        return false;
    }
    
}
