package atomicstryker.magicyarn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import atomicstryker.magicyarn.common.pathfinding.AStarNode;
import atomicstryker.magicyarn.common.pathfinding.AStarPathPlanner;

public class MPMagicYarn
{
	private AStarNode origin = null;
	private AStarNode target = null;
	
	private Minecraft mcinstance;
	
	private long timeStartedHoldingButton = 0L;
	private boolean isHoldingButton = false;
	
	private String sTriggerKey = "J";
	private int triggerKey = Keyboard.getKeyIndex(sTriggerKey);
	
	public MPMagicYarn(Minecraft mc)
	{
		mcinstance = mc;
		
		sTriggerKey = AS_Settings_MagicYarn.sTriggerKey;
		triggerKey = AS_Settings_MagicYarn.triggerKey;
	}
	
	public void onUpdate(World worldObj)
	{
		if (worldObj.isRemote)
		{
			if (isHoldingButton != Keyboard.isKeyDown(triggerKey)
				&& mcinstance.currentScreen == null
				/*&& !mcinstance.ingameGUI.isChatOpen()*/)
			{
				if (timeStartedHoldingButton == 0L)
				{
					timeStartedHoldingButton = System.currentTimeMillis();
				}
				else
				{
					onPlayerStoppedUsing(worldObj, mcinstance.thePlayer, (float)((System.currentTimeMillis()-timeStartedHoldingButton)/1000));
					timeStartedHoldingButton = 0L;
				}
				
				isHoldingButton = Keyboard.isKeyDown(triggerKey);
			}
		}
	}

	private void onPlayerStoppedUsing(World world, EntityPlayer player, float timeButtonHeld)
	{
		if(timeButtonHeld > 2.5F)
		{
			timeButtonHeld = 2.5F;
		}

		if(timeButtonHeld < 2.5F)
		{
			if(origin == null)
			{		
				origin = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0, null);
				System.out.println("Magic Yarn Origin set to ["+origin.x+"|"+origin.y+"|"+origin.z+"]");
				world.playSound(player.posX, player.posY, player.posZ, "random.orb", 1.0F, 1.0F, false);
				MagicYarn.showPath = false;
			}
			else
			{
			    origin.parent = null;
				if (target == null && MagicYarn.path == null)
				{					
					target = new AStarNode((int)Math.floor(player.posX), (int)player.posY-1, (int)Math.floor(player.posZ), 0, null);
					System.out.println("Magic Yarn Target set to ["+target.x+"|"+target.y+"|"+target.z+"]");

					MagicYarn.plannerInstance.getPath(origin, target, false);
					MagicYarn.showPath = true;
				}
				else
				{
					boolean soundplayed = false;
					if (MagicYarn.path != null)
					{
						target = new AStarNode((int)Math.floor(player.posX), (int)Math.floor(player.posY)-1, (int)Math.floor(player.posZ), 0, null);
						for (int i = MagicYarn.path.size()-1; i != 0; i--)
						{
							if (((AStarNode) MagicYarn.path.get(i)).equals(target))
							{
								System.out.println("Magic Yarn being cut shorter!");
								world.playSound(player.posX, player.posY, player.posZ, "random.break", 1.0F, 1.0F, false);
								soundplayed = true;
								while (i >= 0)
								{
									MagicYarn.path.remove(i);
									i--;
								}
								break;
							}
						}
					}
					
					target = null;
					MagicYarn.inputPath(null, true);
					MagicYarn.plannerInstance.stopPathSearch();
					System.out.println("Magic Yarn Target nulled");
					if (!soundplayed)
					{
						world.playSound(player.posX, player.posY, player.posZ, "random.pop", 1.0F, 1.0F, false);
					}
					MagicYarn.showPath = false;
				}
			}
		}
		else
		{
			if(origin != null)
			{
				origin = null;
				target = null;
				MagicYarn.inputPath(null, true);
				MagicYarn.lastPath = null;
				MagicYarn.plannerInstance.stopPathSearch();
				System.out.println("Magic Yarn Origin nulled");
				world.playSound(player.posX, player.posY, player.posZ, "random.fizz", 1.0F, 1.0F, false);
				MagicYarn.showPath = false;
			}
		}
	}
}
