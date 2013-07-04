package atomicstryker.ropesplus.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import atomicstryker.ropesplus.common.EntityFreeFormRope;

public class RenderFreeFormRope extends Render
{
    
    float f;
    float f1;
    final float f2;
    final float f3;
    final double thickness;
    final Tessellator tessellator;
    
    double xGuess;
    double yGuess;
    double zGuess;
    
    double x;
    double y;
    double z;
    double prevX;
    double prevY;
    double prevZ;
    
    double ptx;
    double pty;
    double ptz;
    
    private ResourceLocation tex = new ResourceLocation("ropesplus", "textures/items/ropeSegment.png");
    
    public RenderFreeFormRope()
    {
        f = 0.0F;
        f1 = 1.0F;
        f2 = 0;
        f3 = 0.25F;
        thickness = .05D;
        tessellator = Tessellator.instance;
    }
    
    public void renderRope(EntityFreeFormRope rope, double posX, double posY, double posZ, float partialTick)
    {
        EntityPlayer player = (EntityPlayer) renderManager.livingPlayer;
        Vec3 look = player.getLook(partialTick);
        xGuess = player.prevPosX + (player.posX - player.prevPosX) * partialTick - look.xCoord;
        yGuess = player.prevPosY + (player.posY - player.prevPosY) * partialTick - look.yCoord;
        zGuess = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTick - look.zCoord;
        
        this.func_110777_b(rope);
        GL11.glPushMatrix();
        
        double[] startCoords = rope.getCoordsAtRelativeLength(1F);
        prevX = startCoords[0];
        prevY = startCoords[1];
        prevZ = startCoords[2];
        
        double rX = rope.prevPosX + (rope.posX - rope.prevPosX) * (double)partialTick;
        double rY = rope.prevPosY + (rope.posY - rope.prevPosY) * (double)partialTick;
        double rZ = rope.prevPosZ + (rope.posZ - rope.prevPosZ) * (double)partialTick;
        GL11.glTranslatef((float)(posX - rX), (float)(posY - rY), (float)(posZ - rZ));
        
        int segCount = rope.getSegmentCount();
        float jointInterval = 1F / segCount;
        GL11.glDisable(GL11.GL_CULL_FACE);
        for (float i = jointInterval*(segCount-1); i >= 0; i-=jointInterval)
        {
            renderRopeJoint(rope, i);
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        
        x = 0;
        y = 0;
        z = 0;
        
        GL11.glPopMatrix();
        f = 0.0f;
        f1 = 0.0f;
    }
    
    public void renderRopeJoint(EntityFreeFormRope rope, float relativeLength)
    {
        double[] coords = rope.getCoordsAtRelativeLength(relativeLength);
        
        // get quad end coordinates
        x = coords[0];
        y = coords[1];
        z = coords[2];
        
        // get vector of segment, from start to end of it
        double segmentX = prevX - x;
        double segmentY = prevY - y;
        double segmentZ = prevZ - z;
        
        // get vector from player to quad end
        double lookX = x - xGuess;
        double lookY = y - yGuess;
        double lookZ = z - zGuess;
        
        // now cross look with segment vector for an orthogonal width vector
        double widthX = lookX*segmentZ-lookZ*segmentY;
        double widthY = lookZ*segmentX-lookX*segmentZ;
        double widthZ = lookX*segmentY-lookY*segmentX;
        
        // get the length of the resulting vector
        double widthLength = Math.sqrt(widthX * widthX + widthY * widthY + widthZ * widthZ);
        // compute relative rope thickness from viewpoint
        double factor = thickness/widthLength;
        
        // apply relative thickness
        widthX *= factor;
        widthY *= factor;
        widthZ *= factor;
        
        // invert resulting translated quad start values
        double tx = -widthX;
        double ty = -widthY;
        double tz = -widthZ;
        
        // Math.random() here creates a cool moving effect
        double t = 0.2D; //-joint.twist + joint.prevJoint.twist;
        if(t/(2*Math.PI) < -0.9)
        {
            f = f1 + 1.0F - 0.9f; 
        }
        else
        {
            f = f1 + 1.0F + (float)(t/(2*Math.PI)); 
        }
        
        double[] p1 = { -tx + x, y - ty, z - tz };
        double[] p2 = { -ptx + prevX, prevY - pty, prevZ - ptz };
        double[] p3 = { ptx + prevX, prevY + pty, prevZ + ptz };
        double[] p4 = { tx + x, y + ty, z + tz };
        
        double[] normal = calculateNormal(p1, p2, p3);
        double[] v = subtractVectors(p2, p1);
        double[] u = subtractVectors(p4, p1);
        
        double[] u1 = divideVector(u, 2d);
        double[] sideBounds1 = addVectors(p1, u1);
        double[] sideBounds2 = addVectors(sideBounds1, v);
        
        double length = getMagnitude(u) / 2d;
        
        double[] q1 = addVectors(sideBounds1, multiplyVector(normal, length));
        double[] q2 = addVectors(sideBounds2, multiplyVector(normal, length));
        double[] q3 = addVectors(sideBounds2, multiplyVector(normal, -length));
        double[] q4 = addVectors(sideBounds1, multiplyVector(normal, -length));
        
        GL11.glNormal3f(0.0F, 0.0F, 1F);
        tessellator.startDrawingQuads();
        addVertex(p1, f, f3);
        addVertex(p2, f1, f3);
        addVertex(p3, f1, f2);
        addVertex(p4, f, f2);
        tessellator.draw();
        
        GL11.glNormal3f(0.0F, 0.0F, 1F);
        tessellator.startDrawingQuads();
        addVertex(q1, f, f3);
        addVertex(q2, f1, f3);
        addVertex(q3, f1, f2);
        addVertex(q4, f, f2);
        tessellator.draw();
        
        prevX = x;
        prevY = y;
        prevZ = z;
        
        ptx = tx;
        pty = ty;
        ptz = tz;
        
        f1 = f;
    }
    
    private void addVertex(double[] vertex, double u, double v)
    {
        tessellator.addVertexWithUV(vertex[0], vertex[1], vertex[2], u, v);
    }

    private double[] normalize(double[] v)
    {
        return divideVector(v, getMagnitude(v));
    }

    private double getMagnitude(double[] v)
    {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    private double[] multiplyVector(double[] v, double value)
    {
        return new double[] { v[0] * value, v[1] * value, v[2] * value };
    }

    private double[] divideVector(double[] v, double value)
    {
        return multiplyVector(v, 1d / value);
    }

    private double[] addVectors(double[] u, double[] v)
    {
        return new double[] { u[0] + v[0], u[1] + v[1], u[2] + v[2] };
    }

    private double[] subtractVectors(double[] u, double[] v)
    {
        return addVectors(u, multiplyVector(v, -1d));
    }

    /**
     * Computes the normal vector of a surface given by 3 points
     */
    private double[] calculateNormal(double[] p1, double[] p2, double[] p3)
    {
        double[] u = getVector(p1, p2);
        double[] v = getVector(p1, p3);
        return normalize(crossProduct(u, v));
    }

    private double[] crossProduct(double[] u, double[] v)
    {
        return new double[] { u[1] * v[2] - v[1] * u[2], v[0] * u[2] - u[0] * v[2], u[0] * v[1] - v[0] * u[1] };
    }

    private double[] getVector(double[] p1, double[] p2)
    {
        return new double[] { p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2] };
    }

    @Override
    public void doRender(Entity ent, double posX, double posY, double posZ, float yaw, float partialTickTime)
    {
        renderRope((EntityFreeFormRope) ent, posX, posY, posZ, partialTickTime);
    }

    @Override
    protected ResourceLocation func_110775_a(Entity entity)
    {
        return tex;
    }

}
