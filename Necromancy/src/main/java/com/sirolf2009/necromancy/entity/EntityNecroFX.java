package com.sirolf2009.necromancy.entity;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class EntityNecroFX extends EntityFX
{

    public EntityNecroFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        this(par1World, par2, par4, par6, par8, par10, par12, 1.0F);
    }

    public EntityNecroFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float par14)
    {
        super(par1World, par2, par4, par6, 0.0D, 0.0D, 0.0D);
        motionX *= 0.010000000149011612D;
        motionY *= 0.010000000149011612D;
        motionZ *= 0.010000000149011612D;
        motionX += par8;
        motionY += par10;
        motionZ += par12;
        particleRed = particleGreen = particleBlue = (float) (Math.random() * 0.50000001192092891D);
        particleScale *= 0.75F;
        particleScale *= par14;
        smokeParticleScale = particleScale;
        particleMaxAge = (int) (8D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D) + 64D);
        particleMaxAge = (int) (particleMaxAge * par14);
        noClip = true;
    }

    @Override
    public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        Tessellator tessellator1 = new Tessellator();
        tessellator1.startDrawingQuads();
        tessellator1.setBrightness(getBrightnessForRender(par2));
        ClientProxy.mc.renderEngine.bindTexture(ReferenceNecromancy.TEXTURES_PARTICLES);
        float f = 32F;
        float f1 = f + 0.0624375F;
        float f2 = 32F;
        float f3 = f2 + 0.0624375F;
        float f4 = 0.1F * particleScale;
        float f5 = (float) (prevPosX + (posX - prevPosX) * par2 - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * par2 - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * par2 - interpPosZ);
        float f8 = 1.0F;
        tessellator1.setColorOpaque_F(particleRed * f8, particleGreen * f8, particleBlue * f8);
        tessellator1.addVertexWithUV(f5 - par3 * f4 - par6 * f4, f6 - par4 * f4, f7 - par5 * f4 - par7 * f4, f1, f3);
        tessellator1.addVertexWithUV(f5 - par3 * f4 + par6 * f4, f6 + par4 * f4, f7 - par5 * f4 + par7 * f4, f1, f2);
        tessellator1.addVertexWithUV(f5 + par3 * f4 + par6 * f4, f6 + par4 * f4, f7 + par5 * f4 + par7 * f4, f, f2);
        tessellator1.addVertexWithUV(f5 + par3 * f4 - par6 * f4, f6 - par4 * f4, f7 + par5 * f4 - par7 * f4, f, f3);
        tessellator1.draw();
        ClientProxy.mc.renderEngine.bindTexture(ReferenceNecromancy.TEXTURES_PARTICLES);
    }

    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge)
        {
            setDead();
        }
        setParticleTextureIndex(7 - particleAge * 8 / particleMaxAge);
        moveEntity(motionX, motionY, motionZ);
        if (posY == prevPosY)
        {
            motionX *= 1.1000000000000001D;
            motionZ *= 1.1000000000000001D;
        }
        motionX *= 0.99999997854232792D;
        motionY *= 0.99999997854232792D;
        motionZ *= 0.99999997854232792D;
        if (onGround)
        {
            motionX *= 0.69999998807907104D;
            motionZ *= 0.69999998807907104D;
        }
    }

    float smokeParticleScale;
}
