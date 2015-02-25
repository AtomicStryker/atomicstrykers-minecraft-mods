package atomicstryker.minions.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

    public boolean hasPlayerMinions(EntityPlayer player)
    {
        return MinionsCore.instance.getMinionsForMaster(player).length > 0;
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
