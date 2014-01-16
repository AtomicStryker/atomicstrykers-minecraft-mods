package atomicstryker.simplyhax;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

public class SimplyHaxVisionRenderer extends RenderGlobal
{

    public SimplyHaxVisionRenderer(Minecraft minecraft)
    {
        super(minecraft);
    }

    /*
    public void callSuper(float f)
    {
        super.renderClouds(f);
    }
    */

    @Override
    public void renderClouds(float f)
    {
    }

    @Override
    public int sortAndRender(EntityLivingBase entityliving, int i, double d)
    {
        SimplyHaxVision.preRender();
        return super.sortAndRender(entityliving, i, d);
    }
}
