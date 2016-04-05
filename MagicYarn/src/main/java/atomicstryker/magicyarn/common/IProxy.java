package atomicstryker.magicyarn.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.io.File;

public interface IProxy
{
    
    void preInit(File configFile);
    
    void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld);

    void init();
    
}
