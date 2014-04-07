package com.sirolf2009.necromancy.core.handler;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class AccessTransformHandler extends AccessTransformer
{

    public AccessTransformHandler() throws IOException
    {
        super("necromancy_at.cfg");
    }

}
