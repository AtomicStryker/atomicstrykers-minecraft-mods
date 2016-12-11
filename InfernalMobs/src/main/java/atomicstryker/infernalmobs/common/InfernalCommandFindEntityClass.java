package atomicstryker.infernalmobs.common;

import org.apache.logging.log4j.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class InfernalCommandFindEntityClass extends CommandBase
{

    @Override
    public String getName()
    {
        return "feclass";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/feclass X returns all currently registered Entities containing X in their classname's";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            throw new WrongUsageException("Invalid Usage of FindEntityClass command", (Object) args);
        }
        else
        {
            String classname = args[0];
            for (int i = 1; i < args.length; i++)
            {
                classname = classname + " " + args[i];
            }

            String result = "Found Entity classes: ";
            boolean found = false;
            for (ResourceLocation rsl : EntityList.getEntityNameList())
            {
                String entclass = rsl.getResourcePath();
                if (entclass.toLowerCase().contains(classname.toLowerCase()))
                {
                    if (!found)
                    {
                        result += entclass;
                        found = true;
                    }
                    else
                    {
                        result += (", " + entclass);
                    }
                }
            }

            if (!found)
            {
                result += "Nothing found.";
            }

            FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, sender.getName() + ": " + result);
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

}
