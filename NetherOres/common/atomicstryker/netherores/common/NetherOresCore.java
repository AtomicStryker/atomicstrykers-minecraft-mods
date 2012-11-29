package atomicstryker.netherores.common;

import ic2.api.Ic2Recipes;
import ic2.api.Items;

import java.io.File;
import java.util.Random;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.FurnaceRecipes;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "NetherOres", name = "Nether Ores", version = "1.4.4R1.2.9", dependencies = "after:IC2")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
connectionHandler = ConnectionHandler.class)
public class NetherOresCore
{
    public final static String terrainTexture = "/atomicstryker/netherores/client/sprites/block0.png";

    public static Block blockNetherOres;

    private static Property explosionPower;
    private static Property explosionChances;
    private Property netherOreBlockId;
    private Property enableStandardFurnaceRecipes;
    private Property enableMaceratorRecipes;
    private boolean foundTin;
    private boolean foundCopper;

    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        loadConfig(evt.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Init
    public void load(FMLInitializationEvent evt)
    {
        blockNetherOres = new BlockNetherOres(Integer.parseInt(netherOreBlockId.value), 0);

        GameRegistry.registerBlock(blockNetherOres, ItemNetherOre.class);
        GameRegistry.registerWorldGenerator(new WorldGenHandler());

        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 0, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 1, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 2, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 3, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 4, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 5, "pickaxe", 2);

        OreDictionary.registerOre("oreNetherCoal", new ItemStack(blockNetherOres, 1, 0));
        OreDictionary.registerOre("oreNetherDiamond", new ItemStack(blockNetherOres, 1, 1));
        OreDictionary.registerOre("oreNetherGold", new ItemStack(blockNetherOres, 1, 2));
        OreDictionary.registerOre("oreNetherIron", new ItemStack(blockNetherOres, 1, 3));
        OreDictionary.registerOre("oreNetherLapis", new ItemStack(blockNetherOres, 1, 4));
        OreDictionary.registerOre("oreNetherRedstone", new ItemStack(blockNetherOres, 1, 5));
        OreDictionary.registerOre("oreNetherCopper", new ItemStack(blockNetherOres, 1, 6));
        OreDictionary.registerOre("oreNetherTin", new ItemStack(blockNetherOres, 1, 7));

        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 0), "Nether Coal");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 1), "Nether Diamond");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 2), "Nether Gold Ore");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 3), "Nether Iron Ore");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 4), "Nether Lapis Lazuli");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 5), "Nether Redstone Ore");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 6), "Nether Copper Ore");
        LanguageRegistry.instance().addName(new ItemStack(NetherOresCore.blockNetherOres, 1, 7), "Nether Tin Ore");
        
        if (!foundCopper && !OreDictionary.getOres("oreCopper").isEmpty())
        {
            registerNetherCopper(OreDictionary.getOres("oreCopper").get(0));
        }
        
        if (!foundTin && !OreDictionary.getOres("oreTin").isEmpty())
        {
            registerNetherTin(OreDictionary.getOres("oreTin").get(0));
        }
        
        if (Boolean.parseBoolean(enableMaceratorRecipes.value) == true && Loader.isModLoaded("IC2"))
        {
            ItemStack goldDust = Items.getItem("goldDust");
            ItemStack ironDust = Items.getItem("ironDust");
            ItemStack copperDust = Items.getItem("copperDust");
            ItemStack tinDust = Items.getItem("tinDust");

            Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 0), new ItemStack(Item.coal, 2));
            Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 1), new ItemStack(Item.diamond, 2));
            if (goldDust != null)
            {
                Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 2), new ItemStack(goldDust.itemID, 4, 0));
            }
            if (ironDust != null)
            {
                Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 3), new ItemStack(ironDust.itemID, 4, 0));
            }
            Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 4), new ItemStack(Item.dyePowder, 8, 4));
            Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 5), new ItemStack(Item.redstone, 6));
            if (copperDust != null)
            {
                Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 6), new ItemStack(copperDust.itemID, 4, 0));
            }
            if (tinDust != null)
            {
                Ic2Recipes.addMaceratorRecipe(new ItemStack(blockNetherOres.blockID, 1, 7), new ItemStack(tinDust.itemID, 4, 0));
            }

            System.out.println("NetherOres: loaded IC2 Macerator Recipes");
        }

        if (Boolean.parseBoolean(enableStandardFurnaceRecipes.value) == true)
        {
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 0, new ItemStack(Item.coal), 1f);
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 1, new ItemStack(Item.diamond), 1f);
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 2, new ItemStack(Block.oreGold), 1f);
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 3, new ItemStack(Block.oreIron), 1f);
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 4, new ItemStack(Item.dyePowder, 8, 4), 1f);
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 5, new ItemStack(Item.redstone, 6), 1f);
        }
    }

    private void loadConfig(File f)
    {
        Configuration c = new Configuration(f);
        c.load();

        netherOreBlockId = c.getBlock(c.CATEGORY_BLOCK, "ID.NetherOreBlock", 140);
        explosionPower = c.get(c.CATEGORY_GENERAL, "ExplosionPower", 2);
        explosionPower.comment = "How powerful an explosion will be. Creepers are 3, TNT is 4, electrified creepers are 6. This affects both the ability of the explosion to punch through blocks as well as the blast radius.";
        explosionChances = c.get(Configuration.CATEGORY_GENERAL, "ExplosionChances", 3);
        explosionChances.comment = "The number of chances a nether ore has to find another one to detonate. When a nether ore block is mined, it searches a random adjacent block. If that block is a nether ore, it becomes armed. This number controls how many times it searches. It will not stop at arming only one if it finds more than one.";
        enableStandardFurnaceRecipes = c.get(Configuration.CATEGORY_GENERAL, "EnableStandardFurnaceRecipes", true);
        enableStandardFurnaceRecipes.comment = "Set this to false to remove the standard furnace recipes (ie, nether iron ore -> normal iron ore). Provided for compatibility with Metallurgy. If you set this to false and no other mod connects to this mod's ores, they will be useless.";
        enableMaceratorRecipes = c.get(Configuration.CATEGORY_GENERAL, "EnableMaceratorRecipes", true);
        enableMaceratorRecipes.comment = "Set this to false to disable use of these ores in the IC2 Macerator.";

        c.save();
    }
    
    private class WorldGenHandler implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
        {
            if (world.getBiomeGenForCoords(chunkX, chunkZ) == BiomeGenBase.hell)
            {
                generateNether(world, random, chunkX*16, chunkZ*16);
            }
        }
    }

    private void generateNether(World world, Random random, int chunkX, int chunkZ)
    {
        for (int i = 0; i < 24; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 0, 12).generate(world, random, chunkX + random.nextInt(16), random.nextInt(128), chunkZ + random.nextInt(16));
        }
        for (int i = 0; i < 3; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 1, 8).generate(world, random, chunkX + random.nextInt(16), random.nextInt(32), chunkZ + random.nextInt(16));
        }
        for (int i = 0; i < 4; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 2, 10).generate(world, random, chunkX + random.nextInt(16), random.nextInt(96), chunkZ + random.nextInt(16));
        }
        for (int i = 0; i < 8; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 3, 14).generate(world, random, chunkX + random.nextInt(16), random.nextInt(96), chunkZ + random.nextInt(16));
        }
        for (int i = 0; i < 6; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 4, 3).generate(world, random, chunkX + random.nextInt(16), random.nextInt(128), chunkZ + random.nextInt(16));
        }
        for (int i = 0; i < 8; i++)
        {
            new WorldGenNetherOres(blockNetherOres.blockID, 5, 9).generate(world, random, chunkX + random.nextInt(16), random.nextInt(96), chunkZ + random.nextInt(16));
        }
        if (foundCopper)
        {
            for (int i = 0; i < 8; i++)
            {
                new WorldGenNetherOres(blockNetherOres.blockID, 6, 14).generate(world, random, chunkX + random.nextInt(16), random.nextInt(96), chunkZ + random.nextInt(16));
            }
        }
        if (foundTin)
        {
            for (int i = 0; i < 8; i++)
            {
                new WorldGenNetherOres(blockNetherOres.blockID, 7, 14).generate(world, random, chunkX + random.nextInt(16), random.nextInt(96), chunkZ + random.nextInt(16));
            }
        }
    }

    @ForgeSubscribe
    public void registerOreEvent(OreRegisterEvent event)
    {
        if (event.Name.equals("oreCopper") && !foundCopper)
        {
            registerNetherCopper(event.Ore.copy());
        }
        else if (event.Name.equals("oreTin") && !foundTin)
        {
            registerNetherTin(event.Ore.copy());
        }
    }
    
    private void registerNetherCopper(ItemStack smeltedOre)
    {
        System.out.println("Nether Ores found Copper Ore registration! Activating Nether Copper!");
        
        foundCopper = true;
        smeltedOre.stackSize = 1;
        MinecraftForge.setBlockHarvestLevel(blockNetherOres, 6, "pickaxe", 1);
        if (Boolean.parseBoolean(enableStandardFurnaceRecipes.value) == true)
        {
            FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 6, smeltedOre, 1f);
        }
    }
    
    private void registerNetherTin(ItemStack smeltedOre)
    {
        System.out.println("Nether Ores found Tin Ore registration! Activating Nether Tin!");
        
        foundTin = true;
        smeltedOre.stackSize = 1;
        FurnaceRecipes.smelting().addSmelting(blockNetherOres.blockID, 7, smeltedOre, 1f);
        if (Boolean.parseBoolean(enableStandardFurnaceRecipes.value) == true)
        {
            MinecraftForge.setBlockHarvestLevel(blockNetherOres, 7, "pickaxe", 1);
        }
    }

    public static int getExplosionChance()
    {
        return explosionChances.getInt(3);
    }
    
    public static int getExplosionPower()
    {
        return explosionPower.getInt(2);
    }
}
