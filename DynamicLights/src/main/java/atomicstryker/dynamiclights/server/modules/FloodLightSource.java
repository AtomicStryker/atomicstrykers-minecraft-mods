package atomicstryker.dynamiclights.server.modules;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality emulating portable or static Flood Lights
 *
 */
//@Mod(modid = "dynamiclights_floodlights", name = "Dynamic Lights Flood Light", version = "1.0.3", dependencies = "required-after:dynamiclights")
//public class FloodLightSource
//{
//    private EntityPlayer thePlayer;
//    private ItemConfigHelper itemsMap;
//    private final PartialLightSource[] partialLights;
//    private boolean enabled;
//    private boolean simpleMode;
//    private Configuration config;
//
//    public FloodLightSource()
//    {
//        partialLights = new PartialLightSource[5];
//    }
//
//    @EventHandler
//    public void preInit(FMLPreInitializationEvent evt)
//    {
//        config = new Configuration(evt.getSuggestedConfigurationFile());
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @EventHandler
//    public void modsLoaded(FMLPostInitializationEvent evt)
//    {
//        config.load();
//
//        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "FloodLightItems", "ender_eye");
//        itemsList.setComment("Item IDs that shine floodlight while held.");
//        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
//
//        simpleMode = config.get(Configuration.CATEGORY_GENERAL, "simpleMode", true, "instead of simulating a cone of light (EXPENSIVE!!), simulates a single point light").getBoolean(true);
//
//        config.save();
//    }
//
//    @SubscribeEvent
//    public void onTick(TickEvent.RenderTickEvent tick)
//    {
//        if (tick.phase == Phase.END)
//        {
//            thePlayer = FMLClientHandler.instance().getClient().player;
//            if (thePlayer != null && thePlayer.isEntityAlive() && !DynamicLights.globalLightsOff())
//            {
//                int lightLevel = Math.max(getLightFromItemStack(thePlayer.getHeldItemMainhand()), getLightFromItemStack(thePlayer.getHeldItemOffhand()));
//
//                checkDummyInit(thePlayer.world);
//
//                if (lightLevel > 0)
//                {
//                    handleLight(partialLights[0], lightLevel, 0f, 0f);
//
//                    if (!simpleMode)
//                    {
//                        handleLight(partialLights[1], lightLevel, 15f, 15f);
//                        handleLight(partialLights[2], lightLevel, -15f, 15f);
//                        handleLight(partialLights[3], lightLevel, 15f, -15f);
//                        handleLight(partialLights[4], lightLevel, -15f, -15f);
//                    }
//                    setLightsEnabled(true);
//                }
//                else
//                {
//                    setLightsEnabled(false);
//                }
//            }
//        }
//    }
//
//    private void handleLight(PartialLightSource source, int light, float yawRot, float pitchRot)
//    {
//        Vec3d posvec = thePlayer.getLook(1.0f);
//        thePlayer.rotationPitch += pitchRot;
//        thePlayer.rotationYaw += yawRot;
//        Vec3d look = thePlayer.getLook(1.0f);
//        thePlayer.rotationPitch -= pitchRot;
//        thePlayer.rotationYaw -= yawRot;
//        look = posvec.addVector(look.x * 16d, look.y * 16d, look.z * 16d);
//        RayTraceResult mop = thePlayer.world.rayTraceBlocks(posvec, look);
//        if (mop != null)
//        {
//        	BlockPos pos = mop.getBlockPos();
//            int dist = (int) Math.round(thePlayer.getDistance(pos.getX()+0.5d, pos.getY()+0.5d, pos.getZ()+0.5d));
//            source.lightLevel = light - dist;
//            source.entity.posX = pos.getX()+0.5d;
//            source.entity.posY = pos.getY()+0.5d;
//            source.entity.posZ = pos.getZ()+0.5d;
//        }
//        else
//        {
//            source.lightLevel = 0;
//        }
//    }
//
//    private void setLightsEnabled(boolean newEnabled)
//    {
//        if (newEnabled != enabled)
//        {
//            enabled = newEnabled;
//
//            if (!simpleMode)
//            {
//                for (PartialLightSource p : partialLights)
//                {
//                    if (newEnabled)
//                    {
//                        DynamicLights.addLightSource(p);
//                    }
//                    else
//                    {
//                        DynamicLights.removeLightSource(p);
//                    }
//                }
//            }
//            else
//            {
//                if (newEnabled)
//                {
//                    DynamicLights.addLightSource(partialLights[0]);
//                }
//                else
//                {
//                    DynamicLights.removeLightSource(partialLights[0]);
//                }
//            }
//        }
//    }
//
//    private void checkDummyInit(World world)
//    {
//        if (partialLights[0] == null)
//        {
//            for (int i = 0; i < partialLights.length; i++)
//            {
//                partialLights[i] = new PartialLightSource(new DummyEntity(world));
//                DynamicLights.addLightSource(partialLights[i]);
//            }
//        }
//    }
//
//    private int getLightFromItemStack(ItemStack stack)
//    {
//        if (stack != null)
//        {
//            int r = itemsMap.retrieveValue(stack.getItem().getRegistryName(), stack.getItemDamage());
//            return r < 0 ? 0 : r;
//        }
//        return 0;
//    }
//
//    private class PartialLightSource implements IDynamicLightSource
//    {
//        private final Entity entity;
//        private int lightLevel = 0;
//
//        private PartialLightSource(Entity e)
//        {
//            entity = e;
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
//    private class DummyEntity extends Entity
//    {
//        public DummyEntity(World par1World) { super(par1World); }
//        @Override
//        protected void entityInit(){}
//        @Override
//        protected void readEntityFromNBT(NBTTagCompound var1){}
//        @Override
//        protected void writeEntityToNBT(NBTTagCompound var1){}
//    }
//
//}
