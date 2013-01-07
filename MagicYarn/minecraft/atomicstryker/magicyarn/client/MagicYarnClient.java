package atomicstryker.magicyarn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import atomicstryker.magicyarn.common.IProxy;
import atomicstryker.magicyarn.common.MagicYarn;
import atomicstryker.magicyarn.common.pathfinding.AStarNode;
import atomicstryker.magicyarn.common.pathfinding.IAStarPathedEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class MagicYarnClient implements IProxy, IAStarPathedEntity
{
    
    private final String textureFile = "/atomicstryker/magicyarn/client/sprites/magicYarnTextures.png";
    
    private Minecraft mcinstance;
    private ClientTickHandler clientTicker;
    public static MagicYarnClient instance;
    private MPMagicYarn mpYarnInstance;
    
    private AStarNode origin;
    private AStarNode target;
    
    @Override
    public void preInit(File configFile)
    {
        mcinstance = FMLClientHandler.instance().getClient();
        instance = this;
        origin = null;
        target = null;
        
        MinecraftForgeClient.preloadTexture(textureFile);
        MagicYarn.magicYarn.setTextureFile(textureFile);
        
        clientTicker = new ClientTickHandler(this, mcinstance);
        mpYarnInstance = new MPMagicYarn(mcinstance, this);
        
        TickRegistry.registerTickHandler(clientTicker, Side.CLIENT);
    }
    
    public void onConnectedToNewServer()
    {
        mpYarnInstance.checkHasServerMod();
    }
    
    public void inputPath(ArrayList<AStarNode> given, boolean noSound)
    {        
        if (given != null)
        {
            AStarNode prevN = null;
            for (AStarNode n : given)
            {
                if (prevN != null)
                {
                    n.parent = prevN;
                }
                else
                {
                    n.parent = null;
                }
                prevN = n;
            }
            
            if (clientTicker.path == null)
            {
                clientTicker.path = given;
            }
            else
            {
                clientTicker.path.addAll(0, given);
            }
            
            mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.levelup", 1.0F, 1.0F, false);
        }
        else if (!noSound)
        {
            mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.drr", 1.0F, 1.0F, false);
        }
    }
    
    public void tryPathToPlayer(EntityPlayer otherPlayer)
    {
        resetPaths();
        origin = new AStarNode((int)Math.floor(mcinstance.thePlayer.posX), (int)Math.floor(mcinstance.thePlayer.posY)-1, (int)Math.floor(mcinstance.thePlayer.posZ), 0, null);
        target = new AStarNode((int)Math.floor(otherPlayer.posX), (int)Math.floor(otherPlayer.posY)-1, (int)Math.floor(otherPlayer.posZ), 0, null);
        clientTicker.plannerInstance.getPath(origin, target, false);
    }

    public void stopPathSearch()
    {
        clientTicker.plannerInstance.stopPathSearch();
    }
    
    @Override
    public void onFoundPath(ArrayList<AStarNode> result)
    {
        inputPath(result, false);
        
        if (result != null)
        {
            clientTicker.showPath = true;
        }
    }

    @Override
    public void onNoPathAvailable()
    {
        mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.drr", 1.0F, 1.0F, false);
    }
    
    private void resetPaths()
    {
        origin = null;
        target = null;
        inputPath(null, true);
        clientTicker.plannerInstance.stopPathSearch();
        clientTicker.path = null;
        clientTicker.showPath = false;
    }
    
    @Override
    public void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld)
    {
        if (!world.isRemote)
        {
            return;
        }
        
        if(timeButtonHeld > 1.5F)
        {
            timeButtonHeld = 1.5F;
        }

        if(timeButtonHeld < 1.5F)
        {
            if(origin == null) // no start set
            {       
                origin = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0, null);
                System.out.println("Magic Yarn Origin set from null to ["+origin.x+"|"+origin.y+"|"+origin.z+"]");
                world.playSound(player.posX, player.posY, player.posZ, "random.orb", 1.0F, 1.0F, false);
                clientTicker.showPath = false;
            }
            else // start set
            {
                origin.parent = null;
                if (target == null && clientTicker.path == null) // first target
                {                   
                    target = new AStarNode((int)Math.floor(player.posX), (int)player.posY-1, (int)Math.floor(player.posZ), 0, null);
                    System.out.println("Magic Yarn Target set from null to ["+target.x+"|"+target.y+"|"+target.z+"]");

                    clientTicker.plannerInstance.getPath(origin, target, false);
                }
                else // continue path
                {
                    boolean soundplayed = false;
                    if (clientTicker.path != null && !clientTicker.path.isEmpty())
                    {
                        target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0, null);
                        for (int i = clientTicker.path.size()-1; i != 0; i--)
                        {
                            if (clientTicker.path.get(i).equals(target))
                            {
                                System.out.println("Magic Yarn being cut shorter!");
                                world.playSound(player.posX, player.posY, player.posZ, "random.break", 1.0F, 1.0F, false);
                                soundplayed = true;
                                while (i >= 0)
                                {
                                    clientTicker.path.remove(i);
                                    i--;
                                }
                                break;
                            }
                        }
                    }
                    
                    if (clientTicker.showPath)
                    {
                        origin = clientTicker.path.get(0);
                        target = null;
                        //inputPath(null, true);
                        clientTicker.plannerInstance.stopPathSearch();
                        System.out.println("Magic Yarn preparing for next target");
                        if (!soundplayed)
                        {
                            world.playSound(player.posX, player.posY, player.posZ, "random.pop", 1.0F, 1.0F, false);
                        }
                        clientTicker.showPath = false;
                    }
                    else
                    {
                        target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0, null);
                        clientTicker.plannerInstance.getPath(origin, target, false);
                    }
                }
            }
        }
        else
        {
            if(origin != null)
            {
                resetPaths();
                System.out.println("Magic Yarn full reset");
                world.playSound(player.posX, player.posY, player.posZ, "random.fizz", 1.0F, 1.0F, false);
            }
        }
    }
    
}
