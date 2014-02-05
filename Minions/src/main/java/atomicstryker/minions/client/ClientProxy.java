package atomicstryker.minions.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.minions.common.CommonProxy;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.PacketDispatcher.IPacketHandler;
import atomicstryker.minions.common.network.PacketDispatcher.WrappedPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ClientProxy extends CommonProxy
{
    
    private static MinionsClient client;
    
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        client =  new MinionsClient();
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(new RenderChickenLightningBolt());
        MinecraftForge.EVENT_BUS.register(client);
    }
    
    @Override
    public void load(FMLInitializationEvent evt)
    {
        
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            client.onRenderTick(tick.renderTickTime);
        }
    }
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            client.onWorldTick(tick.world);
        }
    }
    
    @Override
    public void registerRenderInformation()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityMinion.class, new RenderMinion(new ModelMinion(), 0.25F));
    }
    
    @Override
    public boolean hasPlayerMinions(EntityPlayer player)
    {
        return MinionsClient.hasMinionsSMPOverride;
    }
    
    @Override
    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.onMastersGloveRightClickHeld(itemstack, world, player);
    }

    @Override
    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        client.onMastersGloveRightClick(itemstack, world, player);
    }
    
    @Override
	public void playSoundAtEntity(Entity ent, String sound, float volume, float pitch)
	{
		FMLClientHandler.instance().getClient().theWorld.playSound(ent.posX, ent.posY, ent.posZ, sound, volume, pitch, false);
	}
    
    public static class ClientPacketHandler implements IPacketHandler
    {
        @Override
        public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
        {
            client.onPacketData(packetType, packet, player);
        }
    }
}
