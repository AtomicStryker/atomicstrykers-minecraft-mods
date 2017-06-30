package atomicstryker.battletowers.common;

import org.apache.logging.log4j.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandRegenerateAllBattleTowers extends CommandBattleTowers
{

    @Override
    public String getName()
    {
        return "regenerateallbattletowers";
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "/regenerateallbattletowers re-spawns all Battletowers and their golems";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender icommandsender, String[] astring)
    {
        WorldGenHandler.deleteAllTowers(icommandsender.getEntityWorld(), true);
        AS_BattleTowersCore.LOGGER.log(Level.INFO, icommandsender.getName() + ": Battletowers regenerated");
    }

}
