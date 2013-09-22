package atomicstryker.battletowers.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandRegenerateAllBattleTowers extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "regenerateallbattletowers";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/regenerateallbattletowers re-spawns all Battletowers and their golems";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        WorldGenHandler.deleteAllTowers(icommandsender.getEntityWorld(), true);
        notifyAdmins(icommandsender, "Battletowers regenerated", new Object());
    }

}
