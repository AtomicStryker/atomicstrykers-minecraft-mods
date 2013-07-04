package atomicstryker.kenshiro.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import atomicstryker.kenshiro.client.ClientPacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;

@Mod(modid = "AS_Kenshiro", name = "Kenshiro Mod", version = "1.1.5")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_KSM"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_KSM"}, packetHandler = ServerPacketHandler.class)
, connectionHandler = ConnectionHandler.class)
public class KenshiroMod
{
    @SidedProxy(clientSide = "atomicstryker.kenshiro.client.ClientProxy", serverSide = "atomicstryker.kenshiro.common.CommonProxy")
    public static CommonProxy proxy;
    
    private static KenshiroMod instance;
    
    public static KenshiroMod instance()
    {
        return instance;
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.load();
        instance = this;
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        new KenshiroServer();
    }
    
    @SuppressWarnings("unchecked")
	public void stopSkeletonShooter(EntitySkeleton skelly)
    {
        Field[] taskfields = skelly.tasks.getClass().getDeclaredFields();
        taskfields[0].setAccessible(true);
        taskfields[1].setAccessible(true);
        
        ArrayList<EntityAITaskEntry> list = null;
        try
        {
            for(int i = 0; i<= 1; i++)
            {
                list = (ArrayList<EntityAITaskEntry>)taskfields[i].get(skelly.tasks);
                
                Iterator<EntityAITaskEntry> iter = list.iterator();
                while (iter.hasNext())
                {
                    EntityAITaskEntry task = (EntityAITaskEntry)iter.next();
                    if (task.action instanceof EntityAIArrowAttack)
                    {
                        System.out.println("Found and removed EntityAIArrowAttack Task!");
                        list.remove(task);
                        break;
                    }
                }
            }
        }
        catch (IllegalArgumentException e) { System.out.println("TaskHack IllegalArgumentException: "+e);   }
        catch (IllegalAccessException e) {  System.out.println("TaskHack IllegalAccessException: "+e);  }       
    }
    
    public void stopCreeperExplosion(EntityCreeper creeper)
    {
        ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, (Integer)1, 1);
    }
    
    public void debuffEntityLiving(EntityLivingBase target)
    {
        if (!(target instanceof EntityPlayer))
        {
            float prevSpeed = target.getAIMoveSpeed();
            target.setAIMoveSpeed(prevSpeed*0.67F);
        }
    }
    
    public Entity getEntityByID(World world, int ID)
    {
        Entity temp;
        for (int i = 0; i < world.loadedEntityList.size(); i++)
        {
            temp = (Entity) world.loadedEntityList.get(i);
            if (temp.entityId == ID)
            {
                return temp;
            }
        }
        return null;
    }
}
