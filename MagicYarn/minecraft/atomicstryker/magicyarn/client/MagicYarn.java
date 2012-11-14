package atomicstryker.magicyarn.client;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.4.4")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class MagicYarn
{
    private final String textureFile = "/atomicstryker/magicyarn/client/sprites/magicYarnTextures.png";
	public static final Item magicYarn = (new ItemMagicYarn(2526)).setItemName("Magic Yarn");
	private static long time;
	public static AStarPath instance;
	
	public static Minecraft mcinstance;
	public static ArrayList path = null;
	public static ArrayList lastPath = null;
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
		
        instance = new AStarPath(mcinstance);
        mpYarn = new MPMagicYarn(mcinstance);
		
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
    
    public static void inputPath(ArrayList given)
    {
    	inputPath(given, false, false);
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
    		mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.levelup", 1.0F, 1.0F);
    	}
    	else if (!noSound)
    	{
    		mcinstance.theWorld.playSound(mcinstance.thePlayer.posX, mcinstance.thePlayer.posY, mcinstance.thePlayer.posZ, "random.drr", 1.0F, 1.0F);
    	}
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

            mpYarn.onUpdate(mcinstance.theWorld);

            if (showPath && path != null)
            {
                AStarNode temp;
                for(int i = path.size()-1; i >= 0; i--)
                {
                    temp = ((AStarNode)path.get(i));
                    mcinstance.renderGlobal.spawnParticle("magicCrit", temp.x+0.5D, temp.y+0.5D, temp.z+0.5D, temp.parentxoffset*0.75, (temp.parentyoffset*0.5)+0.2, temp.parentzoffset*0.75);
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
}
