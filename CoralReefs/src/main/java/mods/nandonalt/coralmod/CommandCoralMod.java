package mods.nandonalt.coralmod;

import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.Mod;

public class CommandCoralMod extends CommandBase {

	@Override
	public String getCommandName() {
		return "coralmod";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length < 1) {
			final Mod annotation = CoralMod.class.getAnnotation(Mod.class);
			sendChatToPlayer(sender, annotation.name() + ", v" + annotation.version());
		} else {
			if(args[0].equals("regen")) {
				EntityPlayerMP player = getCommandSenderAsPlayer(sender);
				Random random = new Random(player.worldObj.getSeed());
				final int chunkX = MathHelper.floor_double(player.posX) >> 4;
				final int chunkZ = MathHelper.floor_double(player.posZ) >> 4;
				long i1 = random.nextLong() / 2L * 2L + 1L;
				long j1 = random.nextLong() / 2L * 2L + 1L;
				random.setSeed((long)chunkX * i1 + (long)chunkZ * j1 ^ player.worldObj.getSeed());
				if(CoralGenerator.generate(random, chunkX, chunkZ, player.worldObj)) {
					sendChatToPlayer(sender, "Re-generated coral at: " + chunkX + ", " + chunkZ);
				} else {
					sendChatToPlayer(sender, "Couldn't generate coral at: " + chunkX + ", " + chunkZ);
				}
			} else if(args[0].equals("settings")) {
				sendChatToPlayer(sender, GREEN + "===CoralMod Settings===");
				for(String setting : CoralMod.getAllowedFields()) {
					sendChatToPlayer(sender, GREEN + setting + ": " + WHITE + CoralMod.getValue(setting));
				}
			} else if(CoralMod.getAllowedFields().contains(args[0])) {
				if(CoralMod.toggle(args[0])) {
					sendChatToPlayer(sender, args[0] + ": " + CoralMod.getValue(args[0]));
					CoralMod.instance.updateSettings();
				} else {
					sendChatToPlayer(sender, "Couldn't toggle " + args[0]);
				}
			} else {
				throw new WrongUsageException("[regen|settings|SETTING]", new Object[0]);
			}
		}
	}

	private void sendChatToPlayer(ICommandSender sender, String msg) {
		sender.addChatMessage(new ChatComponentText(msg));
	}

	@SuppressWarnings("rawtypes")
    @Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		List<String> tabCompletionOptions = new ArrayList<String>();
		tabCompletionOptions.addAll(CoralMod.getAllowedFields());
		Collections.sort(tabCompletionOptions);
		tabCompletionOptions.add(0, "regen");
		tabCompletionOptions.add(1, "settings");
		return args.length == 1 ? getListOfStringsFromIterableMatchingLastWord(args, tabCompletionOptions) : null;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + getCommandName() + " [regen|settings|SETTING]";
	}

    @Override
    public int compareTo(Object o)
    {
        return 0;
    }
}