package atomicstryker.minions.client;

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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.client.gui.GuiMinionMenu;
import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.points.PointCube;
import atomicstryker.minions.client.render.region.CuboidRegion;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.AssignChestPacket;
import atomicstryker.minions.common.network.ChopTreesPacket;
import atomicstryker.minions.common.network.CustomDigPacket;
import atomicstryker.minions.common.network.DigOreVeinPacket;
import atomicstryker.minions.common.network.DigStairwellPacket;
import atomicstryker.minions.common.network.DropAllPacket;
import atomicstryker.minions.common.network.FollowPacket;
import atomicstryker.minions.common.network.LightningPacket;
import atomicstryker.minions.common.network.MinionSpawnPacket;
import atomicstryker.minions.common.network.MovetoPacket;
import atomicstryker.minions.common.network.PickupEntPacket;
import atomicstryker.minions.common.network.SoundPacket;
import atomicstryker.minions.common.network.StripminePacket;

public class MinionsClient
{
    
    public static boolean isSelectingMineArea = false;
    public static int mineAreaShape = 0;
    public static int customSizeXZ = 3;
    public static int customSizeY = 3;

    public static boolean hasMinionsSMPOverride = false;
    public static boolean hasAllMinionsSMPOverride = false;
    
    private long lastStaffLightningBoltTime = System.currentTimeMillis();
    
    private static long timeNextSoundAllowed = 0L;
    private final static long timeSoundDelay = 400L;
    
    private World lastWorld;
    private static Minecraft mc;
    
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
                int x = mc.objectMouseOver.getBlockPos().getX();
                int y = mc.objectMouseOver.getBlockPos().getY();
                int z = mc.objectMouseOver.getBlockPos().getZ();
                
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
                    Entity p = mc.getRenderViewEntity();
                    MovingObjectPosition pos = p.rayTrace(10, renderTick);
                    if (pos != null)
                    {
                        Vector3 startvec = Vector3.fromEntityCenter(p).add(0, 0.68D, 0);
                        startvec.x -= (double)(MathHelper.cos(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        //startvec.y -= 0.10000000149011612D;
                        startvec.z -= (double)(MathHelper.sin(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        
                        Vector3 endvec = Vector3.fromVec3(pos.hitVec);
                        
                        MinionsCore.instance.networkHelper.sendPacketToServer(
                                new LightningPacket(mc.thePlayer.getCommandSenderName(), startvec.x, startvec.y, startvec.z, endvec.x, endvec.y, endvec.z));
                    }
                }
                else
                {
                    playFartSound(mc.theWorld, mc.thePlayer);
                }
            }
            
            if (menuKey.isKeyDown())
            {
                mc.displayGuiScreen(new GuiMinionMenu());
            }
        }
    }

    public void onPlayerTick(World world)
    {
        if (world != lastWorld && world != null)
        {
            lastWorld = world;
        }
        
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            // this prevents double updates on local play
            ChickenLightningBolt.update();
        }
    }
    
    public static void spawnLightningBolt(Vector3 start, Vector3 end)
    {
        spawnLightningBolt(mc.theWorld, mc.thePlayer, start, end, mc.theWorld.rand.nextLong());
    }
    
    private static void spawnLightningBolt(World world, EntityLivingBase shooter, Vector3 startvec, Vector3 endvec, long randomizer)
    {
        for (int i = 3; i != 0; i--)
        {
            ChickenLightningBolt bolt = new ChickenLightningBolt(world, startvec, endvec, randomizer);
            bolt.defaultFractal();
            bolt.finalizeBolt();
            bolt.setWrapper(shooter);
            ChickenLightningBolt.offerBolt(bolt);   
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
    
    public static void playSoundToAllPlayersOnServer(Entity source, String soundName)
    {
        MinionsCore.instance.networkHelper.sendPacketToServer(new SoundPacket(soundName, source.dimension, source.getEntityId()));
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
        
        // this raytrace does not hit entities since 1.7!
        MovingObjectPosition targetObjectMouseOver = mcinstance.getRenderViewEntity().rayTrace(30.0D, 1.0F);
        // List<EntityMinion> minions = MinionsCore.masterNames.get(playerEnt.getGameProfile().getCommandSenderName());
        MinionsCore.debugPrint("OnMastersGloveRightClick Master: "+playerEnt.getCommandSenderName());
        
        if (targetObjectMouseOver == null)
        {
            return;
        }
        
        // have to use the mc object to find entities, short ranged this is
        Entity target = mcinstance.objectMouseOver.entityHit;
        if (target != null)
        {
            MinionsCore.debugPrint("OnMastersGloveRightClick hit entity "+target);
            if (target instanceof EntityAnimal || target instanceof EntityPlayer)
            {
                MinionsCore.debugPrint("OnMastersGloveRightClick -> PickupEntPacket");
                MinionsCore.instance.networkHelper.sendPacketToServer(new PickupEntPacket(playerEnt.getCommandSenderName(), target.getEntityId()));
            }
            else if (target instanceof EntityMinion)
            {
                MinionsCore.debugPrint("OnMastersGloveRightClick -> DropAllPacket");
                MinionsCore.instance.networkHelper.sendPacketToServer(new DropAllPacket(playerEnt.getCommandSenderName(), target.getEntityId()));
            }
        }
        else if (targetObjectMouseOver.typeOfHit == MovingObjectType.BLOCK)
        {
            int x = targetObjectMouseOver.getBlockPos().getX();
            int y = targetObjectMouseOver.getBlockPos().getY() +1;
            int z = targetObjectMouseOver.getBlockPos().getZ();
            
            MinionsCore.debugPrint("OnMastersGloveRightClick coordinate mode, ["+x+"|"+y+"|"+z+"]");

            if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y-1, z))
            {
                y--;
            }

            // System.out("OnMastersGloveRightClick hasAllMinionsSMPOverride: "+hasAllMinionsSMPOverride);
            if (!hasAllMinionsSMPOverride)
            {
                MinionsCore.instance.networkHelper.sendPacketToServer(new MinionSpawnPacket(playerEnt.getCommandSenderName(), x, y, z));
                playerEnt.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x, y, z, 0.0D, 0.0D, 0.0D);
                return;
            }

            Block ID = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
            TileEntity chestOrInventoryBlock;

            if (MinionsCore.instance.foundTreeBlocks.contains(ID))
            {
                if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new ChopTreesPacket(playerEnt.getCommandSenderName(), x, y, z));
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
                        MinionsCore.instance.networkHelper.sendPacketToServer(new DigStairwellPacket(playerEnt.getCommandSenderName(), x, y, z));
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
                        MinionsCore.instance.networkHelper.sendPacketToServer(new StripminePacket(playerEnt.getCommandSenderName(), x, y, z));
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
                        MinionsCore.instance.networkHelper.sendPacketToServer(new CustomDigPacket(playerEnt.getCommandSenderName(), x, y, z, customSizeXZ, customSizeY));
                    }
                    else
                    {
                        playFartSound(playerEnt.worldObj, playerEnt);
                        return;
                    }
                }
            }
            else if ((chestOrInventoryBlock = worldObj.getTileEntity(new BlockPos(x, y-1, z))) != null
                    && chestOrInventoryBlock instanceof IInventory
                    && ((IInventory)chestOrInventoryBlock).getSizeInventory() >= 24)
            {
                MinionsCore.instance.networkHelper.sendPacketToServer(new AssignChestPacket(playerEnt.getCommandSenderName(), playerEnt.isSneaking(), x, y, z));
            }
            else if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y, z) && hasMinionsSMPOverride)
            {
                // check if player targets his own feet. if so, order minion carry
                if (MathHelper.floor_double(playerEnt.posX) == x
                        && MathHelper.floor_double(playerEnt.posZ) == z
                        && Math.abs(MathHelper.floor_double(playerEnt.posY) - y) < 3)
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new PickupEntPacket(playerEnt.getCommandSenderName(), playerEnt.getEntityId()));
                }
                else
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new MovetoPacket(playerEnt.getCommandSenderName(), x, y, z));
                }
            }
            else if (MinionsCore.instance.isBlockValueable(worldObj.getBlockState(new BlockPos(x, y-1, z)).getBlock()))
            {
                MinionsCore.instance.networkHelper.sendPacketToServer(new DigOreVeinPacket(playerEnt.getCommandSenderName(), x, y, z));
            }
        }
    }
    
    public static void onMastersGloveRightClickHeld(ItemStack var1, World var2, EntityPlayer var3)
    {
        MinionsCore.instance.networkHelper.sendPacketToServer(new FollowPacket(var3.getCommandSenderName()));
    }

    public static void onChangedXPSetting()
    {
        if (mc.currentScreen instanceof GuiMinionMenu)
        {
            mc.currentScreen = null;

            if (MinionsCore.instance.evilDeedXPCost != -1)
            {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Server says you don't have enough XP for Evil Deeds"));
            }
            else
            {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Server says Minions are unobtainable through Evil Deeds here"));
            }
        }
    }

    public static void onMinionMountPacket(int minionID, int targetID)
    {
        Entity minion = mc.theWorld.getEntityByID(minionID);
        Entity target = mc.theWorld.getEntityByID(targetID);
        if (minion != null && target != null)
        {
            target.mountEntity(minion);
        }
    }
    
}
