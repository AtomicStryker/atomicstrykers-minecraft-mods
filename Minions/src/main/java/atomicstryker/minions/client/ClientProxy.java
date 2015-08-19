package atomicstryker.minions.client;

import atomicstryker.minions.common.IProxy;
import atomicstryker.minions.common.entity.EntityMinion;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy implements IProxy
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
    public void onWorldTick(TickEvent.PlayerTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            client.onPlayerTick(tick.player.worldObj);
        }
    }
    
    @Override
    public void registerRenderInformation()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityMinion.class, new RenderMinion(new ModelMinion(), 0.25F));
    }

    @Override
    public void onMastersGloveRightClickHeld(ItemStack itemstack, World world, EntityPlayer player)
    {
        MinionsClient.onMastersGloveRightClickHeld(player);
    }

    @Override
    public void onMastersGloveRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        client.onMastersGloveRightClick(world, player);
    }
}
