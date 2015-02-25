package atomicstryker.minions.common.codechicken;

/**
 * Originally WirelessBolt class by ChickenBones, part of Wireless Redstone
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ChickenLightningBolt
{
    public ArrayList<Segment> segments = new ArrayList<Segment>();
    private final Vector3 start;
    private final Vector3 end;
    private final HashMap<Integer, Integer> splitparents = new HashMap<Integer, Integer>();
    
    public double length;
    public int numsegments0;
    private int numsplits;
    private boolean finalized;
    private Random rand;
    public long seed;
    
    public int particleAge;
    public int particleMaxAge;
    public boolean isDead;
    private AxisAlignedBB boundingBox;

    private World world;
    private Entity source;

    public final static ConcurrentLinkedQueue<ChickenLightningBolt> boltlist = new ConcurrentLinkedQueue<ChickenLightningBolt>();
    
    public static final float speed = 1.5F;//ticks per metre
    public static final int fadetime = 20;
    
    /* Damage in half hearts */
    public static int playerdamage = 1;
    public static int entitydamage = 1;
    
	public class BoltPoint
	{
		public BoltPoint(Vector3 basepoint, Vector3 offsetvec)
		{
			this.point = basepoint.copy().add(offsetvec);
			this.basepoint = basepoint;
			this.offsetvec = offsetvec;
		}
		
		public Vector3 point;
		Vector3 basepoint;
		Vector3 offsetvec;
	}
	
	public class SegmentSorter implements Comparator<Segment>
	{
		public int compare(Segment o1, Segment o2)
		{
			int comp = Integer.valueOf(o1.splitno).compareTo(o2.splitno);
			if(comp == 0)
			{
				return Integer.valueOf(o1.segmentno).compareTo(o2.segmentno);
			}
			else
			{
				return comp;
			}
		}
	}
	
	public class SegmentLightSorter implements Comparator<Segment>
	{
		public int compare(Segment o1, Segment o2)
		{
			return Float.compare(o2.light, o1.light);
		}
	}
	
	public class Segment
	{
		public Segment(BoltPoint start, BoltPoint end, float light, int segmentnumber, int splitnumber)
		{
			this.startpoint = start;
			this.endpoint = end;
			this.light = light;
			this.segmentno = segmentnumber;
			this.splitno = splitnumber;
			
			calcDiff();
		}
		
		public Segment(Vector3 start, Vector3 end)
		{
			this(new BoltPoint(start, new Vector3(0, 0, 0)), new BoltPoint(end, new Vector3(0, 0, 0)), 1, 0, 0);
		}

		public void calcDiff()
		{
			diff = endpoint.point.copy().subtract(startpoint.point);
		}
		
		public void calcEndDiffs()
		{
			if(prev != null)
			{
				Vector3 prevdiffnorm = prev.diff.copy().normalize();
				Vector3 thisdiffnorm = diff.copy().normalize();
				
				prevdiff = thisdiffnorm.copy().add(prevdiffnorm).normalize();
				sinprev = (float) Math.sin(thisdiffnorm.angle(prevdiffnorm.multiply(-1)) / 2);
			}
			else
			{
				prevdiff = diff.copy().normalize();
				sinprev = 1;
			}
			
			if(next != null)
			{
				Vector3 nextdiffnorm = next.diff.copy().normalize();
				Vector3 thisdiffnorm = diff.copy().normalize();
				
				nextdiff = thisdiffnorm.add(nextdiffnorm).normalize();
				sinnext = (float) Math.sin(thisdiffnorm.angle(nextdiffnorm.multiply(-1)) / 2);
			}
			else
			{
				nextdiff = diff.copy().normalize();
				sinnext = 1;
			}
		}
		
		public String toString()
		{
			return startpoint.point.toString() + " " + endpoint.point.toString();
		}
		
		public BoltPoint startpoint;
		public BoltPoint endpoint;
		
		public Vector3 diff;
		
		public Segment prev;
		public Segment next;
		
		public Vector3 nextdiff;
		public Vector3 prevdiff;
		
		public float sinprev;
		public float sinnext;
		public float light;
		
		public int segmentno;
		public int splitno;
	}
	
	public ChickenLightningBolt(World world, Vector3 sourcevec, Vector3 targetvec, long seed)
	{
		this.world = world;
		this.seed = seed;
		this.rand = new Random(seed);
		
		start = sourcevec;
		end = targetvec;
		
		numsegments0 = 1;
		
		length = end.copy().subtract(start).mag();
		particleMaxAge = fadetime + rand.nextInt(fadetime) - (fadetime / 2);
		particleAge = -(int)(length*speed);
		
		boundingBox = AxisAlignedBB.fromBounds(
                Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z), 
                Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z))
                .expand(length / 2, length / 2, length / 2);
		segments.add(new Segment(start, end));
	}
	
	public static Vector3 getFocalPoint(TileEntity tile)
	{
		return Vector3.fromTileEntityCenter((TileEntity) tile);
	}
	
	public ChickenLightningBolt(World world, Vector3 sourcevec, TileEntity target, long seed)
	{
		this(world, sourcevec, getFocalPoint(target), seed);
	}
	
	public void setWrapper(Entity entity)
	{
		source = entity;
	}
	
	private void fractal(int splits, double amount, double splitchance, double splitlength, double splitangle)
	{
		if(finalized)
		{
			return;
		}
		
		ArrayList<Segment> oldsegments = segments;
		segments = new ArrayList<Segment>();
		
		Segment prev = null;
		
		for(Iterator<Segment> iterator = oldsegments.iterator(); iterator.hasNext();)
		{
			Segment segment = iterator.next();
			prev = segment.prev;
			
			Vector3 subsegment = segment.diff.copy().multiply(1F/splits);
			
			BoltPoint[] newpoints = new BoltPoint[splits+1];
			
			Vector3 startpoint = segment.startpoint.point;
			newpoints[0] = segment.startpoint;
			newpoints[splits] = segment.endpoint;
			
			for(int i = 1; i < splits; i++)
			{
				Vector3 randoff = segment.diff.copy().perpendicular().normalize().rotate(rand.nextFloat() * 360, segment.diff);
				randoff.multiply((rand.nextFloat()-0.5F)*amount*2);
				
				Vector3 basepoint = startpoint.copy().add(subsegment.copy().multiply(i));
				
				newpoints[i] = new BoltPoint(basepoint, randoff);
			}
			for(int i = 0; i < splits; i++)
			{
				Segment next = new Segment(newpoints[i], newpoints[i+1], segment.light, segment.segmentno*splits + i, segment.splitno);
				next.prev = prev;
				if(prev != null)
				{
					prev.next = next;
				}
				
				if(i != 0 && rand.nextFloat() < splitchance)
				{
					Vector3 splitrot = next.diff.copy().xCrossProduct().rotate(rand.nextFloat() * 360, next.diff);
					Vector3 diff = next.diff.copy().rotate((rand.nextFloat() * 0.66F + 0.33F) * splitangle, splitrot).multiply(splitlength);
					
					numsplits++;
					splitparents.put(numsplits, next.splitno);
					
					Segment split = new Segment(newpoints[i], new BoltPoint(newpoints[i+1].basepoint, newpoints[i+1].offsetvec.copy().add(diff)), segment.light / 2F, next.segmentno, numsplits);
					split.prev = prev;
					
					segments.add(split);
				}

				prev = next;
				segments.add(next);
			}
			if(segment.next != null)
			{
				segment.next.prev = prev;
			}
		}
		
		numsegments0 *= splits;
	}
	
	public void defaultFractal()
	{
		fractal(2, length/1.5, 0.7F, 0.7F, 45);
		fractal(2, length/4, 0.5F, 0.8F, 50);
		fractal(2, length/15, 0.5F, 0.9F, 55);
		fractal(2, length/30, 0.5F, 1.0F, 60);
		fractal(2, length/60, 0, 0, 0);
		fractal(2, length/100, 0, 0, 0);
		fractal(2, length/400, 0, 0, 0);
	}
	
	private float rayTraceResistance(Vector3 start, Vector3 end, float prevresistance)
	{
		MovingObjectPosition mop = world.rayTraceBlocks(start.toVec3D(), end.toVec3D());
		
		if(mop == null)
		{
			return prevresistance;
		}
		
		if(mop.typeOfHit == MovingObjectType.BLOCK)
		{
			Block blockID = world.getBlockState(mop.getBlockPos()).getBlock();
			
			if(blockID == Blocks.air)
			{
				return prevresistance;
			}
			
			return prevresistance + (blockID.getExplosionResistance(source));
		}
		else
		{
			return prevresistance;
		}
	}
	
	private void vecBBDamageSegment(Vector3 start, Vector3 end, List<Entity> entitylist)
	{
		Vec3 start3D = start.toVec3D();
		Vec3 end3D = end.toVec3D();
		
		for(int i = 0; i < entitylist.size(); i++)
		{
			Entity entity = entitylist.get(i);
			if(entity instanceof EntityLivingBase && 
					(entity.getEntityBoundingBox().isVecInside(start3D) || entity.getEntityBoundingBox().isVecInside(end3D)))
			{
				if(entity instanceof EntityPlayer)
				{
					entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) source), playerdamage);
				}
				else
				{
				    entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) source), playerdamage);
				    entity.setFire(2);
				}
			}
		}
	}
	
	private void bbTestEntityDamage()
	{
		if(!world.isRemote)
		{
	        @SuppressWarnings("unchecked")
	        List<Entity> nearentities = world.getEntitiesWithinAABBExcludingEntity(source, boundingBox);
	        if(nearentities.size() != 0)
	        {
	            int newestsegment = (int) ((particleAge+(int)(length*speed))/ (float)(int)(length*speed) * numsegments0);
	            for(Iterator<Segment> iterator = segments.iterator(); iterator.hasNext();)
	            {
	                Segment segment = iterator.next();
	                
	                if(segment.segmentno > newestsegment)
	                {
	                    continue;
	                }
	                vecBBDamageSegment(segment.startpoint.point, segment.endpoint.point, nearentities);
	            }
	        }
		}
	}
	
	private void calculateCollisionAndDiffs()
	{
		HashMap<Integer, Integer> lastactivesegment = new HashMap<Integer, Integer>();
		
		Collections.sort(segments, new SegmentSorter());
		
		int lastsplitcalc = 0;
		int lastactiveseg = 0;//unterminated
		float splitresistance = 0;
		
		for(Iterator<Segment> iterator = segments.iterator(); iterator.hasNext();)//iterate each branch and do tests for the last active split
		{
			Segment segment = iterator.next();
			if(segment.splitno > lastsplitcalc)//next split trace
			{
				lastactivesegment.put(lastsplitcalc, lastactiveseg);//put last active segment for split in map
				//reset				
				lastsplitcalc = segment.splitno;
				lastactiveseg = lastactivesegment.get(splitparents.get(segment.splitno));//last active is parent
				splitresistance = lastactiveseg < segment.segmentno ? 50 : 0;//already teminated if the last parent segment was before the start of this one
			}
			if(splitresistance >= 40*segment.light)
			{
				continue;
			}
			splitresistance = rayTraceResistance(segment.startpoint.point, segment.endpoint.point, splitresistance);
			lastactiveseg = segment.segmentno;
		}
		lastactivesegment.put(lastsplitcalc, lastactiveseg);//put last active segment for split in map
		
		lastsplitcalc = 0;
		lastactiveseg = lastactivesegment.get(0);
		for(Iterator<Segment> iterator = segments.iterator(); iterator.hasNext();)//iterate each segment and kill off largeones
		{
			Segment segment = iterator.next();
			if(lastsplitcalc != segment.splitno)
			{
				lastsplitcalc = segment.splitno;
				lastactiveseg = lastactivesegment.get(segment.splitno);
			}
			if(segment.segmentno > lastactiveseg)
			{
				iterator.remove();
			}
			segment.calcEndDiffs();
		}
		
		if(lastactivesegment.get(0) + 1 < numsegments0)
		{
		}
	}
	
	public void finalizeBolt()
	{
		if(finalized)
		{
			return;
		}
		finalized = true;
		
		calculateCollisionAndDiffs();
		
		Collections.sort(segments, new SegmentLightSorter());
		
		boltlist.add(this);
	}
	
	public void onUpdate()
	{
		particleAge++;
		
		bbTestEntityDamage();
		
		if(particleAge == 0)
		{	
			//jamTile();
		}
		
		if(particleAge >= particleMaxAge)
		{
			isDead = true;
		}
	}
	
	public static void update()
	{
		for(Iterator<ChickenLightningBolt> iterator = boltlist.iterator(); iterator.hasNext();)
		{
			ChickenLightningBolt bolt = iterator.next();
			bolt.onUpdate();
			if(bolt.isDead)
			{
				iterator.remove();
			}
		}
	}
	
    public static void offerBolt(ChickenLightningBolt bolt)
    {
        if (!boltlist.contains(bolt))
        {
            boltlist.add(bolt);
        }
    }
	
	@Override
	public boolean equals(Object o)
	{
	    if (o instanceof ChickenLightningBolt)
	    {
	        ChickenLightningBolt bolt = (ChickenLightningBolt) o;
	        return bolt.start.equals(start) && bolt.end.equals(end);
	    }
	    return false;
	}
	
	@Override
	public int hashCode()
	{
	    return start.hashCode() + end.hashCode();
	}
}
