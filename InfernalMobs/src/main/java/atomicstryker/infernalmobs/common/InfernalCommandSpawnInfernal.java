package atomicstryker.infernalmobs.common;

import org.apache.logging.log4j.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class InfernalCommandSpawnInfernal extends CommandBase
{

    @Override
    public String getName()
    {
        return "spawninfernal";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/spawninfernal x y z ENTCLASS X spawns an Infernal Mob of class ENTCLASS at x, y, z with Modifiers X";
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 5)
        {
            throw new WrongUsageException("Invalid Usage of SpawnInfernal command, too few arguments", (Object) args);
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

                Class<? extends EntityLivingBase> entClass = null;
                for (ResourceLocation rsl : EntityList.getEntityNameList())
                {
                    if (rsl.getResourcePath().contains(args[3]))
                    {
                        Object o = EntityList.getClass(rsl);
                        if (o.getClass().isAssignableFrom(EntityLiving.class))
                        {
                            entClass = (Class<? extends EntityLivingBase>) o;
                        }
                    }
                }
                if (entClass != null)
                {
                    EntityLivingBase mob = entClass.getConstructor(World.class).newInstance(sender.getEntityWorld());
                    mob.setPosition(x + 0.5, y + 0.5, z + 0.5);
                    sender.getEntityWorld().spawnEntity(mob);

                    InfernalMobsCore.proxy.getRareMobs().remove(mob);
                    InfernalMobsCore.instance().addEntityModifiersByString(mob, modifier);
                    MobModifier mod = InfernalMobsCore.getMobModifiers(mob);
                    if (mod != null)
                    {
                        InfernalMobsCore.LOGGER.log(Level.INFO,
                                sender.getName() + " spawned: " + InfernalMobsCore.getMobModifiers(mob).getLinkedModNameUntranslated() + " at [" + x + "|" + y + "|" + z + "]");
                    }
                    else
                    {
                        throw new WrongUsageException("Error adding Infernal Modifier " + modifier + " to mob " + mob);
                    }
                }
                else
                {
                    throw new WrongUsageException("Invalid SpawnInfernal command, no Entity [" + args[3] + "] known");
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

}
