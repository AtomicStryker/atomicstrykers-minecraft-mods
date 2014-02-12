package atomicstryker.minions.client;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.client.gui.GuiMinionMenu;
import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.points.PointCube;
import atomicstryker.minions.client.render.region.CuboidRegion;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.PacketType;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.network.ForgePacketWrapper;
import atomicstryker.network.PacketDispatcher;
import atomicstryker.network.WrappedPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class MinionsClient
{
    
    public static boolean isSelectingMineArea = false;
    public static int mineAreaShape = 0;
    public static int customSizeXZ = 3;
    public static int customSizeY = 3;

    public static boolean hasMinionsSMPOverride = false;
    public boolean hasAllMinionsSMPOverride = false;
    
    private long lastStaffLightningBoltTime = System.currentTimeMillis();
    
    private long timeNextSoundAllowed = 0L;
    private final long timeSoundDelay = 400L;
    
    private World lastWorld;
    private final Minecraft mc;
    
    private KeyBinding menuKey;
    
    private static CuboidRegion selection = new CuboidRegion();
    private static ArrayList<PointCube> additionalCubes = new ArrayList<PointCube>();
    
    public MinionsClient()
    {
        mc = FMLClientHandler.instance().getClient();        
        menuKey = new KeyBinding("Minions Menu", Keyboard.KEY_M, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(menuKey);
    }
    
    @SubscribeEvent
    public void onDrawSelectionBow(DrawBlockHighlightEvent event)
    {
        if (mc.currentScreen == null && isSelectingMineArea)
        {
            renderSelections(event.partialTicks);
        }
    }
    
    private void renderSelections(float renderTick)
    {
        RenderHelper.disableStandardItemLighting();  
        
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        
        EntityPlayer player = mc.thePlayer;
        double xGuess = player.prevPosX + (player.posX - player.prevPosX) * renderTick;
        double yGuess = player.prevPosY + (player.posY - player.prevPosY) * renderTick;
        double zGuess = player.prevPosZ + (player.posZ - player.prevPosZ) * renderTick;
        GL11.glTranslated(-xGuess, -yGuess, -zGuess);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        
        selection.render();
        
        Iterator<PointCube> iter = additionalCubes.iterator();
        while (iter.hasNext())
        {
            ((PointCube)iter.next()).render();
        }
        
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        
        RenderHelper.enableStandardItemLighting();
    }
    
    private void setSelectionPoint(int id, int x, int y, int z)
    {
        selection.setCuboidPoint(id, x, y, z);
    }
    
    private void addAdditionalCube(int x, int y, int z)
    {
        PointCube newcube = new PointCube(x, y, z);
        newcube.setColor(LineColor.CUBOIDBOX);
        additionalCubes.add(newcube);
    }
    
    private void deleteAdditionalCubes()
    {
        additionalCubes.clear();
    }
    
    private void deleteSelection()
    {
        selection.wipePointCubes();
        additionalCubes.clear();
    }
    
    public void onRenderTick(float renderTick)
    {
        if (mc.currentScreen == null && isSelectingMineArea)
        {
            if (mc.thePlayer.inventory.getCurrentItem() == null
            || mc.thePlayer.inventory.getCurrentItem().getItem() != MinionsCore.instance.itemMastersStaff)
            {
                isSelectingMineArea = false;
                deleteSelection();
            }
            else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK)
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
                    setSelectionPoint(0, x, y, z);
                    setSelectionPoint(1, x+4, y, z+4);

                    deleteAdditionalCubes();
                    addAdditionalCube(x+1, y-1, z);
                    addAdditionalCube(x+2, y-2, z);
                    addAdditionalCube(x+3, y-3, z);
                }
                else if (mineAreaShape == 1) // stripmine
                {
                    setSelectionPoint(0, x, y, z);
                    setSelectionPoint(1, x+xDirection*2, y+1, z+zDirection*2);
                }
                else if (mineAreaShape == 2) // custom size
                {
                    int half = ((customSizeXZ-1) / 2);
                    
                    if (xDirection != 0) // advancing in Xdir, start at x and z-half to x+size and z+half
                    {
                        setSelectionPoint(0, x, y, z - half);
                        setSelectionPoint(1, x + (customSizeXZ*xDirection), y + customSizeY-1, z + half);
                    }
                    else if (zDirection != 0) // advancing in Zdir, start at z and x-half to z+size and x+half
                    {
                        setSelectionPoint(0, x - half, y, z);
                        setSelectionPoint(1, x + half, y + customSizeY-1, z + (customSizeXZ*zDirection));
                    }
                }
            }
        }
        else
        {
            deleteSelection();
        }
        
        if (mc.currentScreen == null)
        {
            if (Mouse.isButtonDown(0)
            && mc.thePlayer.inventory.getCurrentItem() != null
            && mc.thePlayer.inventory.getCurrentItem().getItem() == MinionsCore.instance.itemMastersStaff
            && lastStaffLightningBoltTime + 100L < System.currentTimeMillis())
            {
                if (MinionsCore.instance.hasPlayerWillPower(mc.thePlayer))
                {
                    lastStaffLightningBoltTime = System.currentTimeMillis();
                    EntityLivingBase p = mc.renderViewEntity;
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
            
            if (menuKey.getIsKeyPressed())
            {
                mc.displayGuiScreen(new GuiMinionMenu());
            }
        }
    }

    public void onWorldTick(World world)
    {
        if (world != lastWorld && world != null)
        {
            lastWorld = world;
        }
        
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
        	ChickenLightningBolt.update();
        }
    }

    public void onPacketData(int packetid, WrappedPacket packet, EntityPlayer player)
    {
        Minecraft mcinstance = FMLClientHandler.instance().getClient();
        ByteBuf data = packet.data;
        PacketType packetType = PacketType.byID(packetid);
        
        MinionsCore.instance.debugPrint("Client received packet, ID "+packetType);
        
        switch (packetType)
        {
            case HASMINIONS:
            {
                Class<?>[] decodeAs = {Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                hasMinionsSMPOverride = ((Integer)packetReadout[0] == 1);
                hasAllMinionsSMPOverride = ((Integer)packetReadout[1] == 1);
                MinionsCore.instance.debugPrint("Client got status packet, now: hasMinionsSMPOverride = "+hasMinionsSMPOverride+", hasAllMinionsSMPOverride: "+hasAllMinionsSMPOverride);
                break;
            }
            
            case SOUNDTOALL:
            {
                Class<?>[] decodeAs = {Integer.class, String.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                int entID = (Integer) packetReadout[0];
                String sound = (String) packetReadout[1];

                //System.out.println("Minions server demands sound "+sound+" at entity id "+entID);

                Entity temp = null;
                boolean found = false;

                @SuppressWarnings("unchecked")
				Iterator<Entity> iter = mcinstance.theWorld.loadedEntityList.iterator();
                while (iter.hasNext())
                {
                    temp = iter.next();
                    if (temp.getEntityId() == entID)
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
                Class<?>[] decodeAs = {Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                if (MinionsCore.instance.evilDeedXPCost != (Integer)packetReadout[0])
                {
                    MinionsCore.instance.evilDeedXPCost = (Integer)packetReadout[0];

                    if (mcinstance.currentScreen instanceof GuiMinionMenu)
                    {
                        mcinstance.currentScreen = null;

                        if (MinionsCore.instance.evilDeedXPCost != -1)
                        {
                            mcinstance.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Server says you don't have enough XP for Evil Deeds"));
                        }
                        else
                        {
                            mcinstance.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Server says Minions are unobtainable through Evil Deeds here"));
                        }
                    }
                }
                break;
            }
            
            case LIGHTNINGBOLT:
            {
                Class<?>[] decodeAs = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Long.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

                Vector3 start = new Vector3((Double)packetReadout[0], (Double)packetReadout[1], (Double)packetReadout[2]);
                Vector3 end = new Vector3((Double)packetReadout[3], (Double)packetReadout[4], (Double)packetReadout[5]);

                spawnLightningBolt(mcinstance.theWorld, mcinstance.thePlayer, start, end, (Long)packetReadout[6]);
                break;
            }
            
            case ENTITYMOUNTED:
            {
                // System.out("client received mount packet!");
                Class<?>[] decodeAs = {Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                Entity minion = mcinstance.theWorld.getEntityByID((Integer) packetReadout[0]);
                Entity target = mcinstance.theWorld.getEntityByID((Integer) packetReadout[1]);
                // System.out("client packet mountEntity, target "+target+", minion "+minion);
                if (minion != null && target != null)
                {
                    // System.out("executing mount!");
                    target.mountEntity(minion);
                }
                break;
            }
		default:
			break;
        }
    }
    
    private void spawnLightningBolt(World world, EntityLivingBase shooter, Vector3 startvec, Vector3 endvec, long randomizer)
    {
        for (int i = 3; i != 0; i--)
        {
            ChickenLightningBolt bolt = new ChickenLightningBolt(world, startvec, endvec, randomizer);
            bolt.defaultFractal();
            bolt.finalizeBolt();
            bolt.setWrapper(shooter);
            ChickenLightningBolt.boltlist.add(bolt);   
        }
        
        if (timeNextSoundAllowed + timeSoundDelay < System.currentTimeMillis())
        {
            playSoundToAllPlayersOnServer(shooter, "minions:bolt");
            timeNextSoundAllowed = System.currentTimeMillis();
        }
    }
    
    private void playFartSound(World world, EntityPlayer player)
    {
        if (timeNextSoundAllowed + timeSoundDelay*2 < System.currentTimeMillis())
        {
            timeNextSoundAllowed = System.currentTimeMillis();
            playSoundToAllPlayersOnServer(player, "minions:fart");
        }
    }
    
    public void playSoundToAllPlayersOnServer(Entity source, String soundName)
    {
        Object[] toSend = {source.getEntityId(), soundName};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.SOUNDTOALL.ordinal(), toSend)); // client requests sound distribution packet (entID, soundString)
    }
    
    public void onMastersGloveRightClick(ItemStack var1, World worldObj, EntityPlayer playerEnt)
    {
        if (System.currentTimeMillis() > timeNextSoundAllowed)
        {
            // abuse the sound delay to prevent multi-click bugs
            timeNextSoundAllowed = System.currentTimeMillis() + timeSoundDelay;
        }
        else
        {
            return;
        }
        
        Minecraft mcinstance = FMLClientHandler.instance().getClient();
        
        MovingObjectPosition targetObjectMouseOver = mcinstance.renderViewEntity.rayTrace(30.0D, 1.0F);
        // List<EntityMinion> minions = MinionsCore.masterNames.get(playerEnt.getGameProfile().getName());
        MinionsCore.instance.debugPrint("OnMastersGloveRightClick Master: "+playerEnt.getCommandSenderName());
        Entity target;

        if (targetObjectMouseOver == null)
        {
            // NOOP
        }
        else if ((target = targetObjectMouseOver.entityHit) != null)
        {
            if (target instanceof EntityAnimal || target instanceof EntityPlayer)
            {
                Object[] toSend = {playerEnt.getGameProfile().getName(), playerEnt.getEntityId(), target.getEntityId()};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDPICKUPENT.ordinal(), toSend)); // pickup entity command packet
            }
            else if (target instanceof EntityMinion)
            {
                Object[] toSend = {playerEnt.getGameProfile().getName(), playerEnt.getEntityId(), target.getEntityId()};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDDROPALL.ordinal(), toSend)); // minion drop items command packet
            }
        }
        else if (targetObjectMouseOver.typeOfHit == MovingObjectType.BLOCK)
        {
            int x = targetObjectMouseOver.blockX;
            int y = targetObjectMouseOver.blockY +1;
            int z = targetObjectMouseOver.blockZ;
            
            MinionsCore.instance.debugPrint("OnMastersGloveRightClick coordinate mode, ["+x+"|"+y+"|"+z+"]");

            if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y-1, z))
            {
                y--;
            }

            // System.out("OnMastersGloveRightClick hasAllMinionsSMPOverride: "+hasAllMinionsSMPOverride);
            if (!hasAllMinionsSMPOverride)
            {
                Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
                // System.out("Client sending CMDMINIONSPAWN, ["+x+"|"+y+"|"+z+"]");
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMINIONSPAWN.ordinal(), toSend)); // minion spawn command packet
                playerEnt.worldObj.spawnParticle("hugeexplosion", x, y, z, 0.0D, 0.0D, 0.0D);
                return;
            }

            Block ID = worldObj.getBlock(x, y, z);
            TileEntity chestOrInventoryBlock;

            if (MinionsCore.instance.foundTreeBlocks.contains(ID))
            {
                if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                {
                    Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
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
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
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
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
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
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z, customSizeXZ, customSizeY};
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDCUSTOMDIG.ordinal(), toSend)); // custom dig job command packet
                    }
                    else
                    {
                        playFartSound(playerEnt.worldObj, playerEnt);
                        return;
                    }
                }
            }
            else if ((chestOrInventoryBlock = worldObj.getTileEntity(x, y-1, z)) != null
                    && chestOrInventoryBlock instanceof IInventory
                    && ((IInventory)chestOrInventoryBlock).getSizeInventory() >= 24)
            {
                Object[] toSend = {playerEnt.getGameProfile().getName(), playerEnt.isSneaking(), x, y, z};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDASSIGNCHEST.ordinal(), toSend)); // chest assign command packet
            }
            else if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y, z) && hasMinionsSMPOverride)
            {
                // check if player targets his own feet. if so, order minion carry
                if (MathHelper.floor_double(playerEnt.posX) == x
                        && MathHelper.floor_double(playerEnt.posZ) == z
                        && Math.abs(MathHelper.floor_double(playerEnt.posY) - y) < 3)
                {
                    Object[] toSend = {playerEnt.getGameProfile().getName(), playerEnt.getEntityId(), playerEnt.getEntityId()};
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDPICKUPENT.ordinal(), toSend)); // pickup entity command packet
                }
                else
                {
                    Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMOVETO.ordinal(), toSend)); // moveto command packet
                }
            }
            else if (MinionsCore.instance.isBlockValueable(worldObj.getBlock(x, y-1, z)))
            {

                Object[] toSend = {playerEnt.getGameProfile().getName(), x, y, z};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDMINEOREVEIN.ordinal(), toSend)); // mine ore vein command
            }
        }
    }
    
    public static void onMastersGloveRightClickHeld(ItemStack var1, World var2, EntityPlayer var3)
    {
        Object[] toSend = {var3.getGameProfile().getName()};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDFOLLOW.ordinal(), toSend));
    }
    
    public static void requestXPSettingFromServer()
    {
        Object[] toSend = {MinionsCore.instance.evilDeedXPCost};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend));
    }
}
