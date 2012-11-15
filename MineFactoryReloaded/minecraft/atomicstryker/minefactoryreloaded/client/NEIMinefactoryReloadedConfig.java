package atomicstryker.minefactoryreloaded.client;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore.Machine;
import net.minecraft.src.ItemStack;
import codechicken.nei.MultiItemRange;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIMinefactoryReloadedConfig implements IConfigureNEI
{

    @Override
    public void loadConfig()
    {
        try
        {
            addSubSet();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addSubSet()
    {
        MultiItemRange subTypes = new MultiItemRange();
        int blockID = MineFactoryReloadedCore.machineBlock.blockID;
        for (int i = 0; i <= 8; i++)
        {
            subTypes.add(blockID, i, i);
        }
        
        subTypes.add(MineFactoryReloadedCore.conveyorBlockId.getInt(), 0, 0);
        
        subTypes.add(MineFactoryReloadedCore.steelIngotItem, 0, 0);
        subTypes.add(MineFactoryReloadedCore.factoryHammerItem, 0, 0);
        subTypes.add(MineFactoryReloadedCore.milkItem, 0, 0);
        
        API.addSetRange("Minefactory", subTypes);
    }

    @Override
    public String getName()
    {
        return "Minefactory Reloaded";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

}