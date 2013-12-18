package atomicstryker.stalkercreepers.common;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class SCFMLCorePlugin implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "atomicstryker.stalkercreepers.common.SCTransformer" };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
    }

}
