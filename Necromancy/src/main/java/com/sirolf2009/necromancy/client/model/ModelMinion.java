package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.ISaddleAble;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.client.renderer.RenderMinion;
import com.sirolf2009.necromancy.entity.EntityMinion;

public class ModelMinion extends ModelBase
{

    public RenderMinion renderer;
    public static boolean remodelCommand = false;
    
    private BodyPart[] head, torso, armLeft, armRight, legs;
    private BodyPart[][] parts;
    private float[] torsoPos = new float[3], armLeftPos = new float[3], armRightPos = new float[3], headPos = new float[3];


    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        EntityMinion minion = (EntityMinion) par1Entity;
        if (minion.getModel() == null)
        {
            minion.setModel(this);
        }
        updateModel(minion, minion.ticksExisted < 10 || remodelCommand);
        setRotationAngles(par2, par3, par4, par5, par6, par7, minion);
        if (legs != null)
        {
            GL11.glPushMatrix();
            bindTexByPart(parts[4]);
            NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(legs[0].name);
            mob.preRender(minion, legs, BodyPartLocation.Legs, this);
            for (BodyPart part : legs)
            {
                part.render(par7);
            }
            mob.postRender(minion, legs, BodyPartLocation.Legs, this);
            GL11.glPopMatrix();
        }
        if (torso != null)
        {
            bindTexByPart(parts[1]);
            if (torso[0] != null)
            {
                GL11.glTranslatef(torsoPos[0] / 16, torsoPos[1] / 16, torsoPos[2] / 16);
                NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(torso[0].name);
                mob.preRender(minion, torso, BodyPartLocation.Torso, this);
                for (BodyPart part : torso)
                {
                    part.render(par7);
                }
                if (mob instanceof ISaddleAble && minion.getSaddled())
                {
                    renderer.bindTexture(((ISaddleAble) mob).getSaddleTex());
                    for (BodyPart part : torso)
                    {
                        part.render(par7);
                    }
                }
                mob.postRender(minion, torso, BodyPartLocation.Torso, this);
            }
        }
        if (armLeft != null)
        {
            GL11.glPushMatrix();
            bindTexByPart(parts[2]);
            GL11.glTranslatef(armLeftPos[0] / 16, armLeftPos[1] / 16, armLeftPos[2] / 16);
            NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(armLeft[0].name);
            mob.preRender(minion, armLeft, BodyPartLocation.ArmLeft, this);
            for (BodyPart part : armLeft)
            {
                part.render(par7);
            }
            mob.postRender(minion, armLeft, BodyPartLocation.ArmLeft, this);
            GL11.glPopMatrix();
        }
        if (armRight != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(armRightPos[0] / 16, armRightPos[1] / 16, armRightPos[2] / 16);
            bindTexByPart(parts[3]);
            NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(armRight[0].name);
            mob.preRender(minion, armLeft, BodyPartLocation.ArmRight, this);
            for (BodyPart part : armRight)
            {
                part.render(par7);
            }
            mob.postRender(minion, armRight, BodyPartLocation.ArmRight, this);
            GL11.glPopMatrix();
        }
        if (head != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(headPos[0] / 16, headPos[1] / 16, headPos[2] / 16);
            
            /*
            if (isChristmas() && santahat != null)
            {
                textureHeight = 32;
                textureWidth = 64;
                renderer.bindTexture(ReferenceNecromancy.TEXTURES_MISC_CHRISTMASHAT);
                santahat.render(par7 + 0.001F);
            }
            */
            
            bindTexByPart(parts[0]);
            NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(head[0].name);
            mob.preRender(minion, head, BodyPartLocation.Head, this);
            for (BodyPart part : head)
            {
                part.render(par7);
            }
            mob.postRender(minion, head, BodyPartLocation.Head, this);
            GL11.glPopMatrix();
        }
    }

    private void bindTexByPart(BodyPart[] bodypart)
    {
        NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(bodypart[0].name);
        if (mob != null)
        {
            textureHeight = mob.textureHeight;
            textureWidth = mob.textureWidth;
            renderer.bindTexture(mob.texture);
        }
    }

    public void updateModel(EntityMinion minion, boolean shouldUpdate)
    {
        parts = minion.getBodyParts();
        if (shouldUpdate)
        {
            head = null;
            torso = null;
            armLeft = null;
            armRight = null;
            legs = null;
            NecroEntityBase mob;
            if (remodelCommand)
            {
                for (int i = 0; i < NecroEntityRegistry.registeredEntities.size(); i++)
                    if (parts.length > i && parts[i] != null && parts[i].length > 0 && parts[i][0] != null)
                    {
                        NecroEntityRegistry.registeredEntities.get(parts[i][0].name).updateParts(this);
                    }
                remodelCommand = false;
            }
            if (parts.length > 0 && parts[0] != null && parts[0].length > 0 && parts[0][0] != null
                    && (mob = NecroEntityRegistry.registeredEntities.get(parts[0][0].name)) != null && (head = mob.head) == null)
            {
                head = mob.head == null ? mob.updateParts(this).head : mob.head;
            }
            if (parts.length > 1 && parts[1] != null && parts[1].length > 0 && parts[1][0] != null
                    && (mob = NecroEntityRegistry.registeredEntities.get(parts[1][0].name)) != null && (torso = mob.torso) == null)
            {
                torso = mob.torso == null ? mob.updateParts(this).torso : mob.torso;
            }
            if (parts.length > 2 && parts[2] != null && parts[2].length > 0 && parts[2][0] != null
                    && (mob = NecroEntityRegistry.registeredEntities.get(parts[2][0].name)) != null && (armLeft = mob.armLeft) == null)
            {
                armLeft = mob.armRight == null ? mob.updateParts(this).armRight : mob.armRight;
            }
            if (parts.length > 3 && parts[3] != null && parts[3].length > 0 && parts[3][0] != null
                    && (mob = NecroEntityRegistry.registeredEntities.get(parts[3][0].name)) != null && (armRight = mob.armRight) == null)
            {
                armRight = mob.armLeft == null ? mob.updateParts(this).armLeft : mob.armLeft;
            }
            if (parts.length > 4 && parts[4] != null && parts[4].length > 0 && parts[4][0] != null
                    && (mob = NecroEntityRegistry.registeredEntities.get(parts[4][0].name)) != null && (legs = mob.legs) == null)
            {
                legs = mob.legs == null ? mob.updateParts(this).legs : mob.legs;
            }
            minion.setBodyParts(new BodyPart[][] { head, torso, armLeft, armRight, legs });
        }
        else
        {
            BodyPart[][] bodyparts = minion.getBodyParts();
            head = bodyparts[0];
            torso = bodyparts[1];
            armLeft = bodyparts[2];
            armRight = bodyparts[3];
            legs = bodyparts[4];
        }
        if (legs != null && legs.length > 0 && legs[0] != null)
        {
            torsoPos = legs[0].torsoPos;
        }
        if (torso != null && torso.length > 0 && torso[0] != null)
        {
            armLeftPos = torso[0].armLeftPos;
            armRightPos = torso[0].armRightPos;
            headPos = torso[0].headPos;
        }
    }
    
    /*
    private boolean isChristmas()
    {
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat day = new SimpleDateFormat("dd");
        Date date = new Date();
        if (Integer.valueOf(month.format(date)) == 12 && Integer.valueOf(day.format(date)) > 21)
            return true;
        if (ConfigurationNecromancy.Christmas)
            return true;
        return false;
    }
    */

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
    {
        NecroEntityBase mob;
        String[] parts = ((EntityMinion) par7Entity).getBodyPartsNames();
        if ((mob = NecroEntityRegistry.registeredEntities.get(parts[0])) != null)
        {
            mob.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity, head, BodyPartLocation.Head);
        }
        if ((mob = NecroEntityRegistry.registeredEntities.get(parts[1])) != null)
        {
            mob.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity, torso, BodyPartLocation.Torso);
        }
        if ((mob = NecroEntityRegistry.registeredEntities.get(parts[2])) != null)
        {
            mob.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity, armLeft, BodyPartLocation.ArmLeft);
        }
        if ((mob = NecroEntityRegistry.registeredEntities.get(parts[3])) != null)
        {
            mob.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity, armRight, BodyPartLocation.ArmRight);
        }
        if ((mob = NecroEntityRegistry.registeredEntities.get(parts[4])) != null)
        {
            mob.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity, legs, BodyPartLocation.Legs);
        }
    }
    
}
