package com.sirolf2009.necromancy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;

import com.sirolf2009.necromancy.block.BlockNecromancy;
import com.sirolf2009.necromancy.core.handler.ForgeEventHandler;
import com.sirolf2009.necromancy.core.handler.PacketHandler;
import com.sirolf2009.necromancy.core.proxy.CommonProxy;
import com.sirolf2009.necromancy.creativetab.CreativeTabNecro;
import com.sirolf2009.necromancy.entity.EntityNecromancy;
import com.sirolf2009.necromancy.generation.VillageCreationHandler;
import com.sirolf2009.necromancy.generation.WorldGenerator;
import com.sirolf2009.necromancy.generation.villagecomponent.ComponentVillageCemetery;
import com.sirolf2009.necromancy.item.ItemNecroSkull;
import com.sirolf2009.necromancy.item.ItemNecromancy;
import com.sirolf2009.necromancy.lib.ConfigurationNecromancy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;
import com.sirolf2009.necromancy.network.PacketDispatcher;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

@Mod(modid = ReferenceNecromancy.MOD_ID, name = ReferenceNecromancy.MOD_NAME, version = ReferenceNecromancy.MOD_VERSION)
public class Necromancy
{

    public static final CreativeTabs tabNecromancy = new CreativeTabNecro(CreativeTabs.getNextID(), "Necromancy", 1)
            .setBackgroundImageName("necro_gui.png");
    public static final CreativeTabs tabNecromancyBodyParts = new CreativeTabNecro(CreativeTabs.getNextID(), "BodyParts", 2)
            .setBackgroundImageName("necro_gui.png");

    public int scentProgram;

    public static List<String> specialFolk = new ArrayList<String>();

    public static int maxSpawn = -1;

    public static Logger loggerNecromancy;

    public static PacketHandler packetHandler = new PacketHandler();
    public static ForgeEventHandler eventHandler = new ForgeEventHandler();
    public static VillageCreationHandler villageHandler = new VillageCreationHandler();

    @SidedProxy(clientSide = "com.sirolf2009.necromancy.core.proxy.ClientProxy", serverSide = "com.sirolf2009.necromancy.core.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Instance(ReferenceNecromancy.MOD_ID)
    public static Necromancy instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        loggerNecromancy = Logger.getLogger(ReferenceNecromancy.MOD_ID);
        // loggerNecromancy.setParent(FMLLog.getLogger());

        ConfigurationNecromancy.initProperties(event);

        PacketDispatcher.init("NecromancyMod", packetHandler, packetHandler);

        FMLCommonHandler.instance().bus().register(eventHandler);
        MinecraftForge.EVENT_BUS.register(eventHandler);

        proxy.preInit();
        
        MapGenStructureIO.func_143031_a(ComponentVillageCemetery.class, "NeViCem");

        try
        {
            URL url = new URL("https://dl.dropboxusercontent.com/u/50553915/necromancy/specialFolk.txt");
            Scanner s = new Scanner(url.openStream());
            while (s.hasNext())
            {
                specialFolk.add(s.nextLine());
            }
            s.close();
        }
        catch (IOException e)
        {
            System.err.println("not connected to the internet, special scythes are de-activated");
        }

        ItemNecromancy.initItems();
        EntityNecromancy.initEntities();
        BlockNecromancy.initBlocks();
        
        villageHandler = new VillageCreationHandler();
        VillagerRegistry.instance().registerVillageCreationHandler(villageHandler);
        ArrayList<Class<PacketHandler>> villageComponentsList = new ArrayList<Class<PacketHandler>>();
        villageComponentsList.add(PacketHandler.class);
        
        GameRegistry.registerWorldGenerator(new WorldGenerator(), 5);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        ItemNecroSkull.initSkulls();
        proxy.refreshTextures();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        eventHandler.initCommands(event);
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        if (new File("server.properties").exists())
        {
            PropertyManager manager = new PropertyManager(new File("server.properties"));
            maxSpawn = manager.getIntProperty("max_minion_spawn", -1);
        }
    }
}