package atomicstryker.magicyarn.client;

import java.io.*;
import java.util.*;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public class AS_Settings_MagicYarn
{	
	public static String sTriggerKey = "J";
	public static int triggerKey = Keyboard.getKeyIndex(sTriggerKey);
	
	public static void initSettings(File configfile)
	{
		Properties properties = new Properties();
		
		if (configfile.exists())
		{
			try
			{
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("MagicYarn config exception: "+localIOException);
			}
			
			sTriggerKey = properties.getProperty("sTriggerKey");
			triggerKey = Keyboard.getKeyIndex(sTriggerKey);
		}
		else
		{
			System.out.println("No MagicYarn config found, trying to create...");
		
			try
			{
				configfile.createNewFile();
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("MagicYarn config exception: "+localIOException);
			}
			
			properties.setProperty("sTriggerKey", "J");
			
			try
			{
				FileOutputStream fostream = new FileOutputStream(configfile);
				properties.store(fostream, "Change the Magic Yarn multiplayer trigger key, if you want to.");
			}
			catch (IOException localIOException)
			{
				System.out.println("MagicYarn config exception: "+localIOException);
			}
		}
	}
}
