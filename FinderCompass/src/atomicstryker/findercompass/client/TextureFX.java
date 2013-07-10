package atomicstryker.findercompass.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Lists;

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
public abstract class TextureFX extends TextureAtlasSprite
{    
    /**
     * Texture pixels as byte array, RGBA format
     */
    protected int[] imageData;
    
    protected TextureFX(String name, int width, int height)
    {
        super(name);
        this.field_130223_c = width;
    }

    /**
     * @param imageData Texture pixels as int array
     */
    protected abstract void onTick(int[] imageData);

    @SuppressWarnings("unchecked")
    @Override
    public final void updateAnimation()
    {
        onTick(imageData);
        
        field_110976_a.clear();
        // add it twice, else mc does not consider it animated
        field_110976_a.add(imageData);
        field_110976_a.add(imageData);
    }
    
    /**
     * Load the specified resource as this sprite's data.
     * Returning false from this function will prevent this icon from being stitched onto the master texture. 
     * @param manager Main resource manager
     * @param location File resource location
     * @return False to prevent this Icon from being stitched
     * @throws IOException
     */
    @Override
    public boolean load(ResourceManager manager, ResourceLocation location) throws IOException
    {
        super.load(manager, location);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void func_130100_a(Resource par1Resource) throws IOException
    {
        this.func_110968_a(Lists.newArrayList());
        this.field_110973_g = 0;
        this.field_110983_h = 0;
        
        InputStream inputstream = par1Resource.func_110527_b();
        BufferedImage bufferedimage = ImageIO.read(inputstream);
        this.field_130224_d = bufferedimage.getHeight();
        this.field_130223_c = bufferedimage.getWidth();
        imageData = new int[this.field_130224_d * this.field_130223_c];
        bufferedimage.getRGB(0, 0, this.field_130223_c, this.field_130224_d, imageData, 0, this.field_130223_c);
        // add it twice, else mc does not consider it animated
        field_110976_a.add(imageData);
        field_110976_a.add(imageData);
    }

}
