package atomicstryker.infernalmobs.client;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.common.ISidedProxy;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class InfernalMobsClient implements ISidedProxy, ITickHandler
{
    private final double NAME_VISION_DISTANCE = 32D;
    private Minecraft mc;
    private World lastWorld;
    private long nextPacketTime;
    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsClient;
    
    @Override
    public void load()
    {
        mc = FMLClientHandler.instance().getClient();
        nextPacketTime = 0;
        rareMobsClient = new ConcurrentHashMap<EntityLivingBase, MobModifier>();
        
        TickRegistry.registerTickHandler(this, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
        MinecraftForge.EVENT_BUS.register(new EntityTracker());
    }
    
    private void askServerHealth(Entity ent)
    {
        if (System.currentTimeMillis() > nextPacketTime)
        {
            Object[] toSend = {ent.entityId};
            PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_IM", 4, toSend));
            nextPacketTime = System.currentTimeMillis() + 100l;
        }
    }
    
    private void renderBossOverlay(float renderTick, Minecraft mc)
    {
        if (InfernalMobsCore.instance().getIsHealthBarDisabled())
        {
            return;
        }
        
        Entity ent = getEntityCrosshairOver(renderTick, mc);
        
        if (ent != null
        && ent instanceof EntityLivingBase)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase)ent);
            if (mod != null)
            {
                askServerHealth(ent);
                
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.func_110434_K().func_110577_a(Gui.field_110324_m); // bindtexture textures/gui/icons.png
                GL11.glDisable(GL11.GL_BLEND);
                
                EntityLivingBase target = (EntityLivingBase) ent;
                String buffer = mod.getEntityDisplayName(target);
                
                ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                int screenwidth = resolution.getScaledWidth();
                FontRenderer fontR = mc.fontRenderer;
                
                GuiIngame gui = mc.ingameGUI;
                short lifeBarLength = 182;
                int x = screenwidth / 2 - lifeBarLength / 2;
                
                int lifeBarLeft = (int)((float)mod.getActualHealth(target) / (float)mod.getActualMaxHealth(target) * (float)(lifeBarLength + 1));
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
                this.mc.func_110434_K().func_110577_a(Gui.field_110324_m); // bindtexture textures/gui/icons.png
            }
        }
    }

    private Entity getEntityCrosshairOver(float renderTick, Minecraft mc)
    {
        Entity returnedEntity = null;
        
        if (mc.renderViewEntity != null)
        {
            if (mc.theWorld != null)
            {
                double reachDistance = NAME_VISION_DISTANCE;
                mc.objectMouseOver = mc.renderViewEntity.rayTrace(reachDistance, renderTick);
                double reachDist2 = reachDistance;
                Vec3 viewEntPositionVec = mc.renderViewEntity.getPosition(renderTick);

                if (mc.objectMouseOver != null)
                {
                    reachDist2 = mc.objectMouseOver.hitVec.distanceTo(viewEntPositionVec);
                }

                Vec3 viewEntityLookVec = mc.renderViewEntity.getLook(renderTick);
                Vec3 actualReachVector = viewEntPositionVec.addVector(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance, viewEntityLookVec.zCoord * reachDistance);
                Entity pointedEntity = null;
                float expandBBvalue = 1.0F;
                List<?> entsInBBList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.renderViewEntity, mc.renderViewEntity.boundingBox.addCoord(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance, viewEntityLookVec.zCoord * reachDistance).expand((double)expandBBvalue, (double)expandBBvalue, (double)expandBBvalue));
                double lowestDistance = reachDist2;

                for (int i = 0; i < entsInBBList.size(); ++i)
                {
                    Entity iterEnt = (Entity)entsInBBList.get(i);

                    if (iterEnt.canBeCollidedWith())
                    {
                        float entBorderSize = iterEnt.getCollisionBorderSize();
                        AxisAlignedBB entHitBox = iterEnt.boundingBox.expand((double)entBorderSize, (double)entBorderSize, (double)entBorderSize);
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

                if (pointedEntity != null && (lowestDistance < reachDist2 || mc.objectMouseOver == null))
                {
                    returnedEntity = pointedEntity;
                }
            }
        }
        
        return returnedEntity;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (mc.theWorld == null || (mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame())) return;
        
        renderBossOverlay((Float)tickData[0], mc);
        
        /* client reset in case of swapping worlds */
        if (mc.theWorld != lastWorld)
        {
            boolean newGame = lastWorld == null;
            lastWorld = mc.theWorld;
            
            if (!newGame)
            {
                InfernalMobsCore.instance().checkRareListForObsoletes(lastWorld);
            }
        }
    }

    private EnumSet<TickType> tickTypes = EnumSet.of(TickType.RENDER);
    
    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }

    @Override
    public String getLabel()
    {
        return "InfernalMobs";
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs()
    {
        return rareMobsClient;
    }
    
}
