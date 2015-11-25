package atomicstryker.kenshiro.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import atomicstryker.kenshiro.common.network.EntityKickedPacket;
import atomicstryker.kenshiro.common.network.EntityPunchedPacket;
import atomicstryker.kenshiro.common.network.HandshakePacket;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class KenshiroServer
{
    private static KenshiroServer instance;
    private Map<EntityPlayer, Set<EntityLivingBase>> punchedEntitiesMap;
    
    public KenshiroServer()
    {
        instance = this;
        punchedEntitiesMap = new HashMap<EntityPlayer, Set<EntityLivingBase>>();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public static KenshiroServer instance()
    {
        return instance;
    }
    
    @SubscribeEvent
    public void onEntityJoinsWorld(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayerMP)
        {
            KenshiroMod.instance().networkHelper.sendPacketToPlayer(new HandshakePacket(), (EntityPlayerMP) event.entity);
        }
    }

    public void onClientPunchedBlock(EntityPlayerMP player, int x, int y, int z)
    {
        Block block = player.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
        if (block != null)
        {
        	BlockPos bp = new BlockPos(x, y, z);
            int isCancelled = ForgeHooks.onBlockBreakEvent(player.worldObj, player.theItemInWorldManager.getGameType(), player, bp);
            if (isCancelled >= 0)
            {
            	IBlockState ibs = player.worldObj.getBlockState(bp);
            	if (block.removedByPlayer(player.worldObj, bp, player, true))
                {
                    block.onBlockDestroyedByPlayer(player.worldObj, bp, ibs);
                    block.harvestBlock(player.worldObj, player, bp, ibs, player.worldObj.getTileEntity(bp));
                }
                
                for (EntityPlayerMP p : (ArrayList<EntityPlayerMP>)MinecraftServer.getServer().getConfigurationManager().playerEntityList)
                {
                    p.playerNetServerHandler.sendPacket(new S23PacketBlockChange(player.worldObj, bp));
                }
            }
        }
    }

    public void onClientPunchedEntity(EntityPlayer player, World world, int entityID)
    {
        Entity target = world.getEntityByID(entityID);
        if (target != null
        && target instanceof EntityLivingBase)
        {
            EntityLivingBase targetEnt = (EntityLivingBase) target;
            
            KenshiroMod.instance().debuffEntityLiving(targetEnt);
            
            if (targetEnt.getHealth() > 7)
            {
                targetEnt.attackEntityFrom(DamageSource.causePlayerDamage(player), 7);
            }
            else
            {
                if (punchedEntitiesMap.get(player) == null)
                {
                    punchedEntitiesMap.put(player, new HashSet<EntityLivingBase>());
                }
                punchedEntitiesMap.get(player).add((EntityLivingBase) target);
            }
            
            KenshiroMod.instance().networkHelper.sendPacketToAllAroundPoint(new EntityPunchedPacket(entityID), 
                    new TargetPoint(world.provider.getDimensionId(), target.posX, target.posY, target.posZ, 30D));
        }
        
        if (target instanceof EntityCreeper)
        {
            KenshiroMod.instance().stopCreeperExplosion((EntityCreeper) target);
        }
        else if (target instanceof EntitySkeleton)
        {
            KenshiroMod.instance().stopSkeletonShooter((EntitySkeleton) target);
        }
    }

    public void onClientKickedEntity(EntityPlayer player, EntityLivingBase target)
    {
        player.addExhaustion(40F);
        target.attackEntityFrom(DamageSource.causePlayerDamage(player), 4);
        
        double var9 = player.posX - target.posX;
        double var7;
        for(var7 = player.posZ - target.posZ; var9 * var9 + var7 * var7 < 1.0E-4D; var7 = (Math.random() - Math.random()) * 0.01D)
        {
           var9 = (Math.random() - Math.random()) * 0.01D;
        }
        //((EntityLivingBase) mc.objectMouseOver.entityHit).knockBack(entPlayer, 10, var9, var7);
        
        float quad = MathHelper.sqrt_double(var9-var9 + var7*var7);
        target.addVelocity((var9 / (double)quad)*-1, 0.6, (var9 / (double)quad)*-1*-1);
        
        target.setFire(8);
        
        KenshiroMod.instance().networkHelper.sendPacketToAllAroundPoint(new EntityKickedPacket(player.dimension, player.getEntityId(), target.getEntityId()), 
                new TargetPoint(player.worldObj.provider.getDimensionId(), target.posX, target.posY, target.posZ, 30D));
    }

    public void onClientUnleashedKenshiroVolley(EntityPlayer playerEnt)
    {
        playerEnt.addExhaustion(40F);
        playerEnt.addExhaustion(40F);
        playerEnt.addExhaustion(40F);
        
        punchedEntitiesMap.put(playerEnt, new HashSet<EntityLivingBase>());
    }

    public void onClientFinishedKenshiroVolley(EntityPlayer playerEnt)
    {
        Set<EntityLivingBase> s = punchedEntitiesMap.get(playerEnt);
        if (s != null)
        {
            Iterator<EntityLivingBase> iter = s.iterator();
            while (iter.hasNext())
            {
                EntityLivingBase target = iter.next();
                target.attackEntityFrom(DamageSource.causePlayerDamage(playerEnt), 21);
            }
            punchedEntitiesMap.remove(playerEnt);
        }
    }
    
    @SubscribeEvent
    public void onEntityLivingBaseAttacked(LivingAttackEvent event)
    {
        if (event.source.getEntity() != null
        && !(event.source.getEntity() instanceof EntityPlayer))
        {
            for (EntityPlayer p : punchedEntitiesMap.keySet())
            {
                if (punchedEntitiesMap.get(p).contains(event.source.getEntity()))
                {
                    event.setCanceled(true);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            for (EntityPlayer p : punchedEntitiesMap.keySet())
            {
                for (EntityLivingBase e : punchedEntitiesMap.get(p))
                {
                    if (e instanceof EntityCreeper)
                    {
                        KenshiroMod.instance().stopCreeperExplosion((EntityCreeper) e);
                    }
                }
            }
        }
    }
}
