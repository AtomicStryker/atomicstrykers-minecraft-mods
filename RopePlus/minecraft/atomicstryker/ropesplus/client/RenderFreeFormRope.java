package atomicstryker.ropesplus.client;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Render;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

import atomicstryker.ropesplus.common.EntityFreeFormRope;

public class RenderFreeFormRope extends Render
{
    
    static float f = 0.0F;
    static float f1 = 1.0F;
    static final float f2 = 0;
    static final float f3 = 0.25F;
    static final double thickness = .05D;
    static final Tessellator tessellator = Tessellator.instance;
    double x = 0;
    double y = 0;
    double z = 0;
    double prevX = 0;
    double prevY = 0;
    double prevZ = 0;
    
    double ptx;
    double pty;
    double ptz;

    int count = 0;
    long total = 0;
    
    public void renderRope(EntityFreeFormRope rope, double posX, double posY, double posZ, float partialTick)
    {
        loadTexture("/atomicstryker/ropesplus/client/ropeSegment.png");
        GL11.glPushMatrix();
        
        double rX = rope.prevPosX + (rope.posX - rope.prevPosX) * (double)partialTick;
        double rY = rope.prevPosY + (rope.posY - rope.prevPosY) * (double)partialTick;
        double rZ = rope.prevPosZ + (rope.posZ - rope.prevPosZ) * (double)partialTick;
        
        GL11.glTranslatef((float)(posX - rX), (float)(posY - rY), (float)(posZ - rZ));

        int count = 0;
        tessellator.startDrawingQuads();

        double[] startCoords = rope.getCoordsAtRelativeLength(1F);
        prevX = startCoords[0];
        prevY = startCoords[1];
        prevZ = startCoords[2];

        float jointInterval = 1F / rope.getSegmentCount();
        for (float i = jointInterval; i <= 1F; i+=jointInterval)
        {
            count++;
            renderRopeJoint(rope, i, partialTick);
            if(count == 80)
            {
                tessellator.draw();
                tessellator.startDrawingQuads();
                count = 0;
            }
        }
        
        tessellator.draw();
        x = 0;
        y = 0;
        z = 0;
        
        GL11.glPopMatrix();
        f = 0.0f;
        f1 = 0.0f;
    }
    
    public void renderRopeJoint(EntityFreeFormRope rope, float relativeLength, float partialTick/*PhysRopeJoint joint*/)
    {
        double[] coords = rope.getCoordsAtRelativeLength(relativeLength);
        
        x = coords[0];
        y = coords[1];
        z = coords[2];
        
        double segmentX = prevX - x;
        double segmentY = prevY - y;
        double segmentZ = prevZ - z;
        
        EntityPlayer player = (EntityPlayer) renderManager.livingPlayer;
        double xGuess = player.prevPosX + (player.posX - player.prevPosX) * partialTick;
        double yGuess = player.prevPosY + (player.posY - player.prevPosY) * partialTick;
        double zGuess = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTick;
        
        double lookX = x - xGuess;
        double lookY = y - yGuess;
        double lookZ = z - zGuess;
        
        double widthX = lookX*segmentZ-lookZ*segmentY;
        double widthY = lookZ*segmentX-lookX*segmentZ;
        double widthZ = lookX*segmentY-lookY*segmentX;
        
        double widthLength = Math.sqrt(widthX * widthX + widthY * widthY + widthZ * widthZ);
        double factor = thickness/widthLength;
        
        widthX *= factor;
        widthY *= factor;
        widthZ *= factor;
        
        double tx = -widthX;
        double ty = -widthY;
        double tz = -widthZ;
        
        double t = 0.2D; //-joint.twist + joint.prevJoint.twist;
        if(t/(2*Math.PI) < -0.9)
        {
            f = f1 + 1.0F - 0.9f; 
        }
        else
        {
            f = f1 + 1.0F + (float)(t/(2*Math.PI)); 
        }
        
        GL11.glNormal3f(0.0F, 0.0F, 1F);

        tessellator.addVertexWithUV(-tx + x, 0 + y - ty, 0.0D + z - tz, f, f3);
        tessellator.addVertexWithUV(-ptx + prevX, prevY - pty, 0.0D + prevZ - ptz, f1, f3);
        tessellator.addVertexWithUV(ptx + prevX, prevY + pty, 0.0D + prevZ + ptz, f1, f2);
        tessellator.addVertexWithUV(tx + x, 0 + y + ty, 0.0D + z + tz, f, f2);

        prevX = x;
        prevY = y;
        prevZ = z;
        
        ptx = tx;
        pty = ty;
        ptz = tz;
        
        f1 = f;
    }

    @Override
    public void doRender(Entity ent, double posX, double posY, double posZ, float yaw, float partialTickTime)
    {
        renderRope((EntityFreeFormRope) ent, posX, posY, posZ, partialTickTime);
    }

}
