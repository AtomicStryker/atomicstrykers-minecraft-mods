package atomicstryker.battletowers.common;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

import net.minecraft.src.*;

public class AS_EntityGolem extends EntityMob implements IEntityAdditionalSpawnData
{
    private int rageCounter;
    private int explosionAttack;
    private int towerID;
    private int drops;
	private float golemMoveSpeed = 0.05F;
    private int attackCounter;
    
    private int towerX = -1;
    private int towerY = -1;
    private int towerZ = -1;

    public AS_EntityGolem(World world, int i)
    {
        super(world);
        texture = "/atomicstryker/battletowers/client/golemdormant.png";
        moveSpeed = golemMoveSpeed;
        setSize(1.6F, 3.4F);
        rotationYaw = 0.0F;
        rageCounter = 0;
        explosionAttack = 0;
        isImmuneToFire = true;
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
		attackCounter = 0;

		towerID = i;
		this.updateGolemType();
    }
    
    public AS_EntityGolem(World world)
    {
        super(world);
        texture = "/atomicstryker/battletowers/client/golemdormant.png";
        moveSpeed = golemMoveSpeed;
        health = 300;
        setSize(1.6F, 3.4F);
        rotationYaw = 0.0F;
        rageCounter = 0;
        explosionAttack = 0;
        isImmuneToFire = true;
        drops = 1;
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
		attackCounter = 0;
    }
    
    @Override
    public int getAttackStrength(Entity par1Entity)
    {
        return 6;
    }

	@Override
	public void writeSpawnData(ByteArrayDataOutput data)
	{
		data.writeInt(this.towerID);
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data)
	{
		towerID = data.readInt();
		this.updateGolemType();
	}

	private void updateGolemType()
	{
		drops = 5 + towerID;
		health = 150 + 50*towerID;
	}

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Integer(Integer.valueOf(1))); // dormant datawatcher
    }

    public void setDormant()
    {
        if (!worldObj.isRemote)
        {
            this.dataWatcher.updateObject(16, new Integer(Integer.valueOf(1)));
        }
    }

    public void setAwake()
    {
        if (!worldObj.isRemote)
        {
            this.dataWatcher.updateObject(16, new Integer(Integer.valueOf(0)));
            worldObj.playSoundAtEntity(this, "golemawaken", getSoundVolume() * 2.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
        }
    }

    public boolean getIsDormant()
    {
        boolean isDormantNetworked = this.dataWatcher.getWatchableObjectInt(16) != 0;
        if (isDormantNetworked)
        {
            texture = "/atomicstryker/battletowers/client/golemdormant.png";
        }
        else
        {
            texture = "/atomicstryker/battletowers/client/golem.png";
        }
    	return isDormantNetworked;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int amount)
    {
        if (damageSource.getEntity() != null
        && damageSource.getEntity() instanceof EntityPlayer)
        {
            setAwake();
            entityToAttack = (EntityLiving) damageSource.getEntity();
        }
        
        return super.attackEntityFrom(damageSource, amount);
    }

	@Override
	public int getMaxHealth() {
		return 150 + 50 * (drops-5);
	}

	@Override
    public void setDead()
    {
        super.setDead();
    }

	@Override
    public void onDeath(DamageSource var1)
    {
    	super.onDeath(var1);
    	Entity entity = var1.getEntity();

        if(scoreValue > 0 && entity != null)
        {
            entity.addToPlayerScore(this, scoreValue);
        }
        if(!worldObj.isRemote)
        {
            int i = drops;
            for(int j = 0; j < i; j++)
            {
                dropItem(Item.diamond.shiftedIndex, 1);
				dropItem(Item.redstone.shiftedIndex, 1);
            }

            i = rand.nextInt(4) + 8;
            for(int k = 0; k < i; k++)
            {
                dropItem(Block.blockClay.blockID, 1);
            }
			if(entityToAttack != null && (AS_BattleTowersCore.towerDestroyerEnabled != 0))
			{
				AS_BattleTowersCore.onBattleTowerDestroyed(new AS_TowerDestroyer(worldObj, new ChunkCoordinates(towerX, towerY, towerZ), System.currentTimeMillis(), entityToAttack));
			}
        }
        worldObj.setEntityState(this, (byte)3);
    }

	@Override
    public void knockBack(Entity entity, int i, double d, double d1)
    {
        //moveSpeed = 0.25F + (float)((double)(450 - health) / 1750D);
        if(rand.nextInt(5) == 0)
        {
            motionX *= 1.5D;
            motionZ *= 1.5D;
            motionY += 0.60000002384185791D;
        }
        //rageCounter = 150;
    }
    
	@Override
    protected void updateEntityActionState()
    {
    	if (!this.getIsDormant())
    	{
    		super.updateEntityActionState();
    	}
    }
    
    @Override
    public void onUpdate()
    {
    	//dataWrapper.synchronizeRegisteredFields();

        if(!this.getIsDormant())
        {
			motionX *= 0.7;
			motionZ *= 0.7;

            if(rageCounter <= 0 && explosionAttack == 0)
            {
                if(explosionAttack == 0 && (entityToAttack instanceof EntityPlayer) && worldObj.getClosestPlayerToEntity(this, 30D) == null)
                {
                    entityToAttack = null;
                }
				else
                {
				    worldObj.playSoundAtEntity(this, "golemspecial", getSoundVolume() * 2.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
                    motionY += 0.9D;
                    explosionAttack = 1;
					moveSpeed = 1F;
                }
            }
			else if(entityToAttack == null)
			{
				health = 300;
                rageCounter = 125;
                explosionAttack = 0;

                if (this.onGround)
                {
                    this.setDormant();
                }
			}
			else if((rageCounter <= -30 || onGround) && explosionAttack == 1)
            {
                if(health <= 100)
                {
                    health += 15;
                }

				if (!worldObj.isRemote && (this.posY - entityToAttack.posY) > 0.3D)
				{
					worldObj.createExplosion(this, posX, posY-0.3D, posZ, 4F, true);
				}
                rageCounter = 125;
                explosionAttack = 0;
				moveSpeed = golemMoveSpeed;
            }
        }
        
        checkForVictim();
        super.onUpdate();
    }
    
	private void checkForVictim()
    {
        if(this.getIsDormant() && !worldObj.isRemote)
        {
            EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 6D);
            if(entityplayer != null && canEntityBeSeen(entityplayer))
            {
                this.setAwake();
                rageCounter = 175;
            }
        }
		else if (entityToAttack != null)
        {
			if (towerY == -1)
			{
			    towerX = (int)posX;
			    towerY = (int)posY;
			    towerZ = (int)posZ;
			}

			boolean targetNearby = (entityToAttack.getDistanceSqToEntity(this) < 6F*6F);

            if(!targetNearby
			|| explosionAttack == 1
			|| (targetNearby && ((this.posY - entityToAttack.posY) > 0.3D)))
            {
                rageCounter-=2;
                //System.out.println("Golem losing patience: "+rageCounter);
            }
			else
            {
                rageCounter = 175;
            }

			double diffX = entityToAttack.posX - posX;
			double diffY = (entityToAttack.boundingBox.minY + (double)(entityToAttack.height / 2.0F)) - (posY + (double)(height * 0.8D));
			double diffZ = entityToAttack.posZ - posZ;
			renderYawOffset = rotationYaw = (-(float)Math.atan2(diffX, diffZ) * 180F) / (float)Math.PI;
            // System.out.println("Golem attack counter: "+attackCounter);

            if (attackCounter == 10)
            {
                worldObj.playSoundAtEntity(this, "golemcharge", getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }
            attackCounter++;
            if (attackCounter == 20)
            {
                worldObj.playSoundAtEntity(this, "mob.ghast.fireball", getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);

                if (!worldObj.isRemote)
                {
                    AS_EntityGolemFireball entityfireball = new AS_EntityGolemFireball(worldObj, this, diffX, diffY, diffZ);
                    Vec3 vec3d = getLook(1.0F);
                    entityfireball.posX = posX + vec3d.xCoord * 2D;
                    entityfireball.posY = posY + (double) (height) * 0.8D;
                    entityfireball.posZ = posZ + vec3d.zCoord * 2D;
                    worldObj.spawnEntityInWorld(entityfireball);
                }
                attackCounter = -40;
            }
        }
        else if (attackCounter > 0)
        {
            attackCounter--;
        }
    }

    @Override
    protected boolean canDespawn()
    {
    	return this.isDead;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        if (towerY == -1)
        {
            towerX = (int)posX;
            towerY = (int)posY;
            towerZ = (int)posZ;
        }
        
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte("isDormant", (byte) (this.getIsDormant() ? 1 : 0));
        nbttagcompound.setByte("hasexplosionAttacked", (byte)explosionAttack);
        nbttagcompound.setByte("rageCounter", (byte)rageCounter);
        nbttagcompound.setByte("Drops", (byte)drops);
        nbttagcompound.setInteger("towerX", towerX);
        nbttagcompound.setInteger("towerY", towerY);
        nbttagcompound.setInteger("towerZ", towerZ);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if ((nbttagcompound.getByte("isDormant") & 0xff) == 1)
        {
        	this.setDormant();
        }
        else
        {
        	this.setAwake();
        }
        explosionAttack = nbttagcompound.getByte("hasexplosionAttacked") & 0xff;
        rageCounter = nbttagcompound.getByte("rageCounter") & 0xff;
        drops = nbttagcompound.getByte("Drops") & 0xff;
        moveSpeed = golemMoveSpeed;
        
        towerX = nbttagcompound.getInteger("towerX");
        towerY = nbttagcompound.getInteger("towerY");
        towerZ = nbttagcompound.getInteger("towerZ");
    }

    @Override
    protected void attackEntity(Entity entity, float f)
    {
        if((double)f < 3D && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY)
        {
            entity.attackEntityFrom(DamageSource.causeMobDamage(this), getAttackStrength(entity));
        }
        if(onGround)
        {
            double d = entity.posX - posX;
            double d1 = entity.posZ - posZ;
            float f1 = MathHelper.sqrt_double(d * d + d1 * d1);
            motionX = (d / (double)f1) * 0.5D * 0.20000000192092895D + motionX * 0.20000000098023224D;
            motionZ = (d1 / (double)f1) * 0.5D * 0.10000000192092896D + motionZ * 0.20000000098023224D;
        }
		else
        {
            super.attackEntity(entity, f);
        }
    }
    
    @Override
    public String getEntityName()
    {
    	return "Battletower Golem";
    }

    @Override
    protected String getLivingSound()
    {
        if(!this.getIsDormant())
        {
            return "golem";
        }
        else
        {
            return "ambient.cave.cave";
        }
    }

    @Override
    protected String getHurtSound()
    {
        return "golemhurt";
    }

    @Override
    protected String getDeathSound()
    {
        return "golemdeath";
    }

    @Override
    protected int getDropItemId()
    {
        return Item.paper.shiftedIndex;
    }
}
