package atomicstryker.findercompass.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import atomicstryker.findercompass.client.CompassSetting;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
            pw.println("// BlockID - the Block Name or Ore Dictionary Name the compass should look for. Google Minecraft Data for correct values or show ore dictionary names in game");
            pw.println("// R:G:B - the color values the needle should use");
            pw.println("// ScanrangeHor - scanrange -x,-z to +x,+z");
            pw.println("// ScanrangeVer - scanrange depth, '1' is visible blocks from a 1x2 tunnel");
            pw.println("// MinBlockY - minimum block height to scan");
            pw.println("// MaxBlockY - maximum block height to scan");
            pw.println("// boolDelayed - boolean for scanning only every 15 seconds");
            pw.println("// [OPTIONAL] damageDropped - the system some mods like Redpower2 use to stack more than one block at the same blockID. You can specify the subvalue here");
            pw.println("// damageDropped is optional and only allowed for block names! It does not need to be set in order for a config line to work. You can let your line end with boolDelayed as before.");
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
            pw.println("oreDiamond:51:255:204:15:1:1:16:0");
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
                        try
                        {
                            int prefixoffset = 0;
                            String[] splitString = buffer.split(":");
                            String blockID = splitString[0];
                            
							if (splitString.length > 9 && (blockID.equals("minecraft")
									|| GameData.getBlockRegistry().getObject(blockID) == Blocks.air)) {
								prefixoffset = 1;
								blockID = splitString[0] + ":" + splitString[1];
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
                                configInts[8] = 0;
                            }
                            System.out.println("Full readout: " + blockID + ":" + configInts[0] + ":" + configInts[1] + ":" + configInts[2] + ":" + configInts[3] + ":" + configInts[4] + ":"
                                    + configInts[5] + ":" + configInts[6] + ":" + configInts[7] + ":" + configInts[8]);

							Map<String, CompassTargetData> data = getBlocks(blockID, configInts[8]);
							for (Entry<String, CompassTargetData> blockData : data.entrySet()) {
								CompassTargetData needle = currentSetting.getCustomNeedle(blockData.getKey());
								if (needle != null)
								{
									needle.addAll(blockData.getValue());
								} else {									
									currentSetting.getCustomNeedles().put(blockData.getValue(), configInts);
								}
							}

							if (data.isEmpty()) {
								System.err.println("Finder Compass could not find a Block " + blockID
										+ ", skipping that entry...");
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
            for (Object o : GameData.getBlockRegistry().getKeys())
            {
                System.out.println(o);
            }
        }
    }
    
	private Map<String,CompassTargetData> getBlocks(String pBlockName, int pDamage) {
		Map<String, CompassTargetData> data = new HashMap<String, CompassTargetData>();

		Block block = GameData.getBlockRegistry().getObject(pBlockName);
		if (block != Blocks.air) {
			String oreDictName = null;
			CompassTargetData compassTargetData =null;
			
			int[] blockIDs = OreDictionary.getOreIDs(new ItemStack(block, 1, pDamage));
			for (int oreBlockID : blockIDs) {
				oreDictName = OreDictionary.getOreName(oreBlockID);
				compassTargetData = data.get(oreDictName);
				if (compassTargetData == null){
					compassTargetData = new CompassTargetData(oreDictName);
				}
				
				for (ItemStack stack : OreDictionary.getOres(oreDictName)) {
					Item item = stack.getItem();
					int damage = item.getDamage(stack);
					block = GameData.getBlockRegistry().getObject(item.delegate.name());
					compassTargetData.add(block, damage);
				}
				
				if (!compassTargetData.isEmpty()){
					data.put(oreDictName, compassTargetData);
				}
			}
			
			if (compassTargetData == null){
				compassTargetData = new CompassTargetData(block, pDamage, oreDictName);
				if (!compassTargetData.isEmpty()){
					data.put(oreDictName, compassTargetData);
				}
			}
		} else {
			String oreDictName = pBlockName;
			CompassTargetData compassTargetData = new CompassTargetData(oreDictName);			
			for (ItemStack stack : OreDictionary.getOres(oreDictName)) {
				Item item = stack.getItem();
				int damage = item.getDamage(stack);
				block = GameData.getBlockRegistry().getObject(item.delegate.name());
				compassTargetData.add(block, damage);
			}
			
			if (!compassTargetData.isEmpty()){
				data.put(oreDictName, compassTargetData);
			}
		}

		return data;
	}
}
