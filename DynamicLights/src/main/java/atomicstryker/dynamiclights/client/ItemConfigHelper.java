package atomicstryker.dynamiclights.client;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

public class ItemConfigHelper
{
    private final String SWILDCARD = "*";
    private final int WILDCARD = -1;
    
    private Map<ItemData, Integer> dataMap;
    
    public ItemConfigHelper(String configLine, int defaultValue)
    {
        dataMap = new HashMap<ItemData, Integer>();
        for (String s : configLine.split(","))
        {
            try
            {
                String[] duo = s.split("=");
                ItemData item = fromString(duo[0]);
                if (item != null)
                {
                    dataMap.put(item, duo.length > 1 ? Integer.parseInt(duo[1]) : defaultValue);
                }
                else
                {
                    System.out.println("Failed to match String ["+s+"] to a Block or Item, skipping.");
                }
            }
            catch (Exception e)
            {
                System.err.println("Error, String ["+s+"] is not a valid Entry, skipping.");
                e.printStackTrace();
            }
        }
    }
    
    public int retrieveValue(String name, int meta)
    {
        if (name != null)
        {
            for (ItemData item : dataMap.keySet())
            {
                if (item.matches(name, meta))
                {
                    return dataMap.get(item);
                }
            }
        }
        return -1;
    }
    
    /**
     * Possible setups:
     * X := simple ID X, wildcards metadata
     * X-Y := simple ID X and metadata Y
     * X-Y-Z := simple ID X, metadata range Y to Z
     * @param s trimmed String input, matching one of the setups
     * @return ItemData instance
     */
    private ItemData fromString(String s)
    {
        String[] strings = s.split("-");
        int len = strings.length;
        
        if (tryFindingObject(strings[0]) != null)
        {
            int sm = len > 1 ? catchWildcard(strings[len > 3 ? 2 : 1]) : WILDCARD;
            int em = len > 2 ? catchWildcard(strings[len > 3 ? 3 : 2]) : sm;
            return new ItemData(strings[0], sm, em);
        }
        return null;
    }
    
    private Object tryFindingObject(String s)
    {
        Item item = GameData.itemRegistry.getObject(s);
        if (item != null)
        {
            return item;
        }
        
        Block block = GameData.blockRegistry.getObject(s);
        if (block != Blocks.air)
        {
            return block;
        }
        return null;
    }
    
    private int catchWildcard(String s)
    {
        if (s.equals(SWILDCARD))
        {
            return WILDCARD;
        }
        return Integer.parseInt(s);
    }
    
    private class ItemData
    {
        private String nameOf;
        final int startMeta;
        final int endMeta;
        
        public ItemData(String name, int sm, int em)
        {
            nameOf = name;
            startMeta = sm;
            endMeta = em;
        }
        
        @Override
        public String toString()
        {
            return nameOf+"-"+startMeta+"-"+endMeta;
        }
        
        public boolean matches(String name, int meta)
        {
            return name.equals(nameOf) && isContained(startMeta, endMeta, meta);
        }
        
        private boolean isContained(int s, int e, int i)
        {
            return (s == WILDCARD || i >= s) && (e == WILDCARD || i <= e);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof ItemData)
            {
                ItemData i = (ItemData) o;
                return i.nameOf.equals(nameOf) && i.startMeta == startMeta && i.endMeta == endMeta;
            }
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return nameOf.hashCode() + startMeta + endMeta;
        }
    }
    
}
