package com.sirolf2009.necromancy.lib;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ConfigurationNecromancy
{    
    public static boolean RenderSpecialScythe;

    public static void initProperties(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        
        RenderSpecialScythe = config.get("special scythes (only for a select number of people)", "Other", true).getBoolean(false);

        if (config.hasChanged())
        {
            config.save();
        }
    }

}
