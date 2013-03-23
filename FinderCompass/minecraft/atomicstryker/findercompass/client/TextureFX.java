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
 * Slightly modified
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
     * Texture height in pixels
     */
    protected int height;
    
    /**
     * Texture pixels as byte array, RGBA format
     */
    protected byte[] imageData;
    
    /**
     *  "fake" texture which is updated every tick then copied to the main texture sheet
     */
    private Texture dynamicTexture;
    
    /**
     * To use with TextureMap.setTextureEntry, TextureMap is passed as IconRegister for Block and Item
     * @param name
     * @param width
     * @param height
     */
    protected TextureFX(String name, int width, int height)
    {
        super(name);
        this.width = width;
        this.height = height;
    }

    /**
     * @param imageData Texture pixels as byte array, RGBA format
     */
    protected abstract void onTick(byte[] imageData);
    
    /**
     * @return Texture width in pixels
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * @return Texture height in pixels
     */
    public int getHeight()
    {
        return height;
    }

    @Override
    public final void updateAnimation()
    {
        if (textureList.size() > 2)
        {
            // if a texture pack has an animation (with 3+ frames) it overrides the procedural one
            super.updateAnimation();
        }
        else
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
    }

    @Override
    public final boolean loadTexture(TextureManager manager, ITexturePack texturepack, String name, String fileName, BufferedImage image, ArrayList textures)
    {
        if (image != null)
        {
            width = image.getWidth();
            height = image.getHeight();
        }
        onSetup();
        // add it twice, else mc does not consider it animated
        textures.add(dynamicTexture);
        textures.add(dynamicTexture);
        return true;
    }
    
    private void onSetup()
    {
        imageData = new byte[width * height * 4];

        String texName = getIconName();

        // second arg for texture type, 2 results in no texture data and id -1 ... no clue what for
        // clamp/repeat mode and interpolation mode ignored since we don't render directly from this texture
        dynamicTexture = new Texture(texName, 0, width, height, 1, GL11.GL_CLAMP, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, null);
    }

}
