package atomicstryker.infernalmobs.common;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.mods.*;
import atomicstryker.infernalmobs.common.network.*;
import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

@Mod(InfernalMobsCore.MOD_ID)
@Mod.EventBusSubscriber(modid = InfernalMobsCore.MOD_ID)
public class InfernalMobsCore {

    static final String MOD_ID = "infernalmobs";
    public static Logger LOGGER;
    public static ISidedProxy proxy = DistExecutor.safeRunForDist(() -> InfernalMobsClient::new, () -> InfernalMobsServer::new);
    private static InfernalMobsCore instance;
    private final long existCheckDelay = 5000L;
    public NetworkHelper networkHelper;
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

    public InfernalMobsCore() {
        instance = this;

        nextExistCheckTime = System.currentTimeMillis();
        classesAllowedMap = new HashMap<>();
        classesForcedMap = new HashMap<>();
        classesHealthMap = new HashMap<>();
        modifiedPlayerTimes = new HashMap<>();

        MinecraftForge.EVENT_BUS.register(this);
        proxy.preInit();

        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());

        networkHelper = new NetworkHelper("infernalmobs", MobModsPacket.class, HealthPacket.class, VelocityPacket.class, KnockBackPacket.class, AirPacket.class);

        LOGGER = LogManager.getLogger();
    }

    public static InfernalMobsCore instance() {
        return instance;
    }

    public static MobModifier getMobModifiers(LivingEntity ent) {
        return proxy.getRareMobs().get(ent);
    }

    public static boolean getIsRareEntity(LivingEntity ent) {
        return proxy.getRareMobs().containsKey(ent);
    }

    public static void removeEntFromElites(LivingEntity entity) {
        proxy.getRareMobs().remove(entity);
    }

    public String getNBTTag() {
        return "InfernalMobsMod";
    }

    @SubscribeEvent
    public void commonSetup(FMLServerStartedEvent evt) {
        // dedicated server starting point
        initIfNeeded();
    }

    /**
     * is triggered either by server start or by client login event from InfernalMobsClient
     */
    public void initIfNeeded() {
        if (mobMods == null) {
            prepareModList();

            proxy.load();

            configFile = new File(proxy.getMcFolder(), File.separatorChar + "config" + File.separatorChar + "infernalmobs.cfg");
            loadConfig();

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
        mobMods.add(MM_Vengeance.class);
        mobMods.add(MM_Weakness.class);
        mobMods.add(MM_Webber.class);
        mobMods.add(MM_Wither.class);
    }

    /**
     * Forge Config file
     */
    private void loadConfig() {
        InfernalMobsConfig defaultConfig = new InfernalMobsConfig();
        defaultConfig.setEliteRarity(15);
        defaultConfig.setUltraRarity(7);
        defaultConfig.setInfernoRarity(7);
        defaultConfig.setUseSimpleEntityClassNames(true);
        defaultConfig.setDisableHealthBar(false);
        defaultConfig.setModHealthFactor(1.0D);

        List<String> dropsElite = new ArrayList<>();
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_SHOVEL)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_PICKAXE)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_AXE)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_SWORD)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HOE)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HELMET)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_BOOTS)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_CHESTPLATE)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_LEGGINGS)));
        dropsElite.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.COOKIE, 5)));
        defaultConfig.setDroppedItemIDsElite(dropsElite);

        List<String> dropsUltra = new ArrayList<>();
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HOE)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.BOW)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_HELMET)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_BOOTS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_CHESTPLATE)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.IRON_LEGGINGS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_HELMET)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_BOOTS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_CHESTPLATE)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_LEGGINGS)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.GOLDEN_APPLE, 3)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.BLAZE_POWDER, 5)));
        dropsUltra.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENCHANTED_BOOK)));
        defaultConfig.setDroppedItemIDsUltra(dropsUltra);

        List<String> dropsInfernal = new ArrayList<>();
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENCHANTED_BOOK)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND, 3)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_SWORD)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_AXE)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_HOE)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_PICKAXE)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_SHOVEL)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_HELMET)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_BOOTS)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.CHAINMAIL_LEGGINGS)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_HELMET)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_BOOTS)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.DIAMOND_LEGGINGS)));
        dropsInfernal.add(ItemConfigHelper.fromItemStack(new ItemStack(Items.ENDER_PEARL, 3)));
        defaultConfig.setDroppedItemIDsInfernal(dropsInfernal);

        defaultConfig.setMaxDamage(10D);
        defaultConfig.setDimensionIDBlackList(new ArrayList<>());

        Map<String, Boolean> modsEnabledMap = new HashMap<>();
        for (Class<?> c : mobMods) {
            modsEnabledMap.put(c.getSimpleName(), true);
        }
        defaultConfig.setModsEnabled(modsEnabledMap);

        config = GsonConfig.loadConfigWithDefault(InfernalMobsConfig.class, configFile, defaultConfig);

        lootItemDropsElite = new ItemConfigHelper(config.getDroppedItemIDsElite(), LOGGER);
        lootItemDropsUltra = new ItemConfigHelper(config.getDroppedItemIDsUltra(), LOGGER);
        lootItemDropsInfernal = new ItemConfigHelper(config.getDroppedItemIDsInfernal(), LOGGER);

        mobMods.removeIf(c -> !config.getModsEnabled().containsKey(c.getSimpleName()) || !config.getModsEnabled().get(c.getSimpleName()));
    }

    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn
     * them into Elites here
     *
     * @param entity Entity in question
     */
    public void processEntitySpawn(LivingEntity entity) {
        if (!entity.world.isRemote && config != null) {
            if (!getIsRareEntity(entity)) {
                if (isClassAllowed(entity) && (instance.checkEntityClassForced(entity) || entity.world.rand.nextInt(config.getEliteRarity()) == 0)) {
                    try {
                        /*
                            get server world from resource location:
                            RegistryKey<World> registrykey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, resourcelocation);
                            ServerWorld serverworld = p_212592_0_.getSource().getServer().getWorld(registrykey);
                         */
                        RegistryKey<World> worldRegistryKey = entity.getEntityWorld().getDimensionKey();
                        ResourceLocation worldResourceLocation = worldRegistryKey.func_240901_a_();

                        // Skip Infernal-Spawn when Dimension is Blacklisted, entries look like: "minecraft:overworld"
                        if (!config.getDimensionIDBlackList().contains(worldResourceLocation.toString())) {
                            MobModifier mod = instance.createMobModifiers(entity);
                            if (mod != null) {
                                proxy.getRareMobs().put(entity, mod);
                                mod.onSpawningComplete(entity);
                                // System.out.println("InfernalMobsCore modded
                                // mob: "+entity+", id "+entity.getEntityId()+":
                                // "+mod.getLinkedModName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.ERROR, "processEntitySpawn() threw an exception");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isClassAllowed(LivingEntity entity) {
        if ((entity instanceof IMob)) {
            if (entity instanceof TameableEntity) {
                return false;
            }
            if (instance.checkEntityClassAllowed(entity)) {
                return true;
            }
        }
        return false;
    }

    private String getEntityNameSafe(Entity entity) {
        String result;
        try {
            result = ForgeRegistries.ENTITIES.getKey(entity.getType()).getPath();
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
        int number = 2 + entity.world.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class<? extends MobModifier>> possibleMods = Lists.newArrayList(mobMods);

        if (entity.world.rand.nextInt(config.getUltraRarity()) == 0) // ultra mobs
        {
            number += 3 + entity.world.rand.nextInt(2);

            if (entity.world.rand.nextInt(config.getInfernoRarity()) == 0) // infernal
            // mobs
            {
                number += 3 + entity.world.rand.nextInt(2);
            }
        }

        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more
        // and have some
        {
            /* random index of mod list */
            int index = entity.world.rand.nextInt(possibleMods.size());
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
        if (!getIsRareEntity(entity)) {
            // this can fire before the localhost client has logged in, loading a world save, need to init the mod!
            initIfNeeded();
            MobModifier mod = stringToMobModifiers(savedMods);
            if (mod != null) {
                proxy.getRareMobs().put(entity, mod);
                mod.onSpawningComplete(entity);
                mod.setHealthAlreadyHacked(entity);
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
    public void addRemoteEntityModifiers(World world, int entID, String mods) {
        Entity ent = world.getEntityByID(entID);
        if (ent != null) {
            addEntityModifiersByString((LivingEntity) ent, mods);
            // System.out.println("Client added remote infernal mod on entity
            // "+ent+", is now "+mod.getModName());
        }
    }

    public void dropLootForEnt(LivingEntity mob, MobModifier mods) {
        int xpValue = 25;
        while (xpValue > 0) {
            int xpDrop = ExperienceOrbEntity.getXPSplit(xpValue);
            xpValue -= xpDrop;
            mob.world.addEntity(new ExperienceOrbEntity(mob.world, mob.getPosX(), mob.getPosY(), mob.getPosZ(), xpDrop));
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
                if (item instanceof EnchantedBookItem) {
                    itemStack = EnchantedBookItem.getEnchantedItemStack(getRandomEnchantment(mob.getRNG()));
                } else {
                    int usedStr = (modStr - 5 > 0) ? 5 : modStr;
                    enchantRandomly(mob.world.rand, itemStack, item.getItemEnchantability(), usedStr);
                    // EnchantmentHelper.addRandomEnchantment(mob.world.rand,
                    // itemStack, item.getItemEnchantability());
                }
                ItemEntity itemEnt = new ItemEntity(mob.world, mob.getPosX(), mob.getPosY(), mob.getPosZ(), itemStack);
                mob.world.addEntity(itemEnt);
                modStr -= 5;
            } else {
                // fixes issue with empty drop lists
                modStr--;
            }
        }
    }

    private EnchantmentData getRandomEnchantment(Random rand) {
        if (enchantmentList == null) {
            enchantmentList = new ArrayList<>(26); // 26 is the vanilla
            // enchantment count as of
            // 1.9
            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
                if (enchantment != null && enchantment.type != null) {
                    enchantmentList.add(enchantment);
                }
            }
        }

        Enchantment e = enchantmentList.get(rand.nextInt(enchantmentList.size()));
        int min = e.getMinLevel();
        int range = e.getMaxLevel() - min;
        int lvl = min + rand.nextInt(range + 1);
        EnchantmentData ed = new EnchantmentData(e, lvl);
        return ed;
    }

    /**
     * Custom Enchanting Helper
     *
     * @param rand               Random gen to use
     * @param itemStack          ItemStack to be enchanted
     * @param itemEnchantability ItemStack max enchantability level
     * @param modStr             MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Random rand, ItemStack itemStack, int itemEnchantability, int modStr) {
        int remainStr = (modStr + 1) / 2; // should result in 1-3
        List<?> enchantments = EnchantmentHelper.buildEnchantmentList(rand, itemStack, itemEnchantability, true);
        if (enchantments != null) {
            Iterator<?> iter = enchantments.iterator();
            while (iter.hasNext() && remainStr > 0) {
                remainStr--;
                EnchantmentData eData = (EnchantmentData) iter.next();
                itemStack.addEnchantment(eData.enchantment, eData.enchantmentLevel);
            }
        }
    }

    /**
     * @param mob    Infernal Entity
     * @param prefix 0 for Elite rarity, 1 for Ultra and 2 for Infernal
     * @return ItemStack instance to drop to the World
     */
    private ItemStack getRandomItem(LivingEntity mob, int prefix) {
        List<ItemStack> list = (prefix == 0) ? instance.lootItemDropsElite.getItemStackList() : (prefix == 1) ? instance.lootItemDropsUltra.getItemStackList() : instance.lootItemDropsInfernal.getItemStackList();
        return list.size() > 0 ? list.get(mob.world.rand.nextInt(list.size())).copy() : null;
    }

    public void sendVelocityPacket(ServerPlayerEntity target, float xVel, float yVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new VelocityPacket(xVel, yVel, zVel), target);
        }
    }

    public void sendKnockBackPacket(ServerPlayerEntity target, float xVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new KnockBackPacket(xVel, zVel), target);
        }
    }

    public void sendHealthPacket(LivingEntity mob) {
        networkHelper.sendPacketToAllAroundPoint(new HealthPacket("", mob.getEntityId(), mob.getHealth(), mob.getMaxHealth()), new PacketDistributor.TargetPoint(mob.getPosX(), mob.getPosY(), mob.getPosZ(), 32d, mob.getEntityWorld().getDimensionKey()));
    }

    public void sendHealthRequestPacket(String playerName, LivingEntity mob) {
        networkHelper.sendPacketToServer(new HealthPacket(playerName, mob.getEntityId(), 0f, 0f));
    }

    public void sendAirPacket(ServerPlayerEntity target, int lastAir) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new AirPacket(lastAir), target);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick) {
        if (System.currentTimeMillis() > nextExistCheckTime) {
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            Map<LivingEntity, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs();
            // System.out.println("Removed unloaded Entity "+mob+" with ID
            // "+mob.getEntityId()+" from rareMobs");
            mobsmap.keySet().stream().filter(this::filterMob).forEach(InfernalMobsCore::removeEntFromElites);

            resetModifiedPlayerEntitiesAsNeeded(tick.world);
        }

        if (!tick.world.isRemote) {
            infCheckA = null;
            infCheckB = null;
        }
    }

    private boolean filterMob(LivingEntity mob) {
        return !mob.isAlive() || mob.world == null;
    }

    private void resetModifiedPlayerEntitiesAsNeeded(World world) {
        Iterator<Map.Entry<String, Long>> iterator = modifiedPlayerTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (System.currentTimeMillis() > entry.getValue() + (existCheckDelay * 2)) {
                String username = entry.getKey();
                for (PlayerEntity player : world.getPlayers()) {
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
        return !(entity instanceof FakePlayer);
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
