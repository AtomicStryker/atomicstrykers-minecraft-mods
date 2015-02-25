package atomicstryker.magicyarn.client;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import atomicstryker.astarpathing.AStarNode;
import atomicstryker.astarpathing.AStarPathPlanner;

public class ClientTickHandler
{
    
    private final MagicYarnClient clientInstance;
    private final Minecraft mcinstance;
    private World lastWorld;
    public AStarPathPlanner plannerInstance;
    
    public ArrayList<AStarNode> path = null;
    public boolean showPath = false;
    
    private HashMap<String, AStarNode[]> otherPaths;
    
    public ClientTickHandler(MagicYarnClient client, Minecraft mc)
    {
        clientInstance = client;
        mcinstance = mc;
        otherPaths = new HashMap<String, AStarNode[]>();
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (mcinstance.thePlayer == null || mcinstance.theWorld == null) return;
            
            if (lastWorld != mcinstance.theWorld)
            {
                lastWorld = mcinstance.theWorld;
                plannerInstance = new AStarPathPlanner(lastWorld, clientInstance);
            }
            
            if (showPath)
            {
                if (path != null)
                {
                    //EntityFX efx = null;
                    for (AStarNode temp : path)
                    {
                        if (temp.parent != null)
                        {
                            mcinstance.effectRenderer.spawnEffectParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                                    (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75, 0);
                            /*efx = new EntityCritFX(mcinstance.theWorld, temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                                    (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75);
                            efx.setRBGColorF(efx.getRedColorF(), 0, efx.getBlueColorF());
                            //efx.setParticleTextureIndex(efx.getParticleTextureIndex() + 1);
                            if (efx != null)
                            {
                                mcinstance.effectRenderer.addEffect(efx);
                            }*/
                        }
                    }
                    
                    if (!otherPaths.isEmpty())
                    {
                        int colorindex = 0;
                        for (String user : otherPaths.keySet())
                        {
                            AStarNode[] nodes = otherPaths.get(user);
                            //float r = colors[colorindex][0];
                            //float g = colors[colorindex][1];
                            //float b = colors[colorindex][2];
                            for (AStarNode temp : nodes)
                            {
                                if (temp.parent != null)
                                {
                                    mcinstance.effectRenderer.spawnEffectParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                                            (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75, 0);
                                    /*efx = new EntityCritFX(mcinstance.theWorld, temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                                            (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75);
                                    efx.setRBGColorF(r, g, b);
                                    //efx.setParticleTextureIndex(efx.getParticleTextureIndex() + 1);
                                    if (efx != null)
                                    {
                                        mcinstance.effectRenderer.addEffect(efx);
                                    }*/
                                }
                            }
                            colorindex++;
                            if (colorindex >= colors.length)
                            {
                                colorindex = 0;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private final float[][] colors = {
            { 1f, 1f, 0 }, // yellow
            { 0.75f, 0.5f, 0.25f }, // brown
            { 0, 0, 1f }, // blue
            { 1f, 0, 0 }, // red
            { 0, 1f, 1f }, // cyan
    };
    
    public void addOtherPath(String user, AStarNode[] nodes)
    {
        otherPaths.remove(user);
        otherPaths.put(user, nodes);
    }
    
    public void removeOtherPath(String user)
    {
        otherPaths.remove(user);
    }
}
