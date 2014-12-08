package atomicstryker.multimine.common.fmlmagic;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// for testing, add to cpw.mods.fml.relauncher.RelaunchLibraryManager.rootPlugins

public class MMFMLCorePlugin implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "atomicstryker.multimine.common.fmlmagic.MMTransformer" };
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

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

}
