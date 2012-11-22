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
        subTypes.add(PowerConverterCore.jetpackFuellerItem);
        subTypes.add(PowerConverterCore.powerConverterBlock, 0, 7);
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
