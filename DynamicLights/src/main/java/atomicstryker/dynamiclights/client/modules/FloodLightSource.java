package atomicstryker.dynamiclights.client.modules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality emulating portable or static Flood Lights
 *
 */
@Mod(modid = "DynamicLights_floodLights", name = "Dynamic Lights Flood Light", version = "1.0.2", dependencies = "required-after:DynamicLights")
public class FloodLightSource
{
    private EntityPlayer thePlayer;
    private ItemConfigHelper itemsMap;
    private final PartialLightSource[] partialLights;
    private boolean enabled;
    private boolean simpleMode;
    private Configuration config;
    
    public FloodLightSource()
    {
        partialLights = new PartialLightSource[5];
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = new Configuration(evt.getSuggestedConfigurationFile());        
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        config.load();
        
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "FloodLightItems", "ender_eye");
        itemsList.comment = "Item IDs that shine floodlight while held.";
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
        
        simpleMode = config.get(Configuration.CATEGORY_GENERAL, "simpleMode", true, "instead of simulating a cone of light (EXPENSIVE!!), simulates a single point light").getBoolean(true);
        
        config.save();
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            thePlayer = FMLClientHandler.instance().getClient().thePlayer;
            if (thePlayer != null && thePlayer.isEntityAlive() && !DynamicLights.globalLightsOff())
            {
                int lightLevel = itemsMap.getLightFromItemStack(thePlayer.getCurrentEquippedItem());
                
                checkDummyInit(thePlayer.worldObj);
                
                if (lightLevel > 0)
                {
                    handleLight(partialLights[0], lightLevel, 0f, 0f);
                    
                    if (!simpleMode)
                    {
                        handleLight(partialLights[1], lightLevel, 15f, 15f);
                        handleLight(partialLights[2], lightLevel, -15f, 15f);
                        handleLight(partialLights[3], lightLevel, 15f, -15f);
                        handleLight(partialLights[4], lightLevel, -15f, -15f);
                    }
                    setLightsEnabled(true);
                }
                else
                {
                    setLightsEnabled(false);
                }
            }
        }
    }
    
    private void handleLight(PartialLightSource source, int light, float yawRot, float pitchRot)
    {
        Vec3 pos = thePlayer.getPosition(1.0f);
        thePlayer.rotationPitch += pitchRot;
        thePlayer.rotationYaw += yawRot;
        Vec3 look = thePlayer.getLook(1.0f);
        thePlayer.rotationPitch -= pitchRot;
        thePlayer.rotationYaw -= yawRot;
        look = pos.addVector(look.xCoord * 16d, look.yCoord * 16d, look.zCoord * 16d);
        MovingObjectPosition mop = thePlayer.worldObj.rayTraceBlocks(pos, look);
        if (mop != null)
        {
            int dist = (int) Math.round(thePlayer.getDistance(mop.blockX+0.5d, mop.blockY+0.5d, mop.blockZ+0.5d));
            source.lightLevel = light - dist;
            source.entity.posX = mop.blockX+0.5d;
            source.entity.posY = mop.blockY+0.5d;
            source.entity.posZ = mop.blockZ+0.5d;
        }
        else
        {
            source.lightLevel = 0;
        }
    }

    private void setLightsEnabled(boolean newEnabled)
    {
        if (newEnabled != enabled)
        {
            enabled = newEnabled;
            
            if (!simpleMode)
            {
                for (PartialLightSource p : partialLights)
                {
                    if (newEnabled)
                    {
                        DynamicLights.addLightSource(p);
                    }
                    else
                    {
                        DynamicLights.removeLightSource(p);
                    }
                }
            }
            else
            {
                if (newEnabled)
                {
                    DynamicLights.addLightSource(partialLights[0]);
                }
                else
                {
                    DynamicLights.removeLightSource(partialLights[0]);
                }
            }
        }
    }

    private void checkDummyInit(World world)
    {
        if (partialLights[0] == null)
        {
            for (int i = 0; i < partialLights.length; i++)
            {
                partialLights[i] = new PartialLightSource(new DummyEntity(world));
                DynamicLights.addLightSource(partialLights[i]);
            }
        }
    }
    
    private class PartialLightSource implements IDynamicLightSource
    {
        private final Entity entity;
        private int lightLevel = 0;
        
        private PartialLightSource(Entity e)
        {
            entity = e;
        }

        @Override
        public Entity getAttachmentEntity()
        {
            return entity;
        }

        @Override
        public int getLightLevel()
        {
            return lightLevel;
        }
    }
    
    private class DummyEntity extends Entity
    {
        public DummyEntity(World par1World) { super(par1World); }
        @Override
        protected void entityInit(){}
        @Override
        protected void readEntityFromNBT(NBTTagCompound var1){}
        @Override
        protected void writeEntityToNBT(NBTTagCompound var1){}
    }

}
