package atomicstryker.battletowers.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;

public abstract class CommandBattleTowers extends CommandBase
{

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof ICommand)
        {
            return ((ICommand)o).getCommandName().compareTo(getCommandName());
        }
        return 0;
    }

}
