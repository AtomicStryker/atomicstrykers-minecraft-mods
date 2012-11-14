package atomicstryker.ropesplus.common;

import java.io.*;
import java.util.*;

import net.minecraft.src.Material;
import net.minecraftforge.common.Configuration;

public class Settings_RopePlus
{
	public static int blockIdRopeDJRoslin = 242;
	public static int blockIdRope = 243;
	public static int blockIdGrapplingHook = 244;
	public static int itemIdRope = 2511;
	public static int itemIdGrapplingHook = 2512;
	
	public static int itemIdArrowConfusion = 2513;
	public static int itemIdArrowDirt = 2514;
	public static int itemIdArrowExplosion = 2515;
	public static int itemIdArrowFire = 2516;
	public static int itemIdArrowGrass = 2517;
	public static int itemIdArrowIce = 2518;
	public static int itemIdArrowLaser = 2519;
	public static int itemIdArrowRope = 2520;
	public static int itemIdArrowSlime = 2521;
	public static int itemIdArrowTorch = 2522;
	public static int itemIdArrowWarp = 2523;
    public static int itemIdRopesPlusBow = 2510;
	
	public static int ropeTexture = 1;
	
	public static Configuration config;
	
	public static void InitSettings(File suggested)
	{		
		config = new Configuration(suggested);
		config.load();
		
		blockIdRope = Integer.parseInt(config.getBlock(config.CATEGORY_BLOCK, "blockIdRope", blockIdRope).value);
		blockIdGrapplingHook = Integer.parseInt(config.getBlock(config.CATEGORY_BLOCK, "blockIdGrapplingHook", blockIdGrapplingHook).value);
		blockIdRopeDJRoslin = Integer.parseInt(config.getBlock(config.CATEGORY_BLOCK, "blockIdRopeDJRoslin", blockIdRopeDJRoslin).value);
		
		itemIdRope = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdRope", itemIdRope).value);
		itemIdGrapplingHook = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdGrapplingHook", itemIdGrapplingHook).value);
		itemIdRopesPlusBow = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdRopesPlusBow", itemIdRopesPlusBow).value);
		
		itemIdArrowConfusion = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowConfusion", itemIdArrowConfusion).value);
		itemIdArrowDirt = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowDirt", itemIdArrowDirt).value);
		itemIdArrowExplosion = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowExplosion", itemIdArrowExplosion).value);
		itemIdArrowFire = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowFire", itemIdArrowFire).value);
		itemIdArrowGrass = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowGrass", itemIdArrowGrass).value);
		itemIdArrowIce = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowIce", itemIdArrowIce).value);
		itemIdArrowLaser = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowLaser", itemIdArrowLaser).value);
		itemIdArrowRope = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowRope", itemIdArrowRope).value);
		itemIdArrowSlime = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowSlime", itemIdArrowSlime).value);
		itemIdArrowTorch = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowTorch", itemIdArrowTorch).value);
		itemIdArrowWarp = Integer.parseInt(config.getItem(config.CATEGORY_ITEM, "itemIdArrowWarp", itemIdArrowWarp).value);
		
		ropeTexture = config.get(config.CATEGORY_GENERAL, "Rope Texture", 1).getInt();
		
		config.save();
	}
}
