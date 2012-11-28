package atomicstryker.battletowers.common;

import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;

public class AS_TowerDestroyer
{
	public Entity player;
	private int x;
	private int y;
	private int z;
	private World world;
	private long triggerTime;
	private long lastExplosionSoundTime;
	private final int maxfloor = 6;
	private int floor = maxfloor;
	private final int floorDistance = 7;
	private final float explosionPower = 10F;
	
	private final long initialExplosionDelay = 15000L;
	private final long perFloorExplosionDelay = 5000L;
	private boolean deleteMe = false;

    public AS_TowerDestroyer(World worldObj, ChunkCoordinates coords, long time, Entity golemkiller)
    {
		this.world = worldObj;
		this.player = golemkiller;
		this.x = coords.posX;
		this.y = coords.posY;
		this.z = coords.posZ;
		this.triggerTime = time;
		this.lastExplosionSoundTime = time;
		
		world.playSoundEffect(x, y, z, "towerbreakstart", 4F, 1.0F);
    }
	
	public void update()
	{
	    if (deleteMe || yCoord() < 70)
	    {
	        return;
	    }
	    
		if(floor == maxfloor && System.currentTimeMillis() > triggerTime + initialExplosionDelay)
		{
			triggerTime = System.currentTimeMillis();
			
			// kaboom baby
			if (!world.isRemote)
			{
				world.createExplosion(player, x, yCoord(), z, explosionPower, true);
				cleanUpStragglerBlocks();
			}
			
			floor--;
		}
		else if (floor < maxfloor && System.currentTimeMillis() > triggerTime + perFloorExplosionDelay) // each floor bursts 10 seconds after that
		{
			if (floor < 1)
			{
			    deleteMe = true;
				return;
			}
			triggerTime = System.currentTimeMillis();
			
			// kaboom baby
			if (!world.isRemote)
			{
				world.createExplosion(player, x, yCoord(), z, explosionPower, true);
				cleanUpStragglerBlocks();
			}
			
			floor--;
		}
		else
		{
			createSFX(randomTowerCoord(x), (int)yCoord(), randomTowerCoord(z));
		}
	}
	
	public boolean isFinished()
	{
	    return deleteMe;
	}
	
	private double yCoord()
	{
		return y - (floorDistance * Math.abs(maxfloor - floor));
	}
	
	private int randomTowerCoord(int i)
	{
		return i - 7 + world.rand.nextInt(15);
	}
	
	private void cleanUpStragglerBlocks()
	{
		int ytemp = (int)yCoord();
		for(int xIterator = -8; xIterator < 8; xIterator++) // do each X
		{
			for(int zIterator = -8; zIterator < 8; zIterator++) // do each Z
			{
				for(int yIterator = 1; yIterator < 9; yIterator++) // do Y 8 blocks high
				{
					if(world.getBlockId(x+xIterator, ytemp+yIterator, z+zIterator) != 0)
					{
						world.setBlock(x+xIterator, ytemp+yIterator, z+zIterator, 0);
					}
				}
			}
		}
	}
	
	private void createSFX(int i, int j, int k)
	{
	    if (System.currentTimeMillis() > lastExplosionSoundTime + 4000L)
	    {
	        switch(world.rand.nextInt(4))
	        {
    	        case 0:
    	        {
    	            world.playSoundEffect(i, j, k, "random.fizz", 4F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
    	            break;
    	        }
    	        case 1:
    	        {
    	            world.playSoundEffect(i, j, k, "towercrumble", 4F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
    	            break;
    	        }
	        }
	        lastExplosionSoundTime = System.currentTimeMillis();
	    }
		
		double d = (float)i + world.rand.nextFloat();
		double d1 = (float)j + world.rand.nextFloat();
		double d2 = (float)k + world.rand.nextFloat();
		double d3 = d - i;
		double d4 = d1 - j;
		double d5 = d2 - k;
		double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
		d3 /= d6;
		d4 /= d6;
		d5 /= d6;
		double d7 = 0.5D / (d6 / 1D + 0.10000000000000001D);
		d7 *= world.rand.nextFloat() * world.rand.nextFloat() + 0.3F;
		d3 *= d7;
		d4 *= d7;
		d5 *= d7;
		
		switch(world.rand.nextInt(4))
		{
    		case 0:
    		{
    		    world.spawnParticle("explode", (d + i * 1.0D) / 2D, (d1 + j * 1.0D) / 2D, (d2 + k * 1.0D) / 2D, d3, d4, d5);
    		    break;
    		}
    		case 1:
    		{
    		    world.spawnParticle("smoke", d, d1, d2, d3, d4, d5);
    		    break;
    		}
    		case 2:
    		{
    		    world.spawnParticle("lava", d, d1, d2, 0.0D, 0.0D, 0.0D);
    		    break;
    		}
    		case 4:
    		{
    		    world.spawnParticle("largesmoke", (double)i + Math.random(), (double)j + 1.2D, (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
    		    break;
    		}
		}
	}
}
