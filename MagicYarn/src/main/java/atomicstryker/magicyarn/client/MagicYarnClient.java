package atomicstryker.magicyarn.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.astarpathing.AStarNode;
import atomicstryker.astarpathing.IAStarPathedEntity;
import atomicstryker.magicyarn.common.IProxy;
import atomicstryker.magicyarn.common.MagicYarn;
import atomicstryker.magicyarn.common.network.PathPacket;

public class MagicYarnClient implements IProxy, IAStarPathedEntity
{
    
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
          
        clientTicker = new ClientTickHandler(this, mcinstance);
        mpYarnInstance = new MPMagicYarn(mcinstance, this);
        FMLCommonHandler.instance().bus().register(mpYarnInstance);        
        FMLCommonHandler.instance().bus().register(clientTicker);
    }
    
    public void onServerAnsweredChallenge()
    {
        mpYarnInstance.onServerHasMod();
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
            
            mcinstance.theWorld.playSoundAtEntity(mcinstance.thePlayer, "random.levelup", 1.0F, 1.0F);
            
            sendPathToServer(mcinstance.thePlayer.getGameProfile().getName(), clientTicker.path);
        }
        else if (!noSound)
        {
            mcinstance.theWorld.playSoundAtEntity(mcinstance.thePlayer, "random.bowhit", 1.0F, 1.0F);
        }
    }

    public void tryPathToPlayer(EntityPlayer otherPlayer)
    {
        resetPaths();
        clientTicker.plannerInstance.getPath(
                (int)Math.floor(mcinstance.thePlayer.posX), (int)Math.floor(mcinstance.thePlayer.posY), (int)Math.floor(mcinstance.thePlayer.posZ),
                (int)Math.floor(otherPlayer.posX), (int)Math.floor(otherPlayer.posY), (int)Math.floor(otherPlayer.posZ), false);
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
        mcinstance.theWorld.playSoundAtEntity(mcinstance.thePlayer, "random.bowhit", 1.0F, 1.0F);
    }
    
    private void resetPaths()
    {
        origin = null;
        target = null;
        inputPath(null, true);
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
                    target = new AStarNode((int)Math.floor(player.posX), (int)player.posY, (int)Math.floor(player.posZ), 0, null);
                    System.out.println("Magic Yarn Target set from null to ["+target.x+"|"+target.y+"|"+target.z+"]");

                    clientTicker.plannerInstance.getPath(origin.x, origin.y, origin.z, target.x, target.y, target.z, false);
                }
                else // continue path
                {
                    boolean soundplayed = false;
                    if (clientTicker.path != null && !clientTicker.path.isEmpty())
                    {
                        target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ), 0, null);
                        int idx = 0;
                        for (int i = 0; i < clientTicker.path.size(); i++)
                        {
                            if (clientTicker.path.get(i).equals(target))
                            {
                                System.out.println("Magic Yarn being cut shorter!");
                                world.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
                                soundplayed = true;
                                idx = i;
                                break;
                            }
                        }
                        
                        while (idx > 0)
                        {
                            clientTicker.path.remove(0);
                            idx--;
                        }
                    }
                    
                    if (clientTicker.showPath)
                    {
                        origin = clientTicker.path.get(0);
                        target = null;
                        //inputPath(null, true);
                        System.out.println("Magic Yarn preparing for next target");
                        if (!soundplayed)
                        {
                            world.playSound(player.posX, player.posY, player.posZ, "random.pop", 1.0F, 1.0F, false);
                        }
                        clientTicker.showPath = false;
                    }
                    else
                    {
                        target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ), 0, null);
                        clientTicker.plannerInstance.getPath(origin.x, origin.y, origin.z, target.x, target.y, target.z, false);
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
    
    
    private void sendPathToServer(String username, ArrayList<AStarNode> path)
    {
        if (!mpYarnInstance.getHasServerMod())
        {
            return;
        }
        
        /*
         * Packet spec: It starts with the int id 2, followed by the path owner's username String,
         * followed by an int depicting the count of AStarNodes being transferred,
         * followed by the path AStarNodes as triplets of ints x,y,z. Parents can be reconstructed
         * as the previous AStarNode in the series, the list is sorted.
         */
        
        ByteBuf data = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(data, username);
        data.writeLong(path.size());
        for (AStarNode n : path)
        {
            data.writeInt(n.x);
            data.writeInt(n.y);
            data.writeInt(n.z);
        }
        
        MagicYarn.instance.networkHelper.sendPacketToServer(new PathPacket(data));
    }
    
    public void onReceivedPathPacket(ByteBuf data)
    {
        String username = ByteBufUtils.readUTF8String(data);
        if (username.equals(mcinstance.thePlayer.getCommandSenderName()))
        {
            return;
        }
        
        int nodes = data.readInt();
        AStarNode[] out = new AStarNode[nodes];
        int i = 0;
        AStarNode read;
        AStarNode prevN = null;
        while (nodes > 0)
        {
            read = new AStarNode(data.readInt(), data.readInt(), data.readInt(), 0, prevN);
            out[i] = read;
            prevN = read;
            i++;
            nodes--;
        }
        clientTicker.addOtherPath(username, out);
    }

    public void onReceivedPathDeletionPacket(ByteBuf data)
    {
        clientTicker.removeOtherPath(ByteBufUtils.readUTF8String(data));
    }

    @Override
    public void init()
    {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(MagicYarn.instance.magicYarn, 0, new ModelResourceLocation("magicyarn:magicyarn", "inventory"));
    }
    
}
