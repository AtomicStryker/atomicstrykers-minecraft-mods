package atomicstryker.battletowers.common;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.Level;

public class CommandSpawnBattleTower extends CommandBattleTowers
{

    @Override
    public String getName()
    {
        return "spawnbattletower";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/spawnbattletower spawns a Battletower with supplied data x,y,z,type,underground";
    }

    @Override
    public void execute(ICommandSender icommandsender, String[] astring) throws CommandException
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
                FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, icommandsender.getName() + ": Battletower spawned");
            }
            catch (Exception e)
            {
                throw new WrongUsageException("Invalid Usage of Battletower spawn command", (Object)astring);
            }
        }
    }

}
