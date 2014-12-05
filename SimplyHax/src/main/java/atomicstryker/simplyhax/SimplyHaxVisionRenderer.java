package atomicstryker.simplyhax;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

public class SimplyHaxVisionRenderer extends RenderGlobal
{

    public SimplyHaxVisionRenderer(Minecraft minecraft)
    {
        super(minecraft);
    }
    
    @Override
    public void renderClouds(float p_174976_1_, int p_174976_2_)
    {
    }
    
    @Override
    public void renderEntities(Entity ent, ICamera cam, float f)
    {
        SimplyHaxVision.preRender();
        super.renderEntities(ent, cam, f);
    }
}
