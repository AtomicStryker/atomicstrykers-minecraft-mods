package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityWitch extends NecroEntityVillager
{

    public NecroEntityWitch()
    {
        super("Witch");
        headItem = ItemBodyPart.getItemStackFromName("Witch Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Witch Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Witch Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Witch Legs", 1);
        texture = new ResourceLocation("textures/entity/witch.png");
        textureHeight = 128;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.poisonous_potato);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart villagerHead = new BodyPart(this, model, 0, 0);
        villagerHead.addBox(-4.0F, -6.0F, -4.0F, 8, 10, 8, 0.0F);
        BodyPart nose = new BodyPart(this, model, 24, 0);
        nose.setRotationPoint(0.0F, -2.0F, 0.0F);
        nose.addBox(-1.0F, 3.0F, -6.0F, 2, 4, 2, 0.0F);
        BodyPart field_82901_h = new BodyPart(this, model, 0, 0);
        field_82901_h.setRotationPoint(0.0F, -2.0F, 0.0F);
        field_82901_h.addBox(0.0F, 7.0F, -6.75F, 1, 1, 1, -0.25F);
        nose.addChild(field_82901_h);
        BodyPart witchHat = new BodyPart(this, model, 0, 64);
        witchHat.setRotationPoint(-5.0F, -6.03125F, -5.0F);
        witchHat.addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
        villagerHead.addChild(witchHat);
        ModelRenderer modelrenderer = new BodyPart(this, model, 0, 76);
        modelrenderer.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelrenderer.addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
        modelrenderer.rotateAngleX = -0.05235988F;
        modelrenderer.rotateAngleZ = 0.02617994F;
        witchHat.addChild(modelrenderer);
        ModelRenderer modelrenderer1 = new BodyPart(this, model, 0, 87);
        modelrenderer1.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelrenderer1.setTextureOffset(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
        modelrenderer1.rotateAngleX = -0.10471976F;
        modelrenderer1.rotateAngleZ = 0.05235988F;
        modelrenderer.addChild(modelrenderer1);
        ModelRenderer modelrenderer2 = new BodyPart(this, model, 0, 95);
        modelrenderer2.setRotationPoint(1.75F, -2.0F, 2.0F);
        modelrenderer2.setTextureOffset(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
        modelrenderer2.rotateAngleX = -0.20943952F;
        modelrenderer2.rotateAngleZ = 0.10471976F;
        modelrenderer1.addChild(modelrenderer2);
        return new BodyPart[] { villagerHead, nose };
    }

    @Override
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(3.0D); // health
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(16.0D); // followrange
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(14.0D); // health
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                      // res
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(3.0D); // health
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                        // res
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D); // damage
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(3.0D); // health
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                         // res
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(3.0D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
