package atomicstryker.ruins.common;

import java.util.ArrayList;
import java.util.Iterator;

class RuinTemplateLayer
{
    private final int[][] layer;

    public RuinTemplateLayer(ArrayList<String> layerdata, int width, int length, int maxrule) throws Exception
    {
        layer = new int[width][length];
        Iterator<String> i = layerdata.iterator();
        String row;
        String[] rowdata;
        for (int x = 0; x < width; x++)
        {
            row = i.next();
            rowdata = row.split(",");
            for (int z = 0; z < length; z++)
            {
                // ruins are flipped when read in, hence this crazy hack.
                layer[x][z] = Integer.parseInt(rowdata[length - z - 1]);

                if (layer[x][z] > maxrule)
                {
                    throw new Exception("Template uses undefined Rule, number: " + layer[x][z]);
                }
            }
        }
    }

    public int getRuleAt(int x, int z)
    {
        try
        {
            return layer[x][z];
        }
        catch (Exception e)
        {
            System.err.println("Attempting to get rule at " + x + ", " + z + ".");
            return 0;
        }
    }
}