package atomicstryker.battletowers.common;

import org.apache.logging.log4j.Level;

import atomicstryker.battletowers.common.WorldGenHandler.TowerPosition;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;

public class CommandDeleteBattleTower extends CommandBattleTowers
{

    @Override
    public String getName()
    {
        return "deletebattletower";
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "/deletebattletower deletes the nearest existing Battletower, given x,z coordinates";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender icommandsender, String[] astring) throws CommandException
    {
        if (astring.length < 2)
        {
            throw new WrongUsageException("Invalid Usage of Battletower delete command, must provide x,z coordinates", (Object) astring);
        }
        else
        {
            try
            {
                int x = Integer.valueOf(astring[0]);
                int z = Integer.valueOf(astring[1]);
                TowerPosition tp = WorldGenHandler.deleteNearestTower(icommandsender.getEntityWorld(), x, z);
                if (tp != null)
                {
                    AS_BattleTowersCore.LOGGER.log(Level.INFO, icommandsender.getName() + ": Battletower deleted: " + tp.toString());
                    for (Object o : icommandsender.getEntityWorld().getEntitiesWithinAABB(AS_EntityGolem.class, new AxisAlignedBB(tp.x - 10, 0.0D, tp.z - 10, tp.x + 10, 255, tp.z + 10)))
                    {
                        ((Entity) o).setDead();
                        break;
                    }
                }
                else
                {
                    AS_BattleTowersCore.LOGGER.log(Level.INFO, icommandsender.getName() + ": no Battletower deleted, no valid target");
                }
            }
            catch (Exception e)
            {
                throw new WrongUsageException("Invalid Usage of Battletower delete command, must provide x,z coordinates", (Object) astring);
            }
        }
    }

}
