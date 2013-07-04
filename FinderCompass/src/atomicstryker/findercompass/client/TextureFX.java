package atomicstryker.findercompass.client;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureStitched;
import net.minecraft.client.texturepacks.ITexturePack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author immibis
 * Part of Immibis Core, http://www.minecraftforum.net/topic/1001131-
 * Modified by AtomicStryker to allow the display of a texture file aswell
 *
 */
@SideOnly(Side.CLIENT)
public abstract class TextureFX extends TextureStitched
{
    /**
     * Texture width in pixels
     */
    protected int width;
    
    /**
     * Texture pixels as byte array, RGBA format
     */
    protected byte[] imageData;
    
    /**
     *  "fake" texture which is updated every tick then copied to the main texture sheet
     */
    private Texture dynamicTexture;
    
    protected TextureFX(String name, int width, int height)
    {
        super(name);
        this.width = width;
    }

    /**
     * @param imageData Texture pixels as byte array, RGBA format
     */
    protected abstract void onTick(byte[] imageData);

    @Override
    public final void updateAnimation()
    {
        if (dynamicTexture == null)
        {
            onSetup();
        }

        onTick(imageData);

        // copy the texture into dynamicTexture's buffer, then to the texture sheet
        ByteBuffer intermediate = dynamicTexture.getTextureData();
        intermediate.position(0);
        intermediate.put(imageData);

        textureSheet.copyFrom(originX, originY, dynamicTexture, rotated);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public final boolean loadTexture(TextureManager manager, ITexturePack texturepack, String name, String fileName, BufferedImage image, ArrayList textures)
    {
        if (image != null)
        {
            width = image.getWidth();
        }
        onSetup();
        // add it twice, else mc does not consider it animated
        textures.add(dynamicTexture);
        textures.add(dynamicTexture);
        return true;
    }
    
    private void onSetup()
    {
        width = AS_FinderCompass.getTileSize();
        imageData = new byte[width * width * 4];
        System.out.println("Finder Compass Dynamic Texture setup, Image Width: "+width);
        dynamicTexture = new Texture(getIconName(), 2, width, width, 1, GL11.GL_CLAMP, GL11.GL_RGBA, GL11.GL_NEAREST, GL11.GL_NEAREST, null);
    }

}
