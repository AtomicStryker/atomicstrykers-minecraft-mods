package com.sirolf2009.necromancy.entity;

import java.awt.Color;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityCaveSpider;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityChicken;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityCow;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityCreeper;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityEnderman;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityIsaac;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityMooshroom;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityPig;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityPigZombie;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntitySheep;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntitySkeleton;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntitySpider;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntitySquid;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityVillager;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityWitch;
import com.sirolf2009.necromancy.entity.necroapi.NecroEntityZombie;

import cpw.mods.fml.common.registry.EntityRegistry;

public class EntityNecromancy
{

    public static int TeddyID;
    public static int IsaacID;

    public static void initEntities()
    {
        TeddyID = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityTeddy.class, "teddyNecro", TeddyID, new Color(99, 69, 29).getRGB(), Color.red.getRGB());
        EntityRegistry.registerModEntity(EntityTeddy.class, "teddyNecro", 0, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerGlobalEntityID(EntityNightCrawler.class, "NightCrawler", EntityRegistry.findGlobalUniqueEntityId(),
                new Color(6, 6, 6).getRGB(), new Color(13, 13, 13).getRGB());
        EntityRegistry.registerModEntity(EntityNightCrawler.class, "NightCrawler", 1, Necromancy.instance, 25, 5, true);
        EntityRegistry.addSpawn(EntityNightCrawler.class, 1, 1, 1, EnumCreatureType.monster, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills,
                BiomeGenBase.coldTaiga, BiomeGenBase.coldTaigaHills, BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.extremeHills,
                BiomeGenBase.forest, BiomeGenBase.jungle, BiomeGenBase.megaTaiga, BiomeGenBase.plains, BiomeGenBase.savanna, BiomeGenBase.swampland,
                BiomeGenBase.taiga);

        IsaacID = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityIsaacNormal.class, "IsaacNormal", IsaacID, new Color(6, 6, 6).getRGB(),
                new Color(204, 153, 153).getRGB());
        EntityRegistry.registerModEntity(EntityIsaacNormal.class, "IsaacNormal", 2, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerGlobalEntityID(EntityIsaacBlood.class, "IsaacBlood", EntityRegistry.findGlobalUniqueEntityId(),
                new Color(16, 6, 6).getRGB(), new Color(214, 153, 153).getRGB());
        EntityRegistry.registerModEntity(EntityIsaacBlood.class, "IsaacBlood", 3, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerGlobalEntityID(EntityIsaacHead.class, "IsaacHead", EntityRegistry.findGlobalUniqueEntityId(),
                new Color(26, 6, 6).getRGB(), new Color(214, 153, 153).getRGB());
        EntityRegistry.registerModEntity(EntityIsaacHead.class, "IsaacHead", 4, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerGlobalEntityID(EntityIsaacBody.class, "IsaacBody", EntityRegistry.findGlobalUniqueEntityId(),
                new Color(36, 6, 6).getRGB(), new Color(214, 153, 153).getRGB());
        EntityRegistry.registerModEntity(EntityIsaacBody.class, "IsaacBody", 5, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerGlobalEntityID(EntityMinion.class, "minionNecro", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerModEntity(EntityMinion.class, "minionNecro", 6, Necromancy.instance, 25, 5, true);

        EntityRegistry.registerModEntity(EntityTear.class, "TearNormal", 7, Necromancy.instance, 144, 2, true);
        EntityRegistry.registerModEntity(EntityTearBlood.class, "TearBlood", 8, Necromancy.instance, 144, 2, true);

        NecroEntityRegistry.RegisterEntity(new NecroEntitySkeleton());
        NecroEntityRegistry.RegisterEntity(new NecroEntityZombie());
        NecroEntityRegistry.RegisterEntity(new NecroEntityPig());
        NecroEntityRegistry.RegisterEntity(new NecroEntityCow());
        NecroEntityRegistry.RegisterEntity(new NecroEntityPigZombie());
        NecroEntityRegistry.RegisterEntity(new NecroEntityCreeper());
        NecroEntityRegistry.RegisterEntity(new NecroEntitySpider());
        NecroEntityRegistry.RegisterEntity(new NecroEntityEnderman());
        NecroEntityRegistry.RegisterEntity(new NecroEntityIsaac());
        NecroEntityRegistry.RegisterEntity(new NecroEntityChicken());
        NecroEntityRegistry.RegisterEntity(new NecroEntityMooshroom());
        NecroEntityRegistry.RegisterEntity(new NecroEntityVillager());
        NecroEntityRegistry.RegisterEntity(new NecroEntityWitch());
        NecroEntityRegistry.RegisterEntity(new NecroEntitySquid());
        NecroEntityRegistry.RegisterEntity(new NecroEntityCaveSpider());
        NecroEntityRegistry.RegisterEntity(new NecroEntitySheep());
    }
}
