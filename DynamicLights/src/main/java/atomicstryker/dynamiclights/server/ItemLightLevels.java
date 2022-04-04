package atomicstryker.dynamiclights.server;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ItemLightLevels {
	private static final Logger LOGGER = LogManager.getLogger();
	private static Map<String, Map<Item, Integer>> cache = new HashMap<>();

	public static void clearCache() {
		LOGGER.info("Clearing item tag to light level mapping cache");
		cache.clear();
	}

	public static int getLightFromItemStack(ItemStack stack, String tagName) {
		if(stack == null || stack.isEmpty()) {
			return 0;
		}

		Map<Item, Integer> innerCache = cache.computeIfAbsent(tagName, s -> new HashMap<>());

		// 1.18.2: return innerCache.computeIfAbsent(stack.getItem(), item1 -> stack.getTags().map(t -> getLightLevelByTagName(t.location().toString(), tagName)).filter(t -> t > 0 && t <= 15).max(Integer::compareTo).orElse(0));
		return innerCache.computeIfAbsent(stack.getItem(), item1 -> stack.getItem().getTags().stream().map(t -> getLightLevelByTagName(t.toString(), tagName)).filter(t -> t > 0 && t <= 15).max(Integer::compareTo).orElse(0));
	}

	private static int getLightLevelByTagName(String testee, String tagName) {
		String prefix = DynamicLights.MOD_ID + ":" + tagName;
		if(!testee.startsWith(prefix)) {
			return 0;
		}

		if(testee.equals(prefix)) {
			return 15;
		}

		int level = 0;
		try {
			String suffix = testee.substring(prefix.length()+1);
			level = Integer.parseInt(suffix);
		} catch (Exception ignored) {}

		return level;
	}
}