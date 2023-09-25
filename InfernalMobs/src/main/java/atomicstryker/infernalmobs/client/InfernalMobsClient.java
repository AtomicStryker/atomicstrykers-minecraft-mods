package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.io.File;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = InfernalMobsCore.MOD_ID)
public class InfernalMobsClient {

    private static Minecraft mc;

    @SubscribeEvent
    public static void playerLoginToServer(ClientPlayerNetworkEvent.LoggingIn evt) {
        // client starting point, also local servers
        mc = Minecraft.getInstance();
        InfernalMobsCore.instance().initIfNeeded(evt.getPlayer().level());
    }

    @SubscribeEvent
    public static void onEntityJoinedWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && mc.player != null && (event.getEntity() instanceof Mob || (event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Enemy))) {
            InfernalMobsCore.networkChannel.send(new MobModsPacket(mc.player.getName().getString(), event.getEntity().getId(), (byte) 0), PacketDistributor.SERVER.noArg());
            InfernalMobsCore.LOGGER.debug("onEntityJoinedWorld {}, ent-id {} querying modifiers from server", event.getEntity(), event.getEntity().getId());
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(ClientPlayerNetworkEvent.LoggingOut evt) {
        if (evt.getPlayer() != null) {
            SidedCache.getInfernalMobs(evt.getPlayer().level()).clear();
        }
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

    public static File getMcFolder() {
        return Minecraft.getInstance().gameDirectory;
    }

}
