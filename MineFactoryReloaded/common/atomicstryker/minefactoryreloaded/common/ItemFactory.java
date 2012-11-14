package atomicstryker.minefactoryreloaded.common;

import atomicstryker.minefactoryreloaded.client.MineFactoryClient;
import net.minecraft.src.Item;

public class ItemFactory extends Item
{
	public ItemFactory(int i)
	{
		super(i);
	}
	
	@Override
	public String getTextureFile()
	{
        return MineFactoryReloadedCore.itemTexture;
	}
}
