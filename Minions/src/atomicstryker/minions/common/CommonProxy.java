package atomicstryker.minions.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.FMLCommonHandler;
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
        return (MinionsCore.masterNames.get(player.username) != null);
    }

    public void sendSoundToClients(Entity ent, String string)
    {
        Object[] toSend = {ent.entityId, string};
        try
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendToAllNear(ent.posX, ent.posY, ent.posZ, 15, ent.worldObj.provider.dimensionId, ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.SOUNDTOALL.ordinal(), toSend));
        }
        catch (Exception e){}
    }

    public void OnMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }

    public void OnMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        // NOOP
    }

	public void playSoundAtEntity(Entity ent, String sound, float volume, float pitch)
	{
		// NOOP
	}
}