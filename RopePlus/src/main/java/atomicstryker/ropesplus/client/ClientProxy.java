package atomicstryker.ropesplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.EntityGrapplingHook;
import atomicstryker.ropesplus.common.IProxy;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;

public class ClientProxy implements IProxy
{
    private boolean letGoOfHookShot;
    private float pulledByHookShot;
    private boolean hasRopeOut;
    private int renderIDGrapplingHook;
    
    public ClientProxy()
    {
        letGoOfHookShot = false;
        pulledByHookShot = -1f;
        hasRopeOut = false;
    }

    @Override
    public void loadConfig(Configuration config)
    {
        FMLCommonHandler.instance().bus().register(new RopesPlusClient());
        RopesPlusClient.toolTipEnabled = config.get(Configuration.CATEGORY_GENERAL, "Equipped Bow Tool Tip", true).getBoolean(true);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void load()
    {        
        RenderingRegistry.registerEntityRenderingHandler(EntityGrapplingHook.class, new RenderGrapplingHook(Minecraft.getMinecraft().getRenderManager()));
        Render arrowRenderer = new RenderArrow303(Minecraft.getMinecraft().getRenderManager());
        for(Class<?> arrow : RopesPlusCore.coreArrowClasses)
        {
            RenderingRegistry.registerEntityRenderingHandler((Class<? extends Entity>) arrow, arrowRenderer);
        }
        
        //renderIDGrapplingHook = RenderingRegistry.getNextAvailableRenderId();
        //RenderingRegistry.registerBlockHandler(new BlockRenderHandler());
        //TODO disabled for now
        ItemModelMesher mm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mm.register(RopesPlusCore.instance.itemGrapplingHook, 0, new ModelResourceLocation("ropesplus:itemGrapplingHook", "inventory"));
        mm.register(RopesPlusCore.instance.bowRopesPlus, 0, new ModelResourceLocation("ropesplus:itemBowRopesPlus", "inventory"));
        mm.register(RopesPlusCore.instance.itemHookShot, 0, new ModelResourceLocation("ropesplus:itemHookshot", "inventory"));
        mm.register(RopesPlusCore.instance.itemHookShotCartridge, 0, new ModelResourceLocation("ropesplus:itemHookshotCartridge", "inventory"));
        
        for (ItemArrow303 item : RopesPlusCore.instance.arrowItems)
        {
            mm.register(item, 0, new ModelResourceLocation("ropesplus:"+item.arrow.name, "inventory"));
        }
        
        RenderingRegistry.registerEntityRenderingHandler(EntityFreeFormRope.class, new RenderFreeFormRope(Minecraft.getMinecraft().getRenderManager()));
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
    public float getShouldRopeChangeState()
    {
        return pulledByHookShot;
    }

    @Override
    public void setShouldRopeChangeState(float f)
    {
        pulledByHookShot = f;
    }
    
    @Override
    public int getGrapplingHookRenderId()
    {
        return renderIDGrapplingHook;
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
