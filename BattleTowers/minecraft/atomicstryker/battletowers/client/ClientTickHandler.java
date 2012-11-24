package atomicstryker.battletowers.client;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.GuiChest;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.PlayerControllerMP;

import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.ForgePacketWrapper;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class ClientTickHandler implements ITickHandler
{
    private final EnumSet tickTypes;
    
    private MovingObjectPosition playerTarget;
    private boolean hackFailed;
    
    public ClientTickHandler()
    {
        tickTypes = EnumSet.of(TickType.RENDER);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        
        if (mc.currentScreen != null
        && mc.currentScreen instanceof GuiChest)
        {
            List ents = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, AxisAlignedBB.getBoundingBox(mc.thePlayer.posX - 8D, mc.thePlayer.posY - 8D, mc.thePlayer.posZ - 8D, mc.thePlayer.posX + 8D, mc.thePlayer.posY + 8D, mc.thePlayer.posZ + 8D));
            if (!ents.isEmpty())
            {
                for (int i = ents.size() - 1; i >= 0; i--)
                {
                    if (ents.get(i) instanceof AS_EntityGolem)
                    {
                        AS_EntityGolem golem = (AS_EntityGolem) ents.get(i);
                        
                        Object[] objArray = { golem.entityId };
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_BT", 2, objArray));
                        
                        mc.displayGuiScreen(null);
                        break;
                    }
                }
            }
        }

        if (!hackFailed
        && mc.theWorld != null
        && mc.objectMouseOver != null
        && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE
        && mc.objectMouseOver != playerTarget)
        {
            playerTarget = mc.objectMouseOver;

            int x = playerTarget.blockX;
            int y = playerTarget.blockY;
            int z = playerTarget.blockZ;

            if (mc.theWorld.getBlockId(x, y, z) == Block.chest.blockID)
            {
                List ents = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, AxisAlignedBB.getBoundingBox(x - 7D, y - 7D, z - 7D, x + 7D, y + 7D, z + 7D));
                if (!ents.isEmpty())
                {
                    for (int i = ents.size() - 1; i >= 0; i--)
                    {
                        if (ents.get(i) instanceof AS_EntityGolem)
                        {
                            boolean multiplayer = mc.theWorld.isRemote;

                            AS_EntityGolem golem = (AS_EntityGolem) ents.get(i);
                            Object progressHack = null;
                            float progress = 0;
                            try
                            {
                                progressHack = ReflectionHelper.getPrivateValue(PlayerControllerMP.class, mc.playerController, 6);
                                progress = (Float) progressHack;
                            }
                            catch (Exception e)
                            {
                                System.err.println("Tell AtomicStryker to update his BattleTowers Block Break Progress Hack because: " + e);
                                hackFailed = true;
                                e.printStackTrace();
                            }

                            if (progress > 0)
                            {
                                if (multiplayer)
                                {
                                    Object[] objArray = { golem.entityId };
                                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_BT", 2, objArray));
                                }
                                else
                                {
                                    golem.setAwake();
                                    golem.setTarget(mc.thePlayer);
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }

    @Override
    public String getLabel()
    {
        return "BattleTowers";
    }

}
