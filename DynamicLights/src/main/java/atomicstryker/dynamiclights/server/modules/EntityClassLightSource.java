package atomicstryker.dynamiclights.server.modules;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to EntityItem instances.
 * Dropped Torches and such can give off Light through this Module.
 *
 */
//@Mod(modid = "dynamiclights_entityclasses", name = "Dynamic Lights on specified Entities", version = "1.0.1", dependencies = "required-after:dynamiclights")
//public class EntityClassLightSource
//{
//    private Minecraft mcinstance;
//    private long nextUpdate;
//    private long updateInterval;
//    private ArrayList<EntityLightAdapter> trackedItems;
//    private boolean threadRunning;
//
//    private Configuration config;
//    private HashMap<Class<? extends Entity>, Integer> lightValueMap;
//
//    @EventHandler
//    public void preInit(FMLPreInitializationEvent evt)
//    {
//        lightValueMap = new HashMap<>();
//
//        config = new Configuration(evt.getSuggestedConfigurationFile());
//        config.load();
//
//        Property updateI = config.get(Configuration.CATEGORY_GENERAL, "update Interval", 1000);
//        updateI.setComment("Update Interval time for all Entities in milliseconds. The lower the better and costlier.");
//        updateInterval = updateI.getInt();
//
//        config.save();
//
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @EventHandler
//    public void load(FMLInitializationEvent evt)
//    {
//        mcinstance = FMLClientHandler.instance().getClient();
//        nextUpdate = System.currentTimeMillis();
//        trackedItems = new ArrayList<>();
//        threadRunning = false;
//    }
//
//    @SubscribeEvent
//    public void onTick(TickEvent.ClientTickEvent tick)
//    {
//        if (mcinstance.world != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff())
//        {
//            nextUpdate = System.currentTimeMillis() + updateInterval;
//
//            if (!threadRunning)
//            {
//                Thread thread = new EntityListChecker(mcinstance.world.loadedEntityList);
//                thread.setPriority(Thread.MIN_PRIORITY);
//                thread.start();
//                threadRunning = true;
//            }
//        }
//    }
//
//    private int getLightFromEntity(Entity e)
//    {
//        if (e != null && e.isEntityAlive())
//        {
//            if (lightValueMap.containsKey(e.getClass()))
//            {
//                return lightValueMap.get(e.getClass());
//            }
//
//            config.load();
//            int value = config.get(Configuration.CATEGORY_GENERAL, e.getClass().getSimpleName(), 0).getInt();
//            config.save();
//
//            lightValueMap.put(e.getClass(), value);
//            return value;
//        }
//        return 0;
//    }
//
//    private class EntityListChecker extends Thread
//    {
//        private final Entity[] list;
//
//        public EntityListChecker(List<Entity> input)
//        {
//            list = new Entity[input.size()];
//            for (int i = 0; i < list.length; i++)
//            {
//                list[i] = input.get(i);
//            }
//        }
//
//        @Override
//        public void run()
//        {
//            ArrayList<EntityLightAdapter> newList = new ArrayList<>();
//            int light;
//
//            for (Entity ent : list)
//            {
//                light = getLightFromEntity(ent);
//                // Loop all loaded Entities, find alive and valid ItemEntities
//                if (light > 0)
//                {
//                    // now find them in the already tracked item adapters
//                    boolean found = false;
//                    Iterator<EntityLightAdapter> iter = trackedItems.iterator();
//                    EntityLightAdapter adapter;
//                    while (iter.hasNext())
//                    {
//                        adapter = iter.next();
//                        if (adapter.getAttachmentEntity().equals(ent)) // already tracked!
//                        {
//                            newList.add(adapter); // put them in the new list
//                            found = true;
//                            iter.remove(); // remove them from the old
//                            break;
//                        }
//                    }
//
//                    if (!found) // wasnt already tracked
//                    {
//                        DynamicLights.addLightSource(new EntityLightAdapter(ent, light));
//                    }
//                }
//            }
//            // any remaining adapters were not in the loaded entities. The main Dynamic Lights mod will kill them.
//            trackedItems = newList;
//            threadRunning = false;
//        }
//
//    }
//
//    private class EntityLightAdapter implements IDynamicLightSource
//    {
//
//        private final Entity entity;
//        private final int lightLevel;
//
//        public EntityLightAdapter(Entity e, int light)
//        {
//            lightLevel = light;
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
//}
