package atomicstryker.ropesplus.client;

import net.minecraft.client.renderer.entity.Render;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.EntityGrapplingHook;
import atomicstryker.ropesplus.common.IProxy;
import atomicstryker.ropesplus.common.RopesPlusCore;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy implements IProxy
{
    private boolean letGoOfHookShot;
    private boolean pulledByHookShot;
    private boolean hasRopeOut;
    
    public ClientProxy()
    {
        letGoOfHookShot = false;
        pulledByHookShot = false;
        hasRopeOut = false;
    }

    @Override
    public void loadConfig(Configuration config)
    {        
        MinecraftForge.EVENT_BUS.register(new RopesPlusSounds());
        RopesPlusClient.toolTipEnabled = config.get(config.CATEGORY_GENERAL, "Equipped Bow Tool Tip", true).getBoolean(true);
    }
    
    @Override
    public void load()
    {        
        RenderingRegistry.registerEntityRenderingHandler(EntityGrapplingHook.class, new RenderGrapplingHook());
        Render arrowRenderer = new RenderArrow303();
        for(Class arrow : RopesPlusCore.coreArrowClasses)
        {
            RenderingRegistry.registerEntityRenderingHandler(arrow, arrowRenderer);
        }
        
        RenderingRegistry.registerBlockHandler(BlockRenderHandler.instance.new BlockGrapplingHookRenderHandler());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityFreeFormRope.class, new RenderFreeFormRope());
        
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropesPlusBlocks.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropesPlusItems.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/itemGrapplingHookThrown.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropeSegment.png");
        
        TickRegistry.registerTickHandler(new RopesPlusClient(), Side.CLIENT);
    }
    
    @Override
    public boolean getShouldHookShotDisconnect()
    {
        return letGoOfHookShot;
    }

    @Override
    public void setShouldHookShotDisconnect(boolean b)
    {
        letGoOfHookShot = b;
    }
    
    @Override
    public boolean getShouldHookShotPull()
    {
        return pulledByHookShot;
    }

    @Override
    public void setShouldHookShotPull(boolean b)
    {
        pulledByHookShot = b;
    }
    
    @Override
    public int getGrapplingHookRenderId()
    {
        return RopesPlusClient.renderIDGrapplingHook;
    }

    @Override
    public boolean getHasClientRopeOut()
    {
        return hasRopeOut;
    }

    @Override
    public void setHasClientRopeOut(boolean b)
    {
        hasRopeOut = b;
    }

}
