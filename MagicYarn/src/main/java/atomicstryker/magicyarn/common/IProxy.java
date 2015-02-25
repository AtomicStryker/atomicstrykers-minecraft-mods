package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IProxy
{
    
    public void preInit(File configFile);
    
    public void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld);

    public void init();
    
}
