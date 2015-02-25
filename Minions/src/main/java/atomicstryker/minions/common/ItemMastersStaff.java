package atomicstryker.minions.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

/**
 * Minion control Item class. Nothing to see here really.
 * 
 * 
 * @author AtomicStryker
 */

public class ItemMastersStaff extends Item
{

    public ItemMastersStaff()
    {
        super();
        this.maxStackSize = 1;
        
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int ticksHeld)
    {
        if (world.isRemote)
        {
            int ticksLeftFromMax = this.getMaxItemUseDuration(itemstack) - ticksHeld;
            float pointStrength = (float) ticksLeftFromMax / 20.0F;
            pointStrength = (pointStrength * pointStrength + pointStrength * 2.0F) / 3.0F;

            if (pointStrength > 1.0F)
            {
                // full power!
                MinionsCore.proxy.onMastersGloveRightClickHeld(itemstack, world, player);
            }
            else
            {
                // shorter tap
                MinionsCore.proxy.onMastersGloveRightClick(itemstack, world, player);
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack var1)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack var1)
    {
        return EnumAction.BLOCK;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
        
        if (world.isRemote)
        {
            //PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), null));
        }
        
        return itemStack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.RED + super.getItemStackDisplayName(itemStack);
    }
}
