package com.sirolf2009.necromancy.lib;

import net.minecraft.util.ResourceLocation;

public class ReferenceNecromancy
{
    public static final String MOD_ID = "necromancy";
    public static final String MOD_NAME = "Necromancy";
    public static final String MOD_VERSION = "1.5";

    public static final String LOC_RESOURCES_SOUNDS = "/sounds";
    public static final String LOC_RESOURCES_TEXTURES = "textures";
    public static final String LOC_RESOURCES_TEXTURES_BLOCKS = LOC_RESOURCES_TEXTURES + "/blocks";
    public static final String LOC_RESOURCES_TEXTURES_ENTITIES = LOC_RESOURCES_TEXTURES + "/entities";
    public static final String LOC_RESOURCES_TEXTURES_GUIS = LOC_RESOURCES_TEXTURES + "/guis";
    public static final String LOC_RESOURCES_TEXTURES_ITEMS = LOC_RESOURCES_TEXTURES + "/items";
    public static final String LOC_RESOURCES_TEXTURES_ITEMS_BODYPARTS = LOC_RESOURCES_TEXTURES_ITEMS + "/bodyparts";
    public static final String LOC_RESOURCES_TEXTURES_MODELS = LOC_RESOURCES_TEXTURES + "/models";
    public static final String LOC_MODELS = "/models";

    public static final ResourceLocation TEXTURES_ENTITIES_NECROMANCER = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES
            + "/villagernecro.png");
    public static final ResourceLocation TEXTURES_ENTITIES_NIGHTCRAWLER = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES
            + "/nightcrawler.png");
    public static final ResourceLocation TEXTURES_ENTITIES_TEDDY = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES + "/teddy.png");
    public static final ResourceLocation TEXTURES_ENTITIES_ISAAC = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES + "/isaac.png");
    public static final ResourceLocation TEXTURES_ENTITIES_ISAACBLOOD = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES
            + "/isaacblood.png");
    public static final ResourceLocation TEXTURES_ENTITIES_TEAR = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES + "/tearblood.png");
    public static final ResourceLocation TEXTURES_ENTITIES_TEARBLOOD = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_ENTITIES + "/tear.png");

    public static final ResourceLocation TEXTURES_MODELS_SCYTHE = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS + "/scythe.png");
    public static final ResourceLocation TEXTURES_MODELS_SCYTHEBONE = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS + "/scythebone.png");
    public static final ResourceLocation TEXTURES_MODELS_NECRONOMICON = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS
            + "/necronomicon.png");
    public static final ResourceLocation TEXTURES_MODELS_ALTAR = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS + "/altartexture.png");
    public static final ResourceLocation TEXTURES_MODELS_SEWING = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS + "/sewingtexture.png");
    public static final ResourceLocation TEXTURES_MODELS_SCENTBURNER = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_MODELS
            + "/scentburnertexture.png");

    public static final ResourceLocation TEXTURES_GUI_ALTAR = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_GUIS + "/altargui.png");
    public static final ResourceLocation TEXTURES_GUI_SEWING = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_GUIS + "/sewinggui.png");
    public static final ResourceLocation TEXTURES_GUI_SCENTBURNER = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES_GUIS + "/scentburnergui.png");

    public static final ResourceLocation TEXTURES_PARTICLES = new ResourceLocation(MOD_ID, LOC_RESOURCES_TEXTURES + "/particles.png");

    public static final ResourceLocation TEXTURES_MISC_CHRISTMASHAT = new ResourceLocation("MOD_ID", LOC_RESOURCES_TEXTURES_ENTITIES
            + "/christmashat.png");
}
