package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MM_Sticky extends MobModifier {

    private final static long coolDown = 15000L;
    private static Class<?>[] modBans = {MM_Storm.class};
    private static String[] suffix = {"ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink"};
    private static String[] prefix = {"thieving", "snagging", "quickfingered"};
    private long nextAbilityUse = 0L;
    private Class<?>[] disallowed = {Creeper.class};

    public MM_Sticky() {
        super();
    }

    public MM_Sticky(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Sticky";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() != null
                && (source.getEntity() instanceof Player)) {
            Player p = (Player) source.getEntity();
            ItemStack weapon = p.getInventory().getItem(p.getInventory().selected);
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse
                    && source.getEntity() != null
                    && !(source instanceof IndirectEntityDamageSource)) {
                nextAbilityUse = time + coolDown;
                ItemEntity drop = p.drop(p.getInventory().removeItem(p.getInventory().selected, 1), false);
                if (drop != null) {
                    drop.setPickUpDelay(50);
                    mob.level.playSound(null, mob.blockPosition(), SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);
                }
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

}
