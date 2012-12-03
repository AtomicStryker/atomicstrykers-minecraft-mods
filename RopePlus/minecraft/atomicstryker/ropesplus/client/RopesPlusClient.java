package atomicstryker.ropesplus.client;

import java.io.File;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Render;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.ropesplus.common.CommonProxy;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.EntityGrapplingHook;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.TickRegistry;

public class RopesPlusClient extends CommonProxy implements ITickHandler
{
    
    private EntityArrow303 selectedArrow;
    private static int arrowCount;
    private int selectedSlot;
    private boolean cycled;
    private static EntityPlayer localPlayer;
    private GuiScreen prevScreen;
    
    private static int keyforward;
    private static int keyback;
    private String keyNameForward;
    private String keyNameBackward;
    
    private int countDownToArrowCount;
    
    public static int renderIDGrapplingHook;
    public static boolean grapplingHookOut;
    
    private boolean letGoOfHookShot;
    private boolean pulledByHookShot;
    
    private static EntityFreeFormRope onZipLine;
    private static float lastZipLineLength;
    private static long timeNextZipUpdate;
    private static int zipTicker;
    
    public RopesPlusClient()
    {
        tickTypes = EnumSet.of(TickType.RENDER);
        
        selectedArrow = null;
        arrowCount = -1;
        selectedSlot = 0;
        cycled = false;
        localPlayer = null;
        prevScreen = null;
        countDownToArrowCount = 100;
        grapplingHookOut = false;
        letGoOfHookShot = false;
        pulledByHookShot = false;
        onZipLine = null;
        lastZipLineLength = 0;
        timeNextZipUpdate = 0;

        keyforward = Keyboard.getKeyIndex("COMMA");
        keyback = Keyboard.getKeyIndex("PERIOD");
        keyNameForward = "COMMA";
        keyNameBackward = "PERIOD";

        renderIDGrapplingHook = RenderingRegistry.getNextAvailableRenderId();
    }
    
    @Override
    public void loadConfig(File configFile)
    {
        Configuration config = new Configuration(configFile);
        config.load();
        
        keyforward = Keyboard.getKeyIndex(config.get(config.CATEGORY_GENERAL, "keyforward", "COMMA").value);
        keyback = Keyboard.getKeyIndex(config.get(config.CATEGORY_GENERAL, "keyback", "PERIOD").value);
        
        config.save();
        
        MinecraftForge.EVENT_BUS.register(new RopesPlusSounds());
    }
    
    @Override
    public void load()
    {        
        RenderingRegistry.registerEntityRenderingHandler(EntityGrapplingHook.class, new RenderGrapplingHook());
        Render arrowRenderer = new RenderArrow303();
        for(Class arrow : RopesPlusCore.coreArrowClasses)
        {
            RenderingRegistry.registerEntityRenderingHandler(arrow, arrowRenderer);
        }
        
        RenderingRegistry.registerBlockHandler(BlockRenderHandler.instance.new BlockGrapplingHookRenderHandler());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityFreeFormRope.class, new RenderFreeFormRope());
        
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropesPlusBlocks.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropesPlusItems.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/itemGrapplingHookThrown.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/ropesplus/client/ropeSegment.png");
        
        TickRegistry.registerTickHandler(new RopesPlusClient(), Side.CLIENT);
    }
    
    private void selectArrow()
    {
        if(localPlayer == null)
        {
            selectedArrow = null;
            selectedSlot = 0;
            return;
        }
        findNextArrow(true);
        if(selectedArrow == null)
        {
            cycle(true);
        }
    }

    private void findNextArrow(boolean flag)
    {
        EntityArrow303 entityarrow303 = selectedArrow;
        int i = selectedSlot;
        findNextArrowBetween(entityarrow303, i, 1, localPlayer.inventory.mainInventory.length, flag);
        if(selectedArrow == null)
        {
            findNextArrowBetween(entityarrow303, 0, 1, i, flag);
        }
    }

    private void findPrevArrow()
    {
        EntityArrow303 entityarrow303 = selectedArrow;
        int i = selectedSlot;
        findNextArrowBetween(entityarrow303, i, -1, -1, false);
        if(selectedArrow == null)
        {
            findNextArrowBetween(entityarrow303, localPlayer.inventory.mainInventory.length - 1, -1, i + 1, false);
        }
    }

    private void findNextArrowBetween(EntityArrow303 previousarrow303, int i, int j, int k, boolean dontKeepArrowType)
    {
        for(int l = i; j > 0 && l < k || l > k; l += j)
        {
            ItemStack itemstack = localPlayer.inventory.mainInventory[l];
            if(itemstack == null)
            {
                continue;
            }
            Item item = itemstack.getItem();
            
            if(item == null)
            {
                continue;
            }
            
            if (!(item instanceof ItemArrow303) && !(item.shiftedIndex == Item.arrow.shiftedIndex))
            {               
                continue;
            }
            
            if (item.shiftedIndex == Item.arrow.shiftedIndex)
            {
                EntityArrow303 itemarrow303 = new EntityArrow303(localPlayer.worldObj);
                if(previousarrow303 == null || previousarrow303.tip != itemarrow303.tip)
                {
                    selectedArrow = itemarrow303;
                    selectedSlot = l;
                    sendPacketToUpdateArrowChoice();
                    return;
                }
                continue;
            }
            
            ItemArrow303 itemarrow303 = (ItemArrow303)item;
            if(previousarrow303 == null || dontKeepArrowType && itemarrow303.arrow == previousarrow303 || !dontKeepArrowType && itemarrow303.arrow != previousarrow303)
            {
                selectedArrow = itemarrow303.arrow;
                selectedSlot = l;
                sendPacketToUpdateArrowChoice();
                return;
            }
        }
        selectedArrow = null;
        selectedSlot = 0;
    }

    private int countArrows(EntityArrow303 entityarrow303)
    {
        int i = 0;
        InventoryPlayer inventoryplayer = localPlayer.inventory;
        ItemStack aitemstack[] = inventoryplayer.mainInventory;
        int j = aitemstack.length;
        for(int k = 0; k < j; k++)
        {
            ItemStack itemstack = aitemstack[k];
            if(itemstack != null && itemstack.itemID == entityarrow303.itemId)
            {
                i += itemstack.stackSize;
            }
        }

        return i;
    }

    private void sendPacketToUpdateArrowChoice()
    {
        arrowCount = -1;
        Object[] toSend = {selectedSlot};
        FMLClientHandler.instance().sendPacket(ForgePacketWrapper.createPacket("AS_Ropes", 1, toSend));
    }

    private void cycle(boolean directionForward)
    {
        EntityArrow303 previousarrow303 = selectedArrow;
        int i = selectedSlot;
        if(directionForward)
        {
            findNextArrow(false);
        }
        else
        {
            findPrevArrow();
        }
        
        ItemStack itemstack;
        Item item;
        
        if(selectedArrow == null
        && previousarrow303 != null
        && (itemstack = localPlayer.inventory.mainInventory[i]) != null
        && ((item = itemstack.getItem()) instanceof ItemArrow303)
        && ((ItemArrow303)item).arrow == previousarrow303)
        {
            selectedArrow = previousarrow303;
            selectedSlot = i;
            System.out.println("client cycle");
            sendPacketToUpdateArrowChoice();
        }
    }
    
    /*========= ONTICK =============*/
    
    private final EnumSet tickTypes;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        
        if(localPlayer != mc.thePlayer || prevScreen != mc.currentScreen)
        {
            localPlayer = mc.thePlayer;
            prevScreen = mc.currentScreen;
            selectArrow();
        }
        
        if(mc.currentScreen == null)
        {
            ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();
            if(itemstack != null && (itemstack.itemID == Item.bow.shiftedIndex || itemstack.itemID == RopesPlusCore.bowRopesPlus.shiftedIndex))
            {
                boolean pressingForward = Keyboard.isKeyDown(keyforward);
                boolean pressingBackward = Keyboard.isKeyDown(keyback);
                if(cycled)
                {
                    if(!pressingForward && !pressingBackward)
                    {
                        cycled = false;
                    }
                }
                else
                {
                    cycled = true;
                    if(pressingForward)
                    {
                        cycle(false);
                    }
                    else if(pressingBackward)
                    {
                        cycle(true);
                    }
                    else
                    {
                        cycled = false;
                    }
                }
                if(selectedArrow == null)
                {
                    return;
                }
                
                String s = selectedArrow.name;
                if (arrowCount == -1 || countDownToArrowCount-- < 0)
                {
                    countDownToArrowCount = 100;
                    arrowCount = countArrows(selectedArrow);
                }
                s = (new StringBuilder()).append(s).append("x").append(arrowCount).toString();
                mc.fontRenderer.drawStringWithShadow(s, 2, 10, 0x2F96EB);
                mc.fontRenderer.drawStringWithShadow("Swap arrows with "+keyNameForward+", "+keyNameBackward, 2, 20, 0xffffff);
            }
        }
        
        if (onZipLine != null)
        {
            if (mc.gameSettings.keyBindUseItem.pressed && lastZipLineLength > 0.2)
            {
                Object[] toSend = { onZipLine.entityId, lastZipLineLength };
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_Ropes", 7, toSend));
                onZipLine = null;
            }
            else if (System.currentTimeMillis() > timeNextZipUpdate)
            {
                double startCoords[] = onZipLine.getCoordsAtRelativeLength(lastZipLineLength);
                localPlayer.setPositionAndUpdate(startCoords[0], startCoords[1]-2.5D, startCoords[2]);
                localPlayer.setVelocity(0, 0, 0);
                localPlayer.fallDistance = 0f;
                lastZipLineLength += 0.025;
                timeNextZipUpdate = System.currentTimeMillis() + 50L;
                
                if (++zipTicker == 10)
                {
                    zipTicker = 0;
                    Object[] toSend = { onZipLine.entityId, lastZipLineLength };
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_Ropes", 7, toSend));
                }
                
                if (lastZipLineLength > .9F)
                {
                    onZipLine = null;
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
        return "RopesPlusClient";
    }
    
    public static void onAffixedToHookShotRope(int ropeEntID)
    {
        if (localPlayer != null && localPlayer.worldObj != null)
        {
            Entity ent = localPlayer.worldObj.getEntityByID(ropeEntID);
            if (ent != null && ent instanceof EntityFreeFormRope)
            {
                ((EntityFreeFormRope)ent).setShooter(localPlayer);
            }
        }
    }
    
    public static void onUsedZipLine(int ropeEntID)
    {
        if (localPlayer != null && localPlayer.worldObj != null)
        {
            Entity ent = localPlayer.worldObj.getEntityByID(ropeEntID);
            if (ent != null && ent instanceof EntityFreeFormRope)
            {
                onZipLine = (EntityFreeFormRope) ent;
                lastZipLineLength = 0;
                timeNextZipUpdate = System.currentTimeMillis();
            }
        }
    }
    
    @Override
    public boolean getShouldHookShotDisconnect()
    {
        return letGoOfHookShot;
    }

    @Override
    public void setShouldHookShotDisconnect(boolean b)
    {
        letGoOfHookShot = b;
    }
    
    @Override
    public boolean getShouldHookShotPull()
    {
        return pulledByHookShot;
    }

    @Override
    public void setShouldHookShotPull(boolean b)
    {
        pulledByHookShot = b;
    }
    
    @Override
    public int getGrapplingHookRenderId()
    {
        return renderIDGrapplingHook;
    }
    
}
