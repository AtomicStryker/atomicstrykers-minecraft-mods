package atomicstryker.battletowers.common;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.Level;

public class CommandDeleteAllBattleTowers extends CommandBattleTowers
{

    @Override
    public String getName()
    {
        return "deleteallbattletowers";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/deleteallbattletowers deletes all existing Battletowers, as logged in save file";
    }

    @Override
    public void execute(ICommandSender icommandsender, String[] astring)
    {
        WorldGenHandler.deleteAllTowers(icommandsender.getEntityWorld(), false);
        FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, icommandsender.getName()+": All Battletowers deleted");
    }

}
