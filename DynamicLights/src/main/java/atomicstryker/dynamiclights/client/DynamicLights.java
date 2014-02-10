package atomicstryker.dynamiclights.client;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.registry.GameData;

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
@Mod(modid = "DynamicLights", name = "Dynamic Lights", version = "1.3.1")
public class DynamicLights
{
    private Minecraft mcinstance;
    
    @Instance("DynamicLights")
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
    
    /**
     * The Keybinding instance to monitor
     */
    private KeyBinding toggleButton;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        globalLightsOff = false;
        mcinstance = FMLClientHandler.instance().getClient();
        worldLightsMap = new ConcurrentHashMap<World, ConcurrentLinkedQueue<DynamicLightSourceContainer>>();
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        toggleButton = new KeyBinding("Dynamic Lights toggle", Keyboard.KEY_L, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(toggleButton);
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (tick.phase == Phase.END && mcinstance.theWorld != null)
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
            
            if (mcinstance.currentScreen == null && toggleButton.getIsKeyPressed())
            {
                globalLightsOff = !globalLightsOff;
                mcinstance.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Dynamic Lights globally "+(globalLightsOff?"off":"on")));
                
                World world = mcinstance.theWorld;
                if (world != null)
                {
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
    }
    
    /**
     * Used not only to toggle the Lights, but any Ticks in the sub-modules
     * @return true when all computation and tracking should be suspended, false otherwise
     */
    public static boolean globalLightsOff()
    {
        return instance.globalLightsOff;
    }
    
    /**
     * Exposed method which is called by the transformed World.computeBlockLightValue method instead of
     * Block.blocksList[blockID].getLightValue. Loops active Dynamic Light Sources and if it finds
     * one for the exact coordinates asked, returns the Light value from that source if higher.
     * 
     * @param world World queried
     * @param block Block instance of target coords
     * @param x coordinate queried
     * @param y coordinate queried
     * @param z coordinate queried
     * @return Block.blocksList[blockID].getLightValue or Dynamic Light value, whichever is higher
     */
    public static int getLightValue(IBlockAccess world, Block block, int x, int y, int z)
    {
        int vanillaValue = block.getLightValue(world, x, y, z);
        
        if (instance == null || instance.globalLightsOff || world instanceof WorldServer)
        {
            return vanillaValue;
        }
        
        if (!world.equals(instance.lastWorld) || instance.lastList == null)
        {
            instance.lastWorld = world;
            instance.lastList = instance.worldLightsMap.get(world);
        }
        
        int dynamicValue = 0;
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
                            dynamicValue = Math.max(dynamicValue, light.getLightSource().getLightLevel());
                        }
                    }
                }
            }
        }
        return Math.max(vanillaValue, dynamicValue);
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
    
    /**
     * Convenience Method because Trolljang
     * Strips away any minecraft: or mod prefixes GameData applies to items
     */
    public static String getShortItemName(ItemStack item)
    {
        String s = GameData.itemRegistry.getNameForObject(item.getItem());
        if (s != null)
        {
            String[] split = s.split(":");
            return split[split.length-1];
        }
        return "error";
    }
}
