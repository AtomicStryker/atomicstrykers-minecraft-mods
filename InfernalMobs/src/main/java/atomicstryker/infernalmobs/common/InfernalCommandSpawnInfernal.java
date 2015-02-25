package atomicstryker.infernalmobs.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.Level;

public class InfernalCommandSpawnInfernal extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "spawninfernal";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/spawninfernal x y z ENTCLASS X spawns an Infernal Mob of class ENTCLASS at x, y, z with Modifiers X";
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 5)
        {
            throw new WrongUsageException("Invalid Usage of SpawnInfernal command, too few arguments", (Object)args);
        }
        else
        {
            try
            {
                final int x = (args[0].equals("~")) ? sender.getCommandSenderEntity().getPosition().getX() : Integer.valueOf(args[0]);
                final int y = (args[1].equals("~")) ? sender.getCommandSenderEntity().getPosition().getY() : Integer.valueOf(args[1]);
                final int z = (args[2].equals("~")) ? sender.getCommandSenderEntity().getPosition().getZ() : Integer.valueOf(args[2]);
                String modifier = args[4];
                for (int i = 5; i < args.length; i++)
                {
                    modifier = modifier + " " + args[i];
                }
                
                final Class<? extends EntityLivingBase> entClass = (Class<? extends EntityLivingBase>) EntityList.stringToClassMapping.get(args[3]);
                if (entClass != null)
                {
                    EntityLivingBase mob = entClass.getConstructor(World.class).newInstance(sender.getEntityWorld());
                    mob.setPosition(x+0.5, y+0.5, z+0.5);
                    sender.getEntityWorld().spawnEntityInWorld(mob);
                    
                    InfernalMobsCore.proxy.getRareMobs().remove(mob);
                    InfernalMobsCore.instance().addEntityModifiersByString(mob, modifier);
                    MobModifier mod = InfernalMobsCore.getMobModifiers(mob);
                    if (mod != null)
                    {
                        FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, sender.getCommandSenderName() 
                                + " spawned: "+InfernalMobsCore.getMobModifiers(mob).getLinkedModNameUntranslated() 
                                + " at [" + x + "|" + y + "|" + z + "]");
                    }
                    else
                    {
                        throw new WrongUsageException("Error adding Infernal Modifier "+modifier+" to mob "+mob);
                    }
                }
                else
                {
                    throw new WrongUsageException("Invalid SpawnInfernal command, no Entity ["+args[3]+"] known");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new WrongUsageException("Problem executing SpawnInfernal command, stacktrace printed...");
            }
        }
    }
    
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
