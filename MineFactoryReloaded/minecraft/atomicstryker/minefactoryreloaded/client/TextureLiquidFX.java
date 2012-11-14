package atomicstryker.minefactoryreloaded.client;

import org.lwjgl.opengl.GL11;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;


import cpw.mods.fml.client.FMLTextureFX;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.TextureFX;
import net.minecraftforge.client.MinecraftForgeClient;

public class TextureLiquidFX extends FMLTextureFX
{
    protected float array0[];
    protected float array1[];
    protected float array2[];
    protected float array3[];
    private int tickCounter;
    private int tileResolution;
    
    private String texture;
    
    private int rMin;
    private int rMax;
    private int gMin;
    private int gMax;
    private int bMin;
    private int bMax;
    
	public TextureLiquidFX(int textureIndex, String texture, int rMin, int rMax, int gMin, int gMax, int bMin, int bMax)
    {
        super(textureIndex);
        
        this.tileResolution = 16;
        
        this.texture = texture;
        
        this.rMin = rMin;
        this.rMax = rMax;
        this.gMin = gMin;
        this.gMax = gMax;
        this.bMin = bMin;
        this.bMax = bMax;
        
        array0 = new float[tileResolution * tileResolution];
        array1 = new float[tileResolution * tileResolution];
        array2 = new float[tileResolution * tileResolution];
        array3 = new float[tileResolution * tileResolution];
        tickCounter = 0;
        
        imageData = new byte[tileSize * tileSize * 4];
    }

	@Override
	public void bindImage(RenderEngine renderengine)
	{
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderengine.getTexture(MineFactoryReloadedCore.terrainTexture));
	}
	
	@Override
    public void onTick()
    {
        tickCounter++;
        for(int i = 0; i < tileResolution; i++)
        {
            for(int k = 0; k < tileResolution; k++)
            {
                float f = 0.0F;
                for(int j1 = i - 1; j1 <= i + 1; j1++)
                {
                    int k1 = j1 & 0xf;
                    int i2 = k & 0xf;
                    f += array0[k1 + i2 * 16];
                }

                array1[i + k * tileResolution] = f / 3.3F + array2[i + k * tileResolution] * 0.8F;
            }

        }

        for(int j = 0; j < tileResolution; j++)
        {
            for(int l = 0; l < tileResolution; l++)
            {
                array2[j + l * tileResolution] += array3[j + l * tileResolution] * 0.05F;
                if(array2[j + l * tileResolution] < 0.0F)
                {
                    array2[j + l * tileResolution] = 0.0F;
                }
                array3[j + l * tileResolution] -= 0.1F;
                if(Math.random() < 0.050000000000000003D)
                {
                    array3[j + l * tileResolution] = 0.5F;
                }
            }

        }

        float af[] = array1;
        array1 = array0;
        array0 = af;
        for(int i1 = 0; i1 < tileResolution * tileResolution; i1++)
        {
            float f1 = array0[i1];
            if(f1 > 1.0F)
            {
                f1 = 1.0F;
            }
            if(f1 < 0.0F)
            {
                f1 = 0.0F;
            }
            float f2 = f1 * f1;
            int r = (int)(rMin + f2 * (rMax - rMin));
            int g = (int)(gMin + f2 * (gMax - gMin));
            int b = (int)(bMin + f2 * (bMax - bMin));
            int a = 255;
            
            if(anaglyphEnabled)
            {
                int i3 = (r * 30 + g * 59 + b * 11) / 100;
                int j3 = (r * 30 + g * 70) / 100;
                int k3 = (r * 30 + b * 70) / 100;
                r = i3;
                g = j3;
                b = k3;
            }
            
            imageData[i1 * 4 + 0] = (byte)r;
            imageData[i1 * 4 + 1] = (byte)g;
            imageData[i1 * 4 + 2] = (byte)b;
            imageData[i1 * 4 + 3] = (byte)a;
        }
    }
}
