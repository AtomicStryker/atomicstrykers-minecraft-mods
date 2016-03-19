package atomicstryker.ruins.common;

import java.util.HashMap;

public class RuinStats
{

    public int NumCreated = 0, siteTries = 0, BadBlockFails = 0, LevelingFails = 0, CutInFails = 0, OverhangFails = 0, NoAirAboveFails = 0;

    public final HashMap<String, Integer> biomes = new HashMap<String, Integer>();

    public int minDistFails = 0;
}