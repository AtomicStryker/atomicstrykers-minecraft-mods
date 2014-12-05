package atomicstryker.infernalmobs.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameData;
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
import atomicstryker.infernalmobs.common.mods.MM_Vengeance;
import atomicstryker.infernalmobs.common.mods.MM_Weakness;
import atomicstryker.infernalmobs.common.mods.MM_Webber;
import atomicstryker.infernalmobs.common.mods.MM_Wither;
import atomicstryker.infernalmobs.common.network.AirPacket;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.KnockBackPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import atomicstryker.infernalmobs.common.network.NetworkHelper;
import atomicstryker.infernalmobs.common.network.VelocityPacket;

@Mod(modid = "InfernalMobs", name = "Infernal Mobs", version = "1.6.0")
public class InfernalMobsCore
{
    private final long existCheckDelay = 5000L;

    private long nextExistCheckTime;

    /**
     * Array of ItemStacks
     */
    private ArrayList<ItemStack> dropIdListElite;
    private ArrayList<ItemStack> dropIdListUltra;
    private ArrayList<ItemStack> dropIdListInfernal;

    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    private HashMap<String, Float> classesHealthMap;
    private boolean useSimpleEntityClassNames;
    private boolean disableHealthBar;
    private double modHealthFactor;

    @Instance("InfernalMobs")
    private static InfernalMobsCore instance;

    public static InfernalMobsCore instance()
    {
        return instance;
    }

    public String getNBTTag()
    {
        return "InfernalMobsMod";
    }

    private ArrayList<Class<? extends MobModifier>> mobMods;

    private int eliteRarity;
    private int ultraRarity;
    private int infernoRarity;
    public Configuration config;

    @SidedProxy(clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient", serverSide = "atomicstryker.infernalmobs.common.InfernalMobsServer")
    public static ISidedProxy proxy;

    public NetworkHelper networkHelper;

    private double maxDamage;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        dropIdListElite = new ArrayList<ItemStack>();
        dropIdListUltra = new ArrayList<ItemStack>();
        dropIdListInfernal = new ArrayList<ItemStack>();
        nextExistCheckTime = System.currentTimeMillis();
        classesAllowedMap = new HashMap<String, Boolean>();
        classesForcedMap = new HashMap<String, Boolean>();
        classesHealthMap = new HashMap<String, Float>();

        config = new Configuration(evt.getSuggestedConfigurationFile());
        loadMods();

        proxy.preInit();
        FMLCommonHandler.instance().bus().register(this);

        networkHelper = new NetworkHelper("AS_IF", MobModsPacket.class, HealthPacket.class, VelocityPacket.class, KnockBackPacket.class, AirPacket.class);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());

        proxy.load();

        System.out.println("InfernalMobsCore load() completed! Modifiers ready: " + mobMods.size());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        // lets use postInit so mod Blocks and Items are present
        loadConfig();
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartingEvent evt)
    {
        evt.registerServerCommand(new InfernalCommandFindEntityClass());
        evt.registerServerCommand(new InfernalCommandSpawnInfernal());
    }

    /**
     * Registers the MobModifier classes for consideration
     * 
     * @param config
     */
    private void loadMods()
    {
        mobMods = new ArrayList<Class<? extends MobModifier>>();

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

        config.load();
        Iterator<Class<? extends MobModifier>> iter = mobMods.iterator();
        while (iter.hasNext())
        {
            Class<?> c = iter.next();
            if (!config.get(Configuration.CATEGORY_GENERAL, c.getSimpleName() + " enabled", true).getBoolean(true))
            {
                iter.remove();
            }
        }
        config.save();
    }

    /**
     * Forge Config file
     */
    private void loadConfig()
    {
        config.load();

        eliteRarity =
                Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "eliteRarity", 15, "One in THIS many Mobs will become atleast rare")
                        .getString());
        ultraRarity =
                Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "ultraRarity", 7,
                        "One in THIS many already rare Mobs will become atleast ultra").getString());
        infernoRarity =
                Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "infernoRarity", 7,
                        "One in THIS many already ultra Mobs will become infernal").getString());
        useSimpleEntityClassNames =
                config.get(Configuration.CATEGORY_GENERAL, "useSimpleEntityClassnames", true,
                        "Use Entity class names instead of ingame Entity names for the config").getBoolean(true);
        disableHealthBar =
                config.get(Configuration.CATEGORY_GENERAL, "disableGUIoverlay", false, "Disables the ingame Health and Name overlay").getBoolean(
                        false);
        modHealthFactor =
                config.get(Configuration.CATEGORY_GENERAL, "mobHealthFactor", "1.0", "Multiplier applied ontop of all of the modified Mobs health")
                        .getDouble(1.0D);

        parseItemsForList(
                config.get(
                        Configuration.CATEGORY_GENERAL,
                        "droppedItemIDsElite",
                        "iron_shovel,iron_pickaxe,iron_axe,iron_sword,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,cookie-0-6",
                        "List of equally likely to drop Items for Elites, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListElite);

        parseItemsForList(
                config.get(
                        Configuration.CATEGORY_GENERAL,
                        "droppedItemIDsUltra",
                        "bow,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,golden_helmet,golden_chestplate,golden_leggings,golden_boots,golden_apple,blaze_powder-0-3,enchanted_book",
                        "List of equally likely to drop Items for Ultras, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListUltra);

        parseItemsForList(
                config.get(
                        Configuration.CATEGORY_GENERAL,
                        "droppedItemIDsInfernal",
                        "diamond-0-3,diamond_sword,diamond_shovel,diamond_pickaxe,diamond_axe,diamond_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,diamond_helmet,diamond_chestplate,diamond_leggings,diamond_boots,ender_pearl,enchanted_book",
                        "List of equally likely to drop Items for Infernals, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListInfernal);

        maxDamage =
                config.get(Configuration.CATEGORY_GENERAL, "maxOneShotDamage", 10d,
                        "highest amount of damage an Infernal Mob or reflecting Mod will do in a single strike").getDouble(10d);

        config.save();
    }

    private void parseItemsForList(String itemIDs, ArrayList<ItemStack> list)
    {
        Random rand = new Random();
        itemIDs = itemIDs.trim();
        for (String s : itemIDs.split(","))
        {
            String[] meta = s.split("-");

            Object itemOrBlock = parseOrFind(meta[0]);
            if (itemOrBlock != null)
            {
                int imeta = (meta.length > 1) ? Integer.parseInt(meta[1]) : 0;
                int stackSize = (meta.length > 2) ? Integer.parseInt(meta[2]) : 1;
                int randomizer = (meta.length > 3) ? Integer.parseInt(meta[3]) + 1 : 1;
                if (randomizer < 1)
                {
                    randomizer = 1;
                }

                if (itemOrBlock instanceof Block)
                {
                    list.add(new ItemStack(((Block) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                }
                else
                {
                    list.add(new ItemStack(((Item) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                }
            }
        }
    }

    private Object parseOrFind(String s)
    {
        Item item = GameData.getItemRegistry().getObject(s);
        if (item != null)
        {
            return item;
        }

        Block block = GameData.getBlockRegistry().getObject(s);
        if (block != Blocks.air)
        {
            return block;
        }
        return null;
    }

    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn
     * them into Elites here
     * 
     * @param entity
     *            Entity in question
     */
    public void processEntitySpawn(EntityLivingBase entity)
    {
        if (!entity.worldObj.isRemote)
        {
            if (!getIsRareEntity(entity))
            {
                if ((entity instanceof EntityMob || (entity instanceof EntityLivingBase && entity instanceof IMob)) && instance.checkEntityClassAllowed(entity)
                        && (instance.checkEntityClassForced(entity) || entity.worldObj.rand.nextInt(eliteRarity) == 0))
                {
                    MobModifier mod = instance.createMobModifiers(entity);
                    if (mod != null)
                    {
                        proxy.getRareMobs().put(entity, mod);
                        mod.onSpawningComplete(entity);
                        // System.out.println("InfernalMobsCore modded mob: "+entity+", id "+entity.getEntityId()+": "+mod.getLinkedModName());
                    }
                }
            }
        }
    }

    private String getEntityNameSafe(Entity entity)
    {
        String result;
        try
        {
            result = EntityList.getEntityString(entity);
        }
        catch (Exception e)
        {
            result = entity.getClass().getSimpleName();
            System.err.println("Entity of class " + result
                    + " crashed when EntityList.getEntityString was queried, for shame! Using classname instead.");
            System.err.println("If this message is spamming too much for your taste set useSimpleEntityClassnames true in your Infernal Mobs config");
        }
        return result;
    }

    private boolean checkEntityClassAllowed(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesAllowedMap.containsKey(entName))
        {
            return classesAllowedMap.get(entName);
        }

        config.load();
        boolean result = config.get("permittedentities", entName, true).getBoolean(true);
        config.save();
        classesAllowedMap.put(entName, result);

        return result;
    }

    private boolean checkEntityClassForced(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesForcedMap.containsKey(entName))
        {
            return classesForcedMap.get(entName);
        }

        config.load();
        boolean result = config.get("entitiesalwaysinfernal", entName, false).getBoolean(false);
        config.save();
        classesForcedMap.put(entName, result);

        return result;
    }

    public float getMobClassMaxHealth(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesHealthMap.containsKey(entName))
        {
            return classesHealthMap.get(entName);
        }

        config.load();
        float result = (float) config.get("entitybasehealth", entName, entity.getMaxHealth()).getDouble(entity.getMaxHealth());
        config.save();
        classesHealthMap.put(entName, result);

        return result;
    }

    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     * 
     * @param entity
     *            Entity instance whose health you want changed
     * @param amount
     *            value to set
     */
    public void setEntityHealthPastMax(EntityLivingBase entity, float amount)
    {
        entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(amount);
        entity.setHealth(amount);
        instance.sendHealthPacket(entity, amount);
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the
     * Entity
     * 
     * @param entity
     *            Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    @SuppressWarnings("unchecked")
    private MobModifier createMobModifiers(EntityLivingBase entity)
    {
        /* 2-5 modifications standard */
        int number = 2 + entity.worldObj.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class<? extends MobModifier>> possibleMods = (ArrayList<Class<? extends MobModifier>>) mobMods.clone();

        if (entity.worldObj.rand.nextInt(ultraRarity) == 0) // ultra mobs
        {
            number += 3 + entity.worldObj.rand.nextInt(2);

            if (entity.worldObj.rand.nextInt(infernoRarity) == 0) // infernal
                                                                  // mobs
            {
                number += 3 + entity.worldObj.rand.nextInt(2);
            }
        }

        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more
                                                      // and have some
        {
            /* random index of mod list */
            int index = entity.worldObj.rand.nextInt(possibleMods.size());
            MobModifier nextMod = null;

            /*
             * instanciate using one of the two constructors, chainlinking
             * modifiers as we go
             */
            try
            {
                if (lastMod == null)
                {
                    nextMod = possibleMods.get(index).getConstructor(new Class[] { EntityLivingBase.class }).newInstance(entity);
                }
                else
                {
                    nextMod =
                            possibleMods.get(index).getConstructor(new Class[] { EntityLivingBase.class, MobModifier.class })
                                    .newInstance(entity, lastMod);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            boolean allowed = true;
            if (nextMod != null && nextMod.getBlackListMobClasses() != null)
            {
                for (Class<?> cl : nextMod.getBlackListMobClasses())
                {
                    if (entity.getClass().isAssignableFrom(cl))
                    {
                        allowed = false;
                        break;
                    }
                }
            }
            if (lastMod != null)
            {
                if (lastMod.getModsNotToMixWith() != null)
                {
                    for (Class<?> cl : lastMod.getModsNotToMixWith())
                    {
                        if (lastMod.containsModifierClass(cl))
                        {
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
     * @param entity
     *            Target Entity
     * @param savedMods
     *            String depicting the MobModifiers, equal to the ingame Display
     */
    public void addEntityModifiersByString(EntityLivingBase entity, String savedMods)
    {
        if (!getIsRareEntity(entity))
        {
            MobModifier mod = stringToMobModifiers(entity, savedMods);
            if (mod != null)
            {
                proxy.getRareMobs().put(entity, mod);
                mod.onSpawningComplete(entity);
                mod.setHealthAlreadyHacked(entity);
            }
            else
            {
                System.err.println("Infernal Mobs error, could not instantiate modifier "+savedMods);
            }
        }
    }

    private MobModifier stringToMobModifiers(EntityLivingBase entity, String buffer)
    {
        MobModifier lastMod = null;

        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length - 1; j >= 0; j--)
        {
            String modName = tokens[j];

            MobModifier nextMod = null;
            for (Class<? extends MobModifier> c : mobMods)
            {
                /*
                 * instanciate using one of the two constructors, chainlinking
                 * modifiers as we go
                 */
                try
                {
                    if (lastMod == null)
                    {
                        nextMod = c.getConstructor(new Class[] { EntityLivingBase.class }).newInstance(entity);
                    }
                    else
                    {
                        nextMod = c.getConstructor(new Class[] { EntityLivingBase.class, MobModifier.class }).newInstance(entity, lastMod);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (nextMod != null && nextMod.modName.equals(modName))
                {
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

    public static MobModifier getMobModifiers(EntityLivingBase ent)
    {
        return proxy.getRareMobs().get(ent);
    }

    public static boolean getIsRareEntity(EntityLivingBase ent)
    {
        return proxy.getRareMobs().containsKey(ent);
    }

    public static void removeEntFromElites(EntityLivingBase entity)
    {
        proxy.getRareMobs().remove(entity);
    }

    /**
     * Used by the client side to answer to a server packet carrying the Entity
     * ID and mod string
     * 
     * @param world
     *            World the client is in, and the Entity aswell
     * @param entID
     *            unique Entity ID
     * @param mods
     *            MobModifier compliant data String from the server
     */
    public void addRemoteEntityModifiers(World world, int entID, String mods)
    {
        Entity ent = world.getEntityByID(entID);
        if (ent != null)
        {
            addEntityModifiersByString((EntityLivingBase) ent, mods);
            // System.out.println("Client added remote infernal mod on entity "+ent+", is now "+mod.getModName());
        }
    }

    public void dropLootForEnt(EntityLivingBase mob, MobModifier mods)
    {
        int xpValue = 25;
        while (xpValue > 0)
        {
            int xpDrop = EntityXPOrb.getXPSplit(xpValue);
            xpValue -= xpDrop;
            mob.worldObj.spawnEntityInWorld(new EntityXPOrb(mob.worldObj, mob.posX, mob.posY, mob.posZ, xpDrop));
        }

        dropRandomEnchantedItems(mob, mods);
    }

    private void dropRandomEnchantedItems(EntityLivingBase mob, MobModifier mods)
    {
        int modStr = mods.getModSize();
        /* 0 for elite, 1 for ultra, 2 for infernal */
        int prefix = (modStr <= 5) ? 0 : (modStr <= 10) ? 1 : 2;
        while (modStr > 0)
        {
            ItemStack itemStack = getRandomItem(mob, prefix);
            if (itemStack != null)
            {
                Item item = itemStack.getItem();
                if (item != null && item instanceof Item)
                {
                    if (item instanceof ItemEnchantedBook)
                    {
                    	ItemEnchantedBook book = (ItemEnchantedBook) item;
                        itemStack = book.getRandom(mob.getRNG()).theItemId;
                    }
                    else
                    {
                        int usedStr = (modStr - 5 > 0) ? 5 : modStr;
                        enchantRandomly(mob.worldObj.rand, itemStack, item.getItemEnchantability(), usedStr);
                        // EnchantmentHelper.addRandomEnchantment(mob.worldObj.rand,
                        // itemStack, item.getItemEnchantability());
                    }
                }
                EntityItem itemEnt = new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, itemStack);
                mob.worldObj.spawnEntityInWorld(itemEnt);
                modStr -= 5;
            }
            else
            {
                // fixes issue with empty drop lists
                modStr--;
            }
        }
    }

    /**
     * Custom Enchanting Helper
     * 
     * @param rand
     *            Random gen to use
     * @param itemStack
     *            ItemStack to be enchanted
     * @param itemEnchantability
     *            ItemStack max enchantability level
     * @param modStr
     *            MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Random rand, ItemStack itemStack, int itemEnchantability, int modStr)
    {
        int remainStr = (modStr + 1) / 2; // should result in 1-3
        List<?> enchantments = EnchantmentHelper.buildEnchantmentList(rand, itemStack, itemEnchantability);
        if (enchantments != null)
        {
            Iterator<?> iter = enchantments.iterator();
            while (iter.hasNext() && remainStr > 0)
            {
                remainStr--;
                EnchantmentData eData = (EnchantmentData) iter.next();
                itemStack.addEnchantment(eData.enchantmentobj, eData.enchantmentLevel);
            }
        }
    }

    /**
     * @param mob
     *            Infernal Entity
     * @param prefix
     *            0 for Elite rarity, 1 for Ultra and 2 for Infernal
     * @return ItemStack instance to drop to the World
     */
    private ItemStack getRandomItem(EntityLivingBase mob, int prefix)
    {
        ArrayList<ItemStack> list = (prefix == 0) ? instance.dropIdListElite : (prefix == 1) ? instance.dropIdListUltra : instance.dropIdListInfernal;
        return list.size() > 0 ? list.get(mob.worldObj.rand.nextInt(list.size())).copy() : null;
    }

    public void sendVelocityPacket(EntityPlayerMP target, float xVel, float yVel, float zVel)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new VelocityPacket(xVel, yVel, zVel), target);
        }
    }

    public void sendKnockBackPacket(EntityPlayerMP target, float xVel, float zVel)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new KnockBackPacket(xVel, zVel), target);
        }
    }

    public void sendHealthPacket(EntityLivingBase mob, float health)
    {
        networkHelper.sendPacketToAllAroundPoint(new HealthPacket("", mob.getEntityId(), mob.getHealth(), mob.getMaxHealth()), new TargetPoint(
                mob.dimension, mob.posX, mob.posY, mob.posZ, 32d));
    }

    public void sendHealthRequestPacket(EntityLivingBase mob)
    {
        networkHelper.sendPacketToServer(new HealthPacket(FMLClientHandler.instance().getClient().thePlayer.getGameProfile().getName(), mob
                .getEntityId(), 0f, 0f));
    }
    
    public void sendAirPacket(EntityPlayerMP target, int lastAir)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new AirPacket(lastAir), target);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        if (System.currentTimeMillis() > nextExistCheckTime)
        {
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs();
            for (EntityLivingBase mob : mobsmap.keySet())
            {
                if (mob.isDead || !mob.worldObj.loadedEntityList.contains(mob))
                {
                    // System.out.println("Removed unloaded Entity "+mob+" with ID "+mob.getEntityId()+" from rareMobs");
                    removeEntFromElites((EntityLivingBase) mob);
                }
            }
        }
    }

    public boolean getIsHealthBarDisabled()
    {
        return disableHealthBar;
    }

    public double getMobModHealthFactor()
    {
        return modHealthFactor;
    }
    
    public float getLimitedDamage(float test)
    {
        return (float) Math.min(test, maxDamage);
    }

    public boolean getIsEntityAllowedTarget(Entity entity)
    {
        return !(entity instanceof FakePlayer);
    }

}
