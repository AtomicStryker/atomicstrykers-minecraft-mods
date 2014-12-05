package atomicstryker.battletowers.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.network.ChestAttackedPacket;

public class ClientTickHandler
{
    
    private MovingObjectPosition playerTarget;
    private boolean hackFailed;

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        
        if (mc.currentScreen != null
        && mc.currentScreen instanceof GuiChest)
        {
            List<?> ents = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, AxisAlignedBB.fromBounds(mc.thePlayer.posX - 8D, mc.thePlayer.posY - 8D, mc.thePlayer.posZ - 8D, mc.thePlayer.posX + 8D, mc.thePlayer.posY + 8D, mc.thePlayer.posZ + 8D));
            if (!ents.isEmpty())
            {
                for (int i = ents.size() - 1; i >= 0; i--)
                {
                    if (ents.get(i) instanceof AS_EntityGolem)
                    {
                        AS_EntityGolem golem = (AS_EntityGolem) ents.get(i);
                        ChestAttackedPacket packet = new ChestAttackedPacket(mc.thePlayer.getGameProfile().getName(), golem.getEntityId());
                        AS_BattleTowersCore.instance.networkHelper.sendPacketToServer(packet);
                        mc.displayGuiScreen(null);
                        break;
                    }
                }
            }
        }

        if (!hackFailed
        && mc.theWorld != null
        && mc.objectMouseOver != null
        && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK
        && mc.objectMouseOver != playerTarget)
        {
            playerTarget = mc.objectMouseOver;

            int x = playerTarget.getBlockPos().getX();
            int y = playerTarget.getBlockPos().getY();
            int z = playerTarget.getBlockPos().getZ();

            if (mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.chest)
            {
                List<?> ents = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, AxisAlignedBB.fromBounds(x - 7D, y - 7D, z - 7D, x + 7D, y + 7D, z + 7D));
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
                                    ChestAttackedPacket packet = new ChestAttackedPacket(mc.thePlayer.getGameProfile().getName(), golem.getEntityId());
                                    AS_BattleTowersCore.instance.networkHelper.sendPacketToServer(packet);
                                }
                                else
                                {
                                    golem.setAwake();
                                    golem.setAttackTarget(mc.thePlayer);
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

}
