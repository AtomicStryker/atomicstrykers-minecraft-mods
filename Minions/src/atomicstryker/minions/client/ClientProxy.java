package atomicstryker.minions.client;

import java.util.EnumSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.minions.common.CommonProxy;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinionsClient.preInit(event);
        MinecraftForge.EVENT_BUS.register(new MinionsSounds());
    }
    
    @Override
    public void load(FMLInitializationEvent evt)
    {
        TickRegistry.registerTickHandler(new ClientRenderTickHandler(), Side.CLIENT);
        TickRegistry.registerTickHandler(new ClientWorldTickHandler(), Side.CLIENT);
        NetworkRegistry.instance().registerChannel(new ClientPacketHandler(), MinionsCore.getPacketChannel(), Side.CLIENT);
        
        MinecraftForge.EVENT_BUS.register(new RenderChickenLightningBolt());
    }
    
    @Override
    public void registerRenderInformation()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityMinion.class, new RenderMinion(new ModelMinion(), 0.25F));
        RenderingRegistry.registerEntityRenderingHandler(RenderEntLahwran_Minions.class, new MinionsRenderHook(FMLClientHandler.instance().getClient()));
    }
    
    @Override
    public boolean hasPlayerMinions(EntityPlayer player)
    {
        return MinionsClient.hasMinionsSMPOverride || !MinionsCore.getMinionsForMaster(player.username).isEmpty();
    }
    
    @Override
    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.onMastersGloveRightClickHeld(itemstack, world, player);
    }

    @Override
    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.onMastersGloveRightClick(itemstack, world, player);
    }
    
    @Override
	public void playSoundAtEntity(Entity ent, String sound, float volume, float pitch)
	{
		FMLClientHandler.instance().getClient().theWorld.playSound(ent.posX, ent.posY, ent.posZ, sound, volume, pitch, false);
	}
    
    public class ClientRenderTickHandler implements ITickHandler
    {
        private final EnumSet<TickType> tickTypes = EnumSet.of(TickType.RENDER);
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            // NOOP
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            MinionsClient.onRenderTick(tickData);
        }
        
        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

        @Override
        public String getLabel()
        {
            return "MinionsTickRender";
        }
    }
    
    public class ClientWorldTickHandler implements ITickHandler
    {
        private final EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            // NOOP
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            MinionsClient.onWorldTick(tickData);
        }
        
        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

        @Override
        public String getLabel()
        {
            return "MinionsTickClient";
        }
    }
    
    public class ClientPacketHandler implements IPacketHandler
    {
        @Override
        public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
        {
            MinionsClient.onPacketData(manager, packet, player);
        }
    }
}
