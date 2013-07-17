package atomicstryker.infernalmobs.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.client.ClientPacketHandler;
import atomicstryker.infernalmobs.common.mods.MM_1UP;
import atomicstryker.infernalmobs.common.mods.MM_Alchemist;
import atomicstryker.infernalmobs.common.mods.MM_Berserk;
import atomicstryker.infernalmobs.common.mods.MM_Blastoff;
import atomicstryker.infernalmobs.common.mods.MM_Bulwark;
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
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "InfernalMobs", name = "Infernal Mobs", version = "1.3.3")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ServerPacketHandler.class))
public class InfernalMobsCore implements ITickHandler
{
    private final long existCheckDelay = 5000L;
    
    private long nextExistCheckTime;
    
    /**
     * Array of two ints, first Block or Item ID, second meta value, third stacksize, fourth randomizerRange
     */
    private ArrayList<Integer[]> dropIdList;
    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    private ArrayList<String[]> failedItemStrings;
    private boolean useSimpleEntityClassNames;
    private boolean disableHealthBar;
    private double modHealthFactor;
    
    private static InfernalMobsCore instance;
    
    public static InfernalMobsCore instance()
    {
        return instance;
    }
    
    public String getNBTTag()
    {
        return "InfernalMobsMod";
    }
    
    private HashSet<Class<? extends MobModifier>> mobMods;
    
    private int eliteRarity;
    private int ultraRarity;
    private int infernoRarity;
    public Configuration config;
    
    @SidedProxy(clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient", serverSide = "atomicstryker.infernalmobs.common.InfernalMobsServer")
    public static ISidedProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        dropIdList = new ArrayList<Integer[]>();
        nextExistCheckTime = System.currentTimeMillis();
        classesAllowedMap = new HashMap<String, Boolean>();
        classesForcedMap = new HashMap<String, Boolean>();
        failedItemStrings = new ArrayList<String[]>();
        
        config = new Configuration(evt.getSuggestedConfigurationFile());
        loadMods();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());
        
        proxy.load();
        
        System.out.println("InfernalMobsCore load() completed! Modifiers ready: "+mobMods.size());
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        // lets use postInit so mod Blocks and Items are present
        loadConfig();
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        TickRegistry.registerTickHandler(this, Side.SERVER);
    }
    
    /**
     * Registers the MobModifier classes for consideration
     * @param config 
     */
    private void loadMods()
    {
        mobMods = new HashSet<Class<? extends MobModifier>>();
        /*
        try
        {
            Class<?> cx;
            ClassPath cp = ClassPath.from(getClass().getClassLoader());
            System.out.println("Infernal Mobs about to discover mod classes...");
            for (ClassInfo c : cp.getTopLevelClasses("atomicstryker.infernalmobs.common.mods"))
            {
                cx = c.load();
                mobMods.add((Class<? extends MobModifier>) cx);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        */
        
        mobMods.add(MM_1UP.class);
        mobMods.add(MM_Alchemist.class);
        mobMods.add(MM_Berserk.class);
        mobMods.add(MM_Blastoff.class);
        mobMods.add(MM_Bulwark.class);
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
            if (!config.get(Configuration.CATEGORY_GENERAL, c.getSimpleName()+" enabled", true).getBoolean(true))
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
        
        eliteRarity = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "eliteRarity", 15, "One in THIS many Mobs will become atleast rare").getString());
        ultraRarity = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "ultraRarity", 7, "One in THIS many already rare Mobs will become atleast ultra").getString());
        infernoRarity = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "infernoRarity", 7, "One in THIS many already ultra Mobs will become infernal").getString());
        String itemIDs = config.get(Configuration.CATEGORY_GENERAL, "droppedItemIDs",
                "256,257,258,261,267,276,277,278,279,292,293,302,303,304,305,306,307,308,309,310,311,312,313,403",
                "List of equally likely to drop Items seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog").getString();
        useSimpleEntityClassNames = config.get(Configuration.CATEGORY_GENERAL, "useSimpleEntityClassnames", false, "Use Entity class names instead of ingame Entity names for the config").getBoolean(false);
        disableHealthBar = config.get(Configuration.CATEGORY_GENERAL, "disableGUIoverlay", false, "Disables the ingame Health and Name overlay").getBoolean(false);
        modHealthFactor = config.get(Configuration.CATEGORY_GENERAL, "mobHealthFactor", "1.0", "Multiplier applied ontop of all of the modified Mobs health").getDouble(1.0D);
        
        itemIDs = itemIDs.trim();
        for (String s : itemIDs.split(","))
        {
            String[] meta = s.split("-");
            
            int id = parseOrFind(meta[0]);
            if (id > 0)
            {
                Integer[] ints = {
                        id,
                        (meta.length > 1) ? Integer.parseInt(meta[1]) : 0,
                        (meta.length > 2) ? Integer.parseInt(meta[2]) : 1,
                        (meta.length > 3) ? Integer.parseInt(meta[3]) : 0
                                };
                instance.dropIdList.add(ints);
            }
            else
            {
                failedItemStrings.add(meta);
            }
        }
        
        config.save();
    }
    
    /**
     * Attempts to parse a String as number, or the name of a block, or the name of an item
     * @param s input String or blockname or itemname
     * @return block or item ID depicted, or 0 if nothing was found
     */
    private int parseOrFind(String s)
    {
        int result = 0;
        try
        {
            result = Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            result = 0;
            for (Item i : Item.itemsList)
            {
                if (i != null && i.getUnlocalizedName() != null && i.getUnlocalizedName().equals(s))
                {
                    return i.itemID;
                }
            }
            for (Block b : Block.blocksList)
            {
                if (b != null && b.getUnlocalizedName() != null && b.getUnlocalizedName().equals(s))
                {
                    return b.blockID;
                }
            }
            
            if (result == 0)
            {
                System.out.println("Infernal Mobs Item config failed to find match for ["+s+"], checking for partial matches...");
                for (Item i : Item.itemsList)
                {
                    if (i != null && i.getUnlocalizedName() != null && i.getUnlocalizedName().contains(s))
                    {
                        System.out.println("Infernal Mobs Item config going with partial match ["+i.getUnlocalizedName()+"], id "+i.itemID+" "+i);
                        return i.itemID;
                    }
                }
                for (Block b : Block.blocksList)
                {
                    if (b != null && b.getUnlocalizedName() != null && b.getUnlocalizedName().contains(s))
                    {
                        System.out.println("Infernal Mobs Item config going with partial match ["+b.getUnlocalizedName()+"], id "+b.blockID+" "+b);
                        return b.blockID;
                    }
                }
                System.out.println("Infernal Mobs Item config failed to find any match for ["+s+"]");
            }
        }
        return result;
    }
    
    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn them into Elites here
     * @param entity Entity in question
     */
    public void processEntitySpawn(EntityLivingBase entity)
    {
        if (!entity.worldObj.isRemote)
        {
            if (!getIsRareEntity(entity))
            {
                if ((entity instanceof EntityMob || entity instanceof IMob)
                && instance.checkEntityClassAllowed(entity)
                && (instance.checkEntityClassForced(entity) || entity.worldObj.rand.nextInt(eliteRarity) == 0))
                {
                    MobModifier mod = instance.createMobModifiers(entity);
                    if (mod != null)
                    {
                        proxy.getRareMobs().put(entity, mod);
                        mod.onSpawningComplete(entity);
                        //System.out.println("InfernalMobsCore spawned Elite: "+entity+": "+mod.getModName());
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
            result = entity.getEntityName();
        }
        catch (Exception e)
        {
            result = entity.getClass().getSimpleName();
            System.err.println("Entity of class "+result+" crashed when getEntityName() was queried, for shame! Using classname instead.");
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

    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     * @param entity Entity instance whose health you want changed
     * @param amount value to set
     */
    public void setEntityHealthPastMax(EntityLivingBase entity, float amount)
    {
        entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(amount);
        instance.sendHealthPacket(entity, amount);
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the Entity
     * @param entity Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    @SuppressWarnings("unchecked")
    private MobModifier createMobModifiers(EntityLivingBase entity)
    {
        /* 2-5 modifications standard */
        int number = 2 + entity.worldObj.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        HashSet<Class<? extends MobModifier>> possibleMods = (HashSet<Class<? extends MobModifier>>) mobMods.clone();
        
        if (entity.worldObj.rand.nextInt(ultraRarity) == 0) // ultra mobs
        {
            number += 3 + entity.worldObj.rand.nextInt(2);
            
            if (entity.worldObj.rand.nextInt(infernoRarity) == 0) // infernal mobs
            {
                number += 3 + entity.worldObj.rand.nextInt(2);
            }
        }
        
        MobModifier lastMod = null;
        Iterator<Class<? extends MobModifier>> iter = possibleMods.iterator();
        while (number > 0 && iter.hasNext()) // so long we need more and have some
        {
            /* random index of mod list */
            int index = entity.worldObj.rand.nextInt(possibleMods.size());
            MobModifier nextMod = null;
            
            /* instanciate using one of the two constructors, chainlinking modifiers as we go */
            try
            {
                if (lastMod == null)
                {
                    nextMod = (MobModifier) iter.next().getConstructor(new Class[] {EntityLivingBase.class}).newInstance(entity);
                }
                else
                {
                    nextMod = (MobModifier) iter.next().getConstructor(new Class[] {EntityLivingBase.class, MobModifier.class}).newInstance(entity, lastMod);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            boolean allowed = true;
            if (nextMod.getBlackListMobClasses() != null)
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
     * @param entity Target Entity
     * @param savedMods String depicting the MobModifiers, equal to the ingame Display
     */
    public void addEntityModifiersByString(EntityLivingBase entity, String savedMods)
    {
        MobModifier mod = stringToMobModifiers(entity, savedMods);
        if (mod != null)
        {
            proxy.getRareMobs().put(entity, mod);
            mod.onSpawningComplete(entity);
        }
    }
    
    private MobModifier stringToMobModifiers(EntityLivingBase entity, String buffer)
    {
        MobModifier lastMod = null;
        
        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length-1; j >= 0; j--)
        {
            String modName = tokens[j];
            
            MobModifier nextMod = null;
            for (Class<? extends MobModifier> c : mobMods)
            {
                /* instanciate using one of the two constructors, chainlinking modifiers as we go */
                try
                {
                    if (lastMod == null)
                    {
                        nextMod = c.getConstructor(new Class[] {EntityLivingBase.class}).newInstance(entity);
                    }
                    else
                    {
                        nextMod = c.getConstructor(new Class[] {EntityLivingBase.class, MobModifier.class}).newInstance(entity, lastMod);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                if (nextMod.modName.equals(modName))
                {
                    /* Only actually keep the new linked instance if it's what we wanted */
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
     * Used on World/Server/Savegame change to clear the Boss HashMap of old World Entities
     * @param lastWorld 
     */
    public void checkRareListForObsoletes(World lastWorld)
    {
        ArrayList<EntityLivingBase> toRemove = new ArrayList<EntityLivingBase>();
        for (EntityLivingBase ent : proxy.getRareMobs().keySet())
        {
            if (ent.worldObj != lastWorld)
            {
                toRemove.add(ent);
            }
        }
        
        for (EntityLivingBase ent : toRemove)
        {
            proxy.getRareMobs().remove(ent);
        }
        
        loadConfig();
    }

    /**
     * Used by the client side to answer to a server packet carrying the Entity ID and mod string
     * @param world World the client is in, and the Entity aswell
     * @param entID unique Entity ID
     * @param mods MobModifier compliant data String from the server
     */
    public void addRemoteEntityModifiers(World world, int entID, String mods)
    {
        Entity ent = world.getEntityByID(entID);
        if (ent != null)
        {
            addEntityModifiersByString((EntityLivingBase)ent, mods);
            //System.out.println("Client added remote infernal mod on entity "+ent+", is now "+mod.getModName());
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
        
        if (!failedItemStrings.isEmpty())
        {
            System.out.println("Infernal Mobs last ditch effort to identify Items commencing...");
            for (String[] meta : failedItemStrings)
            {
                int id = parseOrFind(meta[0]);
                if (id > 0)
                {
                    Integer[] ints = {
                            id,
                            (meta.length > 1) ? Integer.parseInt(meta[1]) : 0,
                            (meta.length > 2) ? Integer.parseInt(meta[2]) : 1,
                            (meta.length > 3) ? Integer.parseInt(meta[3]) : 0
                                    };
                    instance.dropIdList.add(ints);
                }
            }
            failedItemStrings.clear();
        }
        
        dropRandomEnchantedItems(mob, mods);
    }

    private void dropRandomEnchantedItems(EntityLivingBase mob, MobModifier mods)
    {
        int modStr = mods.getModSize();
        while (modStr > 0)
        {
            ItemStack itemStack = getRandomItem(mob);
            if (itemStack != null && itemStack.itemID > 0)
            {
                Item item = itemStack.getItem();
                if (item != null && item instanceof Item)
                {
                    if (item instanceof ItemEnchantedBook)
                    {
                        itemStack = ((ItemEnchantedBook)item).func_92114_b(mob.getRNG()).theItemId;
                    }
                    else
                    {
                        int usedStr = (modStr-5>0) ? 5 : modStr;
                        enchantRandomly(mob.worldObj.rand, itemStack, item.getItemEnchantability(), usedStr);
                        //EnchantmentHelper.addRandomEnchantment(mob.worldObj.rand, itemStack, item.getItemEnchantability());
                    }
                }
                
                EntityItem itemEnt = new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, itemStack);  
                mob.worldObj.spawnEntityInWorld(itemEnt);
                modStr -= 5;
            }
        }
    }
    
    /**
     * Custom Enchanting Helper
     * 
     * @param rand Random gen to use
     * @param itemStack ItemStack to be enchanted
     * @param itemEnchantability ItemStack max enchantability level
     * @param modStr MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Random rand, ItemStack itemStack, int itemEnchantability, int modStr)
    {
        int remainStr = (modStr+1) / 2; // should result in 1-3
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

    private ItemStack getRandomItem(EntityLivingBase mob)
    {
        Integer[] ints = instance.dropIdList.get(mob.worldObj.rand.nextInt(instance.dropIdList.size()));
        int stackSize = ints[2];
        if (ints[3] != 0) // randomizer
        {
            stackSize += mob.getRNG().nextInt(ints[3])+1;
            stackSize -= mob.getRNG().nextInt(ints[3])+1;
            if (stackSize < 1)
            {
                stackSize = 1;
            }
        }
        
        return new ItemStack(ints[0], stackSize, ints[1]);
    }
    
    //addVelocity player: Packet ID 2, from server, { double xVel, double yVel, double zVel }
    public void sendVelocityPacket(EntityPlayer target, double xVel, double yVel, double zVel)
    {
        Object[] toSend = {xVel, yVel, zVel};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_IM", 2, toSend), (Player)target);
    }

    // knockBack player: Packet ID 3, from server, { double xVel, double zVel }
    public void sendKnockBackPacket(EntityPlayer target, double xVel, double zVel)
    {
        Object[] toSend = { xVel, zVel };
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_IM", 3, toSend), (Player)target);
    }
    
    // health announcement: Packet ID 4, from server, { int entityID, float health }
    public void sendHealthPacket(EntityLivingBase mob, float health)
    {
        Object[] toSend = { mob.entityId, health };
        PacketDispatcher.sendPacketToAllAround(mob.posX, mob.posY, mob.posZ, 32D, mob.dimension, ForgePacketWrapper.createPacket("AS_IM", 4, toSend));
    }
    
    // health request, Packet ID 4, from client, { int entityID }
    public void sendHealthRequestPacket(EntityLivingBase mob)
    {
        Object[] toSend = { mob.entityId };
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_IM", 4, toSend));
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (System.currentTimeMillis() > nextExistCheckTime)
        {
            Set<EntityLivingBase> temp = proxy.getRareMobs().keySet();
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            for (Entity mob : temp)
            {
                if (!mob.worldObj.loadedEntityList.contains(mob))
                {
                    //System.out.println("Removed unloaded Entity "+mob+" with ID "+mob.entityId+" from rareMobs");
                    removeEntFromElites((EntityLivingBase) mob);
                }
            }
        }
    }

    private final EnumSet<TickType> tickTypes = EnumSet.of(TickType.WORLD);
    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }

    @Override
    public String getLabel()
    {
        return "InfernalMobs";
    }

    public boolean getIsHealthBarDisabled()
    {
        return disableHealthBar;
    }
    
    public double getMobModHealthFactor()
    {
        return modHealthFactor;
    }
}
