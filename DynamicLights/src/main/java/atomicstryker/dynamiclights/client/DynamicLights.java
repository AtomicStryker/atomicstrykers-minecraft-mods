package atomicstryker.dynamiclights.client;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.lwjgl.glfw.GLFW;

import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author AtomicStryker
 *         <p>
 *         Rewritten and now-awesome Dynamic Lights Mod.
 *         <p>
 *         Instead of the crude base edits and inefficient giant loops of the
 *         original, this Mod uses ASM transforming to hook into Minecraft with
 *         style and has an API that does't suck. It also uses Forge events to
 *         register dropped Items.
 */
@Mod(DynamicLights.MOD_ID)
@Mod.EventBusSubscriber(modid = DynamicLights.MOD_ID, value = Dist.CLIENT)
public class DynamicLights
{

    public static final String MOD_ID = "dynamiclights";

    private Minecraft mcinstance;

    private static DynamicLights instance;

    /*
     * Optimization - instead of repeatedly getting the same List for the same
     * World, just check once for World being equal.
     */
    private IWorldReader lastWorld;
    private ConcurrentLinkedQueue<atomicstryker.dynamiclights.client.DynamicLightSourceContainer> lastList;

    /**
     * This Map contains a List of DynamicLightSourceContainer for each World. Since
     * the client can only be in a single World, the other Lists just float idle
     * when unused.
     */
    private ConcurrentHashMap<World, ConcurrentLinkedQueue<atomicstryker.dynamiclights.client.DynamicLightSourceContainer>> worldLightsMap;

    /**
     * Keeps track of the toggle button.
     */
    private boolean globalLightsOff;

    /**
     * The Keybinding instance to monitor
     */
    private KeyBinding toggleButton;
    private long nextKeyTriggerTime;
    private static boolean hackingRenderFailed;

    public DynamicLights()
    {
        instance = this;
        globalLightsOff = false;
        worldLightsMap = new ConcurrentHashMap<>();
        nextKeyTriggerTime = System.currentTimeMillis();
        hackingRenderFailed = false;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::onClientSetup);
    }

    public void preInit(FMLCommonSetupEvent evt)
    {
        // nope?
    }

    public void onClientSetup(FMLClientSetupEvent evt)
    {
        mcinstance = Minecraft.getInstance();
        toggleButton = new KeyBinding("Dynamic Lights toggle", GLFW.GLFW_KEY_L, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(toggleButton);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (tick.phase == Phase.END && mcinstance.world != null)
        {
            ConcurrentLinkedQueue<DynamicLightSourceContainer> worldLights = worldLightsMap.get(mcinstance.world);

            if (worldLights != null)
            {
                Iterator<DynamicLightSourceContainer> iter = worldLights.iterator();
                while (iter.hasNext())
                {
                    DynamicLightSourceContainer tickedLightContainer = iter.next();
                    if (tickedLightContainer.onUpdate())
                    {
                        iter.remove();
                        mcinstance.world.checkLightFor(EnumLightType.BLOCK, new BlockPos(tickedLightContainer.getX(), tickedLightContainer.getY(), tickedLightContainer.getZ()));
                        // System.out.println("Dynamic Lights killing off
                        // LightSource on dead Entity
                        // "+tickedLightContainer.getLightSource().getAttachmentEntity());
                    }
                }
            }

            if (mcinstance.currentScreen == null && toggleButton.isPressed() && System.currentTimeMillis() >= nextKeyTriggerTime)
            {
                nextKeyTriggerTime = System.currentTimeMillis() + 1000L;
                globalLightsOff = !globalLightsOff;
                mcinstance.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Dynamic Lights globally " + (globalLightsOff ? "off" : "on")));

                World world = mcinstance.world;
                if (world != null)
                {
                    if (worldLights != null)
                    {
                        for (DynamicLightSourceContainer c : worldLights)
                        {
                            world.checkLightFor(EnumLightType.BLOCK, new BlockPos(c.getX(), c.getY(), c.getZ()));
                        }
                    }
                }
            }
        }
    }

    public static class ClientConfig
    {
        public final ForgeConfigSpec.ConfigValue<String> bannedDimensions;

        ClientConfig(ForgeConfigSpec.Builder builder)
        {
            builder.comment("ClientConfig only settings, mostly things related to rendering").push("client");

            bannedDimensions = builder.comment("Toggle off to make missing model text in the gui fit inside the slot.").translation("forge.configgui.bannedDimensions").define("bannedDimensions", "");

            builder.pop();
        }
    }

    static final ForgeConfigSpec clientSpec;
    public static final ClientConfig CLIENT_CONFIG;

    static
    {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        clientSpec = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    /**
     * Used not only to toggle the Lights, but any Ticks in the sub-modules
     *
     * @return true when all computation and tracking should be suspended, false
     *         otherwise
     */
    public static boolean globalLightsOff()
    {
        return instance.globalLightsOff;
    }

    /**
     * Exposed method which is called by the transformed World.getRawLight method
     * instead of Block.getLightValue. Loops active Dynamic Light Sources and if it
     * finds one for the exact coordinates asked, returns the Light value from that
     * source if higher.
     *
     * @param block
     *            block queried
     * @param blockState
     *            IBlockState queried
     * @param world
     *            World queried
     * @param pos
     *            BlockPos instance of target coords
     * @return max(Block.getLightValue, Dynamic Light)
     */
    @SuppressWarnings("unused")
    public static int getLightValue(Block block, IBlockState blockState, IWorldReader world, BlockPos pos)
    {
        int vanillaValue = block.getLightValue(blockState, world, pos);

        if (instance == null || instance.globalLightsOff || !(world instanceof WorldClient))
        {
            return vanillaValue;
        }

        if (!world.equals(instance.lastWorld) || instance.lastList == null)
        {
            instance.lastWorld = world;
            instance.lastList = instance.worldLightsMap.get(world);
            hackRenderGlobalConcurrently();
        }

        int dynamicValue = 0;
        if (instance.lastList != null && !instance.lastList.isEmpty())
        {
            for (DynamicLightSourceContainer light : instance.lastList)
            {
                if (light.getX() == pos.getX())
                {
                    if (light.getY() == pos.getY())
                    {
                        if (light.getZ() == pos.getZ())
                        {
                            dynamicValue = Math.max(dynamicValue, light.getLightSource().getLightLevel());
                        }
                    }
                }
            }
        }
        return Math.max(vanillaValue, dynamicValue);
    }

    @SuppressWarnings("unchecked")
    private static void hackRenderGlobalConcurrently()
    {
        if (hackingRenderFailed || instance.isBannedDimension(instance.mcinstance.player.dimension.getId()))
        {
            return;
        }

        for (Field f : WorldRenderer.class.getDeclaredFields())
        {
            if (Set.class.isAssignableFrom(f.getType()))
            {
                ParameterizedType fieldType = (ParameterizedType) f.getGenericType();
                if (BlockPos.class.equals(fieldType.getActualTypeArguments()[0]))
                {
                    try
                    {
                        f.setAccessible(true);
                        Set<BlockPos> setLightUpdates = (Set<BlockPos>) f.get(instance.mcinstance.renderGlobal);
                        if (setLightUpdates instanceof ConcurrentSkipListSet)
                        {
                            return;
                        }
                        ConcurrentSkipListSet<BlockPos> cs = new ConcurrentSkipListSet<>(setLightUpdates);
                        f.set(instance.mcinstance.renderGlobal, cs);
                        System.out.println("Dynamic Lights successfully hacked Set WorldRenderer.setLightUpdates and replaced it with a ConcurrentSkipListSet!");
                        return;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Dynamic Lights completely failed to hack Set WorldRenderer.setLightUpdates and will not try again!");
        hackingRenderFailed = true;
    }

    /**
     * Exposed method to register active Dynamic Light Sources with. Does all the
     * necessary checks, prints errors if any occur, creates new World entries in
     * the worldLightsMap
     *
     * @param lightToAdd
     *            IDynamicLightSource to register
     */
    public static void addLightSource(IDynamicLightSource lightToAdd)
    {
        if (lightToAdd.getAttachmentEntity() != null)
        {
            // System.out.printf("Calling addLightSource on entity %s, world %s,
            // dimension %d\n", lightToAdd.getAttachmentEntity(),
            // lightToAdd.getAttachmentEntity().world.getWorldInfo().getWorldName(),
            // lightToAdd.getAttachmentEntity().dimension);
            if (lightToAdd.getAttachmentEntity().isAlive() && !instance.isBannedDimension(lightToAdd.getAttachmentEntity().dimension.getId()))
            {
                DynamicLightSourceContainer newLightContainer = new DynamicLightSourceContainer(lightToAdd);
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(lightToAdd.getAttachmentEntity().world);
                if (lightList != null)
                {
                    if (!lightList.contains(newLightContainer))
                    {
                        // System.out.println("Successfully registered
                        // DynamicLight on Entity:" +
                        // newLightContainer.getLightSource().getAttachmentEntity()
                        // + "in list " + lightList);
                        lightList.add(newLightContainer);
                    }
                    else
                    {
                        System.out.println("Cannot add Dynamic Light: Attachment Entity is already registered!");
                    }
                }
                else
                {
                    lightList = new ConcurrentLinkedQueue<>();
                    lightList.add(newLightContainer);
                    instance.worldLightsMap.put(lightToAdd.getAttachmentEntity().world, lightList);
                }
            }
            else
            {
                System.err.println("Cannot add Dynamic Light: Attachment Entity is dead or in a banned dimension!");
            }
        }
        else
        {
            System.err.println("Cannot add Dynamic Light: Attachment Entity is null!");
        }
    }

    /**
     * Exposed method to remove active Dynamic Light sources with. If it fails for
     * whatever reason, it does so quietly.
     *
     * @param lightToRemove
     *            IDynamicLightSource you want removed.
     */
    public static void removeLightSource(IDynamicLightSource lightToRemove)
    {
        if (lightToRemove != null && lightToRemove.getAttachmentEntity() != null)
        {
            World world = lightToRemove.getAttachmentEntity().world;
            if (world != null)
            {
                DynamicLightSourceContainer iterContainer = null;
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(world);
                if (lightList != null)
                {
                    Iterator<DynamicLightSourceContainer> iter = lightList.iterator();
                    while (iter.hasNext())
                    {
                        iterContainer = iter.next();
                        if (iterContainer.getLightSource().equals(lightToRemove))
                        {
                            iter.remove();
                            break;
                        }
                    }

                    if (iterContainer != null)
                    {
                        world.checkLightFor(EnumLightType.BLOCK, new BlockPos(iterContainer.getX(), iterContainer.getY(), iterContainer.getZ()));
                    }
                }
            }
        }
    }

    /**
     * is a given dimension id on the banned list and will not receive dynamic
     * lighting
     */
    public boolean isBannedDimension(int dimensionID)
    {
        String bans = CLIENT_CONFIG.bannedDimensions.get();
        for (String i : bans.split(","))
        {
            if (Integer.valueOf(i) == dimensionID)
            {
                return true;
            }
        }
        return false;
    }
}
