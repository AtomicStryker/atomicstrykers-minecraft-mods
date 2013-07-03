package ic2.advancedmachines.common;

import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;

public class IC2AudioSource
{
	private static boolean initFailed = false;
	
	@SuppressWarnings("rawtypes")
    private static Class audioManagerClass;
	private static Object audioManagerInstance;
	private static Method audioManagercreateSource;
	private static Method audioManagerremoveSource;
	private static Method audioManagerplayOnce;
	
	@SuppressWarnings("rawtypes")
    private static Class audioSourceClass;
	private static Method audioSourcePlay;
	private static Method audioSourceStop;
	private static Method audioSourceRemove;
	
	private Object audioSourceinstance;
	
	@SuppressWarnings("unchecked")
    public IC2AudioSource(TileEntity tEnt, String soundfile)
	{		
		if (audioManagerClass == null && !initFailed)
		{
			try
			{
				audioManagerClass = Class.forName("ic2.core.audio.AudioManagerClient");
				audioManagercreateSource = audioManagerClass.getDeclaredMethod("createSource", Object.class, String.class);
				audioManagerremoveSource = audioManagerClass.getDeclaredMethod("removeSources", Object.class);
				audioManagerplayOnce = audioManagerClass.getDeclaredMethod("playOnce", Object.class, String.class);
				
				audioManagerInstance = Class.forName("ic2.core.IC2").getDeclaredField("audioManager").get(null);
				
				audioSourceClass = Class.forName("ic2.core.audio.AudioSourceClient");
				audioSourcePlay = audioSourceClass.getDeclaredMethod("play", (Class[])null);
				audioSourceStop = audioSourceClass.getDeclaredMethod("stop", (Class[])null);
				audioSourceRemove = audioSourceClass.getDeclaredMethod("remove", (Class[])null);
				
				System.out.println("IC2AudioSource Init successful!");
				
				System.out.println("audioManagerClass: "+audioManagerClass);
				System.out.println("audioManagercreateSource: "+audioManagercreateSource);
				System.out.println("audioManagerplayOnce: "+audioManagerplayOnce);
				System.out.println("audioManagerInstance: "+audioManagerInstance);
				System.out.println("audioManagercreateSource type: "+audioManagercreateSource.toGenericString());
			}
			catch (Exception e)
			{
				System.out.println("IC2AudioSource Init failed, exception: "+e);
				initFailed = true;
				e.printStackTrace();
			}
		}
		
		if (!initFailed)
		{
			try
			{
			    
				audioSourceinstance = audioManagercreateSource.invoke(audioManagerInstance, tEnt, soundfile);
			}
			catch (Exception e)
			{
			    System.out.println("IC2AudioSource second Init failed, exception: "+e);
				audioSourceinstance = null;
				e.printStackTrace();
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
			catch (Exception e)
			{
			    e.printStackTrace();
			}
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	}
}
