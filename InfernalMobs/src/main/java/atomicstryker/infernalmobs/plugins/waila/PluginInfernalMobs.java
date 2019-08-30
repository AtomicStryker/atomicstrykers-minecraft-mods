package atomicstryker.infernalmobs.plugins.waila;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class PluginInfernalMobs implements IWailaEntityProvider {
	
	static IWailaEntityProvider INSTANCE = new PluginInfernalMobs();
	
	@Nonnull
	@Override
	public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
		if (config.getConfig(WailaRegistrar.CONFIG_INFERNALMOBS_MODS, true) && entity instanceof EntityLiving) {
			EntityLiving entityLiving = (EntityLiving) entity;
			
			// Check if entity has mods
			if (InfernalMobsCore.getIsRareEntity(entityLiving)) {
				// Get all mod names and add as single line to body tip
				MobModifier mods = InfernalMobsCore.getMobModifiers(entityLiving);
				String[] allMods = mods.getLinkedModName().split(" ");
				
				// Sort mods alphanumerically
				Arrays.sort(allMods);
				
				if (config.getConfig(WailaRegistrar.CONFIG_INFERNALMOBS_LINEBREAK, true)) {
					// If infernalmobs.linebreak was set true, add a linebreak if the line would
					// exceed 40 characters with the next modifier name, given the line is not empty.
					int maxChars = 40;
					String currLine = "";
					for (int i = 0; i < allMods.length; i++) {
						String currMod = allMods[i];
						if (currLine.length() > 0 && currLine.length() + currMod.length() > maxChars) {
							currenttip.add(currLine);
							currLine = "";
						}
						currLine = currLine.concat(currMod);
						if (i + 1 < allMods.length)
							currLine = currLine.concat(", ");
					}
					if (currLine.length() > 0)
						currenttip.add(currLine);
				} else {
					// Otherwise just join the modifier names with a comma.
					String modString = String.join(", ", allMods);
					currenttip.add(modString);
				}
			}
		}
		return currenttip;
	}
}
