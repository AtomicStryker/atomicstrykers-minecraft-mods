package atomicstryker.battletowers.client;

import java.util.List;

import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.network.ChestAttackedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ClientTickHandler
{
    
    private RayTraceResult playerTarget;
    private boolean hackFailed;

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        
        if (mc.currentScreen != null
        && mc.currentScreen instanceof GuiChest)
        {
            List<?> ents = mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(mc.player.posX - 8D, mc.player.posY - 8D, mc.player.posZ - 8D, mc.player.posX + 8D, mc.player.posY + 8D, mc.player.posZ + 8D));
            if (!ents.isEmpty())
            {
                for (int i = ents.size() - 1; i >= 0; i--)
                {
                    if (ents.get(i) instanceof AS_EntityGolem)
                    {
                        AS_EntityGolem golem = (AS_EntityGolem) ents.get(i);
                        ChestAttackedPacket packet = new ChestAttackedPacket(mc.player.getGameProfile().getName(), golem.getEntityId());
                        AS_BattleTowersCore.instance.networkHelper.sendPacketToServer(packet);
                        mc.displayGuiScreen(null);
                        break;
                    }
                }
            }
        }

        if (!hackFailed
        && mc.world != null
        && mc.objectMouseOver != null
        && mc.objectMouseOver.typeOfHit == Type.BLOCK
        && mc.objectMouseOver != playerTarget)
        {
            playerTarget = mc.objectMouseOver;

            int x = playerTarget.getBlockPos().getX();
            int y = playerTarget.getBlockPos().getY();
            int z = playerTarget.getBlockPos().getZ();

            if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.CHEST)
            {
                List<?> ents = mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(x - 7D, y - 7D, z - 7D, x + 7D, y + 7D, z + 7D));
                if (!ents.isEmpty())
                {
                    for (int i = ents.size() - 1; i >= 0; i--)
                    {
                        if (ents.get(i) instanceof AS_EntityGolem)
                        {
                            boolean multiplayer = mc.world.isRemote;

                            AS_EntityGolem golem = (AS_EntityGolem) ents.get(i);
                            Object progressHack;
                            float progress = 0;
                            try
                            {
                                progressHack = ReflectionHelper.getPrivateValue(PlayerControllerMP.class, mc.playerController, 4);
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
                                    ChestAttackedPacket packet = new ChestAttackedPacket(mc.player.getGameProfile().getName(), golem.getEntityId());
                                    AS_BattleTowersCore.instance.networkHelper.sendPacketToServer(packet);
                                }
                                else
                                {
                                    golem.setAwake();
                                    golem.setAttackTarget(mc.player);
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
