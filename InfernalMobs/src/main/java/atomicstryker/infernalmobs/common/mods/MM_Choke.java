package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;

public class MM_Choke extends MobModifier
{

    public MM_Choke()
    {
        super();
    }

    public MM_Choke(MobModifier next)
    {
        super(next);
    }

    private EntityLivingBase lastTarget;
    private int lastAir;

    @Override
    public String getModName()
    {
        return "Choke";
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (!hasSteadyTarget()) {
            return super.onUpdate(mob);
        }
        
        if (getMobTarget() != lastTarget)
        {
            lastAir = -999;
            if (lastTarget != null)
            {
                updateAir();
            }
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
                        lastTarget.attackEntityFrom(DamageSource.DROWN, 2.0F);
                    }

                    updateAir();
                }
            }
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (lastTarget != null && source.getTrueSource() == lastTarget && lastAir != -999)
        {
            lastAir += 60;
            updateAir();
        }

        return damage;
    }

    @Override
    public boolean onDeath()
    {
        lastAir = -999;
        if (lastTarget != null)
        {
            updateAir();
            lastTarget = null;
        }
        return false;
    }

    private void updateAir()
    {
        lastTarget.setAir(lastAir);
        if (lastTarget instanceof EntityPlayerMP)
        {
            InfernalMobsCore.instance().sendAirPacket((EntityPlayerMP) lastTarget, lastAir);
        }
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
