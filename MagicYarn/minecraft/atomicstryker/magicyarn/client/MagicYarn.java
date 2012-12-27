package atomicstryker.magicyarn.client;

import java.util.ArrayList;
import java.util.EnumSet;

import atomicstryker.magicyarn.common.pathfinding.AStarNode;
import atomicstryker.magicyarn.common.pathfinding.AStarPathPlanner;
import atomicstryker.magicyarn.common.pathfinding.IAStarPathedEntity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.4.6")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class MagicYarn implements IAStarPathedEntity
{
    private final String textureFile = "/atomicstryker/magicyarn/client/sprites/magicYarnTextures.png";
	public static final Item magicYarn = (new ItemMagicYarn(2526)).setItemName("Magic Yarn");
	private static long time;
	public static AStarPathPlanner plannerInstance;
	
	public static Minecraft mcinstance;
	public static ArrayList<AStarNode> path = null;
	public static ArrayList<AStarNode> lastPath = null;
	public static boolean showPath = false;
	
	private static MPMagicYarn mpYarn;
	
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        AS_Settings_MagicYarn.initSettings(evt.getSuggestedConfigurationFile());
    }
	
    @Init
    public void load(FMLInitializationEvent evt)
    {
        mcinstance = FMLClientHandler.instance().getClient();
        
	    MinecraftForgeClient.preloadTexture(textureFile);
	    magicYarn.setTextureFile(textureFile);
	    
	    LanguageRegistry.instance().addName(magicYarn, "Magic Yarn");
	    
        GameRegistry.addRecipe(new ItemStack(magicYarn, 1), new Object[]{
            "###", "#X#", "###", Character.valueOf('X'), Item.compass, Character.valueOf('#'), Block.cloth
        });
		
		time = System.currentTimeMillis();
		
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
    
    @Override
    public void onFoundPath(ArrayList<AStarNode> result)
    {
        inputPath(result, false, false);
    }

    @Override
    public void onNoPathAvailable()
    {
        
    }
    
    public static void inputPath(ArrayList given, boolean noSound)
    {
    	inputPath(given, noSound, false);
    }
    
    public static void inputPath(ArrayList given, boolean noSound, boolean forceOverwrite)
    {
    	if (path != null || forceOverwrite)
    	{
    		lastPath = path;
    	}
    	path = given;
    	if (path != null)
    	{
    	    AStarNode prevN = null;
    	    for (AStarNode n : path)
    	    {
    	        if (prevN != null)
    	        {
    	            n.parent = prevN;
    	        }
    	        else
    	        {
    	            n.parent = null;
    	        }
    	        prevN = n;
    	    }
    	    
    		mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.levelup", 1.0F, 1.0F, false);
    	}
    	else if (!noSound)
    	{
    		mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.drr", 1.0F, 1.0F, false);
    	}
    }
    
    private void buildInstances()
    {
        plannerInstance = new AStarPathPlanner(mcinstance.theWorld, this);
        mpYarn = new MPMagicYarn(mcinstance);
    }
    
    private class TickHandler implements ITickHandler
    {
        private final EnumSet<TickType> types = EnumSet.of(TickType.CLIENT);

        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            if (mcinstance.thePlayer == null || mcinstance.theWorld == null) return;
            
            if (plannerInstance == null)
            {
                buildInstances();
            }
            else
            {
                // this will abort a rampant worker after 500 ms
                plannerInstance.isBusy();
            }
            
            mpYarn.onUpdate(mcinstance.theWorld);
            
            
            if (showPath && path != null)
            {
                for (AStarNode temp : path)
                {
                    if (temp.parent != null)
                    {
                        mcinstance.renderGlobal.spawnParticle("magicCrit", temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                                (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75);
                    }
                }
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return types;
        }

        @Override
        public String getLabel()
        {
            return "MagicYarn";
        }
        
    }

    public static void getPath(AStarNode origin, AStarNode target, boolean b)
    {
        plannerInstance.getPath(origin, target, b);
    }

    public static void stopPathSearch()
    {
        plannerInstance.stopPathSearch();
    }
}
