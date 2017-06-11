package atomicstryker.minions.client.render.shapes;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.common.util.Vector3;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a rectangular prism around 2 corners
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class Render3DBox
{

    protected LineColor color;
    protected Vector3 first;
    protected Vector3 second;

    public Render3DBox(LineColor color, Vector3 first, Vector3 second)
    {
        this.color = color;
        this.first = first;
        this.second = second;
    }

    public void render(Vec3d cameraPos)
    {
        double x1 = first.getX() - cameraPos.xCoord;
        double y1 = first.getY() - cameraPos.yCoord;
        double z1 = first.getZ() - cameraPos.zCoord;
        double x2 = second.getX() - cameraPos.xCoord;
        double y2 = second.getY() - cameraPos.yCoord;
        double z2 = second.getZ() - cameraPos.zCoord;

        for (LineInfo tempColor : color.getColors())
        {
            tempColor.prepareRender();

            BufferBuilder buf = Tessellator.getInstance().getBuffer();

            // Draw bottom face
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            tempColor.prepareColor();
            buf.pos(x1, y1, z1).endVertex();
            buf.pos(x2, y1, z1).endVertex();
            buf.pos(x2, y1, z2).endVertex();
            buf.pos(x1, y1, z2).endVertex();
            Tessellator.getInstance().draw();

            // Draw top face
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            tempColor.prepareColor();
            buf.pos(x1, y2, z1).endVertex();
            buf.pos(x2, y2, z1).endVertex();
            buf.pos(x2, y2, z2).endVertex();
            buf.pos(x1, y2, z2).endVertex();
            Tessellator.getInstance().draw();

            // Draw join top and bottom faces
            buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
            tempColor.prepareColor();

            buf.pos(x1, y1, z1).endVertex();
            buf.pos(x1, y2, z1).endVertex();

            buf.pos(x2, y1, z1).endVertex();
            buf.pos(x2, y2, z1).endVertex();

            buf.pos(x2, y1, z2).endVertex();
            buf.pos(x2, y2, z2).endVertex();

            buf.pos(x1, y1, z2).endVertex();
            buf.pos(x1, y2, z2).endVertex();

            Tessellator.getInstance().draw();
        }
    }
}
