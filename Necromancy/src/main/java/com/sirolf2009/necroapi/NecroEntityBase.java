package com.sirolf2009.necroapi;

import com.sirolf2009.necromancy.item.ItemNecromancy;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The base class for all the necro mobs
 * 
 * @author sirolf2009
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public abstract class NecroEntityBase
{

    public NecroEntityBase(String mobName)
    {
        this.mobName = mobName;
        hasHead = true;
        hasTorso = true;
        hasArms = true;
        hasLegs = true;
        textureWidth = 64;
        textureHeight = 32;
        try
        {
            Class.forName("com.sirolf2009.necromancy.Necromancy");
            isNecromancyInstalled = true;
            organs = ItemNecromancy.organs;
            initRecipes();
        }
        catch (ClassNotFoundException e)
        {
            System.err.println(mobName + " could not be registered, the necromancy mod is not installed");
            isNecromancyInstalled = false;
        }
    }

    /** Define your recipes here */
    public void initRecipes()
    {
    }

    /** Use this to define the default recipes */
    public void initDefaultRecipes(Object... items)
    {
        Object headItem = null, torsoItem = null, armItem = null, legItem = null;
        if (items.length == 1)
            headItem = torsoItem = armItem = legItem = items[0];
        else
        {
            headItem = items[0];
            torsoItem = items[1];
            armItem = items[2];
            legItem = items[3];
        }
        headRecipe = new Object[] { "SSSS", "SBFS", "SEES", 'S', new ItemStack(organs, 1, 4), // skin
                'E', Items.spider_eye, 'F', headItem, 'B', new ItemStack(organs, 1, 0) // brain
                };
        torsoRecipe = new Object[] { " LL ", "BHUB", "LEEL", "BLLB", 'L', new ItemStack(organs, 1, 4), // skin
                'E', torsoItem, 'H', new ItemStack(organs, 1, 1), // heart
                'U', new ItemStack(organs, 1, 3), // lungs
                'B', Items.bone };
        armRecipe = new Object[] { "LLLL", "BMEB", "LLLL", 'L', new ItemStack(organs, 1, 4), // skin
                'E', armItem, 'M', new ItemStack(organs, 1, 2), // muscle
                'B', Items.bone };
        legRecipe = new Object[] { "LBBL", "LMML", "LEEL", "LBBL", 'L', new ItemStack(organs, 1, 4), // skin
                'E', legItem, 'M', new ItemStack(organs, 1, 2), // muscle
                'B', Items.bone };
    }

    /**
     * Use this method to initialize all the ModelRenderers for your mobs head
     * 
     * @param model
     *            - the model
     */
    public BodyPart[] initHead(ModelBase model)
    {
        return null;
    }

    /**
     * Use this method to initialize all the ModelRenderers for your mobs torso
     * 
     * @param model
     *            - the model
     */
    public BodyPart[] initTorso(ModelBase model)
    {
        return null;
    }

    /**
     * Use this method to initialize all the ModelRenderers for your mobs legs
     * 
     * @param model
     *            - the model
     */
    public BodyPart[] initLegs(ModelBase model)
    {
        return null;
    }

    /**
     * Use this method to initialize all the ModelRenderers for your mobs left
     * arm
     * 
     * @param model
     *            - the model
     */
    public BodyPart[] initArmLeft(ModelBase model)
    {
        return null;
    }

    /**
     * Use this method to initialize all the ModelRenderers for your mobs right
     * arm
     * 
     * @param model
     *            - the model
     */
    public BodyPart[] initArmRight(ModelBase model)
    {
        return null;
    }

    /**
     * The method used to initialize the parts and store them in variables
     * 
     * @param model
     *            - the model
     * @return this
     */
    public NecroEntityBase updateParts(ModelBase model)
    {
        head = initHead(model);
        torso = initTorso(model);
        armLeft = initArmLeft(model);
        armRight = initArmRight(model);
        legs = initLegs(model);
        return this;
    }

    /**
     * Used to set the attributes for your necro entity. If not used, the minion
     * will get the default values
     * 
     * @param minion
     *            - the minion
     * @param location
     *            - the bodypart
     */
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        /*
         * head[0].setAttributes(2.0D, 32.0D, 0.0D, 0.0D, 0.0D);
         * torso[0].setAttributes(12.0D, 0.0D, 0.0D, 0.0D, 0.0D);
         * armLeft[0].setAttributes(2.0D, 0.0D, 0.0D, 0.5D, 1.0D);
         * armRight[0].setAttributes(2.0D, 0.0D, 0.0D, 0.5D, 1.0D);
         * legs[0].setAttributes(2.0D, 0.0D, 0.0D, 0.699D, 0.0D);
         */
    }

    /**
     * Use this method to set the rotation for every bodypart
     * 
     * @param entity
     *            - the entity
     * @param part
     *            - the current parts being animated
     * @param location
     *            - the location of the parts
     */
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, BodyPart[] part, BodyPartLocation location)
    {
    }

    /**
     * Called before rendering
     * 
     * @param entity
     *            - the entity
     * @param part
     *            - the current parts being rendered
     * @param location
     *            - the location of the parts
     * @param model
     *            - the model
     */
    @SideOnly(Side.CLIENT)
    public void preRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
    }

    /**
     * Called after rendering
     * 
     * @param entity
     *            - the entity
     * @param part
     *            - the current parts being rendered
     * @param location
     *            - the location of the parts
     * @param model
     *            - the model
     */
    @SideOnly(Side.CLIENT)
    public void postRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
    }

    /**
     * Deprecated since 1.1b, replaced the string that was used to get the
     * location with {@link BodypartLocation}
     */
    @Deprecated
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, BodyPart[] part, String location)
    {
    }

    /**
     * Deprecated since 1.1b, replaced the string that was used to get the
     * location with {@link BodypartLocation}
     */
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void preRender(Entity entity, BodyPart[] parts, String location, ModelBase model)
    {
    }

    /**
     * Deprecated since 1.1b, replaced the string that was used to get the
     * location with {@link BodypartLocation}
     */
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void postRender(Entity entity, BodyPart[] parts, String location, ModelBase model)
    {
    }

    /** The name for your mob */
    public String mobName;
    /** The location of the mobs texture file */
    public ResourceLocation texture;
    /** The item assigned to your mobs head */
    public ItemStack headItem;
    /** The item assigned to your mobs torso */
    public ItemStack torsoItem;
    /** The item assigned to your mobs arms */
    public ItemStack armItem;
    /** The item assigned to your mobs legs */
    public ItemStack legItem;
    /** The recipe assigned to your mobs head */
    public Object[] headRecipe;
    /** The recipe assigned to your mobs torso */
    public Object[] torsoRecipe;
    /** The recipe assigned to your mobs arms */
    public Object[] armRecipe;
    /** The recipe assigned to your mobs legs */
    public Object[] legRecipe;
    /** set to false if your mob doesn't have a head */
    public boolean hasHead;
    /** set to false if your mob doesn't have a torso */
    public boolean hasTorso;
    /** set to false if your mob doesn't have arms */
    public boolean hasArms;
    /** set to false if your mob doesn't have legs */
    public boolean hasLegs;
    /** The organs item (Brains, Heart, Muscle, Lungs, Skin) */
    public Item organs;
    /** The textures width */
    public int textureWidth;
    /** The textures height */
    public int textureHeight;
    /** i'm sure you can figure this one out... */
    protected boolean isNecromancyInstalled;
    /** Your entities head */
    public BodyPart[] head;
    /** Your entities torso */
    public BodyPart[] torso;
    /** Your entities armLeft */
    public BodyPart[] armLeft;
    /** Your entities armRight */
    public BodyPart[] armRight;
    /** Your entities legs */
    public BodyPart[] legs;
}
