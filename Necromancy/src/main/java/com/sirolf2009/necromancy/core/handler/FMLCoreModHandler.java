package com.sirolf2009.necromancy.core.handler;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class FMLCoreModHandler implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return null;
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
        return "com.sirolf2009.necromancy.core.handler.AccessTransformHandler";
    }

}
