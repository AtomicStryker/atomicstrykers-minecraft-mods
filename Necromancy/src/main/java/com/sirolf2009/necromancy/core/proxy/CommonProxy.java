package com.sirolf2009.necromancy.core.proxy;

import com.sirolf2009.necromancy.Necromancy;

import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy
{

    public void preInit()
    {
    }

    public void init()
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(Necromancy.instance, Necromancy.packetHandler);
    }

    public int addArmour(String path)
    {
        return 0;
    }

    public void refreshTextures()
    {
    }

}