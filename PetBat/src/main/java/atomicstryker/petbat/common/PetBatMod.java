package atomicstryker.petbat.common;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import atomicstryker.petbat.common.network.BatNamePacket;
import atomicstryker.petbat.common.network.NetworkHelper;

@Mod(modid = "PetBat", name = "Pet Bat", version = "1.3.7")
public class PetBatMod implements IProxy
{
    private Item TAME_ITEM_ID;
    private Item GLISTER_ITEM_ID;
    
    public final byte BAT_MAX_LVL = 7;
    
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
        return StatCollector.translateToLocal("translation.PetBat:batlevel"+level);
    }
    
    public String getLevelDescription(int level)
    {
        return StatCollector.translateToLocal("translation.PetBat:batlevel"+level+"desc");
    }
    
    private Field entityBatFlightCoords;
    public Item itemPocketedBat;
    public Configuration config;
    public Item itemBatFlute;
    
    private boolean glisterBatEnabled;
    public long glisterBatEffectDuration;
    private boolean batInventoryTeleport;
    
    @SidedProxy(clientSide = "atomicstryker.petbat.client.ClientProxy", serverSide = "atomicstryker.petbat.common.PetBatMod")
    public static IProxy proxy;
    
    @Instance(value = "PetBat")
    private static PetBatMod instance;
    public static PetBatMod instance()
    {
        return instance;
    }
    
    public NetworkHelper networkHelper;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {        
        config = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            config.load();
			batInventoryTeleport = config.get(Configuration.CATEGORY_GENERAL, "teleportIntoInventory", true).getBoolean(true);
			glisterBatEffectDuration = config.get(Configuration.CATEGORY_GENERAL, "glisterBatEffectDuration (s)", 300).getInt();
			glisterBatEffectDuration *= 1000; // sec to millisec
        }
        catch (Exception e)
        {
            System.err.println("PetBat has a problem loading it's configuration!");
        }
        finally
        {
            config.save();
        }
        
        itemPocketedBat = new ItemPocketedPetBat().setUnlocalizedName("fed_pet_bat");
        GameRegistry.registerItem(itemPocketedBat, "fed_pet_bat");
        
        itemBatFlute = new ItemBatFlute().setUnlocalizedName("bat_flute");
        GameRegistry.registerItem(itemBatFlute, "bat_flute");
        
        networkHelper = new NetworkHelper("AS_PB", BatNamePacket.class);
        
        EntityRegistry.registerModEntity(EntityPetBat.class, "PetBat", 1, this, 25, 5, true);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        entityBatFlightCoords = EntityBat.class.getDeclaredFields()[0];
        entityBatFlightCoords.setAccessible(true);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.onModInit();
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        glisterBatEnabled = Loader.isModLoaded("DynamicLights");
        TAME_ITEM_ID = Items.pumpkin_pie;
        GLISTER_ITEM_ID = Items.glowstone_dust;
    }
	
	public boolean getPetBatInventoryTeleportEnabled()
	{
	    return batInventoryTeleport;
	}
    
    @SubscribeEvent
    public void onPlayerLeftClick(BreakSpeed event)
    {
        EntityPlayer p = event.entityPlayer;
        ItemStack item = p.inventory.getCurrentItem();
        if (item != null && item.getItem() == TAME_ITEM_ID)
        {
            @SuppressWarnings("unchecked")
            List<Entity> entityList = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, p.getEntityBoundingBox().expand(10D, 10D, 10D));
            BlockPos coords = new BlockPos((int)(p.posX+0.5D), (int)(p.posY+1.5D), (int)(p.posZ+0.5D));
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
    
    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event)
    {
        if (event.target instanceof EntityBat)
        {
            EntityPlayer p = event.entityPlayer;
            if (!p.worldObj.isRemote)
            {
                ItemStack item = p.inventory.getCurrentItem();
                if (item != null && item.getItem() == TAME_ITEM_ID)
                {
                    event.setCanceled(true);
                    p.inventory.consumeInventoryItem(TAME_ITEM_ID);
                    
                    EntityBat b = (EntityBat) event.target;
                    EntityPetBat newPet = new EntityPetBat(p.worldObj);
                    newPet.setLocationAndAngles(b.posX, b.posY, b.posZ, b.rotationYaw, b.rotationPitch);
                    newPet.setNames(p.getGameProfile().getName(), getRandomBatName());
                    newPet.setOwnerEntity(p);
                    
                    p.worldObj.spawnEntityInWorld(newPet);
                    b.setDead();
                }
            }
        }
        
        if (glisterBatEnabled && event.target instanceof EntityPetBat)
        {
            EntityPlayer p = event.entityPlayer;
            ItemStack item = p.inventory.getCurrentItem();
            if (item != null && item.getItem() == GLISTER_ITEM_ID)
            {
                new GlisterBatAdapter((EntityPetBat) event.target);
                p.inventory.consumeInventoryItem(GLISTER_ITEM_ID);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerAttacksEntity(AttackEntityEvent event)
    {
        if (event.target instanceof EntityPetBat)
        {
            EntityPetBat bat = (EntityPetBat) event.target;
            if (bat.getOwnerName().equals(event.entityPlayer.getName()) && event.entityPlayer.getCurrentEquippedItem() == null)
            {
                bat.recallToOwner();
                event.setCanceled(true);
            }
        }
    }
    
    private String getRandomBatName()
    {
        return batNames[new Random().nextInt(batNames.length)];
    }
    
    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void onItemToss(ItemTossEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            EntityItem itemDropped = event.entityItem;
            System.out.println("PlayerDropsEvent iterating over drop "+itemDropped);
            EntityItem foundItem;
            final Item id = itemDropped.getEntityItem().getItem();
            if (id == itemPocketedBat)
            {
                final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(itemDropped.worldObj, itemDropped.getEntityItem(), event.player);
                if (bat.getHealth() > 1)
                {
                    bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                    itemDropped.worldObj.spawnEntityInWorld(bat);
                    event.setCanceled(true);
                }
                else
                {
                    // bat is inert. see if it was tossed onto pumpkin pie for revival
                    
                    final List nearEnts = itemDropped.worldObj.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.getEntityBoundingBox().expand(8D, 8D, 8D));
                    for (Object o : nearEnts)
                    {
                        if (o instanceof EntityItem)
                        {
                            foundItem = (EntityItem) o;
                            if (foundItem.getEntityItem().getItem() == TAME_ITEM_ID)
                            {
                                bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                                itemDropped.worldObj.spawnEntityInWorld(bat);
                                bat.setHealth(bat.getMaxHealth()); // set full entity health
                                event.setCanceled(true);
                                foundItem.getEntityItem().stackSize--;
                                if (foundItem.getEntityItem().stackSize < 1)
                                {
                                    foundItem.setDead(); // destroy pie item
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (id == TAME_ITEM_ID)
            {
                final List nearEnts = itemDropped.worldObj.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.getEntityBoundingBox().expand(8D, 8D, 8D));
                for (Object o : nearEnts)
                {
                    if (o instanceof EntityPetBat)
                    {
                        final EntityPetBat bat = (EntityPetBat) o;
                        if ((bat.getAttackTarget() == null || !bat.getAttackTarget().isEntityAlive())
                        && (bat.getFoodAttackTarget() == null || bat.getFoodAttackTarget().isEntityAlive()))
                        {
                            bat.setFoodAttackTarget(itemDropped);
                            break;
                        }
                    }
                    else if (o instanceof EntityItem)
                    {
                        foundItem = (EntityItem) o;
                        if (foundItem.getEntityItem().getItem() == itemPocketedBat) // inert bat lying around
                        {
                            final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(foundItem.worldObj, foundItem.getEntityItem(), event.player);
                            bat.setPosition(foundItem.posX, foundItem.posY, foundItem.posZ);
                            foundItem.worldObj.spawnEntityInWorld(bat);
                            bat.setHealth(bat.getMaxHealth()); // set full entity health
                            event.setCanceled(true);
                            foundItem.setDead(); // destroy bat item
                            break;
                        }
                    }
                }
            }
            else if (id == itemBatFlute) // bat flutes cannot be dropped. ever.
            {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerDropsEvent(PlayerDropsEvent event)
    {
        // iterate drops, remove all batflutes
        final Iterator<EntityItem> iter = event.drops.iterator();
        while (iter.hasNext())
        {
            if (iter.next().getEntityItem().getItem() == itemBatFlute)
            {
                iter.remove();
            }
        }
    }
    
    @SubscribeEvent
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
    
	@Override
	public void onModInit()
	{
		// NOOP
	}
	
    @Override
    public void displayGui(ItemStack itemStack)
    {
        // NOOP, Proxy only relevant on client
    }
    
    public ItemStack removeFluteFromPlayer(EntityPlayer player, String petName)
    {
        for (int i = 0; i < player.inventory.mainInventory.length; i++)
        {
            ItemStack item = player.inventory.mainInventory[i];
            if (item != null && item.getItem() == itemBatFlute)
            {
                if (item.getTagCompound().getString("batName").equals(petName))
                {
                    player.inventory.setInventorySlotContents(i, null);
                    return item;
                }
            }
        }
        return null;
    }

}
