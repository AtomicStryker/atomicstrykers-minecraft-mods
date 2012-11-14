package atomicstryker.minions.client;

import java.util.EnumSet;
import java.util.logging.Level;

import atomicstryker.minions.common.CommonProxy;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
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

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinionsClient.preInit(event);
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
        
        MinecraftForgeClient.preloadTexture("/atomicstryker/minions/client/textures/AS_EntityMinion.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/minions/client/textures/masterstaff.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/minions/client/textures/codechicken/lightning_outer.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/minions/client/textures/codechicken/lightning_inner.png");
    }
    
    @Override
    public boolean hasPlayerMinions(EntityPlayer player)
    {
        return MinionsClient.hasMinionsSMPOverride || (MinionsCore.masterNames.get(player.username) != null);
    }
    
    @Override
    public void OnMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.OnMastersGloveRightClickHeld(itemstack, world, player);
    }

    @Override
    public void OnMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.OnMastersGloveRightClick(itemstack, world, player);
    }
    
    @Override
	public void playSoundAtEntity(Entity ent, String sound, float volume, float pitch)
	{
		FMLClientHandler.instance().getClient().theWorld.playSound(ent.posX, ent.posY, ent.posZ, sound, volume, pitch);
	}
    
    public class ClientRenderTickHandler implements ITickHandler
    {
        private final EnumSet tickTypes = EnumSet.of(TickType.RENDER);
        
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
        private final EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
        
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
