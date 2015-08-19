package atomicstryker.minions.client;

/**
 * @author ChickenBones
 * RenderWirelessBolt class, part of Wireless Redstone, slightly changed
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */

import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt.Segment;
import atomicstryker.minions.common.codechicken.Vector3;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class RenderChickenLightningBolt
{
    
    private ResourceLocation texI = new ResourceLocation("minions", "textures/lightning_inner.png");
    private ResourceLocation texO = new ResourceLocation("minions", "textures/lightning_outer.png");
    
	private static Vector3 getRelativeViewVector(Vector3 pos)
	{
		Entity renderentity = Minecraft.getMinecraft().renderViewEntity;
    	return new Vector3((float)renderentity.posX - pos.x, (float)renderentity.posY + renderentity.getEyeHeight() - pos.y, (float)renderentity.posZ - pos.z);
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
	    float frame = event.partialTicks;
		Entity entity = Minecraft.getMinecraft().thePlayer;
		
		interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)frame;
		interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)frame;
		interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)frame;
		
		GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ);
		
		Tessellator tessellator = Tessellator.instance;
		
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(texO);
		tessellator.startDrawingQuads();
		tessellator.setBrightness(0xF000F0);
		for(ChickenLightningBolt bolt : ChickenLightningBolt.boltlist)
		{
			renderBolt(bolt, tessellator, 0);
		}
        tessellator.draw();
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(texI);
		tessellator.startDrawingQuads();
		tessellator.setBrightness(0xF000F0);
		for(ChickenLightningBolt bolt : ChickenLightningBolt.boltlist)
		{
			renderBolt(bolt, tessellator, 1);
		}
        tessellator.draw();
		
        GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		

		GL11.glTranslated(interpPosX, interpPosY, interpPosZ);
	}
	
	private void renderBolt(ChickenLightningBolt bolt, Tessellator tessellator, int pass)
	{		
        float boltage = bolt.particleAge < 0 ? 0 : (float)bolt.particleAge / (float)bolt.particleMaxAge;
        float mainalpha;
        if(pass == 0)
        {
        	mainalpha = (1 - boltage) * 0.4F;
        }
        else
        {
        	mainalpha = 1 - boltage * 0.5F;
        }
        
        int expandTime = (int)(bolt.length*ChickenLightningBolt.speed);	
        
        int renderstart = (int) ((expandTime/2-bolt.particleMaxAge+bolt.particleAge) / (float)(expandTime/2) * bolt.numsegments0);
        int renderend = (int) ((bolt.particleAge+expandTime) / (float)expandTime * bolt.numsegments0);
        
        for(Iterator<Segment> iterator = bolt.segments.iterator(); iterator.hasNext();)
        {
        	Segment rendersegment = iterator.next();
        	
        	if(rendersegment.segmentno < renderstart || rendersegment.segmentno > renderend)
        	{
        		continue;
        	}
        	
        	Vector3 playervec = getRelativeViewVector(rendersegment.startpoint.point).multiply(-1);
        	
            double width = 0.025F * (playervec.mag() / 5+1) * (1+rendersegment.light)*0.5F;
            
            Vector3 diff1 = playervec.copy().crossProduct(rendersegment.prevdiff).normalize().multiply(width / rendersegment.sinprev);
        	Vector3 diff2 = playervec.copy().crossProduct(rendersegment.nextdiff).normalize().multiply(width / rendersegment.sinnext);
        	
        	Vector3 startvec = rendersegment.startpoint.point;
        	Vector3 endvec = rendersegment.endpoint.point;
        	
            tessellator.setColorRGBA_F(1, 1, 1, mainalpha * rendersegment.light);
            
            tessellator.addVertexWithUV(endvec.x - diff2.x, endvec.y - diff2.y, endvec.z - diff2.z, 0.5, 0);
            tessellator.addVertexWithUV(startvec.x - diff1.x, startvec.y - diff1.y, startvec.z - diff1.z, 0.5, 0);
            tessellator.addVertexWithUV(startvec.x + diff1.x, startvec.y + diff1.y, startvec.z + diff1.z, 0.5, 1);
            tessellator.addVertexWithUV(endvec.x + diff2.x, endvec.y + diff2.y, endvec.z + diff2.z, 0.5, 1);
            
            if(rendersegment.next == null)
            {
            	Vector3 roundend = rendersegment.endpoint.point.copy().add(rendersegment.diff.copy().normalize().multiply(width));
            	                
                tessellator.addVertexWithUV(roundend.x - diff2.x, roundend.y - diff2.y, roundend.z - diff2.z, 0, 0);
                tessellator.addVertexWithUV(endvec.x - diff2.x, endvec.y - diff2.y, endvec.z - diff2.z, 0.5, 0);
                tessellator.addVertexWithUV(endvec.x + diff2.x, endvec.y + diff2.y, endvec.z + diff2.z, 0.5, 1);
                tessellator.addVertexWithUV(roundend.x + diff2.x, roundend.y + diff2.y, roundend.z + diff2.z, 0, 1);
            }
            
            if(rendersegment.prev == null)
            {
            	Vector3 roundend = rendersegment.startpoint.point.copy().subtract(rendersegment.diff.copy().normalize().multiply(width));
            	                
                tessellator.addVertexWithUV(startvec.x - diff1.x, startvec.y - diff1.y, startvec.z - diff1.z, 0.5, 0);
                tessellator.addVertexWithUV(roundend.x - diff1.x, roundend.y - diff1.y, roundend.z - diff1.z, 0, 0);
                tessellator.addVertexWithUV(roundend.x + diff1.x, roundend.y + diff1.y, roundend.z + diff1.z, 0, 1);
                tessellator.addVertexWithUV(startvec.x + diff1.x, startvec.y + diff1.y, startvec.z + diff1.z, 0.5, 1);
            }
        }
	}
	
	static double interpPosX;
	static double interpPosY;
	static double interpPosZ;
}
