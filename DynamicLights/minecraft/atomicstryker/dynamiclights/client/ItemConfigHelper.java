package atomicstryker.dynamiclights.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
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
                if (item.startID != 0)
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
            }
        }
    }
    
    public int retrieveValue(int id, int meta)
    {
        for (ItemData item : dataMap.keySet())
        {
            if (item.matches(id, meta))
            {
                return dataMap.get(item);
            }
        }
        return -1;
    }
    
    /**
     * Possible setups:
     * X := simple ID X, wildcards metadata
     * X-Y := simple ID X and metadata Y
     * X-Y-Z := simple ID X, metadata range Y to Z
     * A-B-C-D := ID range A to B, meta range C to D
     * @param s trimmed String input, matching one of the setups
     * @return ItemData instance
     */
    private ItemData fromString(String s)
    {
        String[] strings = s.split("-");
        int len = strings.length;
        int sid = tryFindingItemID(strings[0]);
        int eid = len > 3 ? tryFindingItemID(strings[1]) : sid;
        int sm = len > 1 ? catchWildcard(strings[2]) : WILDCARD;
        int em = len > 2 ? catchWildcard(strings[3]) : sm;
        return new ItemData(sid, eid, sm, em);
    }
    
    private int tryFindingItemID(String s)
    {
        try
        {
            return catchWildcard(s);
        }
        catch (NumberFormatException e)
        {
            for (Item item : Item.itemsList)
            {
                if (item != null && item.getUnlocalizedName().equals(s))
                {
                    return item.itemID;
                }
            }
            for (Block block : Block.blocksList)
            {
                if (block != null && block.getUnlocalizedName().equals(s))
                {
                    return block.blockID;
                }
            }
            return 0;
        }
    }
    
    private int catchWildcard(String s)
    {
        if (s.equals(SWILDCARD))
        {
            return WILDCARD;
        }
        return Integer.parseInt(s);
    }
    
    private class ItemData implements Comparable<ItemData>
    {
        final int startID;
        final int endID;
        final int startMeta;
        final int endMeta;
        
        public ItemData(int sid, int eid, int sm, int em)
        {
            startID = sid;
            endID = eid;
            startMeta = sm;
            endMeta = em;
        }
        
        @Override
        public String toString()
        {
            return String.format("%d-%d-%d-%d", startID, endID, startMeta, endMeta);
        }
        
        public boolean matches(int id, int meta)
        {
            return isContained(startID, endID, id) && isContained(startMeta, endMeta, meta);
        }
        
        private boolean isContained(int s, int e, int i)
        {
            if (s == WILDCARD)
            {
                return true;
            }
            return i >= s && i <= e;
        }
        
        @Override
        public int compareTo(ItemData i)
        {
            return startID < i.startID ? -1 : startID > i.startID ? 1 : 0;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof ItemData)
            {
                ItemData i = (ItemData) o;
                return i.startID == startID && i.endID == endID && i.startMeta == startMeta && i.endMeta == endMeta;
            }
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return startID+endID+startMeta+endMeta;
        }
    }
    
}
