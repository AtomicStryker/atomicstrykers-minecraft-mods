package atomicstryker.dynamiclights.client.modules;

/**
 * 
 * @author AtomicStryker
 *
 *         Offers Dynamic Light functionality to flame enchanted Arrows fired.
 *         Those can give off Light through this Module.
 *
 */
//@Mod(FlameEnchantedArrowLightSource.MOD_ID)
//@Mod.EventBusSubscriber(modid = FlameEnchantedArrowLightSource.MOD_ID, value = Dist.CLIENT)
//public class FlameEnchantedArrowLightSource
//{
//
//    static final String MOD_ID = "dynamiclights_flamearrows";
//
//    @SubscribeEvent
//    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
//    {
//        if (event.getEntity() instanceof EntityArrow)
//        {
//            EntityArrow arrow = (EntityArrow) event.getEntity();
//            Entity shooterEnt = ((WorldServer) arrow.world).getEntityFromUuid(arrow.shootingEntity);
//            if (shooterEnt instanceof EntityPlayer)
//            {
//                EntityPlayer shooter = (EntityPlayer) shooterEnt;
//                if (EnchantmentHelper.getFireAspectModifier(shooter) != 0)
//                {
//                    DynamicLights.addLightSource(new EntityLightAdapter(arrow));
//                }
//            }
//        }
//    }
//
//    private class EntityLightAdapter implements IDynamicLightSource
//    {
//        private EntityArrow entity;
//        private int lightLevel;
//
//        public EntityLightAdapter(EntityArrow entArrow)
//        {
//            lightLevel = 15;
//            entity = entArrow;
//        }
//
//        @Override
//        public Entity getAttachmentEntity()
//        {
//            return entity;
//        }
//
//        @Override
//        public int getLightLevel()
//        {
//            return lightLevel;
//        }
//    }
//
//}
