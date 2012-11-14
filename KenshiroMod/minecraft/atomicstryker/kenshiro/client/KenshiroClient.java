package atomicstryker.kenshiro.client;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityCrit2FX;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MovingObjectPosition;

import org.lwjgl.input.Mouse;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.kenshiro.common.KenshiroMod;
import atomicstryker.kenshiro.common.PacketType;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.TickRegistry;

public class KenshiroClient
{
	private final boolean DEBUGMODE = false;
	
	private Minecraft minecraft;
	private EntityPlayer entPlayer;
    private MovingObjectPosition mouseTargetObject;
    
    private RenderEntityLahwran renderEnt;
    private boolean hasServerKenshiroInstalled;
	
	private boolean canKenshiro = false;
	private boolean chargingSound = false;
	private boolean triggeredKenshiro = false;
	private boolean triggeredKick = false;
	private long kickTime = 0L;
	private long triggerTime = 0L;
	private long smashTime = 0L;
	private long fxTime = 0L;
	private boolean shindeiru = false;
	private long shindeiruTime = 0L;
	
	private Set<Entity> entitesHit;
	
	private static KenshiroClient instance;
	
	public KenshiroClient()
	{
	    instance = this;
	    entitesHit = new HashSet<Entity>();
	    hasServerKenshiroInstalled = false;
	    minecraft = FMLClientHandler.instance().getClient();
	    
	    RenderingRegistry.registerEntityRenderingHandler(RenderEntityLahwran.class, new RenderHookKenshiro());
	    TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
	
	public static KenshiroClient instance()
	{
	    return instance;
	}
    
    private void spawnRenderEntity(Minecraft mc)
	{
    	renderEnt = new RenderEntityLahwran(mc, mc.theWorld);
        mc.theWorld.addWeatherEffect(renderEnt);
        renderEnt.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }
    
    private class ClientTickHandler implements ITickHandler
    {
        private final EnumSet tickTypes;
        public ClientTickHandler()
        {
            tickTypes = EnumSet.of(TickType.RENDER);
        }
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }
        
        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            onTick();
        }
        
        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }
        
        @Override
        public String getLabel()
        {
            return "KenshiroMod";
        }
    }

    private void onTick()
    {
		entPlayer = minecraft.thePlayer;
	
        if (!hasServerKenshiroInstalled || entPlayer == null)
        {
            return;
        }
        
        canKenshiro = false;
        
        long currtime = System.currentTimeMillis();
        
        mouseTargetObject = minecraft.objectMouseOver;

        if (triggeredKenshiro)
        {
            if (minecraft.currentScreen != null)
            {
                minecraft.displayGuiScreen(null);
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
        		if(mouseTargetObject.typeOfHit == EnumMovingObjectType.TILE)
        		{
        			int x = mouseTargetObject.blockX;
        			int y = mouseTargetObject.blockY;
        			int z = mouseTargetObject.blockZ;
        			
        			int blockID = minecraft.theWorld.getBlockId(x, y, z);
        			int metadata = minecraft.theWorld.getBlockMetadata(x, y, z);
        			float hardness = 0F;
					if (Block.blocksList[blockID] != null)
					{
						hardness = Block.blocksList[blockID].getBlockHardness(minecraft.theWorld, x, y, z);
					}
        			
        			if (((hardness <= 3.0F && hardness >= 0F) || blockID == Block.wood.blockID || blockID == Block.web.blockID)
        			&& currtime > smashTime+150L)
        			{
        			    sendPacketToServerHasDestroyedBlock(x, y, z);
        			    
        				smashTime = currtime;
        				minecraft.playerController.onPlayerDestroyBlock(x, y, z, metadata);
        				Block.blocksList[blockID].harvestBlock(minecraft.theWorld, minecraft.thePlayer, x, y, z, metadata);
        			}
        		}
        		else if(mouseTargetObject.typeOfHit == EnumMovingObjectType.ENTITY)
        		{
        			Entity ent = mouseTargetObject.entityHit;
        			
        			if (ent instanceof EntityLiving)
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
        				minecraft.ingameGUI.getChatGUI().printChatMessage("[You are already dead.]");
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
        			&& mouseTargetObject.typeOfHit == EnumMovingObjectType.ENTITY
        			&& mouseTargetObject.entityHit instanceof EntityLiving)
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

    public void onEntityPunched(EntityLiving ent)
    {
        KenshiroMod.instance().debuffEntityLiving((EntityLiving) ent);
        
        ent.worldObj.spawnParticle("explode", ent.posX, ent.posY, ent.posZ, 0, 0.2, 0);
        ent.worldObj.spawnParticle("largeexplode", ent.posX, ent.posY, ent.posZ, 0, 0.2, 0);
        minecraft.effectRenderer.addEffect(new EntityCrit2FX(ent.worldObj, ent));
        entPlayer.worldObj.playSound(ent.posX, ent.posY, ent.posZ, "kenshiropunch", 1.0F, (entPlayer.getRNG().nextFloat() - entPlayer.getRNG().nextFloat()) * 0.2F + 1.0F);
        
        if (ent instanceof EntityCreeper)
        {
            KenshiroMod.instance().stopCreeperExplosion((EntityCreeper) ent);
        }
        else if (ent instanceof EntitySkeleton)
        {
            KenshiroMod.instance().stopSkeletonShooter((EntitySkeleton) ent);
        }
    }
    
    public void onEntityKicked(EntityPlayer kicker, EntityLiving target)
    {
        //System.out.println("Client onEntityKicked, kicker: "+kicker.username+", entity: "+target);
        target.attackEntityFrom(DamageSource.causePlayerDamage(kicker), 4);
        
        double var9 = entPlayer.posX - target.posX;
        double var7;
        for(var7 = entPlayer.posZ - target.posZ; var9 * var9 + var7 * var7 < 1.0E-4D; var7 = (Math.random() - Math.random()) * 0.01D)
        {
           var9 = (Math.random() - Math.random()) * 0.01D;
        }
        //((EntityLiving) mc.objectMouseOver.entityHit).knockBack(entPlayer, 10, var9, var7);
        
        target.setFire(8);
        
        float quad = MathHelper.sqrt_double(var9-var9 + var7*var7);
        target.addVelocity((var9 / (double)quad)*-1, 0.6, (var9 / (double)quad)*-1*-1);
    }

    private void sendPacketToServerHasDestroyedBlock(int x, int y, int z)
    {
        Object[] toSend = {x, y, z};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.BLOCKPUNCHED.ordinal(), toSend));
    }
    
    private void sendPacketToServerHasPunchedEntity(Entity ent)
    {
        Object[] toSend = {ent.entityId};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.ENTITYPUNCHED.ordinal(), toSend));
    }
    
    private void sendPacketToServerHasKickedEntity(EntityPlayer player, Entity ent)
    {
        Object[] toSend = {player.entityId, ent.entityId};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.ENTITYKICKED.ordinal(), toSend));
    }
    
    private void sendPacketToServerHasUnleashedKenshiro()
    {
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.KENSHIROSTARTED.ordinal(), null));
    }
    
    private void sendPacketToServerHasFinishedKenshiro()
    {
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.KENSHIROENDED.ordinal(), null));
    }
    
    private void sendSoundPacketToServer(String sound, int x, int y, int z)
    {
        Object[] toSend = {sound, x, y, z};
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.SOUNDEFFECT.ordinal(), toSend));
    }
    
    private final int ANIMATION_SWING = 1;
    private final int ANIMATION_CRIT_FX = 6;
    private final int ANIMATION_CRITMAGIC_FX = 7;
    private void sendAnimationPacketToServer(int animation)
    {
        Object[] toSend = { animation };
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.ANIMATION.ordinal(), toSend));
    }

    public boolean getKenshiroMode()
    {
    	if (triggeredKenshiro) return true;
    	
    	return (canKenshiro && entPlayer.prevSwingProgress == 0F);
    }
	
	private void DebugPrint(String s)
	{
		if(DEBUGMODE)
		{
			minecraft.ingameGUI.getChatGUI().printChatMessage(s);
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
            minecraft.ingameGUI.getChatGUI().printChatMessage("Server handshake complete! KenshiroMod enabled!");
            spawnRenderEntity(minecraft);
        }
    }
}
