package atomicstryker.dynamiclights.client;

import atomicstryker.dynamiclights.client.modules.DroppedItemsLightSource;
import atomicstryker.dynamiclights.client.modules.PlayerSelfLightSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author AtomicStryker
 * <p>
 * Rewritten and now-awesome Dynamic Lights Mod.
 * <p>
 * Instead of the crude base edits and inefficient giant loops of the
 * original, this Mod uses ASM transforming to hook into Minecraft with
 * style and has an API that does't suck. It also uses Forge events to
 * register dropped Items.
 */
@Mod(DynamicLights.MOD_ID)
public class DynamicLights {

    public static final String MOD_ID = "dynamiclights";
    private static final Logger LOGGER = LogManager.getLogger();
    private static DynamicLights instance;
    private static boolean hackingRenderFailed;
    DynamicLightsConfig config;
    private Minecraft mcinstance;
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

    private PlayerSelfLightSource playerSelfLightSource;
    private DroppedItemsLightSource droppedItemsLightSource;

    public DynamicLights() {
        instance = this;
        globalLightsOff = false;
        worldLightsMap = new ConcurrentHashMap<>();
        nextKeyTriggerTime = System.currentTimeMillis();
        hackingRenderFailed = false;

        playerSelfLightSource = new PlayerSelfLightSource();
        droppedItemsLightSource = new DroppedItemsLightSource();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::onClientSetupEvent);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Used not only to toggle the Lights, but any Ticks in the sub-modules
     *
     * @return true when all computation and tracking should be suspended, false
     * otherwise
     */
    public static boolean globalLightsOff() {
        return instance.globalLightsOff;
    }

    /**
     * Exposed method which is called by the transformed World.getRawLight method
     * instead of Block.getLightValue. Loops active Dynamic Light Sources and if it
     * finds one for the exact coordinates asked, returns the Light value from that
     * source if higher.
     */
    public static int getDynamicLightValue(IWorldReader worldReader, BlockPos pos) {
        World world = (World) worldReader;
        int vanillaValue = world.getBlockState(pos).getLightValue(world, pos);
        if (instance.globalLightsOff || !isWorldReady() || !isClientWorld(world)) {
            return vanillaValue;
        }

        if (world != instance.lastWorld || instance.lastList == null) {
            instance.lastWorld = world;
            instance.lastList = instance.worldLightsMap.get(world);
            hackRenderGlobalConcurrently();
        }

        int dynamicValue = 0;
        if (instance.lastList != null && !instance.lastList.isEmpty()) {
            for (DynamicLightSourceContainer light : instance.lastList) {
                if (light.getX() == pos.getX()) {
                    if (light.getY() == pos.getY()) {
                        if (light.getZ() == pos.getZ()) {
                            dynamicValue = Math.max(dynamicValue, light.getLightSource().getLightLevel());
                        }
                    }
                }
            }
        }
        return Math.max(vanillaValue, dynamicValue);
    }

    private static boolean isClientWorld(World world) {
        return instance.mcinstance.player.world == world;
    }

    private static boolean isWorldReady() {
        return instance.mcinstance != null && instance.mcinstance.player != null && instance.mcinstance.player.dimension != null;
    }

    @SuppressWarnings("unchecked")
    private static void hackRenderGlobalConcurrently() {
        if (hackingRenderFailed || instance.isBannedDimension(instance.mcinstance.player.dimension.getId())) {
            return;
        }

        for (Field f : WorldRenderer.class.getDeclaredFields()) {
            if (Set.class.isAssignableFrom(f.getType())) {
                ParameterizedType fieldType = (ParameterizedType) f.getGenericType();
                if (BlockPos.class.equals(fieldType.getActualTypeArguments()[0])) {
                    try {
                        f.setAccessible(true);
                        Set<BlockPos> setLightUpdates = (Set<BlockPos>) f.get(instance.mcinstance.worldRenderer);
                        if (setLightUpdates instanceof ConcurrentSkipListSet) {
                            return;
                        }
                        ConcurrentSkipListSet<BlockPos> cs = new ConcurrentSkipListSet<>(setLightUpdates);
                        f.set(instance.mcinstance.worldRenderer, cs);
                        LOGGER.info("Dynamic Lights successfully hacked Set WorldRenderer.setLightUpdates and replaced it with a ConcurrentSkipListSet!");
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        LOGGER.error("Dynamic Lights completely failed to hack Set WorldRenderer.setLightUpdates and will not try again!");
        hackingRenderFailed = true;
    }

    /**
     * Exposed method to register active Dynamic Light Sources with. Does all the
     * necessary checks, prints errors if any occur, creates new World entries in
     * the worldLightsMap
     *
     * @param lightToAdd IDynamicLightSource to register
     */
    public static void addLightSource(IDynamicLightSource lightToAdd) {
        if (lightToAdd.getAttachmentEntity() != null) {
            LOGGER.info("Calling addLightSource on entity {}, world {}, dimension {}", lightToAdd.getAttachmentEntity(),
                    lightToAdd.getAttachmentEntity().world.getWorldInfo().getWorldName(), lightToAdd.getAttachmentEntity().dimension);
            if (lightToAdd.getAttachmentEntity().isAlive() && !instance.isBannedDimension(lightToAdd.getAttachmentEntity().dimension.getId())) {
                DynamicLightSourceContainer newLightContainer = new DynamicLightSourceContainer(lightToAdd);
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(lightToAdd.getAttachmentEntity().world);
                if (lightList != null) {
                    if (!lightList.contains(newLightContainer)) {
                        LOGGER.info("Successfully registered DynamicLight on Entity: {} in list {}", newLightContainer.getLightSource().getAttachmentEntity(), lightList);
                        lightList.add(newLightContainer);
                    } else {
                        LOGGER.info("Cannot add Dynamic Light: Attachment Entity is already registered!");
                    }
                } else {
                    lightList = new ConcurrentLinkedQueue<>();
                    lightList.add(newLightContainer);
                    instance.worldLightsMap.put(lightToAdd.getAttachmentEntity().world, lightList);
                }
            } else {
                LOGGER.error("Cannot add Dynamic Light: Attachment Entity {} is dead or in a banned dimension {}", lightToAdd.getAttachmentEntity(), lightToAdd.getAttachmentEntity().dimension);
            }
        } else {
            LOGGER.error("Cannot add Dynamic Light: Attachment Entity is null!");
        }
    }

    /**
     * Exposed method to remove active Dynamic Light sources with. If it fails for
     * whatever reason, it does so quietly.
     *
     * @param lightToRemove IDynamicLightSource you want removed.
     */
    public static void removeLightSource(IDynamicLightSource lightToRemove) {
        if (lightToRemove != null && lightToRemove.getAttachmentEntity() != null) {
            World world = lightToRemove.getAttachmentEntity().world;
            if (world != null) {
                DynamicLightSourceContainer iterContainer = null;
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(world);
                if (lightList != null) {
                    Iterator<DynamicLightSourceContainer> iter = lightList.iterator();
                    while (iter.hasNext()) {
                        iterContainer = iter.next();
                        if (iterContainer.getLightSource().equals(lightToRemove)) {
                            iter.remove();
                            break;
                        }
                    }

                    if (iterContainer != null) {
                        world.checkLightFor(EnumLightType.BLOCK, new BlockPos(iterContainer.getX(), iterContainer.getY(), iterContainer.getZ()));
                    }
                }
            }
        }
    }

    public void preInit(FMLCommonSetupEvent evt) {
        mcinstance = Minecraft.getInstance();
        try {
            config = GsonConfig.loadConfigWithDefault(DynamicLightsConfig.class, new File(mcinstance.gameDir, "\\config\\dynamiclights.cfg"), new DynamicLightsConfig());
        } catch (IOException e) {
            LOGGER.error("IOException parsing config", e);
        }
    }

    public void onClientSetupEvent(FMLClientSetupEvent event) {
        toggleButton = new KeyBinding("Dynamic Lights toggle", GLFW.GLFW_KEY_L, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(toggleButton);
    }

    @SubscribeEvent
    public void clientTick(TickEvent.PlayerTickEvent event) {

        if (mcinstance.world != null) {
            ConcurrentLinkedQueue<DynamicLightSourceContainer> worldLights = worldLightsMap.get(mcinstance.world);

            if (worldLights != null) {
                Iterator<DynamicLightSourceContainer> iter = worldLights.iterator();
                while (iter.hasNext()) {
                    DynamicLightSourceContainer tickedLightContainer = iter.next();
                    if (tickedLightContainer.onUpdate()) {
                        iter.remove();
                        mcinstance.world.checkLightFor(EnumLightType.BLOCK, new BlockPos(tickedLightContainer.getX(), tickedLightContainer.getY(), tickedLightContainer.getZ()));
                        LOGGER.debug("Dynamic Lights killing off LightSource on dead Entity: " + tickedLightContainer.getLightSource().getAttachmentEntity());
                    }
                }
            }

            if (mcinstance.currentScreen == null && toggleButton.isPressed() && System.currentTimeMillis() >= nextKeyTriggerTime) {
                nextKeyTriggerTime = System.currentTimeMillis() + 1000L;
                globalLightsOff = !globalLightsOff;
                mcinstance.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Dynamic Lights globally " + (globalLightsOff ? "off" : "on")));

                World world = mcinstance.world;
                if (world != null) {
                    if (worldLights != null) {
                        for (DynamicLightSourceContainer c : worldLights) {
                            world.checkLightFor(EnumLightType.BLOCK, new BlockPos(c.getX(), c.getY(), c.getZ()));
                        }
                    }
                }
            }
        }
    }

    /**
     * is a given dimension id on the banned list and will not receive dynamic
     * lighting
     */
    public boolean isBannedDimension(int dimensionID) {
        for (Integer i : config.getBannedDimensions()) {
            if (i == dimensionID) {
                return true;
            }
        }
        return false;
    }
}
