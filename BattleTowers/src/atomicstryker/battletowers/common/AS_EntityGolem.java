package atomicstryker.battletowers.common;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class AS_EntityGolem extends EntityMob implements IEntityAdditionalSpawnData
{
    private int rageCounter;
    private int explosionAttack;
    private int towerID;
    private int drops;
    private int attackCounter;
    
    private int towerX = -1;
    private int towerY = -1;
    private int towerZ = -1;
    
    public AS_EntityGolem(World world)
    {
        super(world);
        setSize(1.6F, 3.4F);
        rotationYaw = 0.0F;
        rageCounter = 0;
        explosionAttack = 0;
        isImmuneToFire = true;
        drops = 1;
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
        attackCounter = 0;
        
        func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(300d); // max health
        func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(3d); // attack damage
        func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3d); // movespeed
        
        /* cant use new AI because golem needs to keep working when the player isnt pathable to
         * 
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 0.3f, true));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 30.0F, 0, true));
        */
    }

    public AS_EntityGolem(World world, int i)
    {
        this(world);
		towerID = i;
		this.updateGolemType();
    }
    
    
    @Override
    protected void updateEntityActionState()
    {
        if (!this.getIsDormant())
        {
            super.updateEntityActionState();
        }
    }
    
    /* cant use new AI because golem needs to keep working when the player isnt pathable to
    @Override
    protected boolean isAIEnabled()
    {
        return true;
    }
    */

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
		float maxHealth = 150 + 50*towerID;
		func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(maxHealth); // max health
		setEntityHealth(maxHealth);
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
            if (getIsDormant())
            {
                worldObj.playSoundAtEntity(this, "golemawaken", getSoundVolume() * 2.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
            }
            
            this.dataWatcher.updateObject(16, new Integer(Integer.valueOf(0)));
        }
    }

    public boolean getIsDormant()
    {
    	return dataWatcher.getWatchableObjectInt(16) != 0;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float amount)
    {
        if (damageSource.getEntity() != null
        && damageSource.getEntity() instanceof EntityPlayer)
        {
            setAwake();
            setTarget((EntityLivingBase) damageSource.getEntity());
        }
        
        return super.attackEntityFrom(damageSource, amount);
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
                dropItem(Item.diamond.itemID, 1);
				dropItem(Item.redstone.itemID, 1);
            }

            i = rand.nextInt(4) + 8;
            for(int k = 0; k < i; k++)
            {
                dropItem(Block.blockClay.blockID, 1);
            }
			if(getEntityToAttack() != null && (AS_BattleTowersCore.towerDestroyerEnabled != 0))
			{
				AS_BattleTowersCore.onBattleTowerDestroyed(new AS_TowerDestroyer(worldObj, new ChunkCoordinates(towerX, towerY, towerZ), System.currentTimeMillis(), getEntityToAttack()));
			}
        }
        worldObj.setEntityState(this, (byte)3);
    }

	@Override
    public void knockBack(Entity entity, float i, double d, double d1)
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
    public void onUpdate()
    {
        if(!this.getIsDormant())
        {		
			if(getEntityToAttack() == null || !getEntityToAttack().isEntityAlive())
            {
                setEntityHealth(300f);
                rageCounter = 125;
                explosionAttack = 0;

                if (this.onGround)
                {
                    this.setDormant();
                }
            }
			else if(rageCounter <= 0 && explosionAttack == 0)
            {
			    worldObj.playSoundAtEntity(this, "golemspecial", getSoundVolume() * 2.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
			    motionY += 0.9D;
			    explosionAttack = 1;
            }
			else if((rageCounter <= -30 || onGround) && explosionAttack == 1)
            {
                if(func_110143_aJ() <= 100) // getEntityHealth
                {
                    setEntityHealth(func_110143_aJ() + 15);
                }

				if (!worldObj.isRemote && (this.posY - getEntityToAttack().posY) > 0.3D)
				{
					worldObj.createExplosion(this, posX, posY-0.3D, posZ, 4F, true);
				}
                rageCounter = 125;
                explosionAttack = 0;
            }
        }
        
        checkForVictim();
        super.onUpdate();
    }
    
	private void checkForVictim()
    {
        if(this.getIsDormant())
        {
            if (!worldObj.isRemote)
            {
                EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 6D);
                if(entityplayer != null && canEntityBeSeen(entityplayer))
                {
                    setAwake();
                    setTarget(entityplayer);
                    rageCounter = 175;
                }
            }
        }
		else if (getEntityToAttack() != null)
        {
			if (towerY == -1)
			{
			    towerX = (int)posX;
			    towerY = (int)posY;
			    towerZ = (int)posZ;
			}

			boolean targetNearby = (getEntityToAttack().getDistanceSqToEntity(this) < 6F*6F);

            if(!targetNearby
			|| explosionAttack == 1
			|| (targetNearby && ((this.posY - getEntityToAttack().posY) > 0.3D)))
            {
                rageCounter-=2;
                //System.out.println("Golem losing patience: "+rageCounter);
            }
			else
            {
                rageCounter = 175;
            }
            
            if (attackCounter == 10)
            {
                worldObj.playSoundAtEntity(this, "golemcharge", getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }
            attackCounter++;
            if (attackCounter >= 20)
            {
                conjureFireBall();
                attackCounter = -40;
            }
        }
        else if (attackCounter > 0)
        {
            attackCounter--;
        }
    }
	
	private void conjureFireBall()
	{
        if (!worldObj.isRemote)
        {
            double diffX = getEntityToAttack().posX - posX;
            double diffY = (getEntityToAttack().boundingBox.minY + (double)(getEntityToAttack().height / 2.0F)) - (posY + (double)(height * 0.8D));
            double diffZ = getEntityToAttack().posZ - posZ;
            
            renderYawOffset = rotationYaw = (-(float)Math.atan2(diffX, diffZ) * 180F) / (float)Math.PI;
            
            worldObj.playSoundAtEntity(this, "mob.ghast.fireball", getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            AS_EntityGolemFireball entityfireball = new AS_EntityGolemFireball(worldObj, this, diffX, diffY, diffZ);
            Vec3 vec3d = getLook(1.0F);
            entityfireball.posX = posX + vec3d.xCoord * 2D;
            entityfireball.posY = posY + (height*0.8) + vec3d.yCoord * 0.5D;
            entityfireball.posZ = posZ + vec3d.zCoord * 2D;
            worldObj.spawnEntityInWorld(entityfireball);
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
        
        towerX = nbttagcompound.getInteger("towerX");
        towerY = nbttagcompound.getInteger("towerY");
        towerZ = nbttagcompound.getInteger("towerZ");
    }
    
    @Override
    public boolean attackEntityAsMob(Entity entity)
    {
        if(onGround)
        {
            double d = entity.posX - posX;
            double d1 = entity.posZ - posZ;
            float f1 = MathHelper.sqrt_double(d * d + d1 * d1);
            motionX = (d / (double)f1) * 0.5D * 0.20000000192092895D + motionX * 0.20000000098023224D;
            motionZ = (d1 / (double)f1) * 0.5D * 0.10000000192092896D + motionZ * 0.20000000098023224D;
        }
        
        return super.attackEntityAsMob(entity);
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
        return Item.paper.itemID;
    }

}
