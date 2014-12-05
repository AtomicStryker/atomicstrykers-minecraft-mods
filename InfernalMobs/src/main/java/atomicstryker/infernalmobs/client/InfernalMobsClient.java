package atomicstryker.infernalmobs.client;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.opengl.GL11;

import atomicstryker.infernalmobs.common.ISidedProxy;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;

public class InfernalMobsClient implements ISidedProxy
{
    private final double NAME_VISION_DISTANCE = 32D;
    private Minecraft mc;
    private World lastWorld;
    private long nextPacketTime;
    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsClient;
    private int airOverrideValue = -999;
    
    private long healthBarRetainTime;
    private EntityLivingBase retainedTarget;
    
    @Override
    public void preInit()
    {
        FMLCommonHandler.instance().bus().register(this);
        mc = FMLClientHandler.instance().getClient();
    }

    @Override
    public void load()
    {
        nextPacketTime = 0;
        rareMobsClient = new ConcurrentHashMap<EntityLivingBase, MobModifier>();
        
        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
        MinecraftForge.EVENT_BUS.register(this);
        
        healthBarRetainTime = 0;
        retainedTarget = null;
    }
    
    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (event.world.isRemote && mc.thePlayer != null
                && (event.entity instanceof EntityMob || (event.entity instanceof EntityLivingBase && event.entity instanceof IMob)))
        {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new MobModsPacket(mc.thePlayer.getGameProfile().getName(), event.entity
                    .getEntityId(), (byte) 0));
        }
    }

    private void askServerHealth(Entity ent)
    {
        if (System.currentTimeMillis() > nextPacketTime)
        {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new HealthPacket(mc.thePlayer.getGameProfile().getName(),ent.getEntityId(), 0f, 0f));
            nextPacketTime = System.currentTimeMillis() + 100l;
        }
    }

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (InfernalMobsCore.instance().getIsHealthBarDisabled() || 
                event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH || BossStatus.bossName != null)
        {
            return;
        }

        Entity ent = getEntityCrosshairOver(event.partialTicks, mc);
        boolean retained = false;
        
        if (ent == null && System.currentTimeMillis() < healthBarRetainTime)
        {
            ent = retainedTarget;
            retained = true;
        }

        if (ent != null && ent instanceof EntityLivingBase)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
            if (mod != null)
            {
                askServerHealth(ent);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(Gui.icons);
                GL11.glDisable(GL11.GL_BLEND);

                EntityLivingBase target = (EntityLivingBase) ent;
                String buffer = mod.getEntityDisplayName(target);

                ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                int screenwidth = resolution.getScaledWidth();
                FontRenderer fontR = mc.fontRendererObj;

                GuiIngame gui = mc.ingameGUI;
                short lifeBarLength = 182;
                int x = screenwidth / 2 - lifeBarLength / 2;

                int lifeBarLeft = (int) ((float) mod.getActualHealth(target) / (float) mod.getActualMaxHealth(target) * (float) (lifeBarLength + 1));
                byte y = 12;
                gui.drawTexturedModalRect(x, y, 0, 74, lifeBarLength, 5);
                gui.drawTexturedModalRect(x, y, 0, 74, lifeBarLength, 5);

                if (lifeBarLeft > 0)
                {
                    gui.drawTexturedModalRect(x, y, 0, 79, lifeBarLeft, 5);
                }

                int yCoord = 10;
                fontR.drawStringWithShadow(buffer, screenwidth / 2 - fontR.getStringWidth(buffer) / 2, yCoord, 0x2F96EB);

                String[] display = mod.getDisplayNames();
                int i = 0;
                while (i < display.length && display[i] != null)
                {
                    yCoord += 10;
                    fontR.drawStringWithShadow(display[i], screenwidth / 2 - fontR.getStringWidth(display[i]) / 2, yCoord, 0xffffff);
                    i++;
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(Gui.icons);
                
                if (!retained)
                {
                    retainedTarget = target;
                    healthBarRetainTime = System.currentTimeMillis() + 3000l;
                }
                
            }
        }
    }

    private Entity getEntityCrosshairOver(float renderTick, Minecraft mc)
    {
        Entity returnedEntity = null;

        if (mc.getRenderViewEntity() != null)
        {
            if (mc.theWorld != null)
            {
                double reachDistance = NAME_VISION_DISTANCE;
                final MovingObjectPosition mopos = mc.getRenderViewEntity().rayTrace(reachDistance, renderTick);
                double reachDist2 = reachDistance;
                final Vec3 viewEntPositionVec = mc.getRenderViewEntity().getPositionVector();

                if (mopos != null)
                {
                    reachDist2 = mopos.hitVec.squareDistanceTo(viewEntPositionVec);
                }

                final Vec3 viewEntityLookVec = mc.getRenderViewEntity().getLook(renderTick);
                final Vec3 actualReachVector =
                        viewEntPositionVec.addVector(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance,
                                viewEntityLookVec.zCoord * reachDistance);
                float expandBBvalue = 1.0F;
                double lowestDistance = reachDist2;
                Entity iterEnt;
                Entity pointedEntity = null;
                for (Object obj : mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                        mc.getRenderViewEntity(),
                        mc.getRenderViewEntity().getBoundingBox().addCoord(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance,
                                viewEntityLookVec.zCoord * reachDistance).expand((double) expandBBvalue, (double) expandBBvalue,
                                (double) expandBBvalue)))
                {
                    iterEnt = (Entity) obj;
                    if (iterEnt.canBeCollidedWith())
                    {
                        float entBorderSize = iterEnt.getCollisionBorderSize();
                        AxisAlignedBB entHitBox = iterEnt.getBoundingBox().expand((double) entBorderSize, (double) entBorderSize, (double) entBorderSize);
                        MovingObjectPosition interceptObjectPosition = entHitBox.calculateIntercept(viewEntPositionVec, actualReachVector);

                        if (entHitBox.isVecInside(viewEntPositionVec))
                        {
                            if (0.0D < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = 0.0D;
                            }
                        }
                        else if (interceptObjectPosition != null)
                        {
                            double distanceToEnt = viewEntPositionVec.distanceTo(interceptObjectPosition.hitVec);

                            if (distanceToEnt < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = distanceToEnt;
                            }
                        }
                    }
                }

                if (pointedEntity != null && (lowestDistance < reachDist2 || mopos == null))
                {
                    returnedEntity = pointedEntity;
                }
            }
        }

        return returnedEntity;
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        if (mc.theWorld == null || (mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame()))
            return;

        /* client reset in case of swapping worlds */
        if (mc.theWorld != lastWorld)
        {
            boolean newGame = lastWorld == null;
            lastWorld = mc.theWorld;

            if (!newGame)
            {
                InfernalMobsCore.proxy.getRareMobs().clear();
            }
        }
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs()
    {
        return rareMobsClient;
    }

    @Override
    public void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth)
    {
        Entity ent = FMLClientHandler.instance().getClient().theWorld.getEntityByID(entID);
        if (ent != null && ent instanceof EntityLivingBase)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
            if (mod != null)
            {
                //System.out.printf("health packet [%f of %f] for %s\n", health, maxhealth, ent);
                mod.setActualHealth(health, maxhealth);
            }
        }
    }

    @Override
    public void onKnockBackPacket(float xv, float zv)
    {
        MM_Gravity.knockBack(FMLClientHandler.instance().getClient().thePlayer, xv, zv);
    }

    @Override
    public void onMobModsPacketToClient(String stringData, int entID)
    {
        InfernalMobsCore.instance().addRemoteEntityModifiers(FMLClientHandler.instance().getClient().theWorld, entID, stringData);
    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv)
    {
        FMLClientHandler.instance().getClient().thePlayer.addVelocity(xv, yv, zv);
    }

    @Override
    public void onAirPacket(int air)
    {
        airOverrideValue = air;
    }
    
    @SubscribeEvent
    public void onTick(RenderGameOverlayEvent.Pre event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.AIR)
        {
            if (!mc.thePlayer.isInsideOfMaterial(Material.water) && airOverrideValue != -999)
            {
                final ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                GL11.glEnable(GL11.GL_BLEND);
                
                int right_height = 39;
                
                final int left = res.getScaledWidth() / 2 + 91;
                final int top = res.getScaledHeight() - right_height;
                final int full = MathHelper.ceiling_double_int((double)(airOverrideValue - 2) * 10.0D / 300.0D);
                final int partial = MathHelper.ceiling_double_int((double)airOverrideValue * 10.0D / 300.0D) - full;

                for (int i = 0; i < full + partial; ++i)
                {
                    mc.ingameGUI.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
                }
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }
}
