package atomicstryker.dynamiclights.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import coloredlightscore.src.api.CLBlock;
import cpw.mods.fml.common.registry.GameData;

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
                dataMap.put(fromString(duo[0]), duo.length > 1 ? Integer.parseInt(duo[1]) : defaultValue);
            }
            catch (Exception e)
            {
                System.err.println("Error, String ["+s+"] is not a valid Entry, skipping.");
                e.printStackTrace();
            }
        }
    }
    
    public int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null)
        {
            int r = retrieveValue(GameData.getItemRegistry().getNameForObject(stack.getItem()), stack.getItemDamage());
            return r < 0 ? 0 : r;
        }
        return 0;
    }
    
    public int retrieveValue(String name, int meta)
    {
        if (name != null)
        {
            for (ItemData item : dataMap.keySet())
            {
                if (item.matches(name, meta))
                {
                    int val = dataMap.get(item);
                    if (val == WILDCARD)
                    {
                        Block b = GameData.getBlockRegistry().getObject(name);
                        if (b instanceof CLBlock)
                        {
                            return ((CLBlock)b).getColorLightValue(meta);
                        }
                        return b != null ? b.getLightValue() : 0;
                    }
                    return val;
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
        int sm = len > 1 ? catchWildcard(strings[len > 3 ? 2 : 1]) : WILDCARD;
        int em = len > 2 ? catchWildcard(strings[len > 3 ? 3 : 2]) : sm;
        
        if (!strings[0].contains(":"))
        {
            strings[0] = "minecraft:"+strings[0];
        }
        
        return new ItemData(strings[0], sm, em);
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
        
        public ItemData(String name, int startmetarange, int endmetarange)
        {
            nameOf = name;
            startMeta = startmetarange;
            endMeta = endmetarange;
        }
        
        @Override
        public String toString()
        {
            return nameOf+" "+startMeta+" "+endMeta;
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
