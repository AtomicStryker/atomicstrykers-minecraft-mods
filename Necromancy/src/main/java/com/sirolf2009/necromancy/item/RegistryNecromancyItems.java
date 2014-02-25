package com.sirolf2009.necromancy.item;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;

import cpw.mods.fml.common.registry.GameRegistry;

public class RegistryNecromancyItems
{

    public static Item apprenticeArmorHead;
    public static Item apprenticeArmorTorso;
    public static Item apprenticeArmorLeggings;
    public static Item apprenticeArmorBoots;
    public static Item genericItems;
    public static Item necronomicon;
    public static Item scythe;
    public static Item scytheBone;
    public static Item bucketBlood;
    public static Item organs;
    public static Item bodyparts;
    public static Item spawner;
    // public static Item skull;
    public static Item isaacsHead;

    public static ArmorMaterial isaac = EnumHelper.addArmorMaterial("Isaac", Integer.MAX_VALUE, new int[] { 0, 0, 0, 0 }, 0);

    public static void initItems()
    {
        genericItems = new ItemGeneric().setUnlocalizedName("ItemNecromancy");
        GameRegistry.registerItem(genericItems, "ItemNecromancy");

        /*
         * AspectList alist; alist = new AspectList(genericItems, 0);
         * alist.aspects.put(Aspect.DEATH, 2);
         * ThaumcraftApi.registerObjectTag(genericItems, 0, alist); alist = new
         * AspectList(genericItems, 1); alist.aspects.put(Aspect.FLIGHT, 2);
         * alist.aspects.put(Aspect.LIFE, 4); alist.aspects.put(Aspect.LIGHT,
         * 2); alist.aspects.put(Aspect.CRYSTAL, 2);
         * ThaumcraftApi.registerObjectTag(genericItems, 1, alist); alist = new
         * AspectList(genericItems, 2); alist.aspects.put(Aspect.HEAL, 2);
         * alist.aspects.put(Aspect.LIFE, 4); alist.aspects.put(Aspect.CRYSTAL,
         * 2); ThaumcraftApi.registerObjectTag(genericItems, 2, alist); alist =
         * new AspectList(genericItems, 3); alist.aspects.put(Aspect.FLESH, 8);
         * alist.aspects.put(Aspect.MIND, 4); for (int x = 0; x <
         * ItemGeneric.names.length; x++) { //LanguageRegistry.addName(new
         * ItemStack(genericItems, 1, x), ItemGeneric.names[x]); }
         */

        necronomicon = new ItemNecronomicon().setUnlocalizedName("Necronomicon");
        GameRegistry.registerItem(necronomicon, "Necronomicon");
        /*
         * //LanguageRegistry.addName(necronomIIcon, "NecronomIIcon"); alist =
         * new AspectList(necronomIIcon, -1); alist.aspects.put(Aspect.MIND,
         * 666); alist.aspects.put(Aspect.DEATH, 666);
         * alist.aspects.put(Aspect.DARKNESS, 666);
         * alist.aspects.put(Aspect.TAINT, 666); alist.aspects.put(Aspect.MAGIC,
         * 666); alist.aspects.put(Aspect.ELDRITCH, 666);
         * ThaumcraftApi.registerObjectTag(necronomIIcon, -1, alist);
         */

        scythe = new ItemScythe(ItemScythe.toolScythe).setUnlocalizedName("ItemScythe");
        GameRegistry.registerItem(scythe, "ItemScythe");
        /*
         * //LanguageRegistry.addName(scythe, "Blood Scythe"); alist = new
         * AspectList(scythe, -1); alist.aspects.put(Aspect.WEAPON, 8);
         * alist.aspects.put(Aspect.LIFE, 2);
         * ThaumcraftApi.registerObjectTag(scythe, -1, alist);
         */

        scytheBone = new ItemScythe(ItemScythe.toolScytheBone).setUnlocalizedName("ItemScytheBone");
        GameRegistry.registerItem(scytheBone, "ItemScytheBone");
        /*
         * //LanguageRegistry.addName(scytheBone, "Bone Scythe"); alist = new
         * AspectList(scytheBone, -1); alist.aspects.put(Aspect.WEAPON, 8);
         * alist.aspects.put(Aspect.DEATH, 2);
         * ThaumcraftApi.registerObjectTag(scytheBone, -1, alist);
         */

        bucketBlood = new ItemBucketBlood(RegistryBlocksNecromancy.blood).setUnlocalizedName("BucketBlood");
        GameRegistry.registerItem(bucketBlood, "BucketBlood");
        
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("blood", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(
                bucketBlood), new ItemStack(Items.bucket));

        /*
         * //LanguageRegistry.addName(bucketBlood, "Blood Bucket"); alist = new
         * AspectList(bucketBlood, -1); alist.aspects.put(Aspect.METAL, 13);
         * alist.aspects.put(Aspect.HEAL, 16); alist.aspects.put(Aspect.LIFE,
         * 32); ThaumcraftApi.registerObjectTag(bucketBlood, -1, alist);
         */

        organs = new ItemOrgans().setUnlocalizedName("Organs");
        GameRegistry.registerItem(organs, "Organs");
        /*
         * alist = new AspectList(organs, 0); alist.aspects.put(Aspect.FLESH,
         * 8); alist.aspects.put(Aspect.MIND, 4);
         * ThaumcraftApi.registerObjectTag(organs, 0, alist); alist = new
         * AspectList(organs, 1); alist.aspects.put(Aspect.FLESH, 8);
         * ThaumcraftApi.registerObjectTag(organs, 1, alist); alist = new
         * AspectList(organs, 2); alist.aspects.put(Aspect.FLESH, 2);
         * ThaumcraftApi.registerObjectTag(organs, 2, alist); alist = new
         * AspectList(organs, 3); alist.aspects.put(Aspect.FLESH, 8);
         * ThaumcraftApi.registerObjectTag(organs, 3, alist); alist = new
         * AspectList(organs, 4); alist.aspects.put(Aspect.TAINT, 2);
         * ThaumcraftApi.registerObjectTag(organs, 4, alist); for (int x = 0; x
         * < ItemOrgans.names.length; x++) { //LanguageRegistry.addName(new
         * ItemStack(organs, 1, x), ItemOrgans.names[x]); }
         */

        bodyparts = new ItemBodyPart().setUnlocalizedName("BodyParts");
        GameRegistry.registerItem(bodyparts, "BodyParts");
        /*
         * alist = new AspectList(organs, -1); alist.aspects.put(Aspect.TAINT,
         * 2); alist.aspects.put(Aspect.BEAST, 2);
         * alist.aspects.put(Aspect.DEATH, 8);
         * ThaumcraftApi.registerObjectTag(organs, -1, alist);
         */

        isaacsHead = new ItemIsaacsHead(isaac, Necromancy.proxy.addArmour("Isaac"), 0);
        GameRegistry.registerItem(isaacsHead, "ItemIsaacsHead");
        // LanguageRegistry.addName(isaacsHead, "Isaac's Severed Head");

        spawner = new ItemSpawner().setUnlocalizedName("NecroSpawner");
        GameRegistry.registerItem(spawner, "NecroSpawner");
        // LanguageRegistry.addName(new ItemStack(spawner, 1, 0),
        // "Isaac's Soul Heart");
    }

    public static void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(RegistryNecromancyItems.necronomicon, 1), new Object[] { "LSL", "IBF", "LNL", Character.valueOf('B'),
                Items.book, Character.valueOf('L'), Items.leather, Character.valueOf('S'), ItemGeneric.getItemStackFromName("Jar of Blood"),
                Character.valueOf('I'), new ItemStack(Items.dye, 1, 0), Character.valueOf('F'), Items.feather, Character.valueOf('N'),
                Items.nether_wart });
        GameRegistry.addRecipe(ItemGeneric.getItemStackFromName("Bone Needle"), new Object[] { "X", Character.valueOf('X'),
                new ItemStack(Items.dye, 1, 15) });
        GameRegistry.addRecipe(new ItemStack(RegistryNecromancyItems.scythe, 1), new Object[] { "IH", " S", " B", Character.valueOf('I'),
                Blocks.obsidian, 'H', Items.iron_hoe, 'S', Items.stick, 'B', ItemGeneric.getItemStackFromName("Jar of Blood") });
        GameRegistry.addRecipe(new ItemStack(RegistryNecromancyItems.scytheBone, 1), new Object[] { "IH", " S", " B", Character.valueOf('I'),
                Blocks.obsidian, 'H', RegistryNecromancyItems.scythe, 'S', Items.bone, 'B', Items.diamond });
        GameRegistry.addRecipe(ItemGeneric.getItemStackFromName("Brain on a Stick"), new Object[] { "# ", " X", '#', Items.fishing_rod, 'X',
                new ItemStack(RegistryNecromancyItems.organs, 1, 0) });
        GameRegistry.addShapelessRecipe(ItemGeneric.getItemStackFromName("Jar of Blood", 8), new Object[] {
                new ItemStack(RegistryNecromancyItems.bucketBlood), Items.glass_bottle, Items.glass_bottle, Items.glass_bottle, Items.glass_bottle,
                Items.glass_bottle, Items.glass_bottle, Items.glass_bottle, Items.glass_bottle });
        GameRegistry.addShapelessRecipe(
                new ItemStack(RegistryNecromancyItems.bucketBlood),
                new Object[] { Items.bucket, ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                        ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                        ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                        ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood") });
    }

}
