package atomicstryker.petbat.common;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import atomicstryker.petbat.client.ClientPacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "PetBat", name = "Pet Bat", version = "1.1.4")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"PetBat"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"PetBat"}, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class PetBatMod implements IProxy
{
    public final int TAME_ITEM_ID = 400; // pumpkin pie
    public final int GLISTER_ITEM_ID = 348; // glowstone dust
    
    private final String[] batLevels = {
            "Pet Bat",
            "Brawler Pet Bat",
            "Hardened Pet Bat",
            "Elite Bat",
            "Badass Bat",
            "Super Badass Bat",
            "Ultimate Badass Bat" 
    };
    public final byte BAT_MAX_LVL = (byte) batLevels.length;
    
    private final String[] batLevelDescripts = {
            "Adorable and only slightly weaponized",
            "Has been in some fights, can bust you out in a pinch",
            "\"Now I'm serious\"",
            "Developed a taste for Blood, now has Life Steal",
            "Don't cross this bat.",
            "It doesn't get much deadlier.",
            "YOU POINT. THEY DIE." 
    };
    
    private final String[] batNames = {
            "Lucius",
            "Draco",
            "Vlad",
            "Darkwing",
            "Zubat",
            "Cecil",
            "Dragos",
            "Cezar",
            "Ciprian",
            "Daniel",
            "Dorin",
            "Mihai",
            "Mircea",
            "Radu"
    };
    
    /**
     * experience to levels table
     * 0 - no xp
     * 25 - lvl 1, 25 xp diff
     * 75 - lvl 2, 50 xp diff
     * 175 - lvl 3, 100 xp diff
     * 375 - lvl 4, 200 xp diff
     * 775 - lvl 5, 400 xp diff
     * 1575 - lvl 6, 800 xp diff
     */
    public int getLevelFromExperience(int xp)
    {
        if (xp < 25) return 0;
        if (xp < 75) return 1;
        if (xp < 175) return 2;
        if (xp < 375) return 3;
        if (xp < 775) return 4;
        if (xp < 1575) return 5;
        return 6;
    }
    
    public int getMissingExperienceToNextLevel(int xp)
    {
        if (xp < 25) return 25-xp;
        if (xp < 75) return 75-xp;
        if (xp < 175) return 175-xp;
        if (xp < 375) return 375-xp;
        if (xp < 775) return 775-xp;
        if (xp < 1575) return 1575-xp;
        return -1;
    }
    
    public String getLevelTitle(int level)
    {
        return batLevels[level];
    }
    
    public String getLevelDescription(int level)
    {
        return batLevelDescripts[level];
    }
    
    private Field entityBatFlightCoords;
    private int itemIDPocketBat;
	private boolean manualEnabled;
    public Item itemPocketedBat;
    public Configuration config;
    
    private boolean glisterBatEnabled;
    public long glisterBatEffectDuration;
    
    @SidedProxy(clientSide = "atomicstryker.petbat.client.ClientProxy", serverSide = "atomicstryker.petbat.common.PetBatMod")
    public static IProxy proxy;
    
    private static PetBatMod instance;
    public static PetBatMod instance()
    {
        return instance;
    }
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {        
        config = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            config.load();
            itemIDPocketBat = config.getItem("ItemPocketedPetBat", 2528).getInt();
			manualEnabled = config.get(config.CATEGORY_GENERAL, "manualEnabled", false).getBoolean(false);
			glisterBatEffectDuration = config.get(config.CATEGORY_GENERAL, "glisterBatEffectDuration (s)", 300).getInt();
			glisterBatEffectDuration *= 1000; // sec to millisec
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "PetBat has a problem loading it's configuration!");
        }
        finally
        {
            config.save();
        }
        
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        instance = this;
        
        EntityRegistry.registerModEntity(EntityPetBat.class, "EntityPetBat", 1, this, 25, 5, true);
        MinecraftForge.EVENT_BUS.register(this);
        
        itemPocketedBat = new ItemPocketedPetBat(itemIDPocketBat).setItemName("fed Pet Bat");
        LanguageRegistry.addName(itemPocketedBat, "fed Pet Bat");
        
        ItemStack fedBat = new ItemStack(itemPocketedBat.itemID, 1, -1);
        GameRegistry.addShapelessRecipe(new ItemStack(itemPocketedBat.itemID, 1, 0), fedBat, new ItemStack(TAME_ITEM_ID, 1, 0));
        GameRegistry.registerCraftingHandler(new BatHealCraftingHandler());
        
        proxy.onModLoad();
        
        entityBatFlightCoords = EntityBat.class.getDeclaredFields()[0];
        entityBatFlightCoords.setAccessible(true);
    }
    
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        glisterBatEnabled = Loader.isModLoaded("DynamicLights");
    }
	
	public boolean getPetBatManualEnabled()
	{
		return manualEnabled;
	}
    
    @ForgeSubscribe
    public void onPlayerLeftClick(BreakSpeed event)
    {
        EntityPlayer p = event.entityPlayer;
        ItemStack item = p.inventory.getCurrentItem();
        if (item != null && item.itemID == TAME_ITEM_ID)
        {
            List<Entity> entityList = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.expand(10D, 10D, 10D));
            ChunkCoordinates coords = new ChunkCoordinates((int)(p.posX+0.5D), (int)(p.posY+1.5D), (int)(p.posZ+0.5D));
            for (Entity ent : entityList)
            {
                if (ent instanceof EntityBat)
                {
                    try
                    {
                        entityBatFlightCoords.set(ent, coords);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @ForgeSubscribe
    public void onEntityInteract(EntityInteractEvent event)
    {
        if (event.target instanceof EntityBat)
        {
            EntityPlayer p = event.entityPlayer;
            if (!p.worldObj.isRemote)
            {
                ItemStack item = p.inventory.getCurrentItem();
                if (item != null && item.itemID == TAME_ITEM_ID)
                {
                    event.setCanceled(true);
                    p.inventory.consumeInventoryItem(TAME_ITEM_ID);
                    
                    EntityBat b = (EntityBat) event.target;
                    EntityPetBat newPet = new EntityPetBat(p.worldObj);
                    newPet.setLocationAndAngles(b.posX, b.posY, b.posZ, b.rotationYaw, b.rotationPitch);
                    newPet.setNames(p.username, getRandomBatName());
                    
                    p.worldObj.spawnEntityInWorld(newPet);
                    b.setDead();
                }
            }
        }
        
        if (glisterBatEnabled && event.target instanceof EntityPetBat)
        {
            EntityPlayer p = event.entityPlayer;
            ItemStack item = p.inventory.getCurrentItem();
            if (item != null && item.itemID == GLISTER_ITEM_ID)
            {
                new GlisterBatAdapter((EntityPetBat) event.target);
                p.inventory.consumeInventoryItem(GLISTER_ITEM_ID);
            }
        }
    }
    
    private String getRandomBatName()
    {
        return batNames[new Random().nextInt(batNames.length)];
    }

    @ForgeSubscribe
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (!event.entity.worldObj.isRemote && event.entity instanceof EntityItem)
        {
            EntityItem item = (EntityItem) event.entity;
            int id = item.getEntityItem().itemID;
            if (id == itemPocketedBat.itemID)
            {
                EntityPetBat bat = ItemPocketedPetBat.toBatEntity(item.worldObj, item.getEntityItem());
                if (bat.getHealth() > 1)
                {
                    bat.setPosition(item.posX, item.posY, item.posZ);
                    item.worldObj.spawnEntityInWorld(bat);
                    event.setCanceled(true);
                }
            }
            else if (id == TAME_ITEM_ID)
            {
                List nearEnts = item.worldObj.getEntitiesWithinAABBExcludingEntity(item, item.boundingBox.expand(8D, 8D, 8D));
                for (Object o : nearEnts)
                {
                    if (o instanceof EntityPetBat)
                    {
                        EntityPetBat bat = (EntityPetBat) o;
                        if ((bat.getAttackTarget() == null || !bat.getAttackTarget().isEntityAlive())
                        && (bat.getFoodAttackTarget() == null || bat.getFoodAttackTarget().isEntityAlive()))
                        {
                            bat.setFoodAttackTarget(item);
                        }
                    }
                }
            }   
        }
    }
    
    @ForgeSubscribe
    public void onEntityLivingUpdate(LivingUpdateEvent event)
    {
        if (event.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) event.entityLiving;
            if (p.isEntityAlive() && p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem().equals(itemPocketedBat))
            {
                if (p.getActivePotionEffect(Potion.nightVision) == null)
                {
                    p.addPotionEffect(new PotionEffect(Potion.nightVision.id, 100));
                }
            }
        }
    }
    
    private class BatHealCraftingHandler implements ICraftingHandler
    {

        @Override
        public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
        {
            if (item.itemID == itemPocketedBat.itemID)
            {
                for (int i = craftMatrix.getSizeInventory()-1; i>= 0; i--)
                {
                    ItemStack slotStack = craftMatrix.getStackInSlot(i);
                    if (slotStack != null
                    && slotStack.itemID == itemPocketedBat.itemID)
                    {
                        String owner = slotStack.stackTagCompound != null ? slotStack.stackTagCompound.getCompoundTag("petbatmod").getString("Owner") : player.username;
                        String name = slotStack.stackTagCompound != null ? slotStack.stackTagCompound.getCompoundTag("display").getString("Name") : "I was cheated";
                        int xp = slotStack.stackTagCompound != null ? slotStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP") : 40;
                        ItemPocketedPetBat.writeCompoundStringToItemStack(item, "display", "Name", name);
                        ItemPocketedPetBat.writeCompoundStringToItemStack(item, "petbatmod", "Owner", owner);
                        ItemPocketedPetBat.writeCompoundIntegerToItemStack(item, "petbatmod", "BatXP", xp);
                        break;
                    }
                }
            }
        }

        @Override
        public void onSmelting(EntityPlayer player, ItemStack item)
        {
        }
        
    }

    @Override
    public void onModLoad()
    {
        // NOOP, Proxy only relevant on client
    }

    @Override
    public void displayGui(ItemStack itemStack)
    {
        // NOOP, Proxy only relevant on client
    }
    
    public boolean hasPlayerGotManual()
    {
        config.load();
        Property prop = config.get(config.CATEGORY_GENERAL, "playerHadManual", false);
        boolean result = prop.getBoolean(false);
        prop.value = "true";
        config.save();
        return result;
    }
}
