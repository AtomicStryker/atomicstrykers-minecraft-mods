package atomicstryker.minions.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        // NOOP
    }

    public void load(FMLInitializationEvent evt)
    {
        // NOOP
    }

    public void registerRenderInformation()
    {
        // NOOP
    }

    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }

    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }
}
