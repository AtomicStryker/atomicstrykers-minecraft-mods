package atomicstryker.minions.client.render.shapes;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.common.util.Vector3;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Draws a rectangular prism around 2 corners
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class Render3DBox {

    protected atomicstryker.minions.client.render.LineColor color;
    protected Vector3 first;
    protected Vector3 second;

    public Render3DBox(LineColor color, Vector3 first, Vector3 second) {
        this.color = color;
        this.first = first;
        this.second = second;
    }

    public void render() {
        double x1 = first.getX();
        double y1 = first.getY();
        double z1 = first.getZ();
        double x2 = second.getX();
        double y2 = second.getY();
        double z2 = second.getZ();

        for (LineInfo tempColor : color.getColors()) {
            tempColor.prepareRender();

            // Draw bottom face
            Tessellator.getInstance().getWorldRenderer().begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            tempColor.prepareColor();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y1, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y1, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y1, z2).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y1, z2).endVertex();
            Tessellator.getInstance().draw();

            // Draw top face
            Tessellator.getInstance().getWorldRenderer().begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            tempColor.prepareColor();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y2, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y2, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y2, z2).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y2, z2).endVertex();
            Tessellator.getInstance().draw();

            // Draw join top and bottom faces
            Tessellator.getInstance().getWorldRenderer().begin(GL11.GL_LINES, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            tempColor.prepareColor();

            Tessellator.getInstance().getWorldRenderer().pos(x1, y1, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y2, z1).endVertex();

            Tessellator.getInstance().getWorldRenderer().pos(x2, y1, z1).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y2, z1).endVertex();

            Tessellator.getInstance().getWorldRenderer().pos(x2, y1, z2).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x2, y2, z2).endVertex();

            Tessellator.getInstance().getWorldRenderer().pos(x1, y1, z2).endVertex();
            Tessellator.getInstance().getWorldRenderer().pos(x1, y2, z2).endVertex();

            Tessellator.getInstance().draw();
        }
    }
}
