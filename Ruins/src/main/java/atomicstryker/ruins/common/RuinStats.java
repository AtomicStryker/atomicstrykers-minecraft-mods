package atomicstryker.ruins.common;

import java.util.HashMap;

class RuinStats
{
    public final HashMap<String, Integer> biomes = new HashMap<>();
    int numCreated = 0;
    int noSurfaceFails = 0;
    int levelingFails = 0;
    int minDistFails = 0;
}