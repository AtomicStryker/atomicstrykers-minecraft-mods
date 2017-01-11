package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.CompassSetting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.ArrayList;

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
            pw.println("// BlockID - the Block Name the compass should look for. Google Minecraft Data for correct values");
            pw.println("// R:G:B - the color values the needle should use");
            pw.println("// ScanrangeHor - scanrange -x,-z to +x,+z");
            pw.println("// ScanrangeVer - scanrange depth, '1' is visible blocks from a 1x2 tunnel");
            pw.println("// MinBlockY - minimum block height to scan");
            pw.println("// MaxBlockY - maximum block height to scan");
            pw.println("// boolDelayed - boolean for scanning only every 15 seconds");
            pw.println("// [OPTIONAL] damageDropped - the system some mods like Redpower2 use to stack more than one block at the same blockID. You can specify the subvalue here");
            pw.println("// damageDropped is optional and does not need to be set in order for a config line to work. You can let your line end with boolDelayed as before. -1 is the explicit meta for ANY damage");
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
            pw.println("gold_ore:245:245:0:15:1:1:100:0");
            pw.println("//");
            pw.println("// - iron");
            pw.println("iron_ore:179:179:179:15:1:1:100:0");
            pw.println("//");
            pw.println("// - coal");
            pw.println("coal_ore:51:26:0:15:1:1:100:0");
            pw.println("//");
            pw.println("//");
            pw.println("Setting:Adventuring");
            pw.println("//");
            pw.println("// NoEnderEyeNeedle");
            pw.println("//");
            pw.println("// this is Mob Spawners with a needle color of {26,255,26}, 60 width and 60 depth, from 1-100 height, big scan every 15 seconds");
            pw.println("mob_spawner:26:255:26:60:60:1:100:1");
            pw.println("//");
            pw.println("// this is Chest (for Adventuring) = block id 54, with a needle color of {184,138,0}, 60 width and 60 depth, from 1-100 height, big scan every 15 seconds");
            pw.println("chest:184:138:0:60:60:1:100:1");
            pw.println("//");
            pw.println("//");
            pw.println("Setting:Shiny Stones");
            pw.println("//");
            pw.println("NoEnderEyeNeedle");
            pw.println("//");
            pw.println("// this is Diamond = block id 56, with a needle color of {51,255,204}, it scans 15 blocks horizontally, 1 vertically, from 1-16 height, every second");
            pw.println("diamond_ore:51:255:204:15:1:1:16:0");
            pw.println("//");
            pw.println("// - lapis lazuli");
            pw.println("lapis_ore:55:70:220:15:1:1:100:0");
            pw.println("//");
            pw.println("// - redstone");
            pw.println("redstone_ore:255:125:155:15:1:1:100:0");
            pw.println("//");
            pw.println("// - emerald ore");
            pw.println("emerald_ore:26:255:26:7:1:4:31:0 ");

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
        CompassSetting currentSetting = null;

        String buffer;
        Block block;
        boolean printBlocks = false;
        try
        {
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
                        System.out.println("Finder Compass skipping legacy setting NoDefaultNeedle");
                    }
                    else if (buffer.contentEquals("NoEnderEyeNeedle"))
                    {
                        if (currentSetting == null)
                        {
                            System.err.println("Finder Compass config line "+buffer+" without Setting before it!!!");
                        }
                        else
                        {
                            currentSetting.setHasStrongholdNeedle(false);
                            System.out.println("Disabling Ender Eye Needle as per config file");
                        }
                    }
                    else if (settingList.isEmpty())
                    {
                        //System.out.println("ERROR: Finder Compass skipping config line because no Setting has been defined yet! Did you update your config file???");
                    }
                    else if (currentSetting != null)
                    {
                        try
                        {
                            int prefixoffset = 0;
                            String[] splitString = buffer.split(":");
                            String blockID = splitString[0];
                            if (blockID.equals("minecraft") || Block.REGISTRY.getObject(new ResourceLocation(blockID)) == Blocks.AIR)
                            {
                                prefixoffset = 1;
                                blockID = splitString[0]+":"+splitString[1];
                            }
                            
                            int[] configInts = new int[9];
                            configInts[0] = Integer.parseInt(splitString[prefixoffset+1]);
                            configInts[1] = Integer.parseInt(splitString[prefixoffset+2]);
                            configInts[2] = Integer.parseInt(splitString[prefixoffset+3]);
                            System.out.println("Finder Compass: loaded custom needle of id " + blockID + ", color [" + configInts[0] + "," + configInts[1] + "," + configInts[2] + "]");
                            configInts[3] = Integer.parseInt(splitString[prefixoffset+4]);
                            configInts[4] = Integer.parseInt(splitString[prefixoffset+5]);
                            configInts[5] = Integer.parseInt(splitString[prefixoffset+6]);
                            configInts[6] = Integer.parseInt(splitString[prefixoffset+7]);
                            configInts[7] = Integer.parseInt(splitString[prefixoffset+8]);
                            if (splitString.length > prefixoffset+9)
                            {
                                configInts[8] = Integer.parseInt(splitString[prefixoffset+9]);
                            }
                            else
                            {
                                configInts[8] = -1;
                            }
                            System.out.println("Full readout: " + blockID + ":" + configInts[0] + ":" + configInts[1] + ":" + configInts[2] + ":" + configInts[3] + ":" + configInts[4] + ":"
                                    + configInts[5] + ":" + configInts[6] + ":" + configInts[7] + ":" + configInts[8]);

                            block = Block.REGISTRY.getObject(new ResourceLocation(blockID));
                            if (block != Blocks.AIR)
                            {
                            	System.err.println("Finder Compass resolved "+blockID+" to "+block);
                                CompassTargetData key = new CompassTargetData(block, configInts[8]);
                                currentSetting.getCustomNeedles().put(key, configInts);
                            }
                            else
                            {
                                System.err.println("Finder Compass could not find a Block "+blockID+", skipping that entry...");
                                printBlocks = true;
                            }
                        }
                        catch (Exception ex)
                        {
                            System.err.println("There was a problem parsing findercompass.cfg, parser failed in line " + curLine);
                            System.err.println("line: "+buffer);
                            ex.printStackTrace();
                        }
                    }
                }
            }
            buffreader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (printBlocks)
        {
            System.err.println("For your Finder Compass ID convenience, following a dump of all currently registered block IDs:");
            Block.REGISTRY.getKeys().forEach(System.out::println);
        }
    }
}
