package atomicstryker.minefactoryreloaded.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;


import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.FMLTextureFX;

import net.minecraft.src.RenderEngine;
import net.minecraft.src.TextureFX;
import net.minecraft.src.TexturePackList;
import net.minecraftforge.client.MinecraftForgeClient;

public class TextureFrameAnimFX extends FMLTextureFX
{
    protected int fileBuffer[];
    private int tick;
    private int numFrames;
    private int tileResolution;
    
    public TextureFrameAnimFX(int indexToReplace, String filePath)
    {
        super(indexToReplace);
        tileResolution = 16;
        tick = 0;
        try
        {
        	TexturePackList tpl = FMLClientHandler.instance().getClient().texturePackList;
        	InputStream s;
        	
        	s = tpl.getSelectedTexturePack().getResourceAsStream(filePath);
        	if(s == null)
        	{
        		s = (net.minecraft.client.Minecraft.class).getResourceAsStream(filePath);
        	}
        	
        	BufferedImage bufferedimage = ImageIO.read(s);
            
            fileBuffer = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
            numFrames = bufferedimage.getWidth() / bufferedimage.getHeight();
            tileResolution = bufferedimage.getHeight();
            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), fileBuffer, 0, bufferedimage.getWidth());
            imageData = new byte[tileResolution * tileResolution * 4];
        }
        catch (Exception e)
        {
			e.printStackTrace();
		}
    }

    @Override
    public void onTick()
    {
        if(tileResolution == 0)
        {
            return;
        }
        tick++;
        tick %= numFrames;
        for(int i = 0; i < tileResolution; i++)
        {
            int j = i * tileResolution * numFrames;
            for(int k = 0; k < tileResolution; k++)
            {
                int l = tileResolution * tick + k;
                int i1 = fileBuffer[j + l];
                int j1 = i * tileResolution + k;
                int k1 = i1 >> 0 & 0xff;
                int l1 = i1 >> 8 & 0xff;
                int i2 = i1 >> 16 & 0xff;
                int j2 = i1 >> 24 & 0xff;
                if(anaglyphEnabled)
                {
                    int k2 = (i2 * 30 + l1 * 59 + k1 * 11) / 100;
                    int l2 = (i2 * 30 + l1 * 70) / 100;
                    int i3 = (i2 * 30 + k1 * 70) / 100;
                    i2 = k2;
                    l1 = l2;
                    k1 = i3;
                }
                imageData[j1 * 4 + 0] = (byte)i2;
                imageData[j1 * 4 + 1] = (byte)l1;
                imageData[j1 * 4 + 2] = (byte)k1;
                imageData[j1 * 4 + 3] = (byte)j2;
            }
        }
    }
    
    @Override
    public void bindImage(RenderEngine renderengine)
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderengine.getTexture(MineFactoryReloadedCore.terrainTexture));
    }
}
