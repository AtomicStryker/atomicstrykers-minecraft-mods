package ic2.advancedmachines.common;

import net.minecraft.src.*;
import java.lang.reflect.*;

public class IC2AudioSource
{
	private static boolean initFailed = false;
	
	private static Class audioManagerClass;
	private static Object audioManagerInstance;
	private static Method audioManagercreateSource;
	private static Method audioManagerremoveSource;
	private static Method audioManagerplayOnce;
	
	private static Class audioSourceClass;
	private static Method audioSourcePlay;
	private static Method audioSourceStop;
	private static Method audioSourceRemove;
	
	private Object audioSourceinstance;
	
	public IC2AudioSource(TileEntity tEnt, String soundfile)
	{		
		if (audioManagerClass == null && !initFailed)
		{
			try
			{
				audioManagerClass = Class.forName("ic2.common.AudioManager");
				audioManagercreateSource = audioManagerClass.getDeclaredMethod("createSource", Object.class, String.class);
				audioManagerremoveSource = audioManagerClass.getDeclaredMethod("removeSources", Object.class);
				audioManagerplayOnce = audioManagerClass.getDeclaredMethod("playOnce", Object.class, String.class);
				
				audioManagerInstance = Class.forName("ic2.common.IC2").getDeclaredField("audioManager").get(null);
				
				audioSourceClass = Class.forName("ic2.common.AudioSource");
				audioSourcePlay = audioSourceClass.getDeclaredMethod("play", (Class[])null);
				audioSourceStop = audioSourceClass.getDeclaredMethod("stop", (Class[])null);
				audioSourceRemove = audioSourceClass.getDeclaredMethod("remove", (Class[])null);
				
				System.out.println("IC2AudioSource Init successful!");
			}
			catch (Exception e)
			{
				System.out.println("IC2AudioSource Init failed, exception: "+e);
				initFailed = true;
			}
		}
		
		if (!initFailed)
		{
			try
			{
				this.audioSourceinstance = audioManagercreateSource.invoke(audioManagerClass, tEnt, soundfile);
			}
			catch (IllegalAccessException e)
			{
				this.audioSourceinstance = null;
			}
			catch (IllegalArgumentException e)
			{
				this.audioSourceinstance = null;
			}
			catch (InvocationTargetException e)
			{
				this.audioSourceinstance = null;
			}
		}
	}
	
	public static void removeSource(Object audioSource)
	{
		if (audioManagerClass != null)
		{
			try
			{
				audioManagerremoveSource.invoke(audioManagerInstance, audioSource);
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public static void playOnce(TileEntity tEnt, String soundFile)
	{
		if (audioManagerClass != null)
		{
			try
			{
				audioManagerplayOnce.invoke(audioManagerInstance, tEnt, soundFile);
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void play()
	{
		if (audioSourceinstance != null)
		{
			try
			{
				audioSourcePlay.invoke(audioSourceinstance, (Object[])null);
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void stop()
	{
		if (audioSourceinstance != null)
		{
			try
			{
				audioSourceStop.invoke(audioSourceinstance, (Object[])null);
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void remove()
	{
		if (audioSourceinstance != null)
		{
			try
			{
				audioSourceRemove.invoke(audioSourceinstance, (Object[])null);
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
}
