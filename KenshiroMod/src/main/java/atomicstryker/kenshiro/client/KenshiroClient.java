package atomicstryker.kenshiro.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.EntityCrit2FX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import org.lwjgl.input.Mouse;

import atomicstryker.kenshiro.common.KenshiroMod;
import atomicstryker.kenshiro.common.network.AnimationPacket;
import atomicstryker.kenshiro.common.network.BlockPunchedPacket;
import atomicstryker.kenshiro.common.network.EntityKickedPacket;
import atomicstryker.kenshiro.common.network.EntityPunchedPacket;
import atomicstryker.kenshiro.common.network.KenshiroStatePacket;
import atomicstryker.kenshiro.common.network.SoundPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class KenshiroClient
{
	private final boolean DEBUGMODE = false;
	
	private Minecraft minecraft;
	private EntityPlayer entPlayer;
    private MovingObjectPosition mouseTargetObject;
    
    private boolean hasServerKenshiroInstalled;
	
	private boolean canKenshiro = false;
	private boolean chargingSound = false;
	private boolean triggeredKenshiro = false;
	private boolean triggeredKick = false;
	private long kickTime = 0L;
	private long triggerTime = 0L;
	private long smashTime = 0L;
	private long fxTime = 0L;
	private long shindeiruTime = 0L;
	
	private Set<Entity> entitesHit;
	
	private static KenshiroClient instance;
	
	private final RenderHookKenshiro renderHook;
	
	public KenshiroClient()
	{
	    instance = this;
	    entitesHit = new HashSet<Entity>();
	    hasServerKenshiroInstalled = false;
	    minecraft = FMLClientHandler.instance().getClient();
	    renderHook = new RenderHookKenshiro();
	}
	
	public static KenshiroClient instance()
	{
	    return instance;
	}
    
    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        entPlayer = minecraft.thePlayer;
        
        if (!hasServerKenshiroInstalled || entPlayer == null)
        {
            return;
        }
        
        renderHook.render(tick.renderTickTime);
        
        canKenshiro = false;
        
        long currtime = System.currentTimeMillis();
        
        mouseTargetObject = minecraft.objectMouseOver;

        if (triggeredKenshiro)
        {
            if (minecraft.currentScreen != null)
            {
                minecraft.func_147108_a((GuiScreen)null);
            }
            
            entPlayer.isSwingInProgress = true;
            
            if (currtime > fxTime+300L)
            {
                fxTime = currtime;
                sendAnimationPacketToServer(ANIMATION_SWING);
                sendAnimationPacketToServer(ANIMATION_CRITMAGIC_FX);
            }
            
            if (mouseTargetObject != null)
            {
                if(mouseTargetObject.typeOfHit == MovingObjectType.BLOCK)
                {
                    int x = mouseTargetObject.blockX;
                    int y = mouseTargetObject.blockY;
                    int z = mouseTargetObject.blockZ;
                    
                    Block blockID = minecraft.theWorld.func_147439_a(x, y, z);
                    int metadata = minecraft.theWorld.getBlockMetadata(x, y, z);
                    float hardness = 0F;
                    if (blockID != Blocks.air)
                    {
                        hardness = blockID.func_149712_f(minecraft.theWorld, x, y, z);
                    }
                    
                    if (((hardness <= 3.0F && hardness >= 0F) || blockID == Blocks.log || blockID == Blocks.log2 || blockID == Blocks.web)
                    && currtime > smashTime+250L)
                    {
                        sendPacketToServerHasDestroyedBlock(x, y, z);
                        
                        smashTime = currtime;
                        minecraft.playerController.onPlayerDestroyBlock(x, y, z, metadata);
                        blockID.func_149636_a(minecraft.theWorld, minecraft.thePlayer, x, y, z, metadata);
                    }
                }
                else if(mouseTargetObject.typeOfHit == MovingObjectType.ENTITY)
                {
                    Entity ent = mouseTargetObject.entityHit;
                    
                    if (ent instanceof EntityLivingBase)
                    {
                        if (!entitesHit.contains(ent))
                        {
                            sendPacketToServerHasPunchedEntity(ent);
                            entitesHit.add(ent);
                        }
                        else if (currtime > smashTime+350L)
                        {
                            sendPacketToServerHasPunchedEntity(ent);
                            smashTime = currtime;
                        }
                    }
                }
            }
            
            if (entPlayer.inventory.mainInventory[entPlayer.inventory.currentItem] != null)
            {
                entPlayer.inventory.currentItem = 9;
            }
            
            if (currtime > triggerTime+3700L)
            {
                triggeredKenshiro = false;
                triggerTime = 0L;
                chargingSound = false;
                
                DebugPrint("Kenshiro ended!");
                
                if (!entitesHit.isEmpty())
                {
                    shindeiruTime = currtime;
                    if (entPlayer.getRNG().nextInt(3) == 0)
                    {
                        sendSoundPacketToServer("kenshiroshindeiru", (int)entPlayer.posX, (int)entPlayer.posY, (int)entPlayer.posZ);
                        minecraft.ingameGUI.func_146158_b().func_146227_a(new ChatComponentText("[You are already dead.]"));
                    }
                    else
                    {
                        sendSoundPacketToServer("kenshiroheartbeat", (int)entPlayer.posX, (int)entPlayer.posY, (int)entPlayer.posZ);
                    }
                }
            }
        }
        
        if (shindeiruTime != 0L && currtime > shindeiruTime + 3500L)
        {
            sendPacketToServerHasFinishedKenshiro();
            shindeiruTime = 0L;         
            entitesHit.clear();
        }
        
        if (kickTime != 0L
        && currtime > kickTime + 1000L)
        {
            kickTime = 0L;
            triggeredKick = false;
        }
        
        if (!triggeredKenshiro
        && !triggeredKick
        && entPlayer != null
        && entPlayer.getFoodStats().getFoodLevel() >= 10
        && entPlayer.inventory.currentItem < entPlayer.inventory.mainInventory.length // Battlegears fix
        && entPlayer.inventory.mainInventory[entPlayer.inventory.currentItem] == null // bare hands
        && entPlayer.inventory.armorInventory[2] == null // bare chest
        && minecraft.currentScreen == null) 
        {
            canKenshiro = true;
        }
        
        if (canKenshiro)
        {
            if (Mouse.isButtonDown(1))
            {
                if (!triggeredKick
                    && !entPlayer.onGround
                    && mouseTargetObject != null
                    && mouseTargetObject.typeOfHit == MovingObjectType.ENTITY
                    && mouseTargetObject.entityHit instanceof EntityLivingBase)
                {
                    triggeredKick = true;
                    kickTime = currtime;

                    sendSoundPacketToServer("kenshirosmash", (int)entPlayer.posX, (int)entPlayer.posY, (int)entPlayer.posZ);
                    sendPacketToServerHasKickedEntity(entPlayer, mouseTargetObject.entityHit);
                }
                else
                {
                    if (triggerTime == 0L)
                    {
                        triggerTime = currtime;
                    }
                    else if (currtime > triggerTime+750L)
                    {
                        if (currtime > triggerTime+2250L)
                        {
                            triggerTime = currtime;
                            triggeredKenshiro = true;
                            // play sound ATATATATA
                            sendSoundPacketToServer("kenshirostyle", (int)entPlayer.posX, (int)entPlayer.posY, (int)entPlayer.posZ);
                            DebugPrint("Rage unleashed!");
                            
                            sendPacketToServerHasUnleashedKenshiro();
                        }
                        else if (!chargingSound)
                        {
                            // play sound "charging rage"
                            chargingSound = true;
                            sendSoundPacketToServer("kenshirocharge", (int)entPlayer.posX, (int)entPlayer.posY, (int)entPlayer.posZ);
                            DebugPrint("Charge starting!");
                        }
                    }
                }
            }
            else
            {
                if (triggerTime != 0L)
                {
                    chargingSound = false;
                    DebugPrint("Charge aborted!");
                }
                triggerTime = 0L;
            }
        }
        else if (!triggeredKenshiro)
        {
            triggerTime = 0L;
        }
    }

    public void onEntityPunched(EntityLivingBase ent)
    {
        KenshiroMod.instance().debuffEntityLiving((EntityLivingBase) ent);
        
        ent.worldObj.spawnParticle("explode", ent.posX, ent.posY, ent.posZ, 0, 0.2, 0);
        ent.worldObj.spawnParticle("largeexplode", ent.posX, ent.posY, ent.posZ, 0, 0.2, 0);
        minecraft.effectRenderer.addEffect(new EntityCrit2FX(ent.worldObj, ent));
        entPlayer.worldObj.playSound(ent.posX, ent.posY, ent.posZ, "kenshiropunch", 1.0F, (entPlayer.getRNG().nextFloat() - entPlayer.getRNG().nextFloat()) * 0.2F + 1.0F, false);
        
        if (ent instanceof EntityCreeper)
        {
            KenshiroMod.instance().stopCreeperExplosion((EntityCreeper) ent);
        }
        else if (ent instanceof EntitySkeleton)
        {
            KenshiroMod.instance().stopSkeletonShooter((EntitySkeleton) ent);
        }
    }
    
    public void onEntityKicked(EntityPlayer kicker, EntityLivingBase target)
    {
        //System.out.println("Client onEntityKicked, kicker: "+kicker.func_146103_bH().getName()+", entity: "+target);
        target.attackEntityFrom(DamageSource.causePlayerDamage(kicker), 4);
        
        double var9 = entPlayer.posX - target.posX;
        double var7;
        for(var7 = entPlayer.posZ - target.posZ; var9 * var9 + var7 * var7 < 1.0E-4D; var7 = (Math.random() - Math.random()) * 0.01D)
        {
           var9 = (Math.random() - Math.random()) * 0.01D;
        }
        //((EntityLivingBase) mc.objectMouseOver.entityHit).knockBack(entPlayer, 10, var9, var7);
        
        target.setFire(8);
        
        float quad = MathHelper.sqrt_double(var9-var9 + var7*var7);
        target.addVelocity((var9 / (double)quad)*-1, 0.6, (var9 / (double)quad)*-1*-1);
    }

    private void sendPacketToServerHasDestroyedBlock(int x, int y, int z)
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new BlockPunchedPacket(minecraft.thePlayer.func_146103_bH().getName(), x, y, z));
    }
    
    private void sendPacketToServerHasPunchedEntity(Entity ent)
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new EntityPunchedPacket(minecraft.thePlayer.func_146103_bH().getName(), ent.func_145782_y()));
    }
    
    private void sendPacketToServerHasKickedEntity(EntityPlayer player, Entity ent)
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new EntityKickedPacket(player.dimension, player.func_145782_y(), ent.func_145782_y()));
    }
    
    private void sendPacketToServerHasUnleashedKenshiro()
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new KenshiroStatePacket(minecraft.thePlayer.func_146103_bH().getName(), true));
    }
    
    private void sendPacketToServerHasFinishedKenshiro()
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new KenshiroStatePacket(minecraft.thePlayer.func_146103_bH().getName(), false));
    }
    
    private void sendSoundPacketToServer(String sound, int x, int y, int z)
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new SoundPacket(sound, minecraft.thePlayer.dimension, x, y, z));
    }
    
    private final int ANIMATION_SWING = 1;
    private final int ANIMATION_CRITMAGIC_FX = 7;
    private void sendAnimationPacketToServer(int animation)
    {
        KenshiroMod.instance().networkHelper.sendPacketToServer(new AnimationPacket(minecraft.thePlayer.func_146103_bH().getName(), animation));
    }

    public boolean getKenshiroMode()
    {
        if (triggeredKenshiro)
        {
            return true;
        }
    	
    	return (canKenshiro && entPlayer.prevSwingProgress == 0F);
    }
	
	private void DebugPrint(String s)
	{
		if(DEBUGMODE)
		{
			minecraft.ingameGUI.func_146158_b().func_146227_a(new ChatComponentText(s));
		}
		else
		{
			System.out.println(s);
		}
	}

    public void setServerHasKenshiroInstalled(boolean value)
    {
        hasServerKenshiroInstalled = value;
        
        if (value)
        {
            minecraft.ingameGUI.func_146158_b().func_146227_a(new ChatComponentText("Kenshiromod active on this server, Mod now active!"));
        }
    }
}
