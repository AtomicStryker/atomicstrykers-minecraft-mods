package atomicstryker.minions.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Iterator;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.client.gui.GuiMinionMenu;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.PacketType;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class MinionsClient
{
    private static String s_MenuKey = "M";
    private static int i_MenuKey = Keyboard.getKeyIndex(s_MenuKey);

    private static MovingObjectPosition targetObjectMouseOver;
    
    public static boolean isSelectingMineArea = false;
    public static int mineAreaShape = 0;
    public static int customSizeXZ = 3;
    public static int customSizeY = 3;

    public static boolean hasMinionsSMPOverride = false;
    public static boolean hasAllMinionsSMPOverride = false;
    
    private static long lastStaffLightningBoltTime = System.currentTimeMillis();
    
    private static long timeLastSound = 0L;
    private final static long timeSoundDelay = 400L;
    
    private static World lastWorld;
    
    public static void preInit(FMLPreInitializationEvent event)
    {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try
        {
            cfg.load();
            s_MenuKey = cfg.get(cfg.CATEGORY_GENERAL, "Minion_Menu_Key", "M").value;
            i_MenuKey = Keyboard.getKeyIndex(s_MenuKey);
            
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "Minions Client has a problem loading it's configuration!");
        }
        finally
        {
            cfg.save();
        }
    }
    
    public static void onRenderTick(Object[] tickData)
    {
        float renderTick = (Float) tickData[0];
        Minecraft mc = FMLClientHandler.instance().getClient();
        
        // Menu
        if (Keyboard.isKeyDown(i_MenuKey) && mc.currentScreen == null)
        {
            mc.displayGuiScreen(new GuiMinionMenu());
        }

        if (mc.currentScreen == null && isSelectingMineArea)
        {
            if (mc.thePlayer.inventory.getCurrentItem() == null
            || mc.thePlayer.inventory.getCurrentItem().itemID != MinionsCore.itemMastersStaff.itemID)
            {
                isSelectingMineArea = false;
                MinionsRenderHook.deleteSelection();
            }
            else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
            {
                int x = mc.objectMouseOver.blockX;
                int y = mc.objectMouseOver.blockY;
                int z = mc.objectMouseOver.blockZ;
                
                int bossX = MathHelper.floor_double(mc.thePlayer.posX);
                int bossZ = MathHelper.floor_double(mc.thePlayer.posZ);
                int xDirection;
                int zDirection;
                
                if (Math.abs(x - bossX) > Math.abs(z - bossZ))
                {
                    xDirection = (x - bossX > 0) ? 1 : -1;
                    zDirection = 0;
                }
                else
                {
                    xDirection = 0;
                    zDirection = (z - bossZ > 0) ? 1 : -1;
                }

                if (mineAreaShape == 0) // mineshaft
                {
                    MinionsRenderHook.setSelectionPoint(0, x, y, z);
                    MinionsRenderHook.setSelectionPoint(1, x+4, y, z+4);

                    MinionsRenderHook.deleteAdditionalCubes();
                    MinionsRenderHook.addAdditionalCube(x+1, y-1, z);
                    MinionsRenderHook.addAdditionalCube(x+2, y-2, z);
                    MinionsRenderHook.addAdditionalCube(x+3, y-3, z);
                }
                else if (mineAreaShape == 1) // stripmine
                {
                    MinionsRenderHook.setSelectionPoint(0, x, y, z);
                    MinionsRenderHook.setSelectionPoint(1, x+xDirection*2, y+1, z+zDirection*2);
                }
                else if (mineAreaShape == 2) // custom size
                {
                    int half = ((customSizeXZ-1) / 2);
                    
                    if (xDirection != 0) // advancing in Xdir, start at x and z-half to x+size and z+half
                    {
                        MinionsRenderHook.setSelectionPoint(0, x, y, z - half);
                        MinionsRenderHook.setSelectionPoint(1, x + (customSizeXZ*xDirection), y + customSizeY-1, z + half);
                    }
                    else if (zDirection != 0) // advancing in Zdir, start at z and x-half to z+size and x+half
                    {
                        MinionsRenderHook.setSelectionPoint(0, x - half, y, z);
                        MinionsRenderHook.setSelectionPoint(1, x + half, y + customSizeY-1, z + (customSizeXZ*zDirection));
                    }
                }
            }
        }
        else
        {
            MinionsRenderHook.deleteSelection();
        }
        
        if (mc.currentScreen == null)
        {
            if (Mouse.isButtonDown(0)
            && mc.thePlayer.inventory.getCurrentItem() != null
            && mc.thePlayer.inventory.getCurrentItem().itemID == MinionsCore.itemMastersStaff.itemID
            && mc.objectMouseOver == null
            && lastStaffLightningBoltTime + 100L < System.currentTimeMillis())
            {
                if (MinionsCore.hasPlayerWillPower(mc.thePlayer))
                {
                    lastStaffLightningBoltTime = System.currentTimeMillis();
                    EntityLiving p = mc.renderViewEntity;
                    MovingObjectPosition pos = p.rayTrace(10, renderTick);
                    if (pos != null)
                    {
                        Vector3 startvec = Vector3.fromEntityCenter(p).add(0, 0.68D, 0);
                        startvec.x -= (double)(MathHelper.cos(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        //startvec.y -= 0.10000000149011612D;
                        startvec.z -= (double)(MathHelper.sin(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        
                        Vector3 endvec = Vector3.fromVec3(pos.hitVec);
                        
                        Object[] toSend = { startvec.x, startvec.y, startvec.z, endvec.x, endvec.y, endvec.z };
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.LIGHTNINGBOLT.ordinal(), toSend)); // request lightning bolt packet
                    }
                }
                else
                {
                    playFartSound(mc.theWorld, mc.thePlayer);
                }
            }
        }
    }

    public static void onWorldTick(Object[] tickData)
    {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world != lastWorld && world != null)
        {
            MinionsRenderHook.renderHookEnt = new RenderEntLahwran_Minions(FMLClientHandler.instance().getClient(), world);
            world.addWeatherEffect(MinionsRenderHook.renderHookEnt);
            lastWorld = world;
        }
        
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
        	ChickenLightningBolt.update();
        }
    }

    public static void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        Minecraft mcinstance = FMLClientHandler.instance().getClient();
        
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        PacketType packetType = PacketType.byID(ForgePacketWrapper.readPacketID(data));

        //System.out.println("Client received packet, ID "+packetType);

        switch (packetType)
        {
            case HASMINIONS:
            {
                Class[] decodeAs = {Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                hasMinionsSMPOverride = ((Integer)packetReadout[0] == 1);
                hasAllMinionsSMPOverride = ((Integer)packetReadout[1] == 1);
                // System.out("Client got status packet, now: hasMinionsSMPOverride = "+hasMinionsSMPOverride+", hasAllMinionsSMPOverride: "+hasAllMinionsSMPOverride);
                break;
            }
            
            case SOUNDTOALL:
            {
                Class[] decodeAs = {Integer.class, String.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                int entID = (Integer) packetReadout[0];
                String sound = (String) packetReadout[1];

                //System.out.println("Minions server demands sound "+sound+" at entity id "+entID);

                Entity temp = null;
                boolean found = false;

                Iterator iter = mcinstance.theWorld.loadedEntityList.iterator();
                while (iter.hasNext())
                {
                    temp = (Entity) iter.next();
                    if (temp.entityId == entID)
                    {
                        found = true;
                        break;
                    }
                }
                if (found)
                {
                    //System.out.println("Found ent, playing sound now!");
                    mcinstance.theWorld.playSound(temp.posX, temp.posY, temp.posZ, sound, 1.0F, 1.0F, false);
                }
                break;
            }
            
            case REQUESTXPSETTING:
            {
                Class[] decodeAs = {Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                if (MinionsCore.evilDeedXPCost != (Integer)packetReadout[0])
                {
                    MinionsCore.evilDeedXPCost = (Integer)packetReadout[0];

                    if (mcinstance.currentScreen instanceof GuiMinionMenu)
                    {
                        mcinstance.currentScreen = null;

                        if (MinionsCore.evilDeedXPCost != -1)
                        {
                            mcinstance.ingameGUI.getChatGUI().printChatMessage("Server says you don't have enough XP for Evil Deeds");
                        }
                        else
                        {
                            mcinstance.ingameGUI.getChatGUI().printChatMessage("Server says Minions are unobtainable through Evil Deeds here");
                        }
                    }
                }
                break;
            }
            
            case LIGHTNINGBOLT:
            {
                Class[] decodeAs = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Long.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                Vector3 start = new Vector3((Double)packetReadout[0], (Double)packetReadout[1], (Double)packetReadout[2]);
                Vector3 end = new Vector3((Double)packetReadout[3], (Double)packetReadout[4], (Double)packetReadout[5]);

                spawnLightningBolt(mcinstance.theWorld, mcinstance.thePlayer, start, end, (Long)packetReadout[6]);
                break;
            }
            
            case ENTITYMOUNTED:
            {
                // System.out("client received mount packet!");
                Class[] decodeAs = {Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                Entity minion = MinionsCore.findEntityByID(mcinstance.theWorld, (Integer) packetReadout[0]);
                Entity target = MinionsCore.findEntityByID(mcinstance.theWorld, (Integer) packetReadout[1]);
                // System.out("client packet mountEntity, target "+target+", minion "+minion);
                if (minion != null && target != null)
                {
                    // System.out("executing mount!");
                    target.mountEntity(minion);
                }
                break;
            }
        }
    }
    
    private static void spawnLightningBolt(World world, EntityLiving shooter, Vector3 startvec, Vector3 endvec, long randomizer)
    {
        for (int i = 3; i != 0; i--)
        {
            ChickenLightningBolt bolt = new ChickenLightningBolt(world, startvec, endvec, randomizer);
            bolt.defaultFractal();
            bolt.finalizeBolt();
            bolt.setWrapper(shooter);
            ChickenLightningBolt.boltlist.add(bolt);   
        }
        
        if (timeLastSound + timeSoundDelay < System.currentTimeMillis())
        {
            playSoundToAllPlayersOnServer(shooter, "mod_minions.bolt");
            timeLastSound = System.currentTimeMillis();
        }
    }
    
    private static void playFartSound(World world, EntityPlayer player)
    {
        if (timeLastSound + timeSoundDelay*2 < System.currentTimeMillis())
        {
            timeLastSound = System.currentTimeMillis();
            playSoundToAllPlayersOnServer(player, "mod_minions.fart");
        }
    }
    
    public static void playSoundToAllPlayersOnServer(Entity source, String soundName)
    {
        Object[] toSend = {source.entityId, soundName};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.SOUNDTOALL.ordinal(), toSend)); // client requests sound distribution packet (entID, soundString)
    }
    
    public static void OnMastersGloveRightClick(ItemStack var1, World worldObj, EntityPlayer playerEnt)
    {
        if (timeLastSound + timeSoundDelay < System.currentTimeMillis())
        {
            // abuse the sound delay to prevent multi-click bugs
            timeLastSound = System.currentTimeMillis();
        }
        else
        {
            return;
        }
        
        Minecraft mcinstance = FMLClientHandler.instance().getClient();
        
        targetObjectMouseOver = mcinstance.objectMouseOver; //mcinstance.renderViewEntity.rayTrace(30.0D, 1.0F);
        EntityMinion[] minions = (EntityMinion[]) MinionsCore.masterNames.get(playerEnt.username);
        // System.out.println("OnMastersGloveRightClick Master: "+playerEnt.username+", minionarray is: "+minions);
        Entity target;

        if (targetObjectMouseOver == null)
        {
            targetObjectMouseOver = mcinstance.renderViewEntity.rayTrace(30.0D, 1.0F);
        }

        if (targetObjectMouseOver == null)
        {
            // NOOP
        }
        else if ((target = targetObjectMouseOver.entityHit) != null)
        {
            if (target instanceof EntityAnimal || target instanceof EntityPlayer)
            {
                Object[] toSend = {playerEnt.username, playerEnt.entityId, target.entityId};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDPICKUPENT.ordinal(), toSend)); // pickup entity command packet
            }
            else if (target instanceof EntityMinion)
            {
                EntityMinion minion = (EntityMinion) target;

                Object[] toSend = {playerEnt.username, playerEnt.entityId, target.entityId};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDDROPALL.ordinal(), toSend)); // minion drop items command packet
            }
        }
        else if (targetObjectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        {
            int x = targetObjectMouseOver.blockX;
            int y = targetObjectMouseOver.blockY +1;
            int z = targetObjectMouseOver.blockZ;
            
            // System.out.println("OnMastersGloveRightClick coordinate mode, ["+x+"|"+y+"|"+z+"]");

            if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y-1, z))
            {
                y--;
            }

            // System.out("OnMastersGloveRightClick hasAllMinionsSMPOverride: "+hasAllMinionsSMPOverride);
            if (!hasAllMinionsSMPOverride)
            {
                Object[] toSend = {playerEnt.username, x, y, z};
                // System.out("Client sending CMDMINIONSPAWN, ["+x+"|"+y+"|"+z+"]");
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMINIONSPAWN.ordinal(), toSend)); // minion spawn command packet
                playerEnt.worldObj.spawnParticle("hugeexplosion", x, y, z, 0.0D, 0.0D, 0.0D);
                return;
            }

            int ID = worldObj.getBlockId(x, y, z);
            TileEntity chestOrInventoryBlock;

            if (MinionsCore.foundTreeBlocks.contains(ID))
            {
                if (MinionsCore.hasPlayerWillPower(playerEnt))
                {
                    Object[] toSend = {playerEnt.username, x, y, z};
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDCHOPTREES.ordinal(), toSend)); // treechop job command packet
                }
                else
                {
                    playFartSound(playerEnt.worldObj, playerEnt);
                    return;
                }
            }
            else if (isSelectingMineArea)
            {
                isSelectingMineArea = false;
                if (mineAreaShape == 0)
                {
                    if (MinionsCore.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.username, x, y, z};
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDSTAIRWELL.ordinal(), toSend)); // stairwell job command packet
                    }
                    else
                    {
                        playFartSound(playerEnt.worldObj, playerEnt);
                        return;
                    }
                }
                else if (mineAreaShape == 1)
                {
                    if (MinionsCore.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.username, x, y, z};
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDSTRIPMINE.ordinal(), toSend)); // stripmine job command packet
                    }
                    else
                    {
                        playFartSound(playerEnt.worldObj, playerEnt);
                        return;
                    }
                }
                else if (mineAreaShape == 2)
                {
                    if (MinionsCore.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.username, x, y, z, customSizeXZ, customSizeY};
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDCUSTOMDIG.ordinal(), toSend)); // custom dig job command packet
                    }
                    else
                    {
                        playFartSound(playerEnt.worldObj, playerEnt);
                        return;
                    }
                }
            }
            else if ((chestOrInventoryBlock = worldObj.getBlockTileEntity(x, y-1, z)) != null
                    && chestOrInventoryBlock instanceof IInventory
                    && ((IInventory)chestOrInventoryBlock).getSizeInventory() >= 24)
            {
                Object[] toSend = {playerEnt.username, x, y, z};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDASSIGNCHEST.ordinal(), toSend)); // chest assign command packet
            }
            else if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y, z) && (minions != null || hasAllMinionsSMPOverride))
            {
                // check if player targets his own feet. if so, order minion carry
                if (MathHelper.floor_double(playerEnt.posX) == x
                        && MathHelper.floor_double(playerEnt.posZ) == z
                        && Math.abs(MathHelper.floor_double(playerEnt.posY) - y) < 3)
                {
                    Object[] toSend = {playerEnt.username, playerEnt.entityId, playerEnt.entityId};
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDPICKUPENT.ordinal(), toSend)); // pickup entity command packet
                }
                else
                {
                    Object[] toSend = {playerEnt.username, x, y, z};
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMOVETO.ordinal(), toSend)); // moveto command packet
                }
            }
            else if (MinionsCore.isBlockValueable(worldObj.getBlockId(x, y-1, z)))
            {

                Object[] toSend = {playerEnt.username, x, y, z};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMINEOREVEIN.ordinal(), toSend)); // mine ore vein command
            }
        }
    }
    
    public static void OnMastersGloveRightClickHeld(ItemStack var1, World var2, EntityPlayer var3)
    {
        Object[] toSend = {var3.username};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDFOLLOW.ordinal(), toSend));
    }
    
    public static void requestXPSettingFromServer()
    {
        Object[] toSend = {MinionsCore.evilDeedXPCost};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend));
    }
}
