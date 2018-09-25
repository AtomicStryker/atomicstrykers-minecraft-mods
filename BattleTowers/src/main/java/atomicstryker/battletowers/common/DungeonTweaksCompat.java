package atomicstryker.battletowers.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Random;

import atomicstryker.battletowers.common.AS_WorldGenTower.TowerTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * this is backwards and modern dungeon tweaks compatible
 * 
 * @author jredfox
 *
 */
public class DungeonTweaksCompat
{

    public static boolean isLegacy = false;
    public static boolean isLoaded = false;

    /**
     * make backwards compatability when isLegacy becomes true
     */
    public static void legacyCheck()
    {
        isLoaded = Loader.isModLoaded("dungeontweaks");
        
        if(!isLoaded)
        {
            return;
        }
        
        try
        {
            Class c = Class.forName("com.EvilNotch.dungeontweeks.main.Events.EventDungeon$Post");
            isLegacy = true;
        }
        catch (Throwable t)
        {

        }
    }

    /**
     * register all dungeon tweaks mobs to anydim towers
     */
    public static void registerDungeons()
    {
        if (!isLoaded || isLegacy)
        {
            return;// I supported this mod in older versions
        }

        try
        {
            Method addDungeonMob = Class.forName("com.evilnotch.dungeontweeks.main.world.worldgen.mobs.DungeonMobs").getMethod("addDungeonMob", ResourceLocation.class, ResourceLocation.class,
                    int.class);
            for (AS_WorldGenTower.TowerTypes tower : AS_WorldGenTower.TowerTypes.values())
            {
                boolean nether = tower == TowerTypes.Netherrack;
                addDungeonMob.invoke(null, tower.getId(), new ResourceLocation("cave_spider"), 100);
                addDungeonMob.invoke(null, tower.getId(), new ResourceLocation("spider"), 90);
                addDungeonMob.invoke(null, tower.getId(), nether ? new ResourceLocation("wither_skeleton") : new ResourceLocation("skeleton"), 120);
                addDungeonMob.invoke(null, tower.getId(), new ResourceLocation("zombie"), 120);

                if (nether)
                {
                    addDungeonMob.invoke(null, tower.getId(), new ResourceLocation("blaze"), 20);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * fire the event based upon tower definitions
     */
    public static void fireDungeonSpawn(TileEntityMobSpawner spawner, World world, Random random, TowerTypes towerChosen)
    {
        ResourceLocation towerId = towerChosen.getId();
        if (isLegacy)
        {
            try
            {
                @SuppressWarnings("unchecked")
                Constructor<? extends Event> constructor = (Constructor<? extends Event>) Class.forName("com.EvilNotch.dungeontweeks.main.Events.EventDungeon$Post").getConstructor(TileEntity.class,
                        BlockPos.class, Random.class, ResourceLocation.class, World.class);
                Event event = constructor.newInstance(spawner, spawner.getPos(), world.rand, towerId, world);
                MinecraftForge.EVENT_BUS.post(event);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        else
        {
            try
            {
                Method fireDungeonTweaks = Class.forName("com.evilnotch.dungeontweeks.main.world.worldgen.mobs.DungeonMobs").getMethod("fireDungeonTweaks", ResourceLocation.class, TileEntity.class,
                        Random.class, World.class);
                fireDungeonTweaks.invoke(null, towerId, spawner, random, world);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

}
