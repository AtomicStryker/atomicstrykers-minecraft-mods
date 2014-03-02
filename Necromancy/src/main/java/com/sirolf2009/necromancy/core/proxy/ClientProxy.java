package com.sirolf2009.necromancy.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.FluidRegistry;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.client.model.ModelIsaacHead;
import com.sirolf2009.necromancy.client.model.ModelIsaacNormal;
import com.sirolf2009.necromancy.client.model.ModelIsaacSevered;
import com.sirolf2009.necromancy.client.model.ModelNightCrawler;
import com.sirolf2009.necromancy.client.model.ModelTeddy;
import com.sirolf2009.necromancy.client.renderer.ItemNecronomiconRenderer;
import com.sirolf2009.necromancy.client.renderer.ItemScytheBoneRenderer;
import com.sirolf2009.necromancy.client.renderer.ItemScytheRenderer;
import com.sirolf2009.necromancy.client.renderer.RenderIsaac;
import com.sirolf2009.necromancy.client.renderer.RenderIsaacBlood;
import com.sirolf2009.necromancy.client.renderer.RenderMinion;
import com.sirolf2009.necromancy.client.renderer.RenderNightCrawler;
import com.sirolf2009.necromancy.client.renderer.RenderTear;
import com.sirolf2009.necromancy.client.renderer.RenderTearBlood;
import com.sirolf2009.necromancy.client.renderer.RenderTeddy;
import com.sirolf2009.necromancy.client.renderer.tileentity.TileEntityAltarRenderer;
import com.sirolf2009.necromancy.client.renderer.tileentity.TileEntitySewingRenderer;
import com.sirolf2009.necromancy.core.handler.KeyHandlerNecro;
import com.sirolf2009.necromancy.entity.EntityIsaacBlood;
import com.sirolf2009.necromancy.entity.EntityIsaacBody;
import com.sirolf2009.necromancy.entity.EntityIsaacHead;
import com.sirolf2009.necromancy.entity.EntityIsaacNormal;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.EntityNecroFX;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;
import com.sirolf2009.necromancy.entity.EntityTeddy;
import com.sirolf2009.necromancy.entity.RegistryNecromancyEntities;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    @Override
    public void preInit()
    {
        FMLCommonHandler.instance().bus().register(new KeyHandlerNecro());
    }

    @Override
    public void init()
    {
        mc = FMLClientHandler.instance().getClient();

        RenderingRegistry.registerEntityRenderingHandler(EntityMinion.class, new RenderMinion());
        RenderingRegistry.registerEntityRenderingHandler(EntityTeddy.class, new RenderTeddy(new ModelTeddy(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityNightCrawler.class, new RenderNightCrawler(new ModelNightCrawler(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacNormal.class, new RenderIsaac(new ModelIsaacNormal(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacBlood.class, new RenderIsaacBlood(new ModelIsaacNormal(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacBody.class, new RenderIsaacBlood(new ModelIsaacSevered(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacHead.class, new RenderIsaacBlood(new ModelIsaacHead(), 0.3F));
        RenderingRegistry.registerEntityRenderingHandler(EntityTear.class, new RenderTear());
        RenderingRegistry.registerEntityRenderingHandler(EntityTearBlood.class, new RenderTearBlood());
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAltar.class, new TileEntityAltarRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySewing.class, new TileEntitySewingRenderer());

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(RegistryBlocksNecromancy.altar), new TileEntityAltarRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(RegistryBlocksNecromancy.sewing), new TileEntitySewingRenderer());
        MinecraftForgeClient.registerItemRenderer(RegistryNecromancyItems.scythe, new ItemScytheRenderer());
        MinecraftForgeClient.registerItemRenderer(RegistryNecromancyItems.scytheBone, new ItemScytheBoneRenderer());
        MinecraftForgeClient.registerItemRenderer(RegistryNecromancyItems.necronomicon, new ItemNecronomiconRenderer());

        FluidRegistry.registerFluid(RegistryBlocksNecromancy.fluidBlood);
        
        VillagerRegistry.instance().registerVillagerSkin(RegistryNecromancyEntities.villagerIDNecro, ReferenceNecromancy.TEXTURES_ENTITIES_VILLAGER);
    }
    
    @Override
    public void spawnParticle(String name, double posX, double posY, double posZ, double motionX, double motionY, double motionZ)
    {
        if (mc != null && mc.renderViewEntity != null && mc.effectRenderer != null)
        {
            if (name.equals("skull"))
            {
                mc.effectRenderer.addEffect(new EntityNecroFX(mc.theWorld, posX, posY, posZ, motionX, motionY, motionZ, 0.5F));
            }
        }
    }
    
    @Override
    public void bindTexture(ResourceLocation par1ResourceLocation)
    {
        mc.renderEngine.bindTexture(par1ResourceLocation);
    }

    @Override
    public int addArmour(String path)
    {
        return RenderingRegistry.addNewArmourRendererPrefix(path);
    }
}