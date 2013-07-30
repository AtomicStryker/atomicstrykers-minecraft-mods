package ic2.advancedmachines.client;

import ic2.advancedmachines.common.AdvancedMachines;
import codechicken.nei.MultiItemRange;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;


public class NEIAdvancedMachinesConfig implements IConfigureNEI
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
        MultiItemRange advancedMachines = new MultiItemRange();
        int blockID = AdvancedMachines.blockAdvancedMachine.blockID;
        advancedMachines.add(blockID, 0, 0);
        advancedMachines.add(blockID, 1, 1);
        advancedMachines.add(blockID, 2, 2);
        API.addSetRange("IC2.AdvancedMachines", advancedMachines);
    }

    @Override
    public String getName()
    {
        return "AdvancedMachines";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

}