package atomicstryker.minions.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * @author lahwran
 * 
 */
public class RenderEntLahwran_Minions extends Entity
{
	Minecraft mc;

	public RenderEntLahwran_Minions(Minecraft mc, World arg0)
	{
		super(arg0);
		ignoreFrustumCheck = true;
		this.mc = mc;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound arg0)
	{
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound arg0)
	{
	}

	@Override
	public void onUpdate()
	{
		if (mc.thePlayer != null)
		{
			this.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		}
	}

	@Override
	public void setDead()
	{
	}
}
