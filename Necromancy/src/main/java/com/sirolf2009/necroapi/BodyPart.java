package com.sirolf2009.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class BodyPart extends ModelRenderer
{
    
    public String name;
    public NecroEntityBase entity;
    
    public float torsoPos[];
    public float armLeftPos[];
    public float armRightPos[];
    public float headPos[];

    /**
     * The base constructor for {@link BodyPart}s, call this if you are creating
     * arms or heads
     * 
     * @param base
     * @param par1ModelBase
     * @param textureXOffset
     * @param textureYOffset
     */
    public BodyPart(NecroEntityBase base, ModelBase par1ModelBase, int textureXOffset, int textureYOffset)
    {
        super(par1ModelBase, textureXOffset, textureYOffset);
        entity = base;
        name = base.mobName;
        textureHeight = base.textureHeight;
        textureWidth = base.textureWidth;
    }

    /**
     * Call this if you are creating legs
     * 
     * @param base
     * @param torsoPos
     * @param par1ModelBase
     * @param textureXOffset
     * @param textureYOffset
     */
    public BodyPart(NecroEntityBase base, float torsoPos[], ModelBase par1ModelBase, int textureXOffset, int textureYOffset)
    {
        this(base, par1ModelBase, textureXOffset, textureYOffset);
        this.torsoPos = torsoPos;
    }

    /**
     * Call this if you are creating torso's
     * 
     * @param base
     * @param armLeftPos
     * @param armRightPos
     * @param headPos
     * @param par1ModelBase
     * @param textureXOffset
     * @param textureYOffset
     */
    public BodyPart(NecroEntityBase base, float armLeftPos[], float armRightPos[], float headPos[], ModelBase par1ModelBase, int textureXOffset, int textureYOffset)
    {
        this(base, par1ModelBase, textureXOffset, textureYOffset);
        this.armLeftPos = armLeftPos;
        this.armRightPos = armRightPos;
        this.headPos = headPos;
    }
}
