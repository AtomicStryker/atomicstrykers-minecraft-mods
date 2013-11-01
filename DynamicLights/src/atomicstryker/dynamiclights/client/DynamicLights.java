package atomicstryker.dynamiclights.client;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * @author AtomicStryker
 * 
 * Rewritten and now-awesome Dynamic Lights Mod.
 * 
 * Instead of the crude base edits and inefficient giant loops of the original,
 * this Mod uses ASM transforming to hook into Minecraft with style and has an
 * API that does't suck. It also uses Forge events to register dropped Items.
 *
 */
@Mod(modid = "DynamicLights", name = "Dynamic Lights", version = "1.2.7")
public class DynamicLights
{
    private Minecraft mcinstance;
    private static DynamicLights instance;
    
    /*
     * Optimization - instead of repeatedly getting the same List for the same World,
     * just check once for World being equal.
     */
    private IBlockAccess lastWorld;
    private ConcurrentLinkedQueue<DynamicLightSourceContainer> lastList;
    
    /**
     * This Map contains a List of DynamicLightSourceContainer for each World. Since the client can only
     * be in a single World, the other Lists just float idle when unused.
     */
    private ConcurrentHashMap<World, ConcurrentLinkedQueue<DynamicLightSourceContainer>> worldLightsMap;
    
    /**
     * Keeps track of the toggle button.
     */
    private boolean globalLightsOff;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        globalLightsOff = false;
        mcinstance = FMLClientHandler.instance().getClient();
        worldLightsMap = new ConcurrentHashMap<World, ConcurrentLinkedQueue<DynamicLightSourceContainer>>();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
        
        KeyBinding[] key = {new KeyBinding("Dynamic Lights toggle", Keyboard.KEY_L)};
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new LightsOnOffKey(key, repeat));
    }
    
    private class TickHandler implements ITickHandler
    {
        private final EnumSet<TickType> ticks;
        public TickHandler()
        {
            ticks = EnumSet.of(TickType.CLIENT);
        }

        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            if (mcinstance.theWorld != null)
            {
                ConcurrentLinkedQueue<?> worldLights = worldLightsMap.get(mcinstance.theWorld);
                
                if (worldLights != null)
                {
                    Iterator<?> iter = worldLights.iterator();
                    while (iter.hasNext())
                    {
                        DynamicLightSourceContainer tickedLightContainer = (DynamicLightSourceContainer) iter.next();
                        if (tickedLightContainer != null && tickedLightContainer.onUpdate())
                        {
                            iter.remove();
                            mcinstance.theWorld.updateLightByType(EnumSkyBlock.Block, tickedLightContainer.getX(), tickedLightContainer.getY(), tickedLightContainer.getZ());
                            //System.out.println("Dynamic Lights killing off LightSource on dead Entity "+tickedLightContainer.getLightSource().getAttachmentEntity());
                        }
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
            return "DynamicLights";
        }
    }
    
    /**
     * Used not only to toggle the Lights, but any Ticks in the sub-modules
     * @return true when all computation and tracking should be suspended, false otherwise
     */
    public static boolean globalLightsOff()
    {
        return instance.globalLightsOff;
    }
    
    private class LightsOnOffKey extends KeyHandler
    {

        private EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
        
        public LightsOnOffKey(KeyBinding[] keyBindings, boolean[] repeatings)
        {
            super(keyBindings, repeatings);
        }

        @Override
        public String getLabel()
        {
            return "DynamicLightsKey";
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
        {
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
        {
            if (tickEnd && mcinstance.currentScreen == null)
            {
                globalLightsOff = !globalLightsOff;
                mcinstance.ingameGUI.getChatGUI().printChatMessage("Dynamic Lights globally "+(globalLightsOff?"off":"on"));
                
                World world = mcinstance.theWorld;
                if (world != null)
                {
                    ConcurrentLinkedQueue<?> worldLights = worldLightsMap.get(world);
                    if (worldLights != null)
                    {
                        Iterator<?> iter = worldLights.iterator();
                        while (iter.hasNext())
                        {
                            DynamicLightSourceContainer c = (DynamicLightSourceContainer) iter.next();
                            world.updateLightByType(EnumSkyBlock.Block, c.getX(), c.getY(), c.getZ());
                        }
                    }
                }
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

    }
    
    /**
     * Exposed method which is called by the transformed World.computeBlockLightValue method instead of
     * Block.blocksList[blockID].getLightValue. Loops active Dynamic Light Sources and if it finds
     * one for the exact coordinates asked, returns the Light value from that source if higher.
     * 
     * @param world World queried
     * @param blockID of the Coordinate Block
     * @param x coordinate queried
     * @param y coordinate queried
     * @param z coordinate queried
     * @return Block.blocksList[blockID].getLightValue or Dynamic Light value, whichever is higher
     */
    public static int getLightValue(IBlockAccess world, int blockID, int x, int y, int z)
    {
        int vanillaValue = Block.blocksList[blockID] != null ? Block.blocksList[blockID].getLightValue(world, x, y, z) : 0;
        
        if (instance == null || instance.globalLightsOff || world instanceof WorldServer)
        {
            return vanillaValue;
        }
        
        if (!world.equals(instance.lastWorld) || instance.lastList == null)
        {
            instance.lastWorld = world;
            instance.lastList = instance.worldLightsMap.get(world);
        }
        
        if (instance.lastList != null && !instance.lastList.isEmpty())
        {
            for (DynamicLightSourceContainer light : instance.lastList)
            {
                if (light.getX() == x)
                {
                    if (light.getY() == y)
                    {
                        if (light.getZ() == z)
                        {
                            int dynamicValue = light.getLightSource().getLightLevel();
                            if (dynamicValue > vanillaValue)
                            {
                                return dynamicValue;
                            }
                        }
                    }
                }
            }
        }
        
        return vanillaValue;
    }
    
    /**
     * Exposed method to register active Dynamic Light Sources with. Does all the necessary
     * checks, prints errors if any occur, creates new World entries in the worldLightsMap
     * @param lightToAdd IDynamicLightSource to register
     */
    public static void addLightSource(IDynamicLightSource lightToAdd)
    {
        //System.out.println("Calling addLightSource "+lightToAdd+", world "+lightToAdd.getAttachmentEntity().worldObj);
        if (lightToAdd.getAttachmentEntity() != null)
        {
            if (lightToAdd.getAttachmentEntity().isEntityAlive())
            {
                DynamicLightSourceContainer newLightContainer = new DynamicLightSourceContainer(lightToAdd);
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(lightToAdd.getAttachmentEntity().worldObj);
                if (lightList != null)
                {
                    if (!lightList.contains(newLightContainer))
                    {
                        //System.out.println("Successfully registered Dynamic Light on Entity: "+newLightContainer.getLightSource().getAttachmentEntity()+" in list "+lightList);
                        lightList.add(newLightContainer);
                    }
                    else
                    {
                        System.out.println("Cannot add Dynamic Light: Attachment Entity is already registered!");
                    }
                }
                else
                {
                    lightList = new ConcurrentLinkedQueue<DynamicLightSourceContainer>();
                    lightList.add(newLightContainer);
                    instance.worldLightsMap.put(lightToAdd.getAttachmentEntity().worldObj, lightList);
                }
            }
            else
            {
                System.err.println("Cannot add Dynamic Light: Attachment Entity is dead!");
            }
        }
        else
        {
            System.err.println("Cannot add Dynamic Light: Attachment Entity is null!");
        }
    }
    
    /**
     * Exposed method to remove active Dynamic Light sources with. If it fails for whatever reason,
     * it does so quietly.
     * @param lightToRemove IDynamicLightSource you want removed.
     */
    public static void removeLightSource(IDynamicLightSource lightToRemove)
    {
        if (lightToRemove != null && lightToRemove.getAttachmentEntity() != null)
        {
            World world = lightToRemove.getAttachmentEntity().worldObj;
            if (world != null)
            {
                DynamicLightSourceContainer iterContainer = null;
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(world);
                if (lightList != null)
                {
                    Iterator<DynamicLightSourceContainer> iter = lightList.iterator();
                    while (iter.hasNext())
                    {
                        iterContainer = (DynamicLightSourceContainer) iter.next();
                        if (iterContainer.getLightSource().equals(lightToRemove))
                        {
                            iter.remove();
                            break;
                        }
                    }
                    
                    if (iterContainer != null)
                    {
                        world.updateLightByType(EnumSkyBlock.Block, iterContainer.getX(), iterContainer.getY(), iterContainer.getZ());
                    }
                }
            }
        }
    }
}
