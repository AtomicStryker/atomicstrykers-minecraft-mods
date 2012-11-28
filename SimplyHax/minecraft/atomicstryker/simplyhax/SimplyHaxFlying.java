package atomicstryker.simplyhax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityRenderer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "SimplyHaxFlying", name = "Simply Hax Flying", version = "1.4.4")
public class SimplyHaxFlying
{
    private long lastTime;
	private boolean buttonCD = false;
	
	private static boolean isFlying = false;
	private boolean isSprinting = false;
	
	private static File configfile;
	private static String togglekey = "R";
	private static int itogglekey = Keyboard.getKeyIndex(togglekey);
	private static String sprintkey = "LSHIFT";
	private static int isprintkey = Keyboard.getKeyIndex(sprintkey);
	private static String keyupwards = "SPACE";
	private static int ikeyupwards = Keyboard.getKeyIndex(keyupwards);
	private static String keydownwards = "LCONTROL";
	private static int ikeydownwards = Keyboard.getKeyIndex(keydownwards);
	private static double maxflyspeed = 1.0D;
	private static float fovModifier = 20F;
	
	private float distanceWalkedModified;
	private static Minecraft mcinstance;
	
	private double modMotionX;
	private double modMotionZ;
	private double modposY;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent evt)
	{
	    configfile = evt.getSuggestedConfigurationFile();
	}
	
    @Init
    public void load(FMLInitializationEvent evt)
    {
        lastTime = System.currentTimeMillis();
        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
        InitSettings();
    }
	
	public static void InitSettings()
	{
		Properties properties = new Properties();
		
		if (configfile.exists())
		{
			System.out.println("SimplyHaxFlying config found, proceeding...");
		
			try
			{
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHaxFlying config exception: "+localIOException);
			}
			
			togglekey = properties.getProperty("flyingToggleKey", "R");
			itogglekey = Keyboard.getKeyIndex(togglekey);
			sprintkey = properties.getProperty("sprintKey", "LSHIFT");
			isprintkey = Keyboard.getKeyIndex(sprintkey);
			keydownwards = properties.getProperty("keydownwards", "LCONTROL");
			ikeydownwards = Keyboard.getKeyIndex(keydownwards);
			keyupwards = properties.getProperty("keyupwards", "SPACE");
			ikeyupwards = Keyboard.getKeyIndex(keyupwards);
			maxflyspeed = Double.parseDouble(properties.getProperty("maxflyspeed", "1.0"));
			fovModifier = Float.parseFloat(properties.getProperty("fovModifier", "20.0"));
		}
		else
		{
			System.out.println("No SimplyHaxFlying config found, trying to create...");
		
			try
			{
				configfile.createNewFile();
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHaxFlying config exception: "+localIOException);
			}
			
			properties.setProperty("flyingToggleKey", "R");
			properties.setProperty("sprintKey", "LSHIFT");
			properties.setProperty("keydownwards", "LCONTROL");
			properties.setProperty("keyupwards", "SPACE");
			properties.setProperty("maxflyspeed", "1.0");
			properties.setProperty("fovModifier", "20.0");
			
			try
			{
				FileOutputStream fostream = new FileOutputStream(configfile);
				properties.store(fostream, "Derp it's the key settings");
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHaxFlying config exception: "+localIOException);
			}
		}
	}
	
	private class TickHandler implements ITickHandler
	{
		private final EnumSet ticks;
		public TickHandler()
		{
			ticks = EnumSet.of(TickType.RENDER);
		}

		@Override
		public void tickStart(EnumSet<TickType> type, Object... tickData)
		{
		}

		@Override
		public void tickEnd(EnumSet<TickType> type, Object... tickData)
		{
			if (mcinstance != FMLClientHandler.instance().getClient())
			{
				mcinstance = FMLClientHandler.instance().getClient();
			}
		
			if (mcinstance.theWorld != null && mcinstance.thePlayer != null)
			{			
				long l = System.currentTimeMillis();
				if(l > lastTime + 200L)
				{
					// do time relevant stuff	
					buttonCD = false;
				}
				
				// do every tick stuff
				
				isSprinting = (Keyboard.isKeyDown(isprintkey) && !IsMenuOpen());
				
				if (!buttonCD
				&& Keyboard.getEventKeyState()
				&& Keyboard.getEventKey() == itogglekey
				&& !IsMenuOpen())
				{
					lastTime = l;
					buttonCD = true;
					
					isFlying = !isFlying;
					
					if (isFlying)
					{
						modposY = mcinstance.thePlayer.posY;
						distanceWalkedModified = mcinstance.thePlayer.distanceWalkedModified;
					}
				}
				
				if (IsAlive(mcinstance.thePlayer))
				{
					if (isSprinting)
					{
						MakeHaste(mcinstance.thePlayer);
						
						float fov = GetFOV();
						if (GetAbsSpeed(mcinstance.thePlayer) > 0.1D)
						{
							if (fov < fovModifier)
							{
								SetFOV(fov + (fovModifier*0.25F));
							}
						}
						else if (fov > 0F)
						{
							SetFOV(fov - (fovModifier*0.25F));
						}
					}
					else
					{
						modMotionX = mcinstance.thePlayer.motionX;
						modMotionZ = mcinstance.thePlayer.motionZ;
						
						float fov = GetFOV();
						if (fov > 0F)
						{
							SetFOV(fov - (fovModifier*0.25F));
						}
					}
					
					if (isFlying)
					{
						MakeFly(mcinstance.thePlayer);
					}
				}
			}
		}

		@Override
		public EnumSet<TickType> ticks()
		{
			return ticks;
		}

		@Override
		public String getLabel()
		{
			return "SimplyHaxF";
		}
		
	}
	
	private void SetFOV(float setting)
	{
		try
		{
			Field[] fieldarray = EntityRenderer.class.getDeclaredFields();
			fieldarray[19].setAccessible(true);
			fieldarray[19].set(mcinstance.entityRenderer, setting);
		}
		catch(IllegalAccessException illegalaccessexception)
		{
			System.out.println("SimplyHaxFlying exception: "+illegalaccessexception);
		}
		catch(SecurityException securityexception)
		{
			System.out.println("SimplyHaxFlying exception: "+securityexception);
		}
	}
	
	private float GetFOV()
	{
		try
		{
			Field[] fieldarray = EntityRenderer.class.getDeclaredFields();
			fieldarray[19].setAccessible(true);
			return Float.valueOf((Float)fieldarray[19].get(mcinstance.entityRenderer));
		}
		catch(IllegalAccessException illegalaccessexception)
		{
			System.out.println("SimplyHaxFlying exception: "+illegalaccessexception);
		}
		catch(SecurityException securityexception)
		{
			System.out.println("SimplyHaxFlying exception: "+securityexception);
		}
		
		return 0F;
	}
	
	private void MakeHaste(EntityPlayerSP entityplayer)
	{
		float rotationMovement = (float)((Math.atan2(entityplayer.motionX, entityplayer.motionZ) * 180D) / 3.1415D);
		float rotationLook = entityplayer.rotationYaw;
		
		// god fucking dammit notch
		if(rotationLook > 360F)
		{
			rotationLook -= (rotationLook % 360F) * 360F;
		}
		else if(rotationLook < 0F)
		{
			rotationLook += ((rotationLook * -1) % 360F) * 360F;
		}
		
		// god fucking dammit, NOTCH
		if (Math.abs(rotationMovement+rotationLook) > 10F)
		{
			rotationLook -= 360F;
		}
		
		double entspeed = GetAbsSpeed(entityplayer);
		
		// unfuck velocity lock
		if (Math.abs(rotationMovement+rotationLook) > 10F)
		{
			modMotionX = mcinstance.thePlayer.motionX;
			modMotionZ = mcinstance.thePlayer.motionZ;
		}
		
		if (!isFlying && (entspeed < 0.3D))
		{
			if (GetAbsModSpeed() > 0.6D || !(entityplayer.onGround))
			{
				modMotionX /= 1.55;
				modMotionZ /= 1.55;
			}
		
			modMotionX *= 1.5;
			entityplayer.motionX = modMotionX;
			modMotionZ *= 1.5;
			entityplayer.motionZ = modMotionZ;
		}
		else if (isFlying && (entspeed < maxflyspeed))
		{
			if (GetAbsModSpeed() > maxflyspeed*3)
			{
				modMotionX /= 2.55;
				modMotionZ /= 2.55;
			}
		
			modMotionX *= 2.5;
			entityplayer.motionX = modMotionX;
			modMotionZ *= 2.5;
			entityplayer.motionZ = modMotionZ;
		}
	}
	
	private void MakeFly(EntityPlayerSP entityplayer)
	{
		entityplayer.distanceWalkedModified = distanceWalkedModified;	// fix the step sounds
		
		if (Keyboard.isKeyDown(ikeyupwards) && !IsMenuOpen())
		{
			entityplayer.motionY = (isSprinting ? 1D : 0.35D);
			modposY = entityplayer.posY;
		}
		else if (Keyboard.isKeyDown(ikeydownwards) && !IsMenuOpen())
		{
			entityplayer.motionY = (isSprinting ? -1D : -0.35D);
			modposY = entityplayer.posY;
		}
		else	// all vertical movement stop
		{
			if(entityplayer.posY < modposY)
			{
				entityplayer.motionY = (modposY - entityplayer.posY);
			}
			else
			{
				entityplayer.motionY = 0;
			}
		}
	}
	
	public static void preMoveEntityPlayerSP()
	{
        if (isFlying)
        {
            mcinstance.thePlayer.motionY = 0;
            mcinstance.thePlayer.fallDistance = 0F; // fix the falling
            mcinstance.thePlayer.onGround = true; // because we say so!
        }
	}
	
	private double GetAbsSpeed(Entity ent)
	{
		return Math.sqrt(ent.motionX*ent.motionX + ent.motionZ*ent.motionZ);
	}
	
	private double GetAbsModSpeed()
	{
		return Math.sqrt(modMotionX*modMotionX + modMotionZ*modMotionZ);
	}
	
	private boolean IsAlive(EntityLiving ent)
	{
		return ent != null && ent.getHealth() > 0 && !ent.isDead;
	}
	
	private boolean IsMenuOpen()
	{
		return mcinstance.currentScreen != null;
	}
}
