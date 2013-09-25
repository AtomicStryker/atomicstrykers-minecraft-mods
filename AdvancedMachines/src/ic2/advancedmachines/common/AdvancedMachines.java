package ic2.advancedmachines.common;

import ic2.api.item.Items;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "AdvancedMachines", name = "IC2 Advanced Machines Addon", version = "5.2.3", dependencies = "required-after:IC2@2.0.225")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class AdvancedMachines implements IGuiHandler, IProxy
{
	public static AdvancedMachines instance;
	
    public static Configuration config;
    
    public static Block blockAdvancedMachine;
    public static ItemStack stackRotaryMacerator;
    public static ItemStack stackSingularityCompressor;
    public static ItemStack stackCentrifugeExtractor;

    public static int guiIdRotary;
    public static int guiIdSingularity;
    public static int guiIdCentrifuge;
    
    public static ItemStack overClockerStack;
    public static ItemStack transformerStack;
    public static ItemStack energyStorageUpgradeStack;
    public static ItemStack ejectorUpgradeStack;
    
    public static String advMaceSound = "Machines/MaceratorOp.ogg";
    public static String advCompSound = "Machines/CompressorOp.ogg";
    public static String advExtcSound = "Machines/ExtractorOp.ogg";
    public static String interruptSound = "Machines/InterruptOne.ogg";
    
    public static int defaultEnergyConsume = 3;
    public static int defaultAcceleration = 2;
    public static int overClockSpeedBonus = 500;
    public static double overClockEnergyRatio = 2.0D;
    public static double overClockAccelRatio = 1.6D;
    public static double overLoadInputRatio = 4.0D;
    
    @SidedProxy(clientSide = "ic2.advancedmachines.client.AdvancedMachinesClient", serverSide = "ic2.advancedmachines.common.AdvancedMachines")
    public static IProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        blockAdvancedMachine = new BlockAdvancedMachines(config.getBlock("IDs", "AdvancedMachineBlock", 1188).getInt());
        blockAdvancedMachine.setCreativeTab(CreativeTabs.tabRedstone);
        
        GameRegistry.registerBlock(blockAdvancedMachine, ItemAdvancedMachine.class, "blockAdvMachine");
        
        stackRotaryMacerator = new ItemStack(blockAdvancedMachine, 1, 0);
        stackSingularityCompressor = new ItemStack(blockAdvancedMachine, 1, 1);
        stackCentrifugeExtractor = new ItemStack(blockAdvancedMachine, 1, 2);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        guiIdRotary = config.get("IDs", "guiIdRotary", 40).getInt();
        guiIdSingularity = config.get("IDs", "guiIdSingularity", 41).getInt();
        guiIdCentrifuge = config.get("IDs", "guiIdCentrifuge", 42).getInt();
        
        Property  prop = config.get("Sounds", "advCompressorSound", advCompSound);
        prop.comment = "Sound files to use on operation. Remember to use '/' instead of backslashes and the Sound directory starts on ic2/sounds. Set empty to disable.";
        advCompSound = prop.getString();
        advExtcSound = config.get("Sounds", "advExtractorSound", advExtcSound).getString();
        advMaceSound = config.get("Sounds", "advMaceratorSound", advMaceSound).getString();
        
        prop = config.get("Sounds", "interuptSound", interruptSound);
        prop.comment = "Sound played when a machine process is interrupted";
        interruptSound = prop.getString();
        
        prop = config.get("OPness control", "baseEnergyConsumption", "3");
        prop.comment = "Base power draw per work tick.";
        defaultEnergyConsume = prop.getInt(3);
        
        prop = config.get("OPness control", "baseMachineAcceleration", "2");
        prop.comment = "Base machine speedup each work tick.";
        defaultAcceleration = prop.getInt(2);
        
        prop = config.get("OPness control", "overClockSpeedBonus", "500");
        prop.comment = "How much additional max speed does each Overclocker grant.";
        overClockSpeedBonus = prop.getInt(500);
        
        prop = config.get("OPness control", "overClockEnergyRatio", "2.0");
        prop.comment = "Exponent of Overclocker energy consumption function. 2.0 equals a squared rise of power draw as you add Overclockers.";
        overClockEnergyRatio = Double.valueOf(prop.getString());
        
        prop = config.get("OPness control", "overClockAccelRatio", "1.6");
        prop.comment = "Exponent of Overclocker acceleration function. 1.6 equals a less-than-squared rise of acceleration speed as you add Overclockers.";
        overClockAccelRatio = Double.valueOf(prop.getString());
        
        prop = config.get("OPness control", "overLoadInputRatio", "4.0");
        prop.comment = "Exponent of Transformer input function. Determines rise of maximum power intake as you add Transformers.";
        overLoadInputRatio = Double.valueOf(prop.getString());
        
        proxy.load();

        GameRegistry.registerTileEntity(TileEntityRotaryMacerator.class, "Rotary Macerator");
        GameRegistry.registerTileEntity(TileEntitySingularityCompressor.class, "Singularity Compressor");
        GameRegistry.registerTileEntity(TileEntityCentrifugeExtractor.class, "Centrifuge Extractor");
        
        NetworkRegistry.instance().registerGuiHandler(this, this);
    }
    
    @EventHandler
    public void afterModsLoaded(FMLPostInitializationEvent evt)
    {
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Rotary Macerator Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackRotaryMacerator,
                    new Object[] {"CRC", "PMP", "PAP",
                Character.valueOf('C'), Items.getItem("coil"),
                Character.valueOf('R'), Items.getItem("elemotor"),
                Character.valueOf('P'), Items.getItem("plateiron"),
                Character.valueOf('M'), Items.getItem("macerator"),
                Character.valueOf('A'), Items.getItem("advancedMachine")});
        }
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Singularity Compressor Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackSingularityCompressor,
                    new Object[] {"RGR", "MMM", "PAP",
                Character.valueOf('R'), Block.obsidian,
                Character.valueOf('G'), Items.getItem("reinforcedGlass"),
                Character.valueOf('P'), Items.getItem("plateiron"),
                Character.valueOf('M'), Items.getItem("compressor"),
                Character.valueOf('A'), Items.getItem("advancedMachine")});
        }
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Centrifuge Extractor Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackCentrifugeExtractor,
                    new Object[] {"CEC", "RMR", "PAP",
                Character.valueOf('C'), Items.getItem("coil"),
                Character.valueOf('E'), Items.getItem("elemotor"),
                Character.valueOf('R'), Items.getItem("electrolyzedWaterCell"),
                Character.valueOf('P'), Items.getItem("plateiron"),
                Character.valueOf('M'), Items.getItem("extractor"),
                Character.valueOf('A'), Items.getItem("advancedMachine")});
        }
        
        config.save();
        
        overClockerStack = Items.getItem("overclockerUpgrade");
        transformerStack = Items.getItem("transformerUpgrade");
        energyStorageUpgradeStack = Items.getItem("energyStorageUpgrade");
        ejectorUpgradeStack = Items.getItem("ejectorUpgrade");
    }
    
    public static boolean explodeMachineAt(World world, int x, int y, int z)
    {
		try 
		{
			Class<?> mainIC2Class = Class.forName("IC2");
			mainIC2Class.getMethod("explodeMachineAt", World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE).invoke(null, world, x, y, z);
			return true;
		}
		catch (Exception e) 
		{
			return false;
		}
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        
        if (te != null)
        {
            if (te instanceof TileEntityRotaryMacerator)
            {
                return ((TileEntityRotaryMacerator)te).getGuiContainer(player.inventory);
            }
            else if (te instanceof TileEntityCentrifugeExtractor)
            {
                return ((TileEntityCentrifugeExtractor)te).getGuiContainer(player.inventory);
            }
            else if (te instanceof TileEntitySingularityCompressor)
            {
                return ((TileEntitySingularityCompressor)te).getGuiContainer(player.inventory);
            }
        }
        
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return proxy.getGuiElementForClient(ID, player, world, x, y, z);
    }
    
    // PROXY SERVERSIDE NOOP METHODS
    @Override
    public void load()
    {
        // NOOP
    }

    @Override
    public Object getGuiElementForClient(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        // NOOP
        return null;
    }
}
