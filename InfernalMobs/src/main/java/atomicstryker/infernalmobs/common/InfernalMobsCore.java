package atomicstryker.infernalmobs.common;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.client.OverlayChoking;
import atomicstryker.infernalmobs.common.mods.MM_1UP;
import atomicstryker.infernalmobs.common.mods.MM_Alchemist;
import atomicstryker.infernalmobs.common.mods.MM_Berserk;
import atomicstryker.infernalmobs.common.mods.MM_Blastoff;
import atomicstryker.infernalmobs.common.mods.MM_Bulwark;
import atomicstryker.infernalmobs.common.mods.MM_Choke;
import atomicstryker.infernalmobs.common.mods.MM_Cloaking;
import atomicstryker.infernalmobs.common.mods.MM_Darkness;
import atomicstryker.infernalmobs.common.mods.MM_Ender;
import atomicstryker.infernalmobs.common.mods.MM_Exhaust;
import atomicstryker.infernalmobs.common.mods.MM_Fiery;
import atomicstryker.infernalmobs.common.mods.MM_Ghastly;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.mods.MM_Lifesteal;
import atomicstryker.infernalmobs.common.mods.MM_Ninja;
import atomicstryker.infernalmobs.common.mods.MM_Poisonous;
import atomicstryker.infernalmobs.common.mods.MM_Quicksand;
import atomicstryker.infernalmobs.common.mods.MM_Regen;
import atomicstryker.infernalmobs.common.mods.MM_Rust;
import atomicstryker.infernalmobs.common.mods.MM_Sapper;
import atomicstryker.infernalmobs.common.mods.MM_Sprint;
import atomicstryker.infernalmobs.common.mods.MM_Sticky;
import atomicstryker.infernalmobs.common.mods.MM_Storm;
import atomicstryker.infernalmobs.common.mods.MM_Unyielding;
import atomicstryker.infernalmobs.common.mods.MM_Vengeance;
import atomicstryker.infernalmobs.common.mods.MM_Weakness;
import atomicstryker.infernalmobs.common.mods.MM_Webber;
import atomicstryker.infernalmobs.common.mods.MM_Wither;
import atomicstryker.infernalmobs.common.network.AirPacket;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.KnockBackPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import atomicstryker.infernalmobs.common.network.VelocityPacket;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Mod(InfernalMobsCore.MOD_ID)
public class InfernalMobsCore {

    public static final String MOD_ID = "infernalmobs";

    public static Logger LOGGER;
    private static InfernalMobsCore instance;
    private final long existCheckDelay = 5000L;

    protected File configFile;
    protected InfernalMobsConfig config;
    private long nextExistCheckTime;
    private ItemConfigHelper lootItemDropsElite;
    private ItemConfigHelper lootItemDropsUltra;
    private ItemConfigHelper lootItemDropsInfernal;
    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    private HashMap<String, Double> classesHealthMap;
    private Entity infCheckA;
    private Entity infCheckB;
    private ArrayList<Class<? extends MobModifier>> mobMods = null;
    private ArrayList<Enchantment> enchantmentList;
    /*
     * saves the last timestamp of long term affected players (eg choke) reset
     * the players by timer if the mod didn't remove them
     */
    private HashMap<String, Long> modifiedPlayerTimes;

    public InfernalMobsCore(IEventBus modEventBus) {
        instance = this;

        nextExistCheckTime = System.currentTimeMillis();
        classesAllowedMap = new HashMap<>();
        classesForcedMap = new HashMap<>();
        classesHealthMap = new HashMap<>();
        modifiedPlayerTimes = new HashMap<>();

        NeoForge.EVENT_BUS.register(this);

        NeoForge.EVENT_BUS.register(new EntityEventHandler());
        NeoForge.EVENT_BUS.register(new SaveEventHandler());

        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(new InfernalMobsClient());
        }

        LOGGER = LogManager.getLogger();

        modEventBus.addListener(this::registerNetworking);
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {

        // the optional method gives us a registrar that does non-mandatory packets
        // so clients having the mod can still connect to servers which dont have it
        final PayloadRegistrar registrar = event.registrar(MOD_ID).optional();

        registrar.playBidirectional(HealthPacket.TYPE, HealthPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        (payload, context) -> instance().onHealthPacketForClient(payload, context),
                        (payload, context) -> instance().onHealthPacket(payload, context)));

        registrar.playBidirectional(MobModsPacket.TYPE, MobModsPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        (payload, context) -> instance().onMobModsPacketForClient(payload, context),
                        (payload, context) -> instance().onMobModsPacket(payload, context)));

        registrar.playToClient(AirPacket.TYPE, AirPacket.STREAM_CODEC, instance()::onAirPacketForClient);

        registrar.playToClient(VelocityPacket.TYPE, VelocityPacket.STREAM_CODEC,
                instance()::onVelocityPacketForClient);

        registrar.playToClient(KnockBackPacket.TYPE, KnockBackPacket.STREAM_CODEC,
                instance()::onKnockBackPacketForClient);
    }

    private void onHealthPacketForClient(HealthPacket healthPacket, IPayloadContext playPayloadContext) {
        if (FMLEnvironment.dist.isClient()) {
            InfernalMobsClient.instance().onHealthPacketForClient(healthPacket, playPayloadContext);
        }
    }

    private void onKnockBackPacketForClient(KnockBackPacket knockBackPacket, IPayloadContext playPayloadContext) {
        if (FMLEnvironment.dist.isClient()) {
            InfernalMobsClient.instance().onKnockBackPacket(knockBackPacket, playPayloadContext);
        }
    }

    private void onMobModsPacketForClient(MobModsPacket mobModsPacket, IPayloadContext playPayloadContext) {
        if (FMLEnvironment.dist.isClient()) {
            InfernalMobsClient.instance().onMobModsPacketToClient(mobModsPacket, playPayloadContext);
        }
    }

    private void onAirPacketForClient(AirPacket airPacket, IPayloadContext playPayloadContext) {
        if (FMLEnvironment.dist.isClient()) {
            InfernalMobsClient.instance().onAirPacket(airPacket, playPayloadContext);
        }
    }

    private void onVelocityPacketForClient(VelocityPacket velocityPacket, IPayloadContext playPayloadContext) {
        if (FMLEnvironment.dist.isClient()) {
            InfernalMobsClient.instance().onVelocityPacket(velocityPacket, playPayloadContext);
        }
    }

    private void onMobModsPacket(MobModsPacket mobModsPacket, IPayloadContext playPayloadContext) {
        ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(mobModsPacket.stringData());
        InfernalMobsCore.LOGGER.trace("player {} from string {} querying server for mods of entity id {}", p, mobModsPacket.stringData(), mobModsPacket.entID());
        if (p != null) {
            Entity ent = p.level().getEntity(mobModsPacket.entID());
            if (ent instanceof LivingEntity) {
                LivingEntity e = (LivingEntity) ent;
                MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                InfernalMobsCore.LOGGER.trace("resolves to entity {} modifiers {}", ent, mod);
                if (mod != null) {
                    InfernalMobsCore.LOGGER.trace("server sending mods {} for ent-ID {}", mod.getLinkedModNameUntranslated(), mobModsPacket.entID());
                    MobModsPacket response = new MobModsPacket(mod.getLinkedModNameUntranslated(), mobModsPacket.entID(), (byte) 1);
                    PacketDistributor.sendToPlayer(p, response);
                    InfernalMobsCore.instance().sendHealthPacket(e);
                }
            }
        }
    }

    private void onHealthPacket(HealthPacket healthPacket, IPayloadContext playPayloadContext) {
        ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(healthPacket.stringData());
        if (p != null) {
            Entity ent = p.level().getEntity(healthPacket.entID());
            if (ent instanceof LivingEntity e) {
                MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                if (mod != null) {
                    HealthPacket response = new HealthPacket(healthPacket.stringData(), healthPacket.entID(), e.getHealth(), e.getMaxHealth());
                    PacketDistributor.sendToPlayer(p, response);
                }
            }
        }
    }

    public static InfernalMobsCore instance() {
        return instance;
    }

    public static MobModifier getMobModifiers(LivingEntity ent) {
        return SidedCache.getInfernalMobs(ent.level()).get(ent);
    }

    public static boolean getIsRareEntityOnline(LivingEntity ent) {
        return SidedCache.getInfernalMobs(ent.level()).containsKey(ent);
    }

    public static boolean getWasMobSpawnedBefore(LivingEntity ent) {
        // check if the entity previously passed infernal mob generation without getting a mod
        String storedInfernalTag = ent.getPersistentData().getString(instance().getNBTTag());
        boolean result = !storedInfernalTag.isEmpty() && instance().getNBTMarkerForNonInfernalEntities().equals(storedInfernalTag);
        if (result) {
            InfernalMobsCore.LOGGER.debug("entity {} was spawned in unmodified before, not modifying it", ent);
        }
        return result;
    }

    public static void setMobWasSpawnedBefore(LivingEntity ent) {
        // if infernal mobs decides not to give an entity mods, we still have to mark it to make sure it never goes through "spawning" again for infernal mobs
        ent.getPersistentData().putString(InfernalMobsCore.instance().getNBTTag(), instance().getNBTMarkerForNonInfernalEntities());
    }

    public static void clearAllElitesOfLevel(Level level) {
        SidedCache.getInfernalMobs(level).clear();
    }

    public static void removeEntFromElites(LivingEntity entity) {
        SidedCache.getInfernalMobs(entity.level()).remove(entity);
    }

    public String getNBTTag() {
        return "InfernalMobsMod";
    }

    /**
     * as reloading worlds or savegames can "spawn" the same entities over and over, infernal mobs has to mark entities
     * nbt data as having passed through spawning before, even when no modifiers were applied
     */
    public String getNBTMarkerForNonInfernalEntities() {
        return "notInfernal";
    }

    @SubscribeEvent
    public void commonSetup(ServerStartedEvent evt) {
        // dedicated server starting point
        initIfNeeded(evt.getServer().getAllLevels().iterator().next());
    }

    /**
     * is triggered either by server start or by client login event from InfernalMobsClient
     */
    public void initIfNeeded(Level world) {
        if (mobMods == null) {
            prepareModList();

            Path mcFolder;
            if (world.isClientSide()) {
                mcFolder = InfernalMobsClient.getMcFolder().toPath();
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                mcFolder = server.getServerDirectory();
            }

            configFile = mcFolder.resolve("config" + File.separatorChar + "infernalmobs.cfg").toFile();
            loadConfig(world);

            LOGGER.info("InfernalMobsCore commonSetup completed! Modifiers ready: " + mobMods.size());
            LOGGER.info("InfernalMobsCore commonSetup completed! config file at: " + configFile.getAbsolutePath());
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent evt) {
        evt.getDispatcher().register(InfernalCommandFindEntityClass.BUILDER);
        evt.getDispatcher().register(InfernalCommandSpawnInfernal.BUILDER);
    }

    /**
     * Registers the MobModifier classes for consideration
     */
    private void prepareModList() {
        mobMods = new ArrayList<>();

        mobMods.add(MM_1UP.class);
        mobMods.add(MM_Alchemist.class);
        mobMods.add(MM_Berserk.class);
        mobMods.add(MM_Blastoff.class);
        mobMods.add(MM_Bulwark.class);
        mobMods.add(MM_Choke.class);
        mobMods.add(MM_Cloaking.class);
        mobMods.add(MM_Darkness.class);
        mobMods.add(MM_Ender.class);
        mobMods.add(MM_Exhaust.class);
        mobMods.add(MM_Fiery.class);
        mobMods.add(MM_Ghastly.class);
        mobMods.add(MM_Gravity.class);
        mobMods.add(MM_Lifesteal.class);
        mobMods.add(MM_Ninja.class);
        mobMods.add(MM_Poisonous.class);
        mobMods.add(MM_Quicksand.class);
        mobMods.add(MM_Regen.class);
        mobMods.add(MM_Rust.class);
        mobMods.add(MM_Sapper.class);
        mobMods.add(MM_Sprint.class);
        mobMods.add(MM_Sticky.class);
        mobMods.add(MM_Storm.class);
        mobMods.add(MM_Unyielding.class);
        mobMods.add(MM_Vengeance.class);
        mobMods.add(MM_Weakness.class);
        mobMods.add(MM_Webber.class);
        mobMods.add(MM_Wither.class);
    }

    /**
     * Forge Config file
     */
    private void loadConfig(Level world) {
        InfernalMobsConfig defaultConfig = new InfernalMobsConfig();
        defaultConfig.setEliteRarity(15);
        defaultConfig.setUltraRarity(7);
        defaultConfig.setInfernoRarity(7);
        defaultConfig.setUseSimpleEntityClassNames(true);
        defaultConfig.setDisableHealthBar(false);
        defaultConfig.setModHealthFactor(1.0D);

        List<String> dropsElite = new ArrayList<>();
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_SHOVEL), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_PICKAXE), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_AXE), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_SWORD), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HOE), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HELMET), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_BOOTS), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_CHESTPLATE), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_LEGGINGS), world.registryAccess()));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.COOKIE, 5), world.registryAccess()));
        defaultConfig.setDroppedItemIDsElite(dropsElite);

        List<String> dropsUltra = new ArrayList<>();
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HOE), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.BOW), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HELMET), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_BOOTS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_CHESTPLATE), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_LEGGINGS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_HELMET), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_BOOTS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_CHESTPLATE), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_LEGGINGS), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_APPLE, 3), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.BLAZE_POWDER, 5), world.registryAccess()));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENCHANTED_BOOK), world.registryAccess()));
        defaultConfig.setDroppedItemIDsUltra(dropsUltra);

        List<String> dropsInfernal = new ArrayList<>();
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENCHANTED_BOOK), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND, 3), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_SWORD), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_AXE), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_HOE), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_PICKAXE), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_SHOVEL), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_HELMET), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_BOOTS), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_CHESTPLATE), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_LEGGINGS), world.registryAccess()));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENDER_PEARL, 3), world.registryAccess()));
        defaultConfig.setDroppedItemIDsInfernal(dropsInfernal);

        defaultConfig.setMaxDamage(10D);
        defaultConfig.setDimensionIDBlackList(new ArrayList<>());

        Map<String, Boolean> modsEnabledMap = new HashMap<>();
        for (Class<?> c : mobMods) {
            modsEnabledMap.put(c.getSimpleName(), true);
        }
        defaultConfig.setModsEnabled(modsEnabledMap);

        config = GsonConfig.loadConfigWithDefault(InfernalMobsConfig.class, configFile, defaultConfig);

        lootItemDropsElite = new ItemConfigHelper(config.getDroppedItemIDsElite(), LOGGER, world.registryAccess());
        lootItemDropsUltra = new ItemConfigHelper(config.getDroppedItemIDsUltra(), LOGGER, world.registryAccess());
        lootItemDropsInfernal = new ItemConfigHelper(config.getDroppedItemIDsInfernal(), LOGGER, world.registryAccess());

        mobMods.removeIf(c -> !config.getModsEnabled().containsKey(c.getSimpleName()) || !config.getModsEnabled().get(c.getSimpleName()));
    }

    /**
     * Called when an Enemy is spawned by natural (Biome Spawning) means, turn
     * them into Elites here
     *
     * @param entity Entity in question, already asserted to be instanceof Enemy
     */
    public void processEntitySpawn(LivingEntity entity) {
        if (!entity.level().isClientSide && config != null) {
            if (!getIsRareEntityOnline(entity) && !getWasMobSpawnedBefore(entity)) {
                if (isClassAllowed(entity) && (instance.checkEntityClassForced(entity) || entity.level().random.nextInt(config.getEliteRarity()) == 0)) {
                    try {
                        /*
                            get server world from resource location:
                            RegistryKey<World> registrykey = RegistryKey.create(Registry.WORLD_KEY, resourcelocation);
                            ServerWorld serverworld = p_212592_0_.getSource().getServer().getWorld(registrykey);
                         */
                        ResourceKey<Level> worldRegistryKey = entity.getCommandSenderWorld().dimension();
                        ResourceLocation worldResourceLocation = worldRegistryKey.location();

                        // Skip Infernal-Spawn when Dimension is Blacklisted, entries look like: "minecraft:overworld"
                        if (!config.getDimensionIDBlackList().contains(worldResourceLocation.toString())) {
                            MobModifier mod = instance.createMobModifiers(entity);
                            if (mod != null) {
                                SidedCache.getInfernalMobs(entity.level()).put(entity, mod);
                                mod.onSpawningCompleteStoreModsAndBuffHealth(entity);
                                // System.out.println("InfernalMobsCore modded
                                // mob: "+entity+", id "+entity.getEntityId()+":
                                // "+mod.getLinkedModName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(org.apache.logging.log4j.Level.ERROR, "processEntitySpawn() threw an exception");
                        e.printStackTrace();
                    }
                } else {
                    setMobWasSpawnedBefore(entity);
                }
            }
        }
    }

    private boolean isClassAllowed(LivingEntity entity) {
        if (entity instanceof TamableAnimal) {
            return false;
        }
        return instance.checkEntityClassAllowed(entity);
    }

    private String getEntityNameSafe(Entity entity) {
        String result;
        try {
            result = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        } catch (Exception e) {
            result = entity.getClass().getSimpleName();
            System.err.println("Entity of class " + result + " crashed when EntityList.getEntityString was queried, for shame! Using classname instead.");
            System.err.println("If this message is spamming too much for your taste set useSimpleEntityClassnames true in your Infernal Mobs config");
        }
        return result;
    }

    private boolean checkEntityClassAllowed(LivingEntity entity) {
        String entName = config.isUseSimpleEntityClassNames() ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesAllowedMap.containsKey(entName)) {
            return classesAllowedMap.get(entName);
        }

        boolean result = true;
        if (!config.getPermittedentities().containsKey(entName)) {
            config.getPermittedentities().put(entName, true);
            GsonConfig.saveConfig(config, configFile);
        } else {
            result = config.getPermittedentities().get(entName);
            classesAllowedMap.put(entName, result);
        }
        return result;
    }

    private boolean checkEntityClassForced(LivingEntity entity) {
        String entName = config.isUseSimpleEntityClassNames() ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesForcedMap.containsKey(entName)) {
            return classesForcedMap.get(entName);
        }

        boolean result = false;
        if (!config.getEntitiesalwaysinfernal().containsKey(entName)) {
            config.getEntitiesalwaysinfernal().put(entName, false);
            GsonConfig.saveConfig(config, configFile);
        } else {
            result = config.getEntitiesalwaysinfernal().get(entName);
            classesForcedMap.put(entName, result);
        }
        return result;
    }

    public double getMobClassMaxHealth(LivingEntity entity) {
        String entName = config.isUseSimpleEntityClassNames() ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesHealthMap.containsKey(entName)) {
            return classesHealthMap.get(entName);
        }

        double result = entity.getMaxHealth();
        if (!config.getEntitybasehealth().containsKey(entName)) {
            config.getEntitybasehealth().put(entName, (double) entity.getMaxHealth());
            GsonConfig.saveConfig(config, configFile);
        } else {
            result = config.getEntitybasehealth().get(entName);
            classesHealthMap.put(entName, result);
        }
        return result;
    }

    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     *
     * @param entity Entity instance whose health you want changed
     * @param amount value to set
     */
    public void setEntityHealthPastMax(LivingEntity entity, float amount) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(amount);
        entity.setHealth(amount);
        instance.sendHealthPacket(entity);
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the
     * Entity
     *
     * @param entity Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    private MobModifier createMobModifiers(LivingEntity entity) {
        /* 2-5 modifications standard */
        int number = 2 + entity.level().random.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class<? extends MobModifier>> possibleMods = Lists.newArrayList(mobMods);

        if (entity.level().random.nextInt(config.getUltraRarity()) == 0) // ultra mobs
        {
            number += 3 + entity.level().random.nextInt(2);

            if (entity.level().random.nextInt(config.getInfernoRarity()) == 0) // infernal
            // mobs
            {
                number += 3 + entity.level().random.nextInt(2);
            }
        }

        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more
        // and have some
        {
            /* random index of mod list */
            int index = entity.level().random.nextInt(possibleMods.size());
            MobModifier nextMod = null;

            /*
             * instanciate using one of the two constructors, chainlinking
             * modifiers as we go
             */
            try {
                if (lastMod == null) {
                    nextMod = possibleMods.get(index).getConstructor(new Class[]{}).newInstance();
                } else {
                    nextMod = possibleMods.get(index).getConstructor(new Class[]{MobModifier.class}).newInstance(lastMod);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean allowed = true;
            if (nextMod != null && nextMod.getBlackListMobClasses() != null) {
                for (Class<?> cl : nextMod.getBlackListMobClasses()) {
                    if (entity.getClass().isAssignableFrom(cl)) {
                        allowed = false;
                        break;
                    }
                }
            }
            if (lastMod != null) {
                if (lastMod.getModsNotToMixWith() != null) {
                    for (Class<?> cl : lastMod.getModsNotToMixWith()) {
                        if (lastMod.containsModifierClass(cl)) {
                            allowed = false;
                            break;
                        }
                    }
                }
            }

            /* scratch mod off list */
            possibleMods.remove(index);

            if (allowed) // so can we use it?
            {
                // link it, note that we need one less, repeat
                lastMod = nextMod;
                number--;
            }
        }

        return lastMod;
    }

    /**
     * Converts a String to MobModifier instances and connects them to an Entity
     *
     * @param entity    Target Entity
     * @param savedMods String depicting the MobModifiers, equal to the ingame Display
     */
    public void addEntityModifiersByString(LivingEntity entity, String savedMods) {
        if (!getIsRareEntityOnline(entity)) {
            // this can fire before the localhost client has logged in, loading a world save, need to init the mod!
            initIfNeeded(entity.level());
            MobModifier mod = stringToMobModifiers(savedMods);
            InfernalMobsCore.LOGGER.debug("reloading mods for {}: {}, mod instance {}", entity, savedMods, mod);
            if (mod != null) {
                SidedCache.getInfernalMobs(entity.level()).put(entity, mod);
                mod.onSpawningCompleteStoreModsAndBuffHealth(entity);
            } else {
                System.err.println("Infernal Mobs error, could not instantiate modifier " + savedMods);
            }
        }
    }

    private MobModifier stringToMobModifiers(String buffer) {
        MobModifier lastMod = null;

        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length - 1; j >= 0; j--) {
            String modName = tokens[j];

            MobModifier nextMod = null;
            for (Class<? extends MobModifier> c : mobMods) {
                /*
                 * instanciate using one of the two constructors, chainlinking
                 * modifiers as we go
                 */
                try {
                    if (lastMod == null) {
                        nextMod = c.getConstructor(new Class[]{}).newInstance();
                    } else {
                        nextMod = c.getConstructor(new Class[]{MobModifier.class}).newInstance(lastMod);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (nextMod != null && nextMod.getModName().equals(modName)) {
                    /*
                     * Only actually keep the new linked instance if it's what
                     * we wanted
                     */
                    lastMod = nextMod;
                    break;
                }
            }
        }

        return lastMod;
    }

    /**
     * Used by the client side to answer to a server packet carrying the Entity
     * ID and mod string
     *
     * @param world World the client is in, and the Entity aswell
     * @param entID unique Entity ID
     * @param mods  MobModifier compliant data String from the server
     */
    public void addRemoteEntityModifiers(Level world, int entID, String mods) {
        Entity ent = world.getEntity(entID);
        if (ent != null) {
            addEntityModifiersByString((LivingEntity) ent, mods);
            // System.out.println("Client added remote infernal mod on entity
            // "+ent+", is now "+mod.getModName());
        }
    }

    public void dropLootForEnt(LivingEntity mob, MobModifier mods) {
        int xpValue = 25;
        while (xpValue > 0) {
            int xpDrop = ExperienceOrb.getExperienceValue(xpValue);
            xpValue -= xpDrop;
            mob.level().addFreshEntity(new ExperienceOrb(mob.level(), mob.getX(), mob.getY(), mob.getZ(), xpDrop));
        }

        dropRandomEnchantedItems(mob, mods);
    }

    private void dropRandomEnchantedItems(LivingEntity mob, MobModifier mods) {
        int modStr = mods.getModSize();
        /* 0 for elite, 1 for ultra, 2 for infernal */
        int prefix = (modStr <= 5) ? 0 : (modStr <= 10) ? 1 : 2;
        while (modStr > 0) {
            ItemStack itemStack = getRandomItem(mob, prefix);
            if (itemStack != null) {

                Item item = itemStack.getItem();
                int usedStr = (modStr - 5 > 0) ? 5 : modStr;
                enchantRandomly(mob.level(), itemStack, item.getEnchantmentValue(), usedStr);

                ItemEntity itemEnt = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), itemStack);
                mob.level().addFreshEntity(itemEnt);
                modStr -= 5;
            } else {
                // fixes issue with empty drop lists
                modStr--;
            }
        }
    }

    /**
     * Custom Enchanting Helper
     *
     * @param level              level object
     * @param itemStack          ItemStack to be enchanted
     * @param itemEnchantability ItemStack max enchantability level
     * @param modStr             MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Level level, ItemStack itemStack, int itemEnchantability, int modStr) {
        int remainStr = (modStr + 1) / 2; // should result in 1-3

        Optional<HolderSet.Named<Enchantment>> optional = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.ON_RANDOM_LOOT);
        Stream<Holder<Enchantment>> holderStream = optional.map(HolderSet::stream)
                .orElseGet(() -> level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(e -> e));
        List<?> enchantments = EnchantmentHelper.selectEnchantment(level.getRandom(), itemStack, itemEnchantability, holderStream);

        Iterator<?> iter = enchantments.iterator();
        while (iter.hasNext() && remainStr > 0) {
            remainStr--;
            EnchantmentInstance eData = (EnchantmentInstance) iter.next();
            itemStack.enchant(eData.enchantment, eData.level);
        }
    }

    /**
     * @param mob    Infernal Entity
     * @param prefix 0 for Elite rarity, 1 for Ultra and 2 for Infernal
     * @return ItemStack instance to drop to the World
     */
    private ItemStack getRandomItem(LivingEntity mob, int prefix) {
        List<ItemStack> list = (prefix == 0) ? instance.lootItemDropsElite.getItemStackList() : (prefix == 1) ? instance.lootItemDropsUltra.getItemStackList() : instance.lootItemDropsInfernal.getItemStackList();
        return list.size() > 0 ? list.get(mob.level().random.nextInt(list.size())).copy() : null;
    }

    public void sendVelocityPacket(ServerPlayer target, float xVel, float yVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            VelocityPacket velocityPacket = new VelocityPacket(xVel, yVel, zVel);
            PacketDistributor.sendToPlayer(target, velocityPacket);
        }
    }

    public void sendKnockBackPacket(ServerPlayer target, float xVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            KnockBackPacket knockBackPacket = new KnockBackPacket(xVel, zVel);
            PacketDistributor.sendToPlayer(target, knockBackPacket);
        }
    }

    public void sendHealthPacket(LivingEntity mob) {
        HealthPacket healthPacket = new HealthPacket("", mob.getId(), mob.getHealth(), mob.getMaxHealth());
        PacketDistributor.sendToPlayersTrackingEntity(mob, healthPacket);
    }

    public void sendHealthRequestPacket(String playerName, LivingEntity mob) {
        HealthPacket healthPacket = new HealthPacket(playerName, mob.getId(), 0f, 0f);
        PacketDistributor.sendToServer(healthPacket);
    }

    public void sendAirPacket(ServerPlayer target, int lastAir) {
        if (getIsEntityAllowedTarget(target)) {
            AirPacket airPacket = new AirPacket(lastAir);
            PacketDistributor.sendToPlayer(target, airPacket);
        }
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post tick) {
        if (System.currentTimeMillis() > nextExistCheckTime) {
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            Map<LivingEntity, MobModifier> mobsmap = SidedCache.getInfernalMobs(tick.getLevel());
            // System.out.println("Removed unloaded Entity "+mob+" with ID
            // "+mob.getEntityId()+" from rareMobs");
            mobsmap.keySet().stream().filter(this::filterMob).forEach(InfernalMobsCore::removeEntFromElites);

            resetModifiedPlayerEntitiesAsNeeded(tick.getLevel());
        }

        if (!tick.getLevel().isClientSide()) {
            infCheckA = null;
            infCheckB = null;
        }
    }

    private boolean filterMob(LivingEntity mob) {
        return !mob.isAlive();
    }

    private void resetModifiedPlayerEntitiesAsNeeded(Level world) {
        Iterator<Map.Entry<String, Long>> iterator = modifiedPlayerTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (System.currentTimeMillis() > entry.getValue() + (existCheckDelay * 2)) {
                String username = entry.getKey();
                for (Player player : world.players()) {
                    if (player.getName().getString().equals(username)) {
                        for (Class<? extends MobModifier> c : mobMods) {
                            try {
                                MobModifier mod = c.getConstructor(new Class[]{}).newInstance();
                                mod.resetModifiedVictim(player);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                iterator.remove();
            }
        }
    }

    public boolean getIsHealthBarDisabled() {
        return config.isDisableHealthBar();
    }

    public double getMobModHealthFactor() {
        return config.getModHealthFactor();
    }

    public float getLimitedDamage(float test) {
        return (float) Math.min(test, config.getMaxDamage());
    }

    public boolean getIsEntityAllowedTarget(Entity entity) {
        return entity.isAttackable() && entity.isAlive();
    }

    /**
     * By caching the last reflection pairing we make sure it doesn't trigger
     * more than once (reflections battling each other, infinite loop, crash)
     *
     * @return true when inf loop is suspected, false otherwise
     */
    public boolean isInfiniteLoop(LivingEntity mob, Entity entity) {
        if ((mob == infCheckA && entity == infCheckB) || (mob == infCheckB && entity == infCheckA)) {
            return true;
        }
        infCheckA = mob;
        infCheckB = entity;
        return false;
    }

    /**
     * add modified player entities to this map with the current time. a timer
     * will call a reset on the players to the modifier class. do not remove
     * players from here in a modifier as aliasing may occur (different mods
     * using this at the same time)
     */
    public HashMap<String, Long> getModifiedPlayerTimes() {
        return modifiedPlayerTimes;
    }

}
