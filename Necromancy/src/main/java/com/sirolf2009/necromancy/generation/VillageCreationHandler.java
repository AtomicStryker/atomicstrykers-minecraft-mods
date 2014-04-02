package com.sirolf2009.necromancy.generation;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;

import com.sirolf2009.necromancy.generation.villagecomponent.ComponentVillageCemetery;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class VillageCreationHandler implements IVillageCreationHandler, IVillageTradeHandler
{

    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i)
    {
        return new StructureVillagePieces.PieceWeight(ComponentVillageCemetery.class, 5, 1);
    }

    @Override
    public Class<?> getComponentClass()
    {
        return ComponentVillageCemetery.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
    {
        recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 6), new ItemStack(Items.book), new ItemStack(RegistryNecromancyItems.necronomicon)));
        recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, new Random().nextInt(3)), null, new ItemStack(RegistryNecromancyItems.bodyparts, 1,
                random.nextInt(ItemBodyPart.necroEntities.size() - 1))));
        recipeList.add(new MerchantRecipe(new ItemStack(RegistryNecromancyItems.bodyparts, 1, random.nextInt(ItemBodyPart.necroEntities.size() - 1)), null,
                new ItemStack(Items.emerald, new Random().nextInt(3))));
        recipeList.add(new MerchantRecipe(new ItemStack(RegistryNecromancyItems.bodyparts, 1, random.nextInt(ItemBodyPart.necroEntities.size() - 1)), null,
                new ItemStack(Items.emerald, new Random().nextInt(3))));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object buildComponent(PieceWeight villagePiece, Start startPiece, List pieces, Random random, int p1, int p2, int p3, int p4, int p5)
    {
        ComponentVillageCemetery cemetery = ComponentVillageCemetery.build(startPiece, pieces, random, p1, p2, p3, p4, p5);
        return cemetery;
    }

}
