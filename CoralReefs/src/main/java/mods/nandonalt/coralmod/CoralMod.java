// CoralReef Mod
// Original author: Nandonalt
// Current maintainer: q3hardcore
// Special thanks to: fry, OvermindDL1

package mods.nandonalt.coralmod;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "coralmod", name="CoralReef Mod", version="1.7.2")
public class CoralMod {

	@Instance("coralmod")
	public static CoralMod instance;

	@SidedProxy(
		clientSide="mods.nandonalt.coralmod.client.ClientProxy",
		serverSide="mods.nandonalt.coralmod.CommonProxy"
	)

	/**
	 * Proxy instance
	 */
	public static CommonProxy proxy;

	/**
	 * Descriptions for toggles
	 */
	public static final String[] DESCRIPTIONS;

	/**
	 * List of safe fields for toggling
	 */
	private static final List<String> allowedFields;

	// Settings
	private static boolean spiky = true;
	private static int size = 1;
	private static boolean enable = true;
	private static boolean bubble = true;
	private static boolean grow = false;
	private static boolean ocean = true;
	private static boolean land = false;
	private static boolean dimensions = false;

	// Generation settings
	private static int minHeight = 0;
	private static int maxHeight = 128;
	private static int radius = 16;
	private static int iterations = 80;
	private static String biomes = "";

	private static boolean listBiomes = true;

	// Coral blocks
	protected static Block Coral1;
	protected static Block Coral2;
	protected static Block Coral3;
	protected static Block Coral4;
	protected static Block Coral5;

	/**
	 * Directory for storing configuration
	 */
	private File configDir;

	/**
	 * Configuration
	 */
	private Configuration config;

	/**
	 * Pre-initialization
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		// Set configuration directory
		configDir = new File(evt.getModConfigurationDirectory(), "coralreef");

		// Configure block IDs
		final Configuration blockIDs = new Configuration(new File(configDir, "blockids.cfg"));
		blockIDs.save();

		// Instantiate blocks
		Coral1 = new BlockCoral(1);
		Coral1.setHardness(0.2F).setStepSound(Block.soundTypeStone).setBlockName("Coral1");

		Coral2 = new BlockCoral2(0);
		Coral2.setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("Coral2");

		Coral3 = new BlockCoral2(1);
		Coral3.setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("Coral3");

		Coral4 = new BlockCoral(6);
		Coral4.setHardness(0.2F).setStepSound(Block.soundTypeStone).setBlockName("Coral4");

		Coral5 = new BlockCoral(6);
		Coral5.setHardness(0.2F).setStepSound(Block.soundTypeStone).setLightLevel(1.0F).setBlockName("CoralLightt");

		// Register blocks
		GameRegistry.registerBlock(Coral1, ItemCoral.class, "Coral1");
		GameRegistry.registerBlock(Coral2, "Coral2");
		GameRegistry.registerBlock(Coral3, "Coral3");
		GameRegistry.registerBlock(Coral4, ItemCoral.class, "Coral4");
		GameRegistry.registerBlock(Coral5, ItemCoral.class, "Coral5");

		// Add recipes
		addRecipes();
		
		proxy.proxyPreInit();
	}

	/**
	 * Initialize
	 */
	@EventHandler
	public void init(FMLInitializationEvent evt) {
		// Load settings
		loadSettings();

		// Client setup
		proxy.proxyInit();

		// Register world generation hook
		MinecraftForge.EVENT_BUS.register(new CoralGenerator());
	}

	/**
         * Register command
         */
	@EventHandler
	public void severStarting(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new CommandCoralMod());
	}

	/**
	 * Adds dye recipes for coral
	 */
	private void addRecipes() {
		Item dye = Items.dye;
		GameRegistry.addRecipe(new ItemStack(dye, 1, 14), new Object[]{"B", 'B', new ItemStack(Coral1, 1, 0)});
		GameRegistry.addRecipe(new ItemStack(dye, 1, 10), new Object[]{"B", 'B', new ItemStack(Coral1, 1, 1)});
		GameRegistry.addRecipe(new ItemStack(dye, 1, 13), new Object[]{"B", 'B', new ItemStack(Coral1, 1, 2)});
		GameRegistry.addRecipe(new ItemStack(dye, 1, 9), new Object[]{"B", 'B', new ItemStack(Coral4, 1, 3)});
		GameRegistry.addRecipe(new ItemStack(dye, 1, 3), new Object[]{"B", 'B', new ItemStack(Coral1, 1, 4)});
		GameRegistry.addRecipe(new ItemStack(dye, 1, 6), new Object[]{"B", 'B', new ItemStack(Coral5, 1, 5)});
	}

	/**
	 * Attempts to load settings, defaults are used if first time
	 */
	private void loadSettings() {
		config = new Configuration(new File(configDir, "settings.cfg"));

		enable = config.get("settings", "coralgen", true).getBoolean(true);
		spiky = config.get("settings", "spikyenabled", true).getBoolean(true);
		bubble = config.get("settings", "enablebubbles", true).getBoolean(true);
		grow = config.get("settings", "enablegrow", false).getBoolean(false);
		size = config.get("settings", "avgsize", 1).getInt();
		ocean = config.get("settings", "oceanonly", true).getBoolean(true);
		land = config.get("settings", "land", false).getBoolean(false);
		dimensions = config.get("settings", "alldimensions", false).getBoolean(false);

		minHeight = config.get("generation", "minheight", 0).getInt();
		if(minHeight < 0) {
			System.err.println("CoralMod: Using default minHeight");
			minHeight = 0;
		}

		maxHeight = config.get("generation", "maxheight", 128).getInt();
		if(maxHeight < 4) {
			System.err.println("CoralMod: Using default maxHeight");
			maxHeight = 128;
		}

		radius = config.get("generation", "radius", 16).getInt();
		if(radius < 0) {
			System.err.println("CoralMod: Using default radius");
			radius = 16;
		}

		iterations = config.get("generation", "iterations", 80).getInt();
		if(iterations < 1 || iterations > 100) {
			System.err.println("CoralMod: Using default iterations");
			iterations = 80;
		}

		biomes = config.get("generation", "biomes", "").getString();

		updateSettings();
	}

	/**
	 * Saves settings
	 */
	public void updateSettings() {
		config.get("settings", "coralgen", true).set(enable);
		config.get("settings", "spikyenabled", true).set(spiky);
		config.get("settings", "enablebubbles", true).set(bubble);
		config.get("settings", "enablegrow", false).set(grow);
		config.get("settings", "avgsize", 1).set(size);
		config.get("settings", "oceanonly", true).set(ocean);
		config.get("settings", "land", false).set(land);
		config.get("settings", "alldimensions", false).set(dimensions);

		config.get("generation", "minheight", 0).set(minHeight);
		config.get("generation", "maxheight", 128).set(maxHeight);
		config.get("generation", "radius", 16).set(radius);
		config.get("generation", "iterations", 80).set(iterations);
		config.get("generation", "biomes", "").set(biomes);

		config.save();
		System.out.println("CoralMod: Saved settings");
	}

	/**
	 * Returns list of allowed fields
	 */
	public static List<String> getAllowedFields() {
		return allowedFields;
	}

	/**
	 * Attempts to toggle specified field
	 */
	public static boolean toggle(final String str) {
		if(!allowedFields.contains(str)) {
			System.err.println("CoralMod: Unknown field " + str);
			return false;
		}

		try {
			Field field = CoralMod.class.getDeclaredField(str);
			field.setAccessible(true);
			Object object = field.get(null);
			if(object instanceof Integer) {
				int currentVal = ((Integer)object).intValue();
				if(currentVal < 0 || currentVal > 2) {
					currentVal = 0;
				}
				field.set(null, (currentVal + 1) % 3);
				return true;
			} else if(object instanceof Boolean) {
				boolean currentVal = ((Boolean)object).booleanValue();
				field.set(null, !currentVal);
				return true;
			}
			return false;
		} catch (Throwable t) {
			System.err.println("CoralMod: Can't toggle " + str);
			t.printStackTrace();
			return false;
		}
	}

	/**
	 * Attempts to return value of specified field
	 */
	public static String getValue(final String str) {
		if(!allowedFields.contains(str)) {
			System.err.println("CoralMod: Unknown field " + str);
			return "";
		}
		try {
			Field field = CoralMod.class.getDeclaredField(str);
			field.setAccessible(true);
			return field.get(null).toString();
		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}
	}

	/**
	 * Checks if a block is water and if it's stationary
	 */
	public static boolean checkWater(World world, int x, int y, int z, boolean stationary) {
	    return checkWater(world, x, y, z) && world.getBlock(x, y, z) instanceof BlockDynamicLiquid;
	}

	/**
	 * Checks if a block is water
	 */
	public static boolean checkWater(World world, int x, int y, int z) {
		Block blockID = world.getBlock(x, y, z);

		// if the block is any type of coral, it's not water
		if(blockID == Coral1 || blockID == Coral2 || blockID == Coral3 
		|| blockID == Coral4 || blockID == Coral5) {
			return false;
		}

		if(land) {
			return true;
		} else {
			if(world.getBlock(x, y, z).getMaterial() == Material.water) {
				return true;
			}
		}
		return false;
	}

	protected static boolean getBubble() {
		return bubble;
	}

	protected static boolean getDimensions() {
		return dimensions;
	}

	protected static boolean getEnable() {
		return enable;
	}

	protected static boolean getGrow() {
		return grow;
	}

	protected static boolean getOcean() {
		return ocean;
	}

	protected static int getSize() {
		if(size < 0 || size > 2) {
			size = 1;
		}
		return size;
	}

	protected static boolean getSpiky() {
		return spiky;
	}

	protected static int getMinHeight() {
		if(minHeight + 4 > maxHeight) {
			System.err.println("CoralMod: Unsafe minHeight");
			minHeight = 0;
		}
		return minHeight;
	}

	protected static int getMaxHeight(World world) {
		if(maxHeight > world.getHeight()) {
			System.err.println("CoralMod: Unsafe maxHeight");
			maxHeight = world.getHeight();
		}
		return maxHeight;
	}

	protected static int getIterations() {
		return iterations;
	}

	protected static int getRadius() {
		return radius;
	}

	protected static List<String> getBiomesList() {
		String[] biomesArray = biomes.split(",");
		if(biomesArray.length > 0) {
			if(listBiomes) {
				System.out.println("CoralMod: Biomes " + biomes);
				listBiomes = false;
			}
		} 
		return Arrays.asList(biomesArray);
	}

	static {
		// ordering should be same for fieldNames
		DESCRIPTIONS = new String[] {
			"CoralReef Gen.",	"Spiky Coral",
			"Bubbles",		"Growing",
			"Average Size",		"Ocean Only",
			"Place on Land",	"All Dimensions"
		};

		// order determines GUI button layout
		final String[] fieldNames = new String[] {
			"enable",		"spiky",
			"bubble",		"grow",
			"size",			"ocean",
			"land",			"dimensions"
		};

		allowedFields = Collections.unmodifiableList(Arrays.asList(fieldNames));
	}

}
