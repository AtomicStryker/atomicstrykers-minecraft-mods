package atomicstryker.infernalmobs.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
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
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "InfernalMobs", name = "Infernal Mobs", version = "1.2.2")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ServerPacketHandler.class))
public class InfernalMobsCore implements ITickHandler
{
    private static final long existCheckDelay = 5000L;
    
    private static long nextExistCheckTime;
    
    /**
     * Array of two ints, first Block or Item ID, second meta value, third stacksize (only for blocks)
     */
    private ArrayList<Integer[]> dropIdList;
    private static boolean healthHacked;
    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    
    private static InfernalMobsCore instance;
    
    public static InfernalMobsCore instance()
    {
        return instance;
    }
    
    public static String getNBTTag()
    {
        return "InfernalMobsMod";
    }
    
    private static ArrayList<Class<? extends MobModifier>> mobMods;
    
    private static int eliteRarity;
    public static Configuration config;
    
    @SidedProxy(clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient", serverSide = "atomicstryker.infernalmobs.common.InfernalMobsServer")
    public static ISidedProxy proxy;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        dropIdList = new ArrayList<Integer[]>();
        nextExistCheckTime = System.currentTimeMillis();
        healthHacked = false;
        classesAllowedMap = new HashMap();
        classesForcedMap = new HashMap();
        
        config = new Configuration(evt.getSuggestedConfigurationFile());
        loadConfig();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());
        
        proxy.load();
        
        System.out.println("InfernalMobsCore load() completed! Modifiers ready: "+mobMods.size());
    }
    
    @ServerStarted
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
        mobMods = new ArrayList<Class<? extends MobModifier>>();
        
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
        
        Iterator<Class<? extends MobModifier>> iter = mobMods.iterator();
        while (iter.hasNext())
        {
            Class c = iter.next();
            if (!config.get(config.CATEGORY_GENERAL, c.getSimpleName()+" enabled", true).getBoolean(true))
            {
                iter.remove();
            }
        }
    }

    /**
     * Forge Config file
     */
    private void loadConfig()
    {
        config.load();
        eliteRarity = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "eliteRarity", 15).getString());
        String itemIDs = config.get(config.CATEGORY_GENERAL, "droppedItemIDs", "256,257,258,261,267,276,277,278,279,292,293,302,303,304,305,306,307,308,309,310,311,312,313,403").getString();
        
        itemIDs = itemIDs.trim();
        String[] numbers = itemIDs.split(",");
        for (String s : numbers)
        {
            String[] meta = s.split("-");
            int id = parseOrFind(meta[0]);
            if (id > 0)
            {
                Integer[] ints = {id, (meta.length > 1) ? Integer.parseInt(meta[1]) : 0, (meta.length > 2) ? Integer.parseInt(meta[2]) : 1 };
                instance.dropIdList.add(ints);
            }
        }
        
        loadMods();
        
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
            for (Block b : Block.blocksList)
            {
                if (b != null && b.getUnlocalizedName() != null && b.getUnlocalizedName().equals(s))
                {
                    return b.blockID;
                }
            }
            
            for (Item i : Item.itemsList)
            {
                if (i != null && i.getUnlocalizedName() != null && i.getUnlocalizedName().equals(s))
                {
                    return i.itemID;
                }
            }
        }
        return result;
    }
    
    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn them into Elites here
     * @param entity Entity in question
     */
    public static void processEntitySpawn(EntityLiving entity)
    {
        if (!entity.worldObj.isRemote)
        {
            if (!getIsRareEntity(entity))
            {
                if (entity instanceof EntityMob
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
    
    private boolean checkEntityClassAllowed(EntityLiving entity)
    {
        String entName = entity.getEntityName();
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
    
    private boolean checkEntityClassForced(EntityLiving entity)
    {
        String entName = entity.getEntityName();
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

    private static Field fieldHealth;
    
    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     * @param entity Entity instance whose health you want changed
     * @param amount value to set
     */
    public static void setEntityHealthPastMax(EntityLiving entity, int amount)
    {
        if (!healthHacked)
        {
            hackHealth(entity);
        }
        
        if (fieldHealth != null)
        {
            try
            {
                fieldHealth.setInt(entity, amount);
                if (!entity.worldObj.isRemote)
                {
                    instance.sendHealthPacket(entity, amount);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private static void hackHealth(EntityLiving entity)
    {
        System.out.println("Infernal Mobs health hack attempt now running...");
        ArrayList<Field> possibleFields = new ArrayList<Field>();
        for (Field f : EntityLiving.class.getDeclaredFields())
        {
            if (String.valueOf(f.getType()).equals("int"))
            {
                f.setAccessible(true);
                possibleFields.add(f);
            }
        }
        System.out.println("Infernal Mobs health hack found candidate fields: "+possibleFields.size());
        
        int prevEntHealth = entity.getHealth();
        for (int i = 0; i < 4; i++)
        {
            entity.setEntityHealth(i+2);
            Iterator<Field> iter = possibleFields.iterator();
            while (iter.hasNext())
            {
                try
                {
                    if (iter.next().getInt(entity) != i+2)
                    {
                        iter.remove();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        if (possibleFields.size() == 1)
        {
            fieldHealth = possibleFields.get(0);
            System.out.println("Infernal Mobs health hack success, field health is: "+fieldHealth);
        }
        else
        {
            System.out.println("Infernal Mobs health hack failed, no field took the 3 changes");
        }
        entity.setEntityHealth(prevEntHealth);
        healthHacked = true;
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the Entity
     * @param entity Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    private MobModifier createMobModifiers(EntityLiving entity)
    {
        /* 2-5 modifications standard */
        int number = 2 + entity.worldObj.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class<? extends MobModifier>> possibleMods = (ArrayList<Class<? extends MobModifier>>) mobMods.clone();
        
        if (entity.worldObj.rand.nextInt(4) == 0) // ultra mobs
        {
            number += 3 + entity.worldObj.rand.nextInt(2);
            
            if (entity.worldObj.rand.nextInt(4) == 0) // infernal mobs
            {
                number += 3 + entity.worldObj.rand.nextInt(2);
            }
        }
        
        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more and have some
        {
            /* random index of mod list */
            int index = entity.worldObj.rand.nextInt(possibleMods.size());
            MobModifier nextMod = null;
            
            /* instanciate using one of the two constructors, chainlinking modifiers as we go */
            try
            {
                if (lastMod == null)
                {
                    nextMod = (MobModifier) possibleMods.get(index).getConstructor(new Class[] {EntityLiving.class}).newInstance(entity);
                }
                else
                {
                    nextMod = (MobModifier) possibleMods.get(index).getConstructor(new Class[] {EntityLiving.class, MobModifier.class}).newInstance(entity, lastMod);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            boolean allowed = true;
            if (nextMod.getBlackListMobClasses() != null)
            {
                for (Class cl : nextMod.getBlackListMobClasses())
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
                    for (Class cl : lastMod.getModsNotToMixWith())
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
    public static void addEntityModifiersByString(EntityLiving entity, String savedMods)
    {
        MobModifier mod = stringToMobModifiers(entity, savedMods);
        proxy.getRareMobs().put(entity, mod);
        mod.onSpawningComplete(entity);
    }
    
    private static MobModifier stringToMobModifiers(EntityLiving entity, String buffer)
    {
        MobModifier lastMod = null;
        
        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length-1; j >= 0; j--)
        {
            String modName = tokens[j];
            
            boolean found;
            MobModifier nextMod = null;
            for (int i = 0; i < mobMods.size(); i++)
            {
                /* instanciate using one of the two constructors, chainlinking modifiers as we go */
                try
                {
                    if (lastMod == null)
                    {
                        nextMod = (MobModifier) mobMods.get(i).getConstructor(new Class[] {EntityLiving.class}).newInstance(entity);
                    }
                    else
                    {
                        nextMod = (MobModifier) mobMods.get(i).getConstructor(new Class[] {EntityLiving.class, MobModifier.class}).newInstance(entity, lastMod);
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
    
    public static MobModifier getMobModifiers(EntityLiving target)
    {
        return proxy.getRareMobs().get(target);
    }

    public static boolean getIsRareEntity(EntityLiving ent)
    {
        return proxy.getRareMobs().containsKey(ent);
    }

    public static void removeEntFromElites(EntityLiving entity)
    {
        proxy.getRareMobs().remove(entity);
    }

    /**
     * Used on World/Server/Savegame change to clear the Boss HashMap of old World Entities
     * @param lastWorld 
     */
    public void checkRareListForObsoletes(World lastWorld)
    {
        ArrayList<EntityLiving> toRemove = new ArrayList<EntityLiving>();
        for (EntityLiving ent : proxy.getRareMobs().keySet())
        {
            if (ent.worldObj != lastWorld)
            {
                toRemove.add(ent);
            }
        }
        
        for (EntityLiving ent : toRemove)
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
    public static void addRemoteEntityModifiers(World world, int entID, String mods)
    {
        Entity ent = world.getEntityByID(entID);
        if (ent != null)
        {
            addEntityModifiersByString((EntityLiving)ent, mods);
            //System.out.println("Client added remote infernal mod on entity "+ent+", is now "+mod.getModName());
        }
    }

    public void dropLootForEnt(EntityLiving mob, MobModifier mods)
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

    private void dropRandomEnchantedItems(EntityLiving mob, MobModifier mods)
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
                        itemStack = ((ItemEnchantedBook)item).func_92109_a(mob.getRNG());
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
        List enchantments = EnchantmentHelper.buildEnchantmentList(rand, itemStack, itemEnchantability);
        if (enchantments != null)
        {
            Iterator iter = enchantments.iterator();
            while (iter.hasNext() && remainStr > 0)
            {
                remainStr--;
                EnchantmentData eData = (EnchantmentData) iter.next();
                itemStack.addEnchantment(eData.enchantmentobj, eData.enchantmentLevel);
            }
        }
    }

    private ItemStack getRandomItem(EntityLiving mob)
    {
        Integer[] ints = instance.dropIdList.get(mob.worldObj.rand.nextInt(instance.dropIdList.size()));
        return new ItemStack(ints[0], ints[2], ints[1]);
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
    
    // health announcement: Packet ID 4, from server, { int entityID, int health }
    public void sendHealthPacket(EntityLiving mob, int health)
    {
        Object[] toSend = { mob.entityId, health };
        PacketDispatcher.sendPacketToAllAround(mob.posX, mob.posY, mob.posZ, 32D, mob.dimension, ForgePacketWrapper.createPacket("AS_IM", 4, toSend));
    }
    
    // health request, Packet ID 4, from client, { int entityID }
    public void sendHealthRequestPacket(EntityLiving mob)
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
            Set<EntityLiving> temp = proxy.getRareMobs().keySet();
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            for (Entity mob : temp)
            {
                if (!mob.worldObj.loadedEntityList.contains(mob))
                {
                    //System.out.println("Removed unloaded Entity "+mob+" with ID "+mob.entityId+" from rareMobs");
                    removeEntFromElites((EntityLiving) mob);
                }
            }
        }
    }

    private final EnumSet tickTypes = EnumSet.of(TickType.WORLD);
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
}
