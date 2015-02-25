package atomicstryker.minions.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy
{
    public void preInit(FMLPreInitializationEvent event);

    public void load(FMLInitializationEvent evt);

    public void registerRenderInformation();

    public boolean hasPlayerMinions(EntityPlayer player);

    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player);

    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player);
}
