package atomicstryker.simplyhax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

@Mod(modid = "SimplyHaxFlying", name = "Simply Hax Flying", version = "1.7.10")
public class SimplyHaxFlying
{

    private Minecraft mc = null;
    private EntityPlayer player = null;
    private PlayerCapabilities pcb = null;

    private File configfile = null;
    private float sprintspeed = 0.75f;
    private float maxflyspeed = 1.0f;
    private float defaultWalkSpeed = 0.3f;

    private KeyBinding sprintButton;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        configfile = evt.getSuggestedConfigurationFile();
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        initSettings();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        sprintButton = new KeyBinding("Simply Hax Sprint", Keyboard.KEY_LSHIFT, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(sprintButton);
    }

    private void initSettings()
    {
        Properties properties = new Properties();

        if (!configfile.exists())
        {
            System.out.println("No SimplyHaxFlying config found, trying to create...");

            try
            {
                configfile.createNewFile();
                properties.load(new FileInputStream(configfile));
            }
            catch (IOException localIOException)
            {
                System.out.println("SimplyHaxFlying config exception: " + localIOException);
            }

            properties.setProperty("maxflyspeed", "1.0");
            properties.setProperty("maxrunspeed", "0.5");

            try
            {
                FileOutputStream fostream = new FileOutputStream(configfile);
                properties.store(fostream, "Derp it's the key settings");
                fostream.close();
            }
            catch (IOException localIOException)
            {
                System.out.println("SimplyHaxFlying config exception: " + localIOException);
            }
        }

        System.out.println("SimplyHaxFlying config found, proceeding...");

        try
        {
            properties.load(new FileInputStream(configfile));
        }
        catch (IOException localIOException)
        {
            System.out.println("SimplyHaxFlying config exception: " + localIOException);
        }

        maxflyspeed = Float.parseFloat(properties.getProperty("maxflyspeed", "1.0"));
        sprintspeed = Float.parseFloat(properties.getProperty("sprintspeed", "0.75"));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (mc == null)
            {
                mc = FMLClientHandler.instance().getClient();
            }

            if (mc.thePlayer != player)
            {
                player = mc.thePlayer;
                pcb = player.capabilities;
                defaultWalkSpeed = pcb.getWalkSpeed();
            }

            pcb.allowFlying = true;

            if (sprintButton.getIsKeyPressed())
            {
                pcb.setFlySpeed(maxflyspeed);
                pcb.setPlayerWalkSpeed(sprintspeed);
            }
            else
            {
                pcb.setFlySpeed(defaultWalkSpeed);
                pcb.setPlayerWalkSpeed(defaultWalkSpeed);
            }
        }
    }
    
    @SubscribeEvent
    public void onFall(LivingFallEvent event)
    {
        if (event.entity == player)
        {
            event.setCanceled(true);
        }
    }
}
