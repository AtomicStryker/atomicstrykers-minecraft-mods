package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBiped;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class NecroEntityIsaac extends NecroEntityBiped
{

    public NecroEntityIsaac()
    {
        super("Isaac");
        headItem = new ItemStack(RegistryNecromancyItems.isaacsHead, 1);
        hasTorso = false;
        hasArms = false;
        hasLegs = false;
        texture = new ResourceLocation(ReferenceNecromancy.LOC_RESOURCES_TEXTURES_ENTITIES + "/IsaacBlood.png");
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart neck4 = new BodyPart(this, model, 0, 0);
        neck4.addBox(-1F, 2F, -1F, 1, 3, 1);
        neck4.setRotationPoint(1F, 2F, 1F);
        neck4.setTextureSize(64, 32);
        neck4.mirror = true;
        BodyPart neck3 = new BodyPart(this, model, 0, 0);
        neck3.addBox(-1F, 2F, -1F, 1, 1, 1);
        neck3.setRotationPoint(0F, 2F, 1F);
        neck3.setTextureSize(64, 32);
        neck3.mirror = true;
        BodyPart neck2 = new BodyPart(this, model, 0, 0);
        neck2.addBox(-1F, 2F, 0F, 1, 1, 1);
        neck2.setRotationPoint(0F, 2F, 0F);
        neck2.setTextureSize(64, 32);
        neck2.mirror = true;
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-5F, -6F, -4F, 10, 9, 8);
        head.setRotationPoint(0F, 1F, 0F);
        head.setTextureSize(64, 32);
        head.mirror = true;
        BodyPart neck1 = new BodyPart(this, model, 0, 0);
        neck1.addBox(-1F, 2F, 0F, 1, 1, 1);
        neck1.setRotationPoint(1F, 2F, -1F);
        neck1.setTextureSize(64, 32);
        neck1.mirror = true;
        return new BodyPart[] { head, neck1, neck2, neck3, neck4 };
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 40.0D, 32.0D, 0D, 0.3D, 2.0D);
        }
    }
}
