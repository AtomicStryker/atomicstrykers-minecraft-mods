package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = InfernalMobsCore.MOD_ID)
public class OverlayBossBar {

    private static final double NAME_VISION_DISTANCE = 32D;

    private static Minecraft mc;

    private static long healthBarRetainTime;
    private static LivingEntity retainedTarget;
    private static long nextPacketTime;

    private static LinkedHashMap<UUID, LerpingBossEvent> vanillaBossEventsMap = null;

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ModLoadingContext.get().getActiveNamespace(), InfernalMobsCore.MOD_ID + "_bossbar"), new InfernalMobsHealthBarGuiOverlay());
        mc = Minecraft.getInstance();
        healthBarRetainTime = 0;
        retainedTarget = null;
        nextPacketTime = 0;
    }

    public static class InfernalMobsHealthBarGuiOverlay implements LayeredDraw.Layer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker partialTick) {
            if (InfernalMobsCore.instance().getIsHealthBarDisabled() || mc.gui.getBossOverlay().shouldPlayMusic()) {
                return;
            }

            LivingEntity ent = getEntityCrosshairOver(partialTick.getRealtimeDeltaTicks(), mc);
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
                    Component name = Component.literal(mod.getEntityDisplayName(ent));
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
                        drawModifiersUnderHealthBar(guiGraphics, mod);
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
    }

    private static void drawModifiersUnderHealthBar(GuiGraphics guiGraphics, MobModifier mod) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);

        int screenwidth = mc.getWindow().getGuiScaledWidth();
        Font fontR = mc.font;

        int yCoord = 10;
        String[] display = mod.getDisplayNames();
        int i = 0;
        while (i < display.length && display[i] != null) {
            yCoord += 10;
            guiGraphics.drawString(mc.font, display[i], screenwidth / 2 - fontR.width(display[i]) / 2, yCoord, 0xffffff);
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

    private static void askServerMods(Entity ent) {
        if (System.currentTimeMillis() > nextPacketTime && (ent instanceof Mob || (ent instanceof LivingEntity && ent instanceof Enemy))) {
            MobModsPacket mobModsPacket = new MobModsPacket(mc.player.getName().getString(), ent.getId(), (byte) 0);
            PacketDistributor.sendToServer(mobModsPacket);
            InfernalMobsCore.LOGGER.debug("askServerMods {}, ent-id {} querying modifiers from server", ent, ent.getId());
            nextPacketTime = System.currentTimeMillis() + 250L;
        }
    }

    private static void askServerHealth(Entity ent) {
        if (System.currentTimeMillis() > nextPacketTime) {
            HealthPacket healthPacket = new HealthPacket(mc.player.getName().getString(), ent.getId(), 0f, 0f);
            PacketDistributor.sendToServer(healthPacket);
            nextPacketTime = System.currentTimeMillis() + 250L;
        }
    }
}
