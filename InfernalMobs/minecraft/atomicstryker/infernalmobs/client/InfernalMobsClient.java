package atomicstryker.infernalmobs.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityList;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.TickRegistry;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.common.ISidedProxy;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class InfernalMobsClient implements ISidedProxy, ITickHandler
{
    private final double NAME_VISION_DISTANCE = 32D;
    private Minecraft mc;
    private World lastWorld;
    private static ArrayList<Entity> lastLoadedEntityList = null;
    private long lastTick;
    
    @Override
    public void load()
    {
        mc = FMLClientHandler.instance().getClient();
        lastTick = System.currentTimeMillis();
        
        TickRegistry.registerTickHandler(this, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
    }
    
    private void renderBossOverlay(float renderTick, Minecraft mc)
    {
        Entity ent = getEntityCrosshairOver(renderTick, mc);
        
        if (ent != null
        && ent instanceof EntityLiving)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLiving)ent);
            if (mod != null)
            {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/icons.png"));
                GL11.glDisable(GL11.GL_BLEND);
                
                EntityLiving target = (EntityLiving) ent;
                String buffer = EntityList.getEntityString(target);
                if (buffer.startsWith("Entity"))
                {
                    buffer = buffer.replaceFirst("Entity", "Rare ");
                }
                else
                {
                    buffer = "Rare "+buffer;
                }
                
                ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                int screenwidth = resolution.getScaledWidth();
                FontRenderer fontR = mc.fontRenderer;
                
                GuiIngame gui = mc.ingameGUI;
                short lifeBarLength = 182;
                int x = screenwidth / 2 - lifeBarLength / 2;
                
                int lifeBarLeft = (int)((float)mod.getActualHealth() / ((float)target.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER) * (float)(lifeBarLength + 1));
                byte y = 12;
                gui.drawTexturedModalRect(x, y, 0, 74, lifeBarLength, 5);
                gui.drawTexturedModalRect(x, y, 0, 74, lifeBarLength, 5);

                if (lifeBarLeft > 0)
                {
                    gui.drawTexturedModalRect(x, y, 0, 79, lifeBarLeft, 5);
                }
                
                fontR.drawStringWithShadow(buffer, screenwidth / 2 - fontR.getStringWidth(buffer) / 2, 10, 0x2F96EB);
                buffer = mod.getModName();
                fontR.drawStringWithShadow(buffer, screenwidth / 2 - fontR.getStringWidth(buffer) / 2, 20, 0xffffff);
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
                List entsInBBList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.renderViewEntity, mc.renderViewEntity.boundingBox.addCoord(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance, viewEntityLookVec.zCoord * reachDistance).expand((double)expandBBvalue, (double)expandBBvalue, (double)expandBBvalue));
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
        if (mc.theWorld == null) return;
        
        renderBossOverlay((Float)tickData[0], mc);
        
        /* client reset in case of swapping worlds */
        if (mc.theWorld != lastWorld)
        {
            boolean newGame = lastWorld == null;
            lastWorld = mc.theWorld;
            lastLoadedEntityList = (ArrayList) lastWorld.getLoadedEntityList();
            
            if (!newGame)
            {
                InfernalMobsCore.instance().checkRareListForObsoletes(lastWorld);
            }
        }
        
        if (lastWorld != null
        && lastTick != System.currentTimeMillis())
        {
            lastTick = System.currentTimeMillis();
            
            if (mc.theWorld.isRemote
             && lastLoadedEntityList != null)
            {
                ArrayList<Entity> bufferList = (ArrayList<Entity>) ((ArrayList)mc.theWorld.getLoadedEntityList()).clone();
                if (bufferList.hashCode() != lastLoadedEntityList.hashCode()) /*optimization.. make sure entityList changed*/
                {
                    bufferList.removeAll(lastLoadedEntityList);
                    for (Entity ent : bufferList)
                    {
                        if (ent instanceof EntityLiving)
                        {
                            askServerForMobMods((EntityLiving)ent);
                        }
                    }
                }
                lastLoadedEntityList = (ArrayList) ((ArrayList) mc.theWorld.getLoadedEntityList()).clone();
            }
        }
    }

    private EnumSet tickTypes = EnumSet.of(TickType.RENDER);
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
    
    private void askServerForMobMods(EntityLiving ent)
    {
        // question: Packet ID 1, from client, { entID }
        Object[] input = { ent.entityId };
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_IM", 1, input));
    }

    public static void onJoinedNewServer()
    {
        lastLoadedEntityList = null;
    }
}
