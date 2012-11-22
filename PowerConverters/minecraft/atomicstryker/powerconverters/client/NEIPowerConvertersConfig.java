package atomicstryker.powerconverters.client;

import atomicstryker.powerconverters.common.PowerConverterCore;
import net.minecraft.src.ItemStack;
import codechicken.nei.MultiItemRange;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIPowerConvertersConfig implements IConfigureNEI
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
        int blockID = PowerConverterCore.powerConverterBlock.blockID;
        for (int i = 0; i <= 7; i++)
        {
            subTypes.add(blockID, i, i);
        }
        API.addSetRange("Blocks.PowerConverters", subTypes);
    }

    @Override
    public String getName()
    {
        return "Power Converters";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

}
