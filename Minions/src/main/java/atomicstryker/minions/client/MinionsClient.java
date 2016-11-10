package atomicstryker.minions.client;

import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.client.gui.GuiMinionMenu;
import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.points.PointCube;
import atomicstryker.minions.client.render.region.CuboidRegion;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

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
    private static ArrayList<PointCube> additionalCubes = new ArrayList<>();
    
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
            renderSelections(event.getPartialTicks());
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

        additionalCubes.forEach(PointCube::render);
        
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
            else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
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
                    else
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
                    RayTraceResult pos = p.rayTrace(10, renderTick);
                    if (pos != null)
                    {
                        Vector3 startvec = Vector3.fromEntityCenter(p).add(0, 0.68D, 0);
                        startvec.x -= (double)(MathHelper.cos(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        //startvec.y -= 0.10000000149011612D;
                        startvec.z -= (double)(MathHelper.sin(p.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
                        
                        Vector3 endvec = Vector3.fromVec3(pos.hitVec);
                        
                        MinionsCore.instance.networkHelper.sendPacketToServer(
                                new LightningPacket(mc.thePlayer.getName(), startvec.x, startvec.y, startvec.z, endvec.x, endvec.y, endvec.z));
                    }
                }
                else
                {
                    playFartSound(mc.thePlayer);
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
    
    private void playFartSound(EntityPlayer player)
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
    
    public void onMastersGloveRightClick(World worldObj, EntityPlayer playerEnt)
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
        RayTraceResult targetObjectMouseOver = mcinstance.getRenderViewEntity().rayTrace(30.0D, 1.0F);
        // List<EntityMinion> minions = MinionsCore.masterNames.get(playerEnt.getGameProfile().getName());
        MinionsCore.debugPrint("OnMastersGloveRightClick Master: "+playerEnt.getName());
        
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
                MinionsCore.instance.networkHelper.sendPacketToServer(new PickupEntPacket(playerEnt.getName(), target.getEntityId()));
            }
            else if (target instanceof EntityMinion)
            {
                MinionsCore.debugPrint("OnMastersGloveRightClick -> DropAllPacket");
                MinionsCore.instance.networkHelper.sendPacketToServer(new DropAllPacket(playerEnt.getName(), target.getEntityId()));
            }
        }
        else if (targetObjectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
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
                MinionsCore.instance.networkHelper.sendPacketToServer(new MinionSpawnPacket(playerEnt.getName(), x, y, z));
                playerEnt.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x, y, z, 0.0D, 0.0D, 0.0D);
                return;
            }

            Block ID = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
            TileEntity chestOrInventoryBlock;

            if (MinionsCore.instance.foundTreeBlocks.contains(ID))
            {
                if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new ChopTreesPacket(playerEnt.getName(), x, y, z));
                }
                else
                {
                    playFartSound(playerEnt);
                }
            }
            else if (isSelectingMineArea)
            {
                isSelectingMineArea = false;
                if (mineAreaShape == 0)
                {
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        MinionsCore.instance.networkHelper.sendPacketToServer(new DigStairwellPacket(playerEnt.getName(), x, y, z));
                    }
                    else
                    {
                        playFartSound(playerEnt);
                    }
                }
                else if (mineAreaShape == 1)
                {
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        MinionsCore.instance.networkHelper.sendPacketToServer(new StripminePacket(playerEnt.getName(), x, y, z));
                    }
                    else
                    {
                        playFartSound(playerEnt);
                    }
                }
                else if (mineAreaShape == 2)
                {
                    if (MinionsCore.instance.hasPlayerWillPower(playerEnt))
                    {
                        MinionsCore.instance.networkHelper.sendPacketToServer(new CustomDigPacket(playerEnt.getName(), x, y, z, customSizeXZ, customSizeY));
                    }
                    else
                    {
                        playFartSound(playerEnt);
                    }
                }
            }
            else if ((chestOrInventoryBlock = worldObj.getTileEntity(new BlockPos(x, y-1, z))) != null
                    && chestOrInventoryBlock instanceof IInventory
                    && ((IInventory)chestOrInventoryBlock).getSizeInventory() >= 24)
            {
                MinionsCore.instance.networkHelper.sendPacketToServer(new AssignChestPacket(playerEnt.getName(), playerEnt.isSneaking(), x, y, z));
            }
            else if (AStarStatic.isPassableBlock(playerEnt.worldObj, x, y, z) && hasMinionsSMPOverride)
            {
                // check if player targets his own feet. if so, order minion carry
                if (MathHelper.floor_double(playerEnt.posX) == x
                        && MathHelper.floor_double(playerEnt.posZ) == z
                        && Math.abs(MathHelper.floor_double(playerEnt.posY) - y) < 3)
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new PickupEntPacket(playerEnt.getName(), playerEnt.getEntityId()));
                }
                else
                {
                    MinionsCore.instance.networkHelper.sendPacketToServer(new MovetoPacket(playerEnt.getName(), x, y, z));
                }
            }
            else if (MinionsCore.instance.isBlockValueable(worldObj.getBlockState(new BlockPos(x, y-1, z)).getBlock()))
            {
                MinionsCore.instance.networkHelper.sendPacketToServer(new DigOreVeinPacket(playerEnt.getName(), x, y, z));
            }
        }
    }
    
    public static void onMastersGloveRightClickHeld(EntityPlayer var3)
    {
        MinionsCore.instance.networkHelper.sendPacketToServer(new FollowPacket(var3.getName()));
    }

    public static void onChangedXPSetting()
    {
        if (mc.currentScreen instanceof GuiMinionMenu)
        {
            mc.currentScreen = null;

            if (MinionsCore.instance.evilDeedXPCost != -1)
            {
                mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Server says you don't have enough XP for Evil Deeds"));
            }
            else
            {
                mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Server says Minions are unobtainable through Evil Deeds here"));
            }
        }
    }

    public static void onMinionMountPacket(int minionID, int targetID)
    {
        Entity minion = mc.theWorld.getEntityByID(minionID);
        Entity target = mc.theWorld.getEntityByID(targetID);
        if (minion != null && target != null)
        {
            target.startRiding(minion);
        }
    }
    
}
