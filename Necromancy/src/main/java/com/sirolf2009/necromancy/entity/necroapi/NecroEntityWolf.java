package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBiped;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityWolf extends NecroEntityBiped
{

    public NecroEntityWolf()
    {
        super("Wolf");
        headItem = ItemBodyPart.getItemStackFromName("Wolf Head", 1);
        hasTorso = false;
        hasArms = false;
        hasLegs = false;
        texture = new ResourceLocation("textures/entity/wolf/wolf.png");
        textureHeight = 32;
        textureWidth = 64;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.name_tag);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {        
        BodyPart wolfHead = new BodyPart(this, model, 0, 0);
        wolfHead.addBox(-2.0F, -2.5F, 2F, 6, 6, 4, 0);
        wolfHead.setRotationPoint(-1.0F, 0.0F, -3.0F);
        wolfHead.setTextureOffset(16, 14).addBox(-2.0F, -4.5F, 4.0F, 2, 2, 1, 0);
        wolfHead.setTextureOffset(16, 14).addBox(2.0F, -4.5F, 4.0F, 2, 2, 1, 0);
        wolfHead.setTextureOffset(0, 10).addBox(-0.5F, 0.5F, -1.0F, 3, 3, 4, 0);
        
        return new BodyPart[] { wolfHead, wolfHead };
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 2D, 1D, 1D, 1D, 2D);
        }
    }
}
