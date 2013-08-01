package atomicstryker.findercompass.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
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
     * Texture pixels as array, default RGB model
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
        
        // this overwrites the Texture in the stitched-together atlas
        TextureUtil.func_110998_a(imageData, getOriginX(), getOriginY(), func_130010_a(), func_110967_i(), false, false);
    }
    
    @Override
    public boolean load(ResourceManager manager, ResourceLocation location) throws IOException
    {
        func_130100_a(manager.func_110536_a(location));
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
    
    @Override
    public int[] func_110965_a(int par1)
    {
        if (par1 == 0 || par1 == 1)
        {
            return imageData;
        }
        return null;
    }

}
