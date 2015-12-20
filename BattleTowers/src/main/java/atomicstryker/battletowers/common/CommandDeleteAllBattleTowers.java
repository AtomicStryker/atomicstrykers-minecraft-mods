package atomicstryker.battletowers.common;

import org.apache.logging.log4j.Level;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandDeleteAllBattleTowers extends CommandBattleTowers
{

    @Override
    public String getCommandName()
    {
        return "deleteallbattletowers";
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
        FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, icommandsender.getName()+": All Battletowers deleted");
    }

}
