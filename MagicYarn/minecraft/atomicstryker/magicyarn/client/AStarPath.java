package atomicstryker.magicyarn.client;

import java.util.*;
import net.minecraft.client.Minecraft;


public class AStarPath
{
	private static volatile AStarWorker worker;
	private static volatile Thread thread;
	private static Minecraft mc;
	
	public AStarPath(Minecraft mccall)
	{
		mc = mccall;
	}
	
	public static void getPath(int startx, int starty, int startz, int destx, int desty, int destz, boolean searchMode, boolean toLastPath)
	{
		AStarNode starter = new AStarNode(startx, starty, startz, 0);
		AStarNode finish = new AStarNode(destx, desty, destz, -1);;
		
		getPath(starter, finish, searchMode, toLastPath);
	}
	
	public static void getPath(AStarNode start, AStarNode end, boolean searchMode, boolean toLastPath)
	{
		if (worker != null)
		{
			thread.interrupt();
			worker = null;
			thread = null;
		}
		worker = new AStarWorker();
		worker.setup(mc, start, end, searchMode, toLastPath);
		thread = new Thread(worker);
		thread.start();
	}
	
	public static void stopPathSearch()
	{
		if (worker != null)
		{
			thread.interrupt();
			worker = null;
			thread = null;
		}
	}
}