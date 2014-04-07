package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.util.IIcon;

import com.sirolf2009.necromancy.item.ItemIsaacsHead;


public class RenderTearBlood extends RenderTear
{
    
    @Override
    protected IIcon getTearIcon()
    {
        return ItemIsaacsHead.tearBloodIcon;
    }

}
