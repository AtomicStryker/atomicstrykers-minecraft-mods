package atomicstryker.minions.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.minions.common.network.ForgePacketWrapper;
import atomicstryker.minions.common.network.PacketDispatcher;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
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

    public void sendSoundToClients(Entity ent, String string)
    {
        Object[] toSend = {ent.func_145782_y(), string};
        PacketDispatcher.sendToAllNear(ent.posX, ent.posY, ent.posZ, 15, ent.worldObj.provider.dimensionId, ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.SOUNDTOALL.ordinal(), toSend));
    }

    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }

    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }

	public void playSoundAtEntity(Entity ent, String sound, float volume, float pitch)
	{
		// NOOP
	}
}
