package com.sirolf2009.necromancy.entity;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class EntityNecroFX extends EntityFX
{
    
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
        particleMaxAge = (int) (8D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D) + 64D);
        particleMaxAge = (int) (particleMaxAge * par14);
        noClip = true;
    }

    public EntityNecroFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        this(par1World, par2, par4, par6, par8, par10, par12, 1.0F);
    }

    @Override
    public void renderParticle(Tessellator tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        tessellator.startDrawingQuads();
        tessellator.setBrightness(getBrightnessForRender(par2));
        ClientProxy.mc.renderEngine.bindTexture(ReferenceNecromancy.TEXTURES_PARTICLES);
        float sC = 0.1F * particleScale;
        float dX = (float) (prevPosX + (posX - prevPosX) * par2 - interpPosX);
        float dY = (float) (prevPosY + (posY - prevPosY) * par2 - interpPosY);
        float dZ = (float) (prevPosZ + (posZ - prevPosZ) * par2 - interpPosZ);
        tessellator.setColorOpaque_F(particleRed, particleGreen, particleBlue);
        tessellator.addVertexWithUV(dX - par3 * sC - par6 * sC, dY - par4 * sC, dZ - par5 * sC - par7 * sC, 32.0624375F, 32.0624375F);
        tessellator.addVertexWithUV(dX - par3 * sC + par6 * sC, dY + par4 * sC, dZ - par5 * sC + par7 * sC, 32.0624375F, 32f);
        tessellator.addVertexWithUV(dX + par3 * sC + par6 * sC, dY + par4 * sC, dZ + par5 * sC + par7 * sC, 32f, 32f);
        tessellator.addVertexWithUV(dX + par3 * sC - par6 * sC, dY - par4 * sC, dZ + par5 * sC - par7 * sC, 32f, 32.0624375F);
        tessellator.draw();
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
    
}
