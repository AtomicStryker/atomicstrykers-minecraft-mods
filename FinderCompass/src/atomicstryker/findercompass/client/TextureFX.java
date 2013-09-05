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
    
    protected TextureFX(String name, int w)
    {
        super(name);
        this.width = w;
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
        
        framesTextureData.clear();
        // add it twice, else mc does not consider it animated
        framesTextureData.add(imageData);
        framesTextureData.add(imageData);
        
        // this overwrites the Texture in the stitched-together atlas
        TextureUtil.uploadTextureSub(imageData, getOriginX(), getOriginY(), getOriginX(), getOriginY(), false, false);
    }
    
    @Override
    public boolean load(ResourceManager manager, ResourceLocation location) throws IOException
    {
        loadSprite(manager.getResource(location));
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void loadSprite(Resource par1Resource) throws IOException
    {
        this.setFramesTextureData(Lists.newArrayList());
        this.frameCounter = 0;
        this.tickCounter = 0;
        
        InputStream inputstream = par1Resource.getInputStream();
        BufferedImage bufferedimage = ImageIO.read(inputstream);
        this.width = bufferedimage.getHeight();
        this.height = bufferedimage.getWidth();
        imageData = new int[this.height * this.width];
        bufferedimage.getRGB(0, 0, this.width, this.height, imageData, 0, this.width);
        // add it twice, else mc does not consider it animated
        framesTextureData.add(imageData);
        framesTextureData.add(imageData);
    }
    
    @Override
    public int[] getFrameTextureData(int par1)
    {
        if (par1 == 0 || par1 == 1)
        {
            return imageData;
        }
        return null;
    }

}
