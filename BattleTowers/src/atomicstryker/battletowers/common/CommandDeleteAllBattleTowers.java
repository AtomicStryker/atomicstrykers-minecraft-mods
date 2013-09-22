package atomicstryker.battletowers.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandDeleteAllBattleTowers extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "deleteallbattletowers";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/deleteallbattletowers deletes all existing Battletowers, as logged in save file";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        WorldGenHandler.deleteAllTowers(icommandsender.getEntityWorld(), false);
        notifyAdmins(icommandsender, "All Battletowers deleted", new Object());
    }

}
