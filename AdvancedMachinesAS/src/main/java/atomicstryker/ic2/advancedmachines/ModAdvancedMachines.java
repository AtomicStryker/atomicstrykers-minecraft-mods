package atomicstryker.ic2.advancedmachines;


import ic2.api.item.IC2Items;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.ContainerOreWashing;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "AdvancedMachines", name = "IC2 Advanced Machines Addon", version = "1.1.0", dependencies = "required-after:IC2@2.1.478")
public class ModAdvancedMachines implements IGuiHandler, IProxy
{
    
    @SidedProxy(clientSide = "atomicstryker.ic2.advancedmachines.client.AdvancedMachinesClient", serverSide = "atomicstryker.ic2.advancedmachines.ModAdvancedMachines")
    public static IProxy proxy;
    
    @Instance(value = "AdvancedMachines")
    public static ModAdvancedMachines instance;
    
    private Configuration config;
    
    private Block blockAdvancedMachine;
    private ItemStack stackRotaryMacerator;
    private ItemStack stackSingularityCompressor;
    private ItemStack stackCentrifugeExtractor;
    private ItemStack stackCombinedRecycler;
    private ItemStack stackRotaryOreWasher;
    
    public int maxMachineSpeedUpFactor;
    public int maxMachineSpeedUpTicks;
    public double machinePowerDrawFactor;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        blockAdvancedMachine = new BlockAdvancedMachines();
        blockAdvancedMachine.setCreativeTab(CreativeTabs.tabRedstone);
        
        GameRegistry.registerBlock(blockAdvancedMachine, ItemAdvancedMachine.class, "blockAdvMachine");
        
        stackRotaryMacerator = new ItemStack(blockAdvancedMachine, 1, 0);
        stackSingularityCompressor = new ItemStack(blockAdvancedMachine, 1, 1);
        stackCentrifugeExtractor = new ItemStack(blockAdvancedMachine, 1, 2);
        stackCombinedRecycler = new ItemStack(blockAdvancedMachine, 1, 3);
        stackRotaryOreWasher = new ItemStack(blockAdvancedMachine, 1, 4);
        
        maxMachineSpeedUpFactor = config.get(Configuration.CATEGORY_GENERAL, "maxMachineSpeedUpFactor", 10, "Advanced Machines will reach X times the speed of normal machines").getInt(10);
        maxMachineSpeedUpTicks = config.get(Configuration.CATEGORY_GENERAL, "maxMachineSpeedUpTicks", 10000, "Advanced Machines will take X ingame ticks to reach max speed").getInt(10000);
        machinePowerDrawFactor = config.get(Configuration.CATEGORY_GENERAL, "machinePowerDrawFactor", 3.0d, "Advanced Machines will draw X times the normal machines power").getDouble(3.0d);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {        
        GameRegistry.registerTileEntity(TileEntityAdvancedMacerator.class, "Rotary Macerator");
        GameRegistry.registerTileEntity(TileEntityAdvancedCompressor.class, "Singularity Compressor");
        GameRegistry.registerTileEntity(TileEntityAdvancedExtractor.class, "Centrifuge Extractor");
        GameRegistry.registerTileEntity(TileEntityAdvancedRecycler.class, "Combined Recycler");
        GameRegistry.registerTileEntity(TileEntityAdvancedOreWasher.class, "Rotary Ore Washer");
        
        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);
        
        proxy.load();
    }
    
    @EventHandler
    public void afterModsLoaded(FMLPostInitializationEvent evt)
    {
        if (config.get(Configuration.CATEGORY_GENERAL, "Rotary Macerator Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackRotaryMacerator,
                    new Object[] {"CRC", "PMP", "PAP",
                Character.valueOf('C'), IC2Items.getItem("coil"),
                Character.valueOf('R'), IC2Items.getItem("elemotor"),
                Character.valueOf('P'), IC2Items.getItem("plateiron"),
                Character.valueOf('M'), IC2Items.getItem("macerator"),
                Character.valueOf('A'), IC2Items.getItem("advancedMachine")});
        }
        
        Recipes.macerator.addRecipe(new IdentRecipe(new ItemStack(Blocks.netherrack)), new NBTTagCompound(), new ItemStack(Blocks.netherrack));
        Recipes.macerator.addRecipe(new IdentRecipe(new ItemStack(Blocks.quartz_ore)), new NBTTagCompound(), new ItemStack(Blocks.quartz_ore));
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Singularity Compressor Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackSingularityCompressor,
                    new Object[] {"RGR", "MMM", "PAP",
                Character.valueOf('R'), Blocks.obsidian,
                Character.valueOf('G'), IC2Items.getItem("reinforcedGlass"),
                Character.valueOf('P'), IC2Items.getItem("plateiron"),
                Character.valueOf('M'), IC2Items.getItem("compressor"),
                Character.valueOf('A'), IC2Items.getItem("advancedMachine")});
        }
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Centrifuge Extractor Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackCentrifugeExtractor,
                    new Object[] {"CEC", "RMR", "PAP",
                Character.valueOf('C'), IC2Items.getItem("coil"),
                Character.valueOf('E'), IC2Items.getItem("elemotor"),
                Character.valueOf('R'), IC2Items.getItem("electrolyzedWaterCell"),
                Character.valueOf('P'), IC2Items.getItem("plateiron"),
                Character.valueOf('M'), IC2Items.getItem("extractor"),
                Character.valueOf('A'), IC2Items.getItem("advancedMachine")});
        }
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Combined Recycler Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackCombinedRecycler,
                    new Object[] {" M ", "PEP", "PRP",
                Character.valueOf('M'), stackRotaryMacerator,
                Character.valueOf('E'), stackCentrifugeExtractor,
                Character.valueOf('P'), IC2Items.getItem("plateiron"),
                Character.valueOf('R'), IC2Items.getItem("recycler")});
        }
        
        if (config.get(Configuration.CATEGORY_GENERAL, "Rotary Ore Washer Enabled", true).getBoolean(true))
        {
            GameRegistry.addRecipe(stackRotaryOreWasher,
                    new Object[] {"CRC", "PWP", "PAP",
                Character.valueOf('C'), IC2Items.getItem("coil"),
                Character.valueOf('R'), IC2Items.getItem("elemotor"),
                Character.valueOf('P'), IC2Items.getItem("plateiron"),
                Character.valueOf('W'), IC2Items.getItem("orewashingplant"),
                Character.valueOf('A'), IC2Items.getItem("advancedMachine")});
        }
        
        config.save();
    }
    
    private class IdentRecipe implements IRecipeInput
    {
        
        private ArrayList<ItemStack> inputresult;
        
        private IdentRecipe(ItemStack toProcess)
        {
            inputresult = new ArrayList<ItemStack>();
            inputresult.add(toProcess);
        }

        @Override
        public int getAmount()
        {
            return 1;
        }

        @Override
        public List<ItemStack> getInputs()
        {
            return inputresult;
        }

        @Override
        public boolean matches(ItemStack itemStack)
        {
            return inputresult.get(0).isItemEqual(itemStack);
        }
        
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntityStandardMachine tesm = (TileEntityStandardMachine) world.getTileEntity(x, y, z);
        if (tesm != null)
        {
            if (tesm instanceof TileEntityAdvancedMacerator)
            {
                return new ContainerAdvancedMacerator(player, (TileEntityAdvancedMacerator) tesm);
            }
            else if (tesm instanceof TileEntityAdvancedOreWasher)
            {
                return new ContainerOreWashing(player, (TileEntityOreWashing) tesm);
            }
            
            return new ContainerAdvancedMachine<TileEntityStandardMachine>(player, tesm);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return proxy.getGuiElementForClient(ID, player, world, x, y, z);
    }
    
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
