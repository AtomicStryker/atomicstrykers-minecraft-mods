package atomicstryker.minions.common;

import java.util.EnumSet;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.minions.common.entity.EntityMinion;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

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
        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendToAllNear(ent.posX, ent.posY, ent.posZ, 15, ent.worldObj.getWorldInfo().getDimension(), ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.SOUNDTOALL.ordinal(), toSend));
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
