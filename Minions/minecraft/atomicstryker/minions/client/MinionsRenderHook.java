package atomicstryker.minions.client;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.points.PointCube;
import atomicstryker.minions.client.render.region.CuboidRegion;

/**
 * Render Hook class, keeps track of the current shape to display
 * 
 * 
 * @author AtomicStryker
 */

public class MinionsRenderHook extends Render
{
	private static Minecraft mcinstance;
	public static Entity renderHookEnt;
	
	private static CuboidRegion selection = new CuboidRegion();
	
	private static ArrayList additionalCubes = new ArrayList();
	
	public MinionsRenderHook(Minecraft mc)
	{
		mcinstance = mc;
		this.setRenderManager(RenderManager.instance);
	}
	
    @Override
    public void doRender(Entity dontcare0, double dontcare1, double dontcare2, double dontcare3, float dontcare4, float renderTick)
	{
        render(renderTick);
    }
	
    @SuppressWarnings("static-access")
    private void render(float renderTick)
	{
        RenderHelper.disableStandardItemLighting();  
    	
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        
        EntityPlayer player = mcinstance.thePlayer;
        double xGuess = player.prevPosX + (player.posX - player.prevPosX) * renderTick;
        double yGuess = player.prevPosY + (player.posY - player.prevPosY) * renderTick;
        double zGuess = player.prevPosZ + (player.posZ - player.prevPosZ) * renderTick;
        GL11.glTranslated(-xGuess, -yGuess, -zGuess);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        
        selection.render();
        
        Iterator iter = additionalCubes.iterator();
        while (iter.hasNext())
        {
        	((PointCube)iter.next()).render();
        }
        
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        
        RenderHelper.enableStandardItemLighting();
    }
    
    public static void setSelectionPoint(int id, int x, int y, int z)
    {
    	selection.setCuboidPoint(id, x, y, z);
    }
    
    public static void addAdditionalCube(int x, int y, int z)
    {
    	PointCube newcube = new PointCube(x, y, z);
    	newcube.setColor(LineColor.CUBOIDBOX);
    	additionalCubes.add(newcube);
    }
    
    public static void deleteAdditionalCubes()
    {
    	additionalCubes.clear();
    }
    
    public static void deleteSelection()
    {
    	selection.wipePointCubes();
    	additionalCubes.clear();
    }
}
