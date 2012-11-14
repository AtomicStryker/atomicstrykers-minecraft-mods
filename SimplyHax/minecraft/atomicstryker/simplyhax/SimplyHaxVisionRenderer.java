package atomicstryker.simplyhax;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.ICamera;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.Vec3;

public class SimplyHaxVisionRenderer extends RenderGlobal
{

    public SimplyHaxVisionRenderer(Minecraft minecraft, RenderEngine renderengine)
    {
        super(minecraft, renderengine);
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
    public int sortAndRender(EntityLiving entityliving, int i, double d)
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
