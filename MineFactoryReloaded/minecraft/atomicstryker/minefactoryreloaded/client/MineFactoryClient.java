package atomicstryker.minefactoryreloaded.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore.Machine;
import atomicstryker.minefactoryreloaded.common.core.Util;


import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraftforge.client.MinecraftForgeClient;

public class MineFactoryClient
{    
    private static MineFactoryClient instance;

    public MineFactoryClient()
    {
        instance = this;
        load();
    }

    public static MineFactoryClient instance()
    {
        return instance;
    }
    
    public int renderId = 1000;

    private void load()
    {
        MinecraftForgeClient.preloadTexture(MineFactoryReloadedCore.terrainTexture);
        MinecraftForgeClient.preloadTexture(MineFactoryReloadedCore.itemTexture);

        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Planter)), "Planter");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fisher)), "Fisher");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Harvester)), "Harvester");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Rancher)), "Rancher");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fertilizer)), "Fertilizer");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Vet)), "Veterinary Station");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Collector)), "Item Collector");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Breaker)), "Block Breaker");
        LanguageRegistry.instance().addName(new ItemStack(MineFactoryReloadedCore.machineBlock, 1, MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Weather)), "Weather Collector");

        LanguageRegistry.instance().addName(MineFactoryReloadedCore.conveyorBlock, "Conveyor Belt");

        LanguageRegistry.instance().addName(MineFactoryReloadedCore.passengerRailPickupBlock, "Passenger Pickup Rail");
        LanguageRegistry.instance().addName(MineFactoryReloadedCore.passengerRailDropoffBlock, "Passenger Dropoff Rail");
        LanguageRegistry.instance().addName(MineFactoryReloadedCore.cargoRailDropoffBlock, "Cargo Dropoff Rail");
        LanguageRegistry.instance().addName(MineFactoryReloadedCore.cargoRailPickupBlock, "Cargo Pickup Rail");

        LanguageRegistry.instance().addName(MineFactoryReloadedCore.steelIngotItem, "Steel Ingot");
        LanguageRegistry.instance().addName(MineFactoryReloadedCore.factoryHammerItem, "Factory Hammer");
        LanguageRegistry.instance().addName(MineFactoryReloadedCore.milkItem, "spilled Milk");
        
        renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(renderId, new FactoryRenderer());

        if(Util.getBool(MineFactoryReloadedCore.animateBlockFaces))
        {
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.conveyorTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/Conveyor.png"));
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.harvesterAnimatedTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/Harvester.png"));
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.rancherAnimatedTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/Rancher.png"));
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.blockBreakerAnimatedTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/BlockBreaker.png"));
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.fertilizerAnimatedTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/Fertilizer.png"));
            TextureFXManager.instance().addAnimation(new TextureFrameAnimFX(MineFactoryReloadedCore.vetAnimatedTexture, MineFactoryReloadedCore.TEXTURE_FOLDER+"animations/Vet.png"));
            TextureFXManager.instance().addAnimation(new TextureLiquidFX(MineFactoryReloadedCore.milkTexture, MineFactoryReloadedCore.itemTexture, 240, 255, 240, 255, 230, 245));
        }
    }
}
