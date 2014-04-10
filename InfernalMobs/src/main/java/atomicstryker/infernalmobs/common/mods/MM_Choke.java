package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Choke extends MobModifier
{
    public MM_Choke(EntityLivingBase mob)
    {
        this.modName = "Choke";
    }

    public MM_Choke(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Choke";
        this.nextMod = prevMod;
    }

    EntityLivingBase lastTarget;
    int lastAir;

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != lastTarget)
        {
            lastAir = -999;
            lastTarget = getMobTarget();
        }

        if (lastTarget != null)
        {
            if (mob.canEntityBeSeen(lastTarget))
            {
                if (lastAir == -999)
                {
                    lastAir = lastTarget.getAir();
                }
                else
                {
                    lastAir = Math.min(lastAir, lastTarget.getAir());
                }

                if (!(lastTarget instanceof EntityPlayer && ((EntityPlayer) lastTarget).capabilities.disableDamage))
                {
                    lastAir--;
                    if (lastAir < -19)
                    {
                        lastAir = 0;
                        lastTarget.attackEntityFrom(DamageSource.drown, 2.0F);
                    }
                    lastTarget.setAir(lastAir);
                }
            }
        }

        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getSourceOfDamage() == lastTarget && lastAir != -999)
        {
            lastAir += 60;
            lastTarget.setAir(lastAir);
        }
        
        return damage;
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "ofBreathlessness", "theAnaerobic", "ofDeprivation" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "Sith Lord", "Dark Lord", "Darth" };

}
