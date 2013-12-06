package atomicstryker.findercompass.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import atomicstryker.findercompass.client.CompassSetting;

public class DefaultConfigFilePrinter
{

    public void writeDefaultFile(File fileToPrint)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileToPrint)));

            pw.println("// In this config, everything beneath a Line of Syntax");
            pw.println("// Setting:yourSettingsNameHere");
            pw.println("// is considered a new and seperate Compass-Mode to be switched by using the compass. You can specify anything you want ANEW for each Setting.");
            pw.println("//");
            pw.println("// Note the hard-coded Ender Eye Needle. If you don't want it, you need to disable it by stating NoEnderEyeNeedle (see examples below)");
            pw.println("//");
            pw.println("// Inside a Setting:");
            pw.println("// Lines may be added in the form BlockID:R:G:B:ScanrangeHor:ScanrangeVer:MinBlockY:MaxBlockY:boolDelayed[:damageDropped]");
            pw.println("//");
            pw.println("// BlockID - the Block ID the compass should look for");
            pw.println("// R:G:B - the color values the needle should use");
            pw.println("// ScanrangeHor - scanrange -x,-z to +x,+z");
            pw.println("// ScanrangeVer - scanrange depth, '1' is visible blocks from a 1x2 tunnel");
            pw.println("// MinBlockY - minimum block height to scan");
            pw.println("// MaxBlockY - maximum block height to scan");
            pw.println("// boolDelayed - boolean for scanning only every 15 seconds");
            pw.println("// [OPTIONAL] damageDropped - the system some mods like Redpower2 use to stack more than one block at the same blockID. You can specify the subvalue here");
            pw.println("// damageDropped is optional and does not need to be set in order for a config line to work. You can let your line end with boolDelayed as before.");
            pw.println("//");
            pw.println("// to get minecraft IDs visit www.minecraftwiki.net/wiki/Data_values");
            pw.println("// to get an RGB color just google online RGB mixer");
            pw.println("//");
            pw.println("// For your convenience here's some presets");
            pw.println("//");
            pw.println("//");
            pw.println("//");
            pw.println("Setting:Working Man's Mineables");
            pw.println("//");
            pw.println("NoEnderEyeNeedle");
            pw.println("//");
            pw.println("// - gold");
            pw.println("14:245:245:0:15:1:1:100:0");
            pw.println("//");
            pw.println("// - iron");
            pw.println("15:179:179:179:15:1:1:100:0");
            pw.println("//");
            pw.println("// - coal");
            pw.println("16:51:26:0:15:1:1:100:0");
            pw.println("//");
            pw.println("//");
            pw.println("Setting:Adventuring");
            pw.println("//");
            pw.println("// NoEnderEyeNeedle");
            pw.println("//");
            pw.println("// this is Mob Spawners = block id 52, with a needle color of {26,255,26}, 60 width and 60 depth, from 1-100 height, big scan every 15 seconds");
            pw.println("52:26:255:26:60:60:1:100:1");
            pw.println("//");
            pw.println("// this is Chest (for Adventuring) = block id 54, with a needle color of {184,138,0}, 60 width and 60 depth, from 1-100 height, big scan every 15 seconds");
            pw.println("54:184:138:0:60:60:1:100:1");
            pw.println("//");
            pw.println("//");
            pw.println("Setting:Shiny Stones");
            pw.println("//");
            pw.println("NoEnderEyeNeedle");
            pw.println("//");
            pw.println("// this is Diamond = block id 56, with a needle color of {51,255,204}, it scans 15 blocks horizontally, 1 vertically, from 1-16 height, every second");
            pw.println("56:51:255:204:15:1:1:16:0");
            pw.println("//");
            pw.println("// - lapis lazuli");
            pw.println("21:55:70:220:15:1:1:100:0");
            pw.println("//");
            pw.println("// - redstone");
            pw.println("73:255:125:155:15:1:1:100:0");
            pw.println("//");
            pw.println("// - emerald ore");
            pw.println("129:26:255:26:7:1:4:31:0 ");

            pw.flush();
            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void parseConfig(BufferedReader buffreader, ArrayList<CompassSetting> settingList)
    {
        int curLine = 0;
        try
        {
            CompassSetting currentSetting = null;
            
            String buffer;
            while ((buffer = buffreader.readLine()) != null)
            {
                buffer = buffer.trim();
                curLine++;
                if (!buffer.startsWith("//") && !buffer.equals(""))
                {
                    if (buffer.startsWith("Setting:"))
                    {
                        settingList.add(new CompassSetting(buffer.substring(8)));
                        currentSetting = settingList.get(settingList.size() - 1);
                        System.out.println("Created new Compass Setting of the name: " + currentSetting.getName());
                    }
                    else if (buffer.contentEquals("NoDefaultNeedle"))
                    {
                        //left in for backwards compatibility
                    }
                    else if (buffer.contentEquals("NoEnderEyeNeedle"))
                    {
                        currentSetting.setHasStrongholdNeedle(false);
                        System.out.println("Disabling Ender Eye Needle as per config file");
                    }
                    else if (settingList.isEmpty())
                    {
                        //System.out.println("ERROR: Finder Compass skipping config line because no Setting has been defined yet! Did you update your config file???");
                    }
                    else
                    {
                        String[] splitString = buffer.split(":");
                        int blockID = Integer.parseInt(splitString[0]);
                        int[] configInts = new int[9];
                        configInts[0] = Integer.parseInt(splitString[1]);
                        configInts[1] = Integer.parseInt(splitString[2]);
                        configInts[2] = Integer.parseInt(splitString[3]);
                        System.out.println("Finder Compass: loaded custom needle of id " + blockID + ", color [" + configInts[0] + "," + configInts[1] + "," + configInts[2] + "]");
                        configInts[3] = Integer.parseInt(splitString[4]);
                        configInts[4] = Integer.parseInt(splitString[5]);
                        configInts[5] = Integer.parseInt(splitString[6]);
                        configInts[6] = Integer.parseInt(splitString[7]);
                        configInts[7] = Integer.parseInt(splitString[8]);
                        if (splitString.length > 9)
                        {
                            configInts[8] = Integer.parseInt(splitString[9]);
                        }
                        else
                        {
                            configInts[8] = 0;
                        }
                        System.out.println("Full readout: " + blockID + ":" + configInts[0] + ":" + configInts[1] + ":" + configInts[2] + ":" + configInts[3] + ":" + configInts[4] + ":"
                                + configInts[5] + ":" + configInts[6] + ":" + configInts[7] + ":" + configInts[8]);
                        CompassIntPair key = new CompassIntPair(blockID, configInts[8]);
                        currentSetting.getCustomNeedles().put(key, configInts);
                    }
                }
            }

            buffreader.close();
        }
        catch (Exception ex)
        {
            System.err.println("There was a problem reading the findercompass.cfg, Parser bailed out in line " + curLine);
            ex.printStackTrace();
        }
    }
}
