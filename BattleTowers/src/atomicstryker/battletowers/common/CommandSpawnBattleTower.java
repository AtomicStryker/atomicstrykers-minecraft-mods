package atomicstryker.battletowers.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandSpawnBattleTower extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "spawnbattletower";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/spawnbattletower spawns a Battletower with supplied data x,y,z,type,underground";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (astring.length < 5)
        {
            throw new WrongUsageException("Invalid Usage of Battletower spawn command", (Object)astring);
        }
        else
        {
            try
            {
                int x = Integer.valueOf(astring[0]);
                int y = Integer.valueOf(astring[1]);
                int z = Integer.valueOf(astring[2]);
                int type = Integer.valueOf(astring[3]);
                boolean underground = Boolean.valueOf(astring[4]);
                WorldGenHandler.generateTower(icommandsender.getEntityWorld(), x, y, z, type, underground);
                notifyAdmins(icommandsender, "Battletower spawned", (Object)astring);
            }
            catch (Exception e)
            {
                throw new WrongUsageException("Invalid Usage of Battletower spawn command", (Object)astring);
            }
        }
    }

}
