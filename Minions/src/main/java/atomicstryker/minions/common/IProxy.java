package atomicstryker.minions.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IProxy
{
    void preInit(FMLPreInitializationEvent event);

    void load(FMLInitializationEvent evt);

    void registerRenderInformation();

    void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player);

    void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player);
}
