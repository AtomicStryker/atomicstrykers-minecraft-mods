package atomicstryker.infernalmobs.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.src.EnchantmentHelper;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityWither;
import net.minecraft.src.EntityXPOrb;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.client.ClientPacketHandler;
import atomicstryker.infernalmobs.common.mods.MM_1UP;
import atomicstryker.infernalmobs.common.mods.MM_Berserk;
import atomicstryker.infernalmobs.common.mods.MM_Blastoff;
import atomicstryker.infernalmobs.common.mods.MM_Bulwark;
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
import atomicstryker.infernalmobs.common.mods.MM_Sprint;
import atomicstryker.infernalmobs.common.mods.MM_Sticky;
import atomicstryker.infernalmobs.common.mods.MM_Storm;
import atomicstryker.infernalmobs.common.mods.MM_Vengeance;
import atomicstryker.infernalmobs.common.mods.MM_Weakness;
import atomicstryker.infernalmobs.common.mods.MM_Webber;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Side;
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

@Mod(modid = "InfernalMobs", name = "Infernal Mobs", version = "1.1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_IM"}, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class InfernalMobsCore implements ITickHandler, ISidedProxy
{
    public static final int RARE_MOB_HEALTH_MODIFIER = 4;
    private static final long existCheckDelay = 5000L;
    
    private static long lastExistCheckTime;
    private ArrayList<Integer> dropIdList;
    
    private static InfernalMobsCore instance;
    
    public static InfernalMobsCore instance()
    {
        return instance;
    }
    
    public static String getNBTTag()
    {
        return "InfernalMobsMod";
    }
    
    private static ConcurrentHashMap<EntityLiving, MobModifier> rareMobs;
    private static ArrayList<Class> mobMods;
    
    private static int eliteRarity;
    public static Configuration config;
    
    @SidedProxy(clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient", serverSide = "atomicstryker.infernalmobs.common.InfernalMobsCore")
    public static ISidedProxy proxy;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        dropIdList = new ArrayList<Integer>();
        lastExistCheckTime = System.currentTimeMillis();
        
        config = new Configuration(evt.getSuggestedConfigurationFile());
        loadConfig();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        rareMobs = new ConcurrentHashMap();
        
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
        mobMods = new ArrayList<Class>();
        
        mobMods.add(MM_1UP.class);
        mobMods.add(MM_Berserk.class);
        mobMods.add(MM_Blastoff.class);
        mobMods.add(MM_Bulwark.class);
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
        mobMods.add(MM_Sprint.class);
        mobMods.add(MM_Sticky.class);
        mobMods.add(MM_Storm.class);
        mobMods.add(MM_Vengeance.class);
        mobMods.add(MM_Weakness.class);
        mobMods.add(MM_Webber.class);
        
        Iterator<Class> iter = mobMods.iterator();
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
        eliteRarity = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "eliteRarity", 15).value);
        String itemIDs = config.get(config.CATEGORY_GENERAL, "droppedItemIDs", "256,257,258,261,267,276,277,278,279,283,284,285,286,292,293,294,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317").value;
        
        itemIDs = itemIDs.trim();
        String[] numbers = itemIDs.split(",");
        for (String s : numbers)
        {
            instance.dropIdList.add(Integer.parseInt(s));
        }
        
        loadMods();
        
        config.save();
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
                if (entity instanceof EntityMob && !(entity instanceof EntityWither) && entity.worldObj.rand.nextInt(eliteRarity) == 0)
                {
                    MobModifier mod = createMobModifiers(entity);
                    if (mod != null)
                    {
                        getRareMobs().put(entity, mod);
                        mod.onSpawningComplete();
                        //System.out.println("InfernalMobsCore spawned Elite: "+entity+": "+mod.getModName());
                    }
                }
            }
        }
    }
    
    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     * @param entity Entity instance whose health you want changed
     * @param amount value to set
     */
    public static void setEntityHealthPastMax(EntityLiving entity, int amount)
    {
        entity.setEntityHealth(amount);
        if (!entity.worldObj.isRemote)
        {
            instance.sendHealthPacket(entity, amount);
        }
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the Entity
     * @param entity Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    private static MobModifier createMobModifiers(EntityLiving entity)
    {
        /* 2-5 modifications */
        int number = 2 + entity.worldObj.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class> possibleMods = (ArrayList<Class>) mobMods.clone();
        
        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty())
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
            /* check white and black lists */
            if (nextMod.getWhiteListMobClasses() != null)
            {
                allowed = false;
                for (Class cl : nextMod.getWhiteListMobClasses())
                {
                    if (entity.getClass().isAssignableFrom(cl))
                    {
                        allowed = true;
                        break;
                    }
                }
            }
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
            
            if (allowed)
            {
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
        if (!getRareMobs().contains(entity)) // prevent dupes and overwriting
        {
            getRareMobs().put(entity, mod);
            mod.onSpawningComplete();
        }
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
        return getRareMobs().get(target);
    }

    public static boolean getIsRareEntity(EntityLiving ent)
    {
        return getRareMobs().containsKey(ent);
    }

    public static void removeEntFromElites(EntityLiving entity)
    {
        getRareMobs().remove(entity);
    }
    
    public static ConcurrentHashMap<EntityLiving, MobModifier> getRareMobs()
    {
        return rareMobs;
    }

    /**
     * Used on World/Server/Savegame change to clear the Boss HashMap of old World Entities
     * @param lastWorld 
     */
    public void checkRareListForObsoletes(World lastWorld)
    {
        ArrayList<EntityLiving> toRemove = new ArrayList<EntityLiving>();
        for (EntityLiving ent : getRareMobs().keySet())
        {
            if (ent.worldObj != lastWorld)
            {
                toRemove.add(ent);
            }
        }
        
        for (EntityLiving ent : toRemove)
        {
            getRareMobs().remove(ent);
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
        //System.out.println("Client adding remote infernal mob!");
        Iterator iter = world.loadedEntityList.iterator();
        while (iter.hasNext())
        {
            Entity ent = (Entity) iter.next();
            if (ent instanceof EntityLiving && ent.entityId == entID)
            {
                addEntityModifiersByString((EntityLiving)ent, mods);
                MobModifier mod = getMobModifiers((EntityLiving) ent);
                if (mod != null)
                {
                    mod.onSpawningComplete();
                }
                //System.out.println("Client added remote infernal mob!");
                break;
            }
        }
    }

    public void dropLootForEnt(EntityLiving mob)
    {
        int xpValue = 25;
        while (xpValue > 0)
        {
            int xpDrop = EntityXPOrb.getXPSplit(xpValue);
            xpValue -= xpDrop;
            mob.worldObj.spawnEntityInWorld(new EntityXPOrb(mob.worldObj, mob.posX, mob.posY, mob.posZ, xpDrop));
        }
        
        dropRandomEnchantedItem(mob);
    }

    private void dropRandomEnchantedItem(EntityLiving mob)
    {
        try
        {
            Item item = getRandomItem(mob);
            ItemStack itemStack = ItemStack.class.getConstructor(new Class[] { Item.class }).newInstance(item);
            EnchantmentHelper.addRandomEnchantment(mob.worldObj.rand, itemStack, item.getItemEnchantability());
            EntityItem itemEnt = new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, itemStack);  
            mob.worldObj.spawnEntityInWorld(itemEnt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private Item getRandomItem(EntityLiving mob)
    {
        return Item.itemsList[instance.dropIdList.get(mob.worldObj.rand.nextInt(instance.dropIdList.size()))];
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
        PacketDispatcher.sendPacketToAllAround(mob.posX, mob.posY, mob.posZ, 15D, mob.worldObj.getWorldInfo().getDimension(), ForgePacketWrapper.createPacket("AS_IM", 4, toSend));
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
        if (System.currentTimeMillis() > lastExistCheckTime + existCheckDelay)
        {
            Set<EntityLiving> temp = rareMobs.keySet();
            lastExistCheckTime = System.currentTimeMillis();
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

    @Override
    public void load()
    {
        // NOOP, ISidedProxy override
    }
}
