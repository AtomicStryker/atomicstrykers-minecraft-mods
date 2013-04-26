package ic2.api;

import java.lang.reflect.Array;

import net.minecraft.item.ItemStack;

/**
 * Provides access to Compressor, Extractor and Macerator recipes, as well as charge-aware recipes
 * and the Recycler blacklist.
 *
 * The recipes are only valid after IC2 has been loaded and are metadata and stack size sensitive,
 * for example you can create a recipe to compress 3 wooden planks into 2 sticks.
 */
public final class Ic2Recipes {
	/**
	 * Add a charge-aware shaped crafting recipe.
	 */
	public static void addCraftingRecipe(ItemStack result, Object... args) {
		try {
			Class.forName(getPackage() + ".core.AdvRecipe").getMethod("addAndRegister", ItemStack.class, Array.newInstance(Object.class, 0).getClass()).invoke(null, result, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a charge-aware shapeless crafting recipe.
	 */
	public static void addShapelessCraftingRecipe(ItemStack result, Object... args) {
		try {
			Class.forName(getPackage() + ".core.AdvShapelessRecipe").getMethod("addAndRegister", ItemStack.class, Array.newInstance(Object.class, 0).getClass()).invoke(null, result, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the base IC2 package name, used internally.
	 *
	 * @return IC2 package name, if unable to be determined defaults to ic2
	 */
	private static String getPackage() {
		Package pkg = Ic2Recipes.class.getPackage();
		if (pkg != null) return pkg.getName().substring(0, pkg.getName().lastIndexOf('.'));
		else return "ic2";
	}
}

