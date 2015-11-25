package atomicstryker.kenshiro.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import atomicstryker.kenshiro.common.network.AnimationPacket;
import atomicstryker.kenshiro.common.network.BlockPunchedPacket;
import atomicstryker.kenshiro.common.network.EntityKickedPacket;
import atomicstryker.kenshiro.common.network.EntityPunchedPacket;
import atomicstryker.kenshiro.common.network.HandshakePacket;
import atomicstryker.kenshiro.common.network.KenshiroStatePacket;
import atomicstryker.kenshiro.common.network.NetworkHelper;
import atomicstryker.kenshiro.common.network.SoundPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

@Mod(modid = "AS_Kenshiro", name = "Kenshiro Mod", version = "1.2.1")
public class KenshiroMod
{
    @SidedProxy(clientSide = "atomicstryker.kenshiro.client.ClientProxy", serverSide = "atomicstryker.kenshiro.common.CommonProxy")
    public static CommonProxy proxy;

    @Instance("AS_Kenshiro")
    private static KenshiroMod instance;

    public static KenshiroMod instance()
    {
        return instance;
    }

    public NetworkHelper networkHelper;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        networkHelper =
                new NetworkHelper("AS_KM", AnimationPacket.class, BlockPunchedPacket.class, EntityKickedPacket.class, EntityPunchedPacket.class,
                        HandshakePacket.class, KenshiroStatePacket.class, SoundPacket.class);
        proxy.preInit();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.load();
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new KenshiroServer());
    }

    public void stopSkeletonShooter(EntitySkeleton skelly)
    {
        Field[] taskfields = skelly.tasks.getClass().getDeclaredFields();

        ArrayList<?> list = null;
        try
        {
            for (int i = 1; i <= 2; i++)
            {
                taskfields[i].setAccessible(true);
                list = (ArrayList<?>) taskfields[i].get(skelly.tasks);

                Iterator<?> iter = list.iterator();
                while (iter.hasNext())
                {
                    Object task = iter.next();
                    if (task.getClass().getDeclaredFields()[1].get(task) instanceof EntityAIArrowAttack)
                    {
                        System.out.println("Found and removed EntityAIArrowAttack Task!");
                        list.remove(task);
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("TaskHack Exception: " + e);
        }
    }

    public void stopCreeperExplosion(EntityCreeper creeper)
    {
        ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, (Integer) 1, 1);
    }

    public void debuffEntityLiving(EntityLivingBase target)
    {
        if (!(target instanceof EntityPlayer))
        {
            float prevSpeed = target.getAIMoveSpeed();
            target.setAIMoveSpeed(prevSpeed * 0.67F);
        }
    }
}
