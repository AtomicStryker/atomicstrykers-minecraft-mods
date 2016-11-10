package atomicstryker.minions.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy
{
    void preInit(FMLPreInitializationEvent event);

    void load(FMLInitializationEvent evt);

    void registerRenderInformation();

    boolean hasPlayerMinions(EntityPlayer player);

    void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player);

    void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player);
}
