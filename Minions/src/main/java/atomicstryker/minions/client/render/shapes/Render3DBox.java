package atomicstryker.minions.client.render.shapes;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.common.util.Vector3;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

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
            Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
            tempColor.prepareColor();
            Tessellator.instance.addVertex(x1, y1, z1);
            Tessellator.instance.addVertex(x2, y1, z1);
            Tessellator.instance.addVertex(x2, y1, z2);
            Tessellator.instance.addVertex(x1, y1, z2);
            Tessellator.instance.draw();

            // Draw top face
            Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
            tempColor.prepareColor();
            Tessellator.instance.addVertex(x1, y2, z1);
            Tessellator.instance.addVertex(x2, y2, z1);
            Tessellator.instance.addVertex(x2, y2, z2);
            Tessellator.instance.addVertex(x1, y2, z2);
            Tessellator.instance.draw();

            // Draw join top and bottom faces
            Tessellator.instance.startDrawing(GL11.GL_LINES);
            tempColor.prepareColor();

            Tessellator.instance.addVertex(x1, y1, z1);
            Tessellator.instance.addVertex(x1, y2, z1);

            Tessellator.instance.addVertex(x2, y1, z1);
            Tessellator.instance.addVertex(x2, y2, z1);

            Tessellator.instance.addVertex(x2, y1, z2);
            Tessellator.instance.addVertex(x2, y2, z2);

            Tessellator.instance.addVertex(x1, y1, z2);
            Tessellator.instance.addVertex(x1, y2, z2);

            Tessellator.instance.draw();
        }
    }
}
