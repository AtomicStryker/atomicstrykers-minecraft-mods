package atomicstryker.petbat.common;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import atomicstryker.petbat.common.network.BatNamePacket;
import atomicstryker.petbat.common.network.NetworkHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod(modid = "petbat", name = "Pet Bat", version = "1.4.4")
public class PetBatMod implements IProxy
{
    private Item TAME_ITEM_ID;
    private Item GLISTER_ITEM_ID;

    private final String[] batNames = { "Lucius", "Draco", "Vlad", "Darkwing", "Zubat", "Cecil", "Dragos", "Cezar", "Ciprian", "Daniel", "Dorin", "Mihai", "Mircea", "Radu" };

    /**
     * experience to levels table 0 - no xp 25 - lvl 1, 25 xp diff 75 - lvl 2,
     * 50 xp diff 175 - lvl 3, 100 xp diff 375 - lvl 4, 200 xp diff 775 - lvl 5,
     * 400 xp diff 1575 - lvl 6, 800 xp diff
     */
    public int getLevelFromExperience(int xp)
    {
        if (xp < 25)
            return 0;
        if (xp < 75)
            return 1;
        if (xp < 175)
            return 2;
        if (xp < 375)
            return 3;
        if (xp < 775)
            return 4;
        if (xp < 1575)
            return 5;
        return 6;
    }

    public int getMissingExperienceToNextLevel(int xp)
    {
        if (xp < 25)
            return 25 - xp;
        if (xp < 75)
            return 75 - xp;
        if (xp < 175)
            return 175 - xp;
        if (xp < 375)
            return 375 - xp;
        if (xp < 775)
            return 775 - xp;
        if (xp < 1575)
            return 1575 - xp;
        return -1;
    }

    public String getLevelTitle(int level)
    {
        return I18n.translateToLocal("translation.PetBat:batlevel" + level);
    }

    public String getLevelDescription(int level)
    {
        return I18n.translateToLocal("translation.PetBat:batlevel" + level + "desc");
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

    @Instance(value = "petbat")
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

        itemPocketedBat = new ItemPocketedPetBat().setUnlocalizedName("fed_pet_bat").setRegistryName("petbat", "fed_pet_bat");
        ForgeRegistries.ITEMS.register(itemPocketedBat);

        itemBatFlute = new ItemBatFlute().setUnlocalizedName("bat_flute").setRegistryName("petbat", "bat_flute");
        ForgeRegistries.ITEMS.register(itemBatFlute);

        networkHelper = new NetworkHelper("AS_PB", BatNamePacket.class);

        EntityRegistry.registerModEntity(new ResourceLocation("petbat", "petbat"), EntityPetBat.class, "petbat", 1, this, 32, 3, false);

        MinecraftForge.EVENT_BUS.register(this);

        entityBatFlightCoords = EntityBat.class.getDeclaredFields()[0];
        entityBatFlightCoords.setAccessible(true);

        proxy.onModPreInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.onModInit();
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        glisterBatEnabled = Loader.isModLoaded("dynamiclights");
        TAME_ITEM_ID = Items.PUMPKIN_PIE;
        GLISTER_ITEM_ID = Items.GLOWSTONE_DUST;
    }

    public boolean getPetBatInventoryTeleportEnabled()
    {
        return batInventoryTeleport;
    }

    @SubscribeEvent
    public void onPlayerLeftClick(BreakSpeed event)
    {
        EntityPlayer p = event.getEntityPlayer();
        ItemStack item = p.inventory.getCurrentItem();
        if (item != null && item.getItem() == TAME_ITEM_ID)
        {
            List<Entity> entityList = p.world.getEntitiesWithinAABBExcludingEntity(p, p.getEntityBoundingBox().expand(10D, 10D, 10D));
            BlockPos coords = new BlockPos((int) (p.posX + 0.5D), (int) (p.posY + 1.5D), (int) (p.posZ + 0.5D));
            entityList.stream().filter(ent -> ent instanceof EntityBat).forEach(ent -> {
                try
                {
                    entityBatFlightCoords.set(ent, coords);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractSpecific event)
    {
        if (event.getTarget() instanceof EntityBat)
        {
            EntityPlayer p = event.getEntityPlayer();
            if (!p.world.isRemote)
            {
                ItemStack item = p.inventory.getCurrentItem();
                if (item != null && item.getItem() == TAME_ITEM_ID)
                {
                    event.setCanceled(true);
                    p.inventory.clearMatchingItems(TAME_ITEM_ID, -1, 1, null);

                    EntityBat b = (EntityBat) event.getTarget();
                    EntityPetBat newPet = new EntityPetBat(p.world);
                    newPet.setLocationAndAngles(b.posX, b.posY, b.posZ, b.rotationYaw, b.rotationPitch);
                    newPet.setNames(p.getGameProfile().getName(), getRandomBatName());
                    newPet.setOwnerEntity(p);

                    p.world.spawnEntity(newPet);
                    b.setDead();
                }
            }
        }

        if (glisterBatEnabled && event.getTarget() instanceof EntityPetBat)
        {
            EntityPlayer p = event.getEntityPlayer();
            ItemStack item = p.inventory.getCurrentItem();
            if (item != null && item.getItem() == GLISTER_ITEM_ID)
            {
                new GlisterBatAdapter((EntityPetBat) event.getTarget());
                p.inventory.clearMatchingItems(GLISTER_ITEM_ID, -1, 1, null);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAttacksEntity(AttackEntityEvent event)
    {
        if (event.getTarget() instanceof EntityPetBat)
        {
            EntityPetBat bat = (EntityPetBat) event.getTarget();
            if (bat.getOwnerName().equals(event.getEntityPlayer().getName()) && event.getEntityPlayer().getHeldItemMainhand() == null)
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
        System.out.println("onItemTossEvent hooked, isRemote: " + event.getEntity().world.isRemote);
        if (!event.getEntity().world.isRemote)
        {
            EntityItem itemDropped = event.getEntityItem();
            System.out.println("PlayerDropsEvent iterating over drop " + itemDropped);
            EntityItem foundItem;
            final Item id = itemDropped.getEntityItem().getItem();
            if (id == itemPocketedBat)
            {
                final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(itemDropped.world, itemDropped.getEntityItem(), event.getPlayer());
                if (bat.getHealth() > 1)
                {
                    bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                    itemDropped.world.spawnEntity(bat);
                    event.setCanceled(true);
                }
                else
                {
                    // bat is inert. see if it was tossed onto pumpkin pie for
                    // revival

                    final List nearEnts = itemDropped.world.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.getEntityBoundingBox().expand(8D, 8D, 8D));
                    for (Object o : nearEnts)
                    {
                        if (o instanceof EntityItem)
                        {
                            foundItem = (EntityItem) o;
                            if (foundItem.getEntityItem().getItem() == TAME_ITEM_ID)
                            {
                                bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                                itemDropped.world.spawnEntity(bat);
                                bat.setHealth(bat.getMaxHealth()); // set full
                                                                   // entity
                                                                   // health
                                event.setCanceled(true);
                                foundItem.getEntityItem().shrink(1);
                                if (foundItem.getEntityItem().getCount() < 1)
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
                final List nearEnts = itemDropped.world.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.getEntityBoundingBox().expand(8D, 8D, 8D));
                for (Object o : nearEnts)
                {
                    if (o instanceof EntityPetBat)
                    {
                        final EntityPetBat bat = (EntityPetBat) o;
                        if ((bat.getAttackTarget() == null || !bat.getAttackTarget().isEntityAlive()) && (bat.getFoodAttackTarget() == null || bat.getFoodAttackTarget().isEntityAlive()))
                        {
                            bat.setFoodAttackTarget(itemDropped);
                            break;
                        }
                    }
                    else if (o instanceof EntityItem)
                    {
                        foundItem = (EntityItem) o;
                        if (foundItem.getEntityItem().getItem() == itemPocketedBat) // inert
                                                                                    // bat
                                                                                    // lying
                                                                                    // around
                        {
                            final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(foundItem.world, foundItem.getEntityItem(), event.getPlayer());
                            bat.setPosition(foundItem.posX, foundItem.posY, foundItem.posZ);
                            foundItem.world.spawnEntity(bat);
                            bat.setHealth(bat.getMaxHealth()); // set full
                                                               // entity health
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
        final Iterator<EntityItem> iter = event.getDrops().iterator();
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
        if (event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) event.getEntityLiving();
            if (p.isEntityAlive() && p.getHeldItemMainhand() != null && p.getHeldItemMainhand().getItem().equals(itemPocketedBat))
            {
                if (p.getActivePotionEffect(MobEffects.NIGHT_VISION) == null)
                {
                    p.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 100));
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
        Iterator<ItemStack> iter = player.inventory.mainInventory.iterator();
        while (iter.hasNext())
        {
            ItemStack item = iter.next();
            if (item.getItem() == itemBatFlute)
            {
                if (item.getTagCompound().getString("batName").equals(petName))
                {
                    iter.remove();
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public void onModPreInit()
    {

    }

}
