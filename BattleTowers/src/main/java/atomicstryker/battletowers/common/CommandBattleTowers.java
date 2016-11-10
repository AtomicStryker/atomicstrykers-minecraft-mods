package atomicstryker.battletowers.common;

import net.minecraft.command.CommandBase;

public abstract class CommandBattleTowers extends CommandBase
{

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

}
