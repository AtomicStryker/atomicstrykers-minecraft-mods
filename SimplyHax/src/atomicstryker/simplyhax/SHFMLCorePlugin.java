package atomicstryker.simplyhax;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class SHFMLCorePlugin implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "atomicstryker.simplyhax.SHTransformer" };
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
