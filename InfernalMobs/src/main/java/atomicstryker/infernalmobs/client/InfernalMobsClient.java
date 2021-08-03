package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = InfernalMobsCore.MOD_ID)
public class InfernalMobsClient {

    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    private static int airOverrideValue = -999;
    private static final double NAME_VISION_DISTANCE = 32D;
    private static long airDisplayTimeout;
    private static Minecraft mc;
    private static long nextPacketTime;

    private static long healthBarRetainTime;
    private static LivingEntity retainedTarget;

    private static LinkedHashMap<UUID, LerpingBossEvent> vanillaBossEventsMap = null;

    @SubscribeEvent
    public static void playerLoginToServer(ClientPlayerNetworkEvent.LoggedInEvent evt) {
        // client starting point, also local servers
        mc = Minecraft.getInstance();
        if (evt.getPlayer() != null) {
            InfernalMobsCore.instance().initIfNeeded(evt.getPlayer().level);
        }
    }

    public static void load() {
        nextPacketTime = 0;
        healthBarRetainTime = 0;
        retainedTarget = null;
    }

    @SubscribeEvent
    public static void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide && mc.player != null && (event.getEntity() instanceof Mob || (event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Enemy))) {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new MobModsPacket(mc.player.getName().getContents(), event.getEntity().getId(), (byte) 0));
            InfernalMobsCore.LOGGER.debug("onEntityJoinedWorld {}, ent-id {} querying modifiers from server", event.getEntity(), event.getEntity().getId());
        }
    }

    private static void askServerMods(Entity ent) {
        if (System.currentTimeMillis() > nextPacketTime && (ent instanceof Mob || (ent instanceof LivingEntity && ent instanceof Enemy))) {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new MobModsPacket(mc.player.getName().getContents(), ent.getId(), (byte) 0));
            InfernalMobsCore.LOGGER.debug("askServerMods {}, ent-id {} querying modifiers from server", ent, ent.getId());
            nextPacketTime = System.currentTimeMillis() + 250L;
        }
    }

    private static void askServerHealth(Entity ent) {
        if (System.currentTimeMillis() > nextPacketTime) {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new HealthPacket(mc.player.getName().getContents(), ent.getId(), 0f, 0f));
            nextPacketTime = System.currentTimeMillis() + 250L;
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
        if (evt.getPlayer() != null && evt.getPlayer().level != null) {
            SidedCache.getInfernalMobs(evt.getPlayer().level).clear();
        }
    }

    @SubscribeEvent
    public static void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (InfernalMobsCore.instance().getIsHealthBarDisabled() || event.getType() != RenderGameOverlayEvent.ElementType.CHAT || mc.gui.getBossOverlay().shouldPlayMusic()) {
            return;
        }

        LivingEntity ent = getEntityCrosshairOver(event.getPartialTicks(), mc);
        boolean retained = false;

        if (ent == null && System.currentTimeMillis() < healthBarRetainTime) {
            ent = retainedTarget;
            retained = true;
        } else if (retainedTarget != null) {
            vanillaBossEventsMap.remove(retainedTarget.getUUID());
            retainedTarget = null;
        }

        if (vanillaBossEventsMap == null) {
            boolean hackSuccess = false;
            for (Field declaredField : BossHealthOverlay.class.getDeclaredFields()) {
                if (declaredField.getType() == Map.class) {
                    declaredField.setAccessible(true);
                    try {
                        vanillaBossEventsMap = (LinkedHashMap<UUID, LerpingBossEvent>) declaredField.get(mc.gui.getBossOverlay());
                        hackSuccess = true;
                    } catch (IllegalAccessException e) {
                        hackSuccess = false;
                    }
                }
            }
            if (!hackSuccess) {
                vanillaBossEventsMap = new LinkedHashMap<>();
            }
        }

        if (ent != null) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(ent);
            if (mod != null) {
                askServerHealth(ent);

                UUID uuid = ent.getUUID();
                Component name = new TextComponent(mod.getEntityDisplayName(ent));
                float progress = mod.getActualHealth(ent) / mod.getActualMaxHealth(ent);
                if (ent.isDeadOrDying()) {
                    progress = 0.01F;
                }
                int modStr = mod.getModSize();
                /* green for elite, yellow for ultra, red for infernal */
                BossEvent.BossBarColor color = (modStr <= 5) ? BossEvent.BossBarColor.GREEN : (modStr <= 10) ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.RED;

                if (!vanillaBossEventsMap.containsKey(uuid)) {
                    // last 3 param bools are darkenScreen, playBossMusic and worldFog
                    vanillaBossEventsMap.put(uuid, new LerpingBossEvent(uuid, name, progress, color, BossEvent.BossBarOverlay.PROGRESS, false, false, false));
                } else {
                    LerpingBossEvent bossEvent = vanillaBossEventsMap.get(uuid);
                    bossEvent.setProgress(progress);
                }

                // MC supports multiple bosses. Infernal Mobs does not. hide the modifier subdisplay in multi case
                if (vanillaBossEventsMap.size() == 1) {
                    drawModifiersUnderHealthBar(event, ent, mod);
                }

                if (!retained) {
                    retainedTarget = ent;
                    healthBarRetainTime = System.currentTimeMillis() + 3000L;
                }

            } else {
                askServerMods(ent);
            }
        }
    }

    private static void drawModifiersUnderHealthBar(RenderGameOverlayEvent.Pre event, LivingEntity ent, MobModifier mod) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);

        int screenwidth = mc.getWindow().getGuiScaledWidth();
        Font fontR = mc.font;

        PoseStack matrixStack = event.getMatrixStack();

        int yCoord = 10;
        String[] display = mod.getDisplayNames();
        int i = 0;
        while (i < display.length && display[i] != null) {
            yCoord += 10;
            fontR.drawShadow(matrixStack, display[i], screenwidth / 2 - fontR.width(display[i]) / 2, yCoord, 0xffffff);
            i++;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
    }

    private static LivingEntity getEntityCrosshairOver(float partialTicks, Minecraft mc) {

        Entity entity = mc.getCameraEntity();
        if (entity != null && mc.level != null) {

            double distance = NAME_VISION_DISTANCE;
            HitResult result = entity.pick(distance, partialTicks, false);
            Vec3 vec3d = entity.getEyePosition(partialTicks);

            double distanceToHit = result.getLocation().distanceToSqr(vec3d);

            Vec3 vec3d1 = entity.getViewVector(1.0F);
            Vec3 vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
            AABB axisalignedbb = entity.getBoundingBox().expandTowards(vec3d1.scale(distance)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityraytraceresult = ProjectileUtil.getEntityHitResult(entity, vec3d, vec3d2, axisalignedbb, (p_lambda$getMouseOver$0_0_) -> !p_lambda$getMouseOver$0_0_.isSpectator() && p_lambda$getMouseOver$0_0_.isPickable(), distanceToHit);
            if (entityraytraceresult != null) {
                Entity entity1 = entityraytraceresult.getEntity();
                Vec3 vec3d3 = entityraytraceresult.getLocation();
                double d2 = vec3d.distanceToSqr(vec3d3);
                if (d2 < distanceToHit && entity1 instanceof LivingEntity) {
                    return (LivingEntity) entity1;
                }
            }
        }
        return null;
    }

    public static void onHealthPacketForClient(int entID, float health, float maxhealth) {
        Minecraft.getInstance().submitAsync(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> onHealthPacket(entID, health, maxhealth)));
    }

    private static void onHealthPacket(int entID, float health, float maxhealth) {
        Entity ent = Minecraft.getInstance().level.getEntity(entID);
        if (ent instanceof LivingEntity) {
            MobModifier mod = InfernalMobsCore.getMobModifiers((LivingEntity) ent);
            if (mod != null) {
                mod.setActualHealth(health, maxhealth);
            }
        }
    }

    public static void onKnockBackPacket(float xv, float zv) {
        mc.submitAsync(() -> MM_Gravity.knockBack(mc.player, xv, zv));
    }

    public static void onMobModsPacketToClient(String stringData, int entID) {
        InfernalMobsCore.instance().addRemoteEntityModifiers(mc.level, entID, stringData);
    }

    public static void onVelocityPacket(float xv, float yv, float zv) {
        mc.submitAsync(() -> mc.player.push(xv, yv, zv));
    }

    public static void onAirPacket(int air) {
        airOverrideValue = air;
        airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    public static File getMcFolder() {
        return Minecraft.getInstance().gameDirectory;
    }

    @SubscribeEvent
    public static void onTick(RenderGameOverlayEvent.Post event) {
        // Post and ALL is after the forge ingame gui has finished rendering, we draw ontop
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (System.currentTimeMillis() > airDisplayTimeout) {
                airOverrideValue = -999;
            }

            // modded Gui.renderPlayerHealth 'air' section
            if (!mc.player.isEyeInFluid(FluidTags.WATER) && airOverrideValue != -999) {

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

                int leftScreenCoordinate = mc.getWindow().getGuiScaledWidth() / 2 + 91;
                int topScreenCoordinate = mc.getWindow().getGuiScaledHeight() - 59;
                int maxHearts = getVehicleMaxHearts(mc.player);
                int maxAir = mc.player.getMaxAirSupply();
                int currentAir = Math.min(airOverrideValue, maxAir);
                int rowCount = getVisibleVehicleHeartRows(maxHearts) - 1;
                topScreenCoordinate = topScreenCoordinate - rowCount * 10;
                int fullBubbles = Mth.ceil((double) (currentAir - 2) * 10.0D / (double) maxAir);
                int partialBubbles = Mth.ceil((double) currentAir * 10.0D / (double) maxAir) - fullBubbles;

                for (int j5 = 0; j5 < fullBubbles + partialBubbles; ++j5) {
                    if (j5 < fullBubbles) {
                        mc.gui.blit(event.getMatrixStack(), leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 16, 18, 9, 9);
                    } else {
                        mc.gui.blit(event.getMatrixStack(), leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 25, 18, 9, 9);
                    }
                }
            }
        }
    }

    private static int getVehicleMaxHearts(LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.showVehicleHealth()) {
            float maxHealth = livingEntity.getMaxHealth();
            int roundedHalf = (int) (maxHealth + 0.5F) / 2;
            if (roundedHalf > 30) {
                roundedHalf = 30;
            }

            return roundedHalf;
        } else {
            return 0;
        }
    }

    private static int getVisibleVehicleHeartRows(int heartCount) {
        return (int) Math.ceil((double) heartCount / 10.0D);
    }
}
