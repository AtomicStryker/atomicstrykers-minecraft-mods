package atomicstryker.simplyhax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@Mod(modid = "SimplyHaxVision", name = "Simply Hax Vision", version = "1.8")
public class SimplyHaxVision
{
	private final static String modname = "Vision";
	private static File configfile;
	private static Minecraft mcinstance;
	
	private static String visionkey = "I";
	private static int ivisionkey = Keyboard.getKeyIndex(visionkey);
	private static float visiondist = 4.0F;
	
	private static boolean visionActive = false;
	private boolean rendererReplaced = false;
	private static RenderGlobal original;
	private static RenderGlobal replacement;
	private ItemStack item;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        configfile = evt.getSuggestedConfigurationFile();
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (mcinstance != FMLClientHandler.instance().getClient())
            {
                initSettings();
                mcinstance = FMLClientHandler.instance().getClient();
                setupRenderer();
            }
        
            if (mcinstance.theWorld != null && mcinstance.thePlayer != null)
            {
                visionActive = (Keyboard.isKeyDown(ivisionkey));
                if (IsMenuOpen()) visionActive = false;
                
                if (rendererReplaced != visionActive)
                {
                    rendererReplaced = visionActive;
                    copyAllClassFields(RenderGlobal.class, rendererReplaced ? original : replacement, rendererReplaced ? replacement : original);
                    mcinstance.renderGlobal = rendererReplaced ? replacement : original;
                }
                
                if (mcinstance.thePlayer.getCurrentEquippedItem() != item)
                {
                    item = mcinstance.thePlayer.getCurrentEquippedItem();
                    if (item != null)
                    {
                        System.out.printf("[SimplyHaxVision] %s swapped Item, now: [%s] damage: %d\n", 
                                mcinstance.thePlayer.getDisplayNameString(), item.getUnlocalizedName(), item.getItemDamage());
                    }
                }
            }
        }
    }
	
	public static void preRender()
	{
		if (isAlive(mcinstance.thePlayer) && visionActive)
		{
			obliqueNearPlaneClip(0.0F, 0.0F, -1F, -visiondist);
		}
	}
	
    private static void obliqueNearPlaneClip(float f, float f1, float f2, float f3)
    {	
        float af[] = new float[16];
        FloatBuffer floatbuffer = ByteBuffer.allocateDirect(af.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        GL11.glGetFloat(2983 /*GL_PROJECTION_MATRIX*/, floatbuffer);
        floatbuffer.get(af).rewind();
        float f4 = (sgn(f) + af[8]) / af[0];
        float f5 = (sgn(f1) + af[9]) / af[5];
        float f6 = -1F;
        float f7 = (1.0F + af[10]) / af[14];
        float f8 = f * f4 + f1 * f5 + f2 * f6 + f3 * f7;
        af[2] = f * (2.0F / f8);
        af[6] = f1 * (2.0F / f8);
        af[10] = f2 * (2.0F / f8) + 1.0F;
        af[14] = f3 * (2.0F / f8);
        floatbuffer.put(af).rewind();
        GL11.glMatrixMode(5889 /*GL_PROJECTION*/);
        GL11.glLoadMatrix(floatbuffer);
        GL11.glMatrixMode(5888 /*GL_MODELVIEW0_ARB*/);
    }
    
    private static float sgn(float f)
    {
        return f<0f ? -1f : (f>0f ? 1f : 0f);
    }
	
	private void setupRenderer()
	{
		SimplyHaxVisionRenderer replacementhax = new SimplyHaxVisionRenderer(mcinstance);
		replacement = (RenderGlobal)replacementhax;
		original = mcinstance.renderGlobal;
		
		copyAllClassFields(RenderGlobal.class, original, replacement);
	}
	
    private void copyAllClassFields(Class<RenderGlobal> copiedclass, Object originalinstance, Object newinstance)
    {
        try
        {
            Field[] fieldarray = copiedclass.getDeclaredFields();
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
			
			for(int i = 0; i < fieldarray.length; i++)
			{
			    modifiersField.setInt(fieldarray[i], fieldarray[i].getModifiers() & ~Modifier.FINAL);
			    
				fieldarray[i].setAccessible(true);
				Object data = fieldarray[i].get(originalinstance);
				fieldarray[i].set(newinstance, data);
			}
        }
        catch(Exception exception)
        {
			System.out.println("SimplyHax"+modname+" exception: "+exception);
        }
    }
	
	public static void initSettings()
	{
		Properties properties = new Properties();
		
		if (configfile.exists())
		{
			System.out.println("SimplyHax"+modname+" config found, proceeding...");
		
			try
			{
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHax"+modname+" config exception: "+localIOException);
			}
			
			visionkey = properties.getProperty("visionkey", "I");
			ivisionkey = Keyboard.getKeyIndex(visionkey);
			visiondist = Float.parseFloat(properties.getProperty("visiondist", "4.0"));
		}
		else
		{
			System.out.println("No SimplyHax"+modname+" config found, trying to create...");
		
			try
			{
				configfile.createNewFile();
				properties.load(new FileInputStream(configfile));
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHax"+modname+" config exception: "+localIOException);
			}
			
			properties.setProperty("visionkey", "I");
			properties.setProperty("visiondist", "4.0");
			
			try
			{
				FileOutputStream fostream = new FileOutputStream(configfile);
				properties.store(fostream, "Settings");
			}
			catch (IOException localIOException)
			{
				System.out.println("SimplyHax"+modname+" config exception: "+localIOException);
			}
		}
	}
	
    private static boolean isAlive(EntityLivingBase ent)
    {
        return ent != null && ent.getHealth() > 0 && !ent.isDead;
    }
	
	private boolean IsMenuOpen()
	{
		return mcinstance.currentScreen != null;
	}
}
