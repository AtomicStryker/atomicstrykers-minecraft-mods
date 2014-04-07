package com.sirolf2009.necromancy.lib;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ConfigurationNecromancy
{
    public static boolean RenderSpecialScythe;

    public static int rarityNightCrawlers;
    public static int rarityIsaacs;

    public static void initProperties(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        RenderSpecialScythe = config.get("special scythes (only for a select number of people)", "Other", true).getBoolean(false);

        rarityNightCrawlers =
                config.get(Configuration.CATEGORY_GENERAL, "rarityNightcrawlers", 30,
                        "Randomly, one in THIS many Zombies will spawn as Nightcrawler instead").getInt();
        rarityIsaacs =
                config.get(Configuration.CATEGORY_GENERAL, "rarityIsaacs", 30, "Randomly, one in THIS many Skeletons will spawn as Isaac instead")
                        .getInt();

        if (config.hasChanged())
        {
            config.save();
        }
    }

}
