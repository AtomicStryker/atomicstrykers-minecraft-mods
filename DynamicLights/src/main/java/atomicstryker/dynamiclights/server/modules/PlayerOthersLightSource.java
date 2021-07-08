package atomicstryker.dynamiclights.server.modules;

/**
 * 
 * @author AtomicStryker
 *
 *         Offers Dynamic Light functionality to Player Entities that aren't the
 *         client. Handheld Items and Armor can give off Light through this
 *         Module.
 *
 */
//@Mod(PlayerOthersLightSource.MOD_ID)
//@Mod.EventBusSubscriber(modid = PlayerOthersLightSource.MOD_ID, value = Dist.CLIENT)
//public class PlayerOthersLightSource
//{
//    static final String MOD_ID = "dynamiclights_otherplayers";
//
//    private Minecraft mcinstance;
//    private long nextUpdate;
//    private ArrayList<OtherPlayerAdapter> trackedPlayers;
//    private boolean threadRunning;
//
//    private static ItemConfigHelper itemsMap;
//
//    public PlayerOthersLightSource()
//    {
//        trackedPlayers = new ArrayList<>();
//        threadRunning = false;
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
//    }
//
//    public void onClientSetup(FMLClientSetupEvent evt)
//    {
//        mcinstance = evt.getMinecraftSupplier().get();
//        nextUpdate = System.currentTimeMillis();
//    }
//
//    public void onConfigLoad(ModConfig.ModConfigEvent event)
//    {
//        if (event.getConfig().getSpec() == CLIENT_SPEC)
//        {
//            loadConfig();
//        }
//    }
//
//    public static final PlayerOthersLightSource.ClientConfig CLIENT_CONFIG;
//    public static final ForgeConfigSpec CLIENT_SPEC;
//
//    public static List<? extends String> itemsList = new ArrayList<>();
//    public static Integer updateInterval = 0;
//
//    static
//    {
//        final Pair<PlayerOthersLightSource.ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PlayerOthersLightSource.ClientConfig::new);
//        CLIENT_SPEC = specPair.getRight();
//        CLIENT_CONFIG = specPair.getLeft();
//    }
//
//    public static class ClientConfig
//    {
//        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemsList;
//        public ForgeConfigSpec.ConfigValue<Integer> updateInterval;
//
//        ClientConfig(ForgeConfigSpec.Builder builder)
//        {
//            itemsList = builder.comment("Items that shine light").translation("forge.configgui.itemsList").defineList("itemsList", getDefaultLightItems(), i -> i instanceof String);
//            updateInterval = builder.comment("Update Interval time for all other player entities in milliseconds. The lower the better and costlier.").define("updateInterval", 1000,
//                    i -> i instanceof Integer);
//            builder.pop();
//        }
//    }
//
//    private static List<String> getDefaultLightItems()
//    {
//        ItemStack torchStack = new ItemStack(Blocks.TORCH);
//        List<String> output = new ArrayList<>();
//        output.add(ItemConfigHelper.fromItemStack(torchStack));
//        return output;
//    }
//
//    public static void loadConfig()
//    {
//        itemsList = CLIENT_CONFIG.itemsList.get();
//        updateInterval = CLIENT_CONFIG.updateInterval.get();
//        itemsMap = new ItemConfigHelper(itemsList);
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
//                Thread thread = new OtherPlayerChecker(mcinstance.world.loadedEntityList);
//                thread.setPriority(Thread.MIN_PRIORITY);
//                thread.start();
//                threadRunning = true;
//            }
//        }
//    }
//
//    private int getLightFromItemStack(ItemStack stack)
//    {
//        return itemsMap.contains(stack) ? 15 : 0;
//    }
//
//    private class OtherPlayerChecker extends Thread
//    {
//        private final Object[] list;
//
//        public OtherPlayerChecker(List<Entity> input)
//        {
//            list = input.toArray();
//        }
//
//        @Override
//        public void run()
//        {
//            ArrayList<OtherPlayerAdapter> newList = new ArrayList<>();
//
//            Entity ent;
//            for (Object o : list)
//            {
//                ent = (Entity) o;
//                // Loop all loaded Entities, find alive and valid other Player Entities
//                if (ent instanceof EntityOtherPlayerMP && ent.isAlive())
//                {
//                    // now find them in the already tracked player adapters
//                    boolean found = false;
//                    Iterator<OtherPlayerAdapter> iter = trackedPlayers.iterator();
//                    OtherPlayerAdapter adapter;
//                    while (iter.hasNext())
//                    {
//                        adapter = iter.next();
//                        if (adapter.getAttachmentEntity().equals(ent)) // already tracked!
//                        {
//                            adapter.onTick(); // execute a tick
//                            newList.add(adapter); // put them in the new list
//                            found = true;
//                            iter.remove(); // remove them from the old
//                            break;
//                        }
//                    }
//
//                    if (!found) // wasnt already tracked
//                    {
//                        // make new, tick, put in new list
//                        adapter = new OtherPlayerAdapter((EntityPlayer) ent);
//                        adapter.onTick();
//                        newList.add(adapter);
//                    }
//                }
//            }
//            // any remaining adapters were not in the loaded entities. The main Dynamic
//            // Lights mod will kill them.
//            trackedPlayers = newList;
//            threadRunning = false;
//        }
//
//    }
//
//    private class OtherPlayerAdapter implements IDynamicLightSource
//    {
//
//        private EntityPlayer player;
//        private int lightLevel;
//        private boolean enabled;
//
//        public OtherPlayerAdapter(EntityPlayer p)
//        {
//            lightLevel = 0;
//            enabled = false;
//            player = p;
//        }
//
//        /**
//         * Since they are IDynamicLightSource instances, they will already receive
//         * updates! Why do we need to do this? Because Player Entities can change
//         * equipment and we really don't want this method in an onUpdate tick, way too
//         * expensive. So we put it in a seperate Thread!
//         */
//        public void onTick()
//        {
//            int prevLight = lightLevel;
//
//            lightLevel = Math.max(getLightFromItemStack(player.getHeldItemMainhand()), getLightFromItemStack(player.getHeldItemOffhand()));
//            for (ItemStack armor : player.inventory.armorInventory)
//            {
//                lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
//            }
//
//            if (prevLight != 0 && lightLevel != prevLight)
//            {
//                lightLevel = 0;
//            }
//            else
//            {
//                if (player.isBurning())
//                {
//                    lightLevel = 15;
//                }
//            }
//
//            if (!enabled && lightLevel > 0)
//            {
//                enableLight();
//            }
//            else if (enabled && lightLevel < 1)
//            {
//                disableLight();
//            }
//        }
//
//        private void enableLight()
//        {
//            DynamicLights.addLightSource(this);
//            enabled = true;
//        }
//
//        private void disableLight()
//        {
//            DynamicLights.removeLightSource(this);
//            enabled = false;
//        }
//
//        @Override
//        public Entity getAttachmentEntity()
//        {
//            return player;
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
