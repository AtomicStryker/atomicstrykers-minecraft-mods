package atomicstryker.ropesplus.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.KeyBinding;

import org.lwjgl.input.Keyboard;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class RopesPlusClient implements ITickHandler
{
    
    private Minecraft mc;
    private EntityArrow303 selectedArrow;
    private static int arrowCount;
    private int selectedSlot;
    private static EntityPlayer localPlayer;
    private GuiScreen prevScreen;
    private ItemStack prevItem;
    
    private int countDownToArrowCount;
    
    public static boolean grapplingHookOut;
    public static int renderIDGrapplingHook;
    
    private static EntityFreeFormRope onZipLine;
    private static float lastZipLineLength;
    private static long timeNextZipUpdate;
    private static int zipTicker;
    
    public RopesPlusClient()
    {
        mc = FMLClientHandler.instance().getClient();
        tickTypes = EnumSet.of(TickType.RENDER);
        
        selectedArrow = null;
        arrowCount = -1;
        selectedSlot = 0;
        localPlayer = null;
        prevScreen = null;
        prevItem = null;
        countDownToArrowCount = 100;
        onZipLine = null;
        lastZipLineLength = 0;
        timeNextZipUpdate = 0;
        
        boolean[] repeat = {false};
        KeyBinding[] keyf = {new KeyBinding("SwapArrowsForward", Keyboard.KEY_COMMA)};
        KeyBindingRegistry.registerKeyBinding(new KeySwapArrowsForward(keyf, repeat));
        KeyBinding[] keyb = {new KeyBinding("SwapArrowsBackward", Keyboard.KEY_PERIOD)};
        KeyBindingRegistry.registerKeyBinding(new KeySwapArrowsBackward(keyb, repeat));

        renderIDGrapplingHook = RenderingRegistry.getNextAvailableRenderId();
    }
    
    private void selectAnyArrow()
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

    private void findNextArrow(boolean keepArrowType)
    {
        EntityArrow303 entityarrow303 = selectedArrow;
        int i = selectedSlot;
        findNextArrow(entityarrow303, 1, keepArrowType);
    }

    private void findPrevArrow()
    {
        EntityArrow303 entityarrow303 = selectedArrow;
        int i = selectedSlot;
        findNextArrow(entityarrow303, -1, false);
    }
    
    /**
     * Iterates forward or backward through the player inventory until a full cycle is done and
     * no other arrow could be found, or sets the selectedSlot to the newly found arrow and
     * propagates the update. Resumes from the other end of the inventory array when hitting it's boundaries.
     * 
     * @param previousarrow303 previously selected EntityArrow303
     * @param indexProgress how to iterate through the inventory
     * @param keepArrowType true if the new arrow type must match the old one, false if it must differ
     */
    private void findNextArrow(EntityArrow303 previousarrow303, int indexProgress, boolean keepArrowType)
    {
        int prevSlot = selectedSlot;
        int newSlot = keepArrowType ? selectedSlot : selectedSlot+indexProgress;
        
        int iterations = 0;
        while (iterations++ < localPlayer.inventory.mainInventory.length)
        {
            if (newSlot < 0)
            {
                newSlot = localPlayer.inventory.mainInventory.length-1;
            }
            else if (newSlot >= localPlayer.inventory.mainInventory.length)
            {
                newSlot = 0;
            }
            
            ItemStack itemstack = localPlayer.inventory.mainInventory[newSlot];
            if(itemstack == null)
            {
                newSlot += indexProgress;
                continue;
            }
            
            Item item = itemstack.getItem();
            
            // handle vanilla arrows
            if (item.shiftedIndex == Item.arrow.shiftedIndex)
            {
                EntityArrow303 itemarrow303 = new EntityArrow303(localPlayer.worldObj);
                if(previousarrow303 == null || previousarrow303.tip != itemarrow303.tip)
                {
                    selectedArrow = itemarrow303;
                    selectedSlot = newSlot;
                    sendPacketToUpdateArrowChoice();
                    return;
                }
                
                newSlot += indexProgress;
                continue;
            }
            
            // handle ropes+ arrows
            if (item == null || (!(item instanceof ItemArrow303)))
            {
                newSlot += indexProgress;
                continue;
            }
            
            ItemArrow303 itemarrow303 = (ItemArrow303)item;
            if(previousarrow303 == null || keepArrowType && itemarrow303.arrow == previousarrow303 || !keepArrowType && itemarrow303.arrow != previousarrow303)
            {
                selectedArrow = itemarrow303.arrow;
                selectedSlot = newSlot;
                sendPacketToUpdateArrowChoice();
                return;
            }
        }
        
        // failure to find anything!
        selectedArrow = null;
        selectedSlot = 0;
    }

    private int countArrows(EntityArrow303 entityarrow303)
    {
        int count = 0;
        InventoryPlayer inventoryplayer = localPlayer.inventory;
        ItemStack aitemstack[] = inventoryplayer.mainInventory;
        for(int k = 0; k < aitemstack.length; k++)
        {
            ItemStack itemstack = aitemstack[k];
            if(itemstack != null && itemstack.itemID == entityarrow303.itemId)
            {
                count += itemstack.stackSize;
            }
        }

        return count;
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
            selectAnyArrow();
        }
        
        if(mc.currentScreen == null)
        {
            ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();
            if (itemstack != prevItem)
            {
                prevItem = itemstack;
                selectAnyArrow();
            }
            
            if(itemstack != null && (itemstack.itemID == Item.bow.shiftedIndex || itemstack.itemID == RopesPlusCore.bowRopesPlus.shiftedIndex))
            {
                boolean hasArrows = selectedArrow != null;
                String s = hasArrows ? selectedArrow.name : "No arrows";
                if (hasArrows)
                {
                    if (arrowCount == -1 || countDownToArrowCount-- < 0)
                    {
                        countDownToArrowCount = 100;
                        arrowCount = countArrows(selectedArrow);
                    }
                    s = s.concat("x"+arrowCount);
                }
                mc.fontRenderer.drawStringWithShadow(s, 2, 10, 0x2F96EB);
                mc.fontRenderer.drawStringWithShadow("See control options for swap buttons", 2, 20, 0xffffff);
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
    
    private class KeySwapArrowsForward extends KeyHandler
    {
        private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
        
        public KeySwapArrowsForward(KeyBinding[] keyBindings, boolean[] repeatings)
        {
            super(keyBindings, repeatings);
        }

        @Override
        public String getLabel()
        {
            return "SwapArrowsForward";
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
        {
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
        {
            if (tickEnd && mc.currentScreen == null)
            {
                cycle(false);
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }
    }
    
    private class KeySwapArrowsBackward extends KeyHandler
    {
        private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
        
        public KeySwapArrowsBackward(KeyBinding[] keyBindings, boolean[] repeatings)
        {
            super(keyBindings, repeatings);
        }

        @Override
        public String getLabel()
        {
            return "SwapArrowsBackward";
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
        {
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
        {
            if (tickEnd && mc.currentScreen == null)
            {
                cycle(true);
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }
    }
    
}
