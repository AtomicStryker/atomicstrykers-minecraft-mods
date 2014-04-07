package atomicstryker.ropesplus.common;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Settings_RopePlus
{
    public static int maxHookShotRopeLength;
    public static boolean disableBowHook = false;
	
	public static Configuration config;
    
	
	public static void InitSettings(File suggested)
	{		
		config = new Configuration(suggested);
		config.load();
		
		maxHookShotRopeLength = config.get(Configuration.CATEGORY_GENERAL, "max HookShot Rope Length", 50).getInt();
		disableBowHook = config.get(Configuration.CATEGORY_GENERAL, "disableBowHook", false, "Set this true if you intend to use a conflicting archery mod").getBoolean(false);
		
		config.save();
	}
}
