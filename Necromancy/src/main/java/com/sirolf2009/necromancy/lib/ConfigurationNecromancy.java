package com.sirolf2009.necromancy.lib;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;

public class ConfigurationNecromancy
{

    public static boolean SearchMinionID;
    public static int MinionID;
    public static boolean SearchTeddyID;
    public static int TeddyID;
    public static boolean SearchIsaacID;
    public static int IsaacID;
    public static boolean SearchIsaacBloodID;
    public static int IsaacBloodID;
    public static boolean SearchIsaacHeadID;
    public static int IsaacHeadID;
    public static boolean SearchIsaacBodyID;
    public static int IsaacBodyID;
    public static boolean SearchNecroVillagerID;
    public static int NecroVillagerID;

    public static boolean Christmas;
    public static boolean RenderSpecialScythe;

    public static void initProperties(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        SearchMinionID = config.get("Entity", "Search for free Minion ID", true).getBoolean(true);
        MinionID = SearchMinionID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Minion ID", 123).getInt();
        SearchTeddyID = config.get("Entity", "Search for free Teddy ID", true).getBoolean(true);
        TeddyID = SearchTeddyID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Teddy ID", 124).getInt();
        SearchIsaacID = config.get("Entity", "Search for free Isaac ID", true).getBoolean(true);
        IsaacID = SearchIsaacID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Isaac ID", 125).getInt();
        SearchIsaacBloodID = config.get("Entity", "Search for free Isaac Blood ID", true).getBoolean(true);
        IsaacBloodID = SearchIsaacBloodID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Isaac Blood ID", 126).getInt();
        SearchIsaacBodyID = config.get("Entity", "Search for free Isaac Body ID", true).getBoolean(true);
        IsaacBodyID = SearchIsaacBodyID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Isaac Body ID", 127).getInt();
        SearchIsaacHeadID = config.get("Entity", "Search for free Isaac Head ID", true).getBoolean(true);
        IsaacHeadID = SearchIsaacHeadID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Isaac Head ID", 128).getInt();
        SearchNecroVillagerID = config.get("Entity", "Search for free Necro Villager ID", true).getBoolean(true);
        NecroVillagerID = SearchNecroVillagerID ? EntityRegistry.findGlobalUniqueEntityId() : config.get("entity", "Necro Villager ID", 129).getInt();

        Christmas = config.get("rendering", "christmas hats", false).getBoolean(false);

        RenderSpecialScythe = config.get("special scythes (only for a select number of people)", "Other", true).getBoolean(false);
        NecroVillagerID = config.get("NecroVillagerID", "Other", 666).getInt();

        if (config.hasChanged())
        {
            config.save();
        }
    }

}
