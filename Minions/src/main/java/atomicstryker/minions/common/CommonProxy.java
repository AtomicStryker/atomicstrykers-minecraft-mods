package atomicstryker.minions.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
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
        MinecraftServer.getServer().getConfigurationManager()
                .sendToAllNear(ent.posX, ent.posY, ent.posZ, 16D, ent.dimension, new S29PacketSoundEffect(string, ent.posX, ent.posY, ent.posZ, 1f, 1f));
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
