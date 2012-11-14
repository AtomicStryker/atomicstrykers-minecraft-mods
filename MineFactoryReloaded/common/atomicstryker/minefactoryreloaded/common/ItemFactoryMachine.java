package atomicstryker.minefactoryreloaded.common;

import java.util.List;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore.Machine;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemFactoryMachine extends ItemBlock
{
	private static int highestMachineMeta = 8;
	
	public ItemFactoryMachine(int i)
	{
		super(i);
        setMaxDamage(0);
        setHasSubtypes(true);
	}

	@Override
    public int getIconFromDamage(int i)
    {
    	return Math.min(i, highestMachineMeta);
    }
    
	@Override
    public int getMetadata(int i)
    {
        return i;
    }

	@Override
    public String getItemNameIS(ItemStack itemstack)
    {
        int md = itemstack.getItemDamage();
        if(md > highestMachineMeta)
        {
        	// return highest meta entry
        	return "factoryWeatherItem";
        }
        
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Planter)) return "factoryPlanterItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fisher)) return "factoryFisherItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Harvester)) return "factoryHarvesterItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Rancher)) return "factoryRancherItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fertilizer)) return "factoryFertilizerItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Vet)) return "factoryVetItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Collector)) return "factoryCollectorItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Breaker)) return "factoryBlockBreakerItem";
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Weather)) return "factoryWeatherItem";
	    return "Invalid";
    }
	
	@Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Planter)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fisher)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Harvester)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Rancher)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fertilizer)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Vet)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Collector)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Breaker)));
	    par3List.add(new ItemStack(par1, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Weather)));
    }
}
