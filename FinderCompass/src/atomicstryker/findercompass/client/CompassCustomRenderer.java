package atomicstryker.findercompass.client;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class CompassCustomRenderer implements IItemRenderer
{
    private boolean optifine;
    
    public CompassCustomRenderer()
    {
        try
        {
            optifine = Class.forName("TextureAnimation") != null;
        }
        catch (ClassNotFoundException e)
        {
            optifine = false;
        }
        System.out.println("Finder Compass detected Optifine: "+optifine);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        switch (type)
        {
        case EQUIPPED_FIRST_PERSON:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {        
        switch (type)
        {
            case EQUIPPED_FIRST_PERSON:
            {
                renderCompass((RenderBlocks) data[0], item, -0.4f, 0.8f, 0.9f, 75f);
                break;
            }
            default:
            {
            }
        }
    }

    private void renderCompass(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ, float rotateAngle)
    {        
        GL11.glTranslatef(translateX, translateY, translateZ);
        GL11.glRotatef(rotateAngle, 0, 1.0F, 0);
        Icon icon = item.getIconIndex();
        ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0825F);
    }

}
