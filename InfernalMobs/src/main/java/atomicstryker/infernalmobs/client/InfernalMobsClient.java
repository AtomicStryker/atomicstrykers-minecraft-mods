package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.KnockBackPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import atomicstryker.infernalmobs.common.network.VelocityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.File;

public class InfernalMobsClient {

    private static InfernalMobsClient INSTANCE;

    public InfernalMobsClient() {
        INSTANCE = this;
    }

    public static InfernalMobsClient instance() {
        return INSTANCE;
    }

    private static Minecraft mc;

    @SubscribeEvent
    public void playerLoginToServer(ClientPlayerNetworkEvent.LoggingIn evt) {
        // client starting point, also local servers
        mc = Minecraft.getInstance();
        InfernalMobsCore.instance().initIfNeeded(evt.getPlayer().level());
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && mc.player != null && (event.getEntity() instanceof Mob || (event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Enemy))) {
            MobModsPacket mobModsPacket = new MobModsPacket(mc.player.getName().getString(), event.getEntity().getId(), (byte) 0);
            PacketDistributor.SERVER.noArg().send(mobModsPacket);
            InfernalMobsCore.LOGGER.trace("onEntityJoinedWorld {}, ent-id {} querying modifiers from server", event.getEntity(), event.getEntity().getId());
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(ClientPlayerNetworkEvent.LoggingOut evt) {
        if (evt.getPlayer() != null) {
            SidedCache.getInfernalMobs(evt.getPlayer().level()).clear();
        }
    }

    public void onHealthPacketForClient(final HealthPacket healthPacket, final PlayPayloadContext context) {
        mc.submitAsync(() -> onHealthPacket(healthPacket.entID(), healthPacket.health(), healthPacket.maxhealth()));
    }

    private void onHealthPacket(int entID, float health, float maxhealth) {
        Entity ent = Minecraft.getInstance().level.getEntity(entID);
        if (ent instanceof LivingEntity) {
            MobModifier mod = InfernalMobsCore.getMobModifiers((LivingEntity) ent);
            if (mod != null) {
                mod.setActualHealth(health, maxhealth);
            }
        }
    }

    public void onKnockBackPacket(KnockBackPacket knockBackPacket, PlayPayloadContext playPayloadContext) {
        mc.submitAsync(() -> MM_Gravity.knockBack(mc.player, knockBackPacket.xv(), knockBackPacket.zv()));
    }

    public void onMobModsPacketToClient(MobModsPacket mobModsPacket, PlayPayloadContext playPayloadContext) {
        InfernalMobsCore.instance().addRemoteEntityModifiers(mc.level, mobModsPacket.entID(), mobModsPacket.stringData());
    }

    public void onVelocityPacket(VelocityPacket velocityPacket, PlayPayloadContext playPayloadContext) {
        mc.submitAsync(() -> mc.player.push(velocityPacket.xv(), velocityPacket.yv(), velocityPacket.zv()));
    }

    public static File getMcFolder() {
        return Minecraft.getInstance().gameDirectory;
    }
}
