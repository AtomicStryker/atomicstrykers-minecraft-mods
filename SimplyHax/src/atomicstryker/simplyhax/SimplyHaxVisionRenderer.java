package atomicstryker.simplyhax;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

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

    @Override
    public void renderEntities(Vec3 vec3, ICamera icamera, float f)
    {
        super.renderEntities(vec3, icamera, f);
    }

    @Override
    public void renderAllRenderLists(int i, double d)
    {
        super.renderAllRenderLists(i, d);
    }

    @Override
    public void updateClouds()
    {
        super.updateClouds();
    }

    @Override
    public void loadRenderers()
    {
        super.loadRenderers();
    }
}
