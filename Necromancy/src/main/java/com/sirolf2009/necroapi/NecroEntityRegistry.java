package com.sirolf2009.necroapi;

import java.util.HashMap;

/**
 * The registry class to register necro mobs
 * 
 * @author sirolf2009
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public class NecroEntityRegistry
{

    /**
     * The map containing the registered mobs
     */
    public static HashMap<String, NecroEntityBase> registeredEntities = new HashMap<String, NecroEntityBase>();
    /**
     * The map containing the registered mobs that have registered a skull
     */
    public static HashMap<String, ISkull> registeredSkullEntities = new HashMap<String, ISkull>();

    /**
     * Call this to register your necro mob
     * 
     * @param the
     *            mob to be registered
     */
    public static void registerEntity(NecroEntityBase data)
    {
        if (data.isNecromancyInstalled && !registeredEntities.containsKey(data.mobName))
        {
            registeredEntities.put(data.mobName, data);
            if (data instanceof ISkull)
            {
                registeredSkullEntities.put(data.mobName, (ISkull) data);
            }
        }
    }
}
