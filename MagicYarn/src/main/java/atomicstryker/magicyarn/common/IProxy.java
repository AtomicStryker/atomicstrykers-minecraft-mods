package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IProxy
{
    
    void preInit(File configFile);
    
    void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld);

    void init();
    
}
