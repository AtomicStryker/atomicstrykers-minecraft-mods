package atomicstryker.findercompass.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.findercompass.common.AS_FinderCompassIntPair;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class AS_FinderCompass extends TextureFX
{
    private static Minecraft mc;
    private int[] tsize;
    private double[] textureId = new double[30];
    private double[] tileSize = new double[30];
    private int[] spawnNeedlecolor = new int[] {255, 20, 20};
    private int[] strongholdNeedlecolor = new int[] {175, 220, 0};    
    private static File settingsFile;
    private final double HEADING_DOWN = 0.0D;
    private final double HEADING_UP = 135.0D;
    private int x;
    private int y;
    private int z;
    private final ChunkCoordinates NullChunk = new ChunkCoordinates(0, 0, 0);
    private long lastTime;
    private int seccounter = 0;
    private boolean updateScan = false;
    
    public static boolean serverHasFinderCompass = false;
    public static ChunkCoordinates strongholdCoords = new ChunkCoordinates(0, 0, 0);
    public static boolean hasStronghold = false;
    private static ArrayList<CompassSetting> settingList;
    private static CompassSetting currentSetting;
    
    private final int tileSize_int_compassCrossMin = -4;
    private final int tileSize_int_compassCrossMax = 4;
    private int tileSizeBase;
    private int tileSizeSquare;
    private double tileSize_double_compassCenterMin;
    private double tileSize_double_compassCenterMax;
    private int tileSize_int_compassNeedleMin;
    private int tileSize_int_compassNeedleMax;
    
    public AS_FinderCompass(Minecraft var1)
    {
        super("findercompass:compass", 16, 16);
		this.mc = var1;
		
        setupTileSizes();
        this.tsize = new int[tileSizeSquare];

        lastTime = System.currentTimeMillis();
        if (settingList == null)
        {
            settingList = new ArrayList<CompassSetting>();
            initializeSettingsFile();
        }
    }

    private void setupTileSizes()
    {
        tileSizeBase = this.getWidth();
        tileSizeSquare = tileSizeBase*tileSizeBase;
        
        this.tileSize_double_compassCenterMin = (double)(this.tileSizeBase / 2) - 0.5D;
        this.tileSize_double_compassCenterMax = (double)(this.tileSizeBase / 2) + 0.5D;
        tileSize_int_compassNeedleMin = -(tileSizeBase >> 2);
        tileSize_int_compassNeedleMax = tileSizeBase;
        
        System.out.println("fml: tilesize_intsize = "+tileSizeBase+"; tilesize_numpixels = "+tileSizeSquare+";");
        System.out.println("fml: compassNeedleMin = "+tileSize_int_compassNeedleMin+"; compassNeedleMax = "+tileSize_int_compassNeedleMax+";");
    }

    private void askServerForStrongholdCoords()
    {
        if (serverHasFinderCompass)
        {
            PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("FindrCmps", 1, null));
        }
    }
    
    @Override
    protected void onTick(byte[] imageData)
    {
        int var1;
        int var2;
        
        for (int pixelIndex = 0; pixelIndex < this.tileSizeSquare; ++pixelIndex)
        {
            int var4 = this.tsize[pixelIndex] >> 24 & 255;
            var1 = this.tsize[pixelIndex] >> 16 & 255;
            int var5 = this.tsize[pixelIndex] >> 8 & 255;
            var2 = this.tsize[pixelIndex] >> 0 & 255;
            
            imageData[pixelIndex * 4 + 0] = (byte)var1;
            imageData[pixelIndex * 4 + 1] = (byte)var5;
            imageData[pixelIndex * 4 + 2] = (byte)var2;
            imageData[pixelIndex * 4 + 3] = (byte)var4;
        }

        if (this.mc.theWorld != null && this.mc.thePlayer != null)
        {
            boolean isNewSecond = false;
            boolean is15SecInterval = false;
            if (System.currentTimeMillis() > this.lastTime + 1000L)
            {
                isNewSecond = true;
                ++this.seccounter;
                this.lastTime = System.currentTimeMillis();
            }
            
            if (currentSetting == null)
            {
                FMLClientHandler.instance().haltGame("Finder Compass config file cannot be read!", new Throwable("Read the installation instructions"));
                return;
            }

            if (currentSetting.getHasDefaultNeedle())
            {
                this.drawNeedle(imageData, 0, this.computeNeedleHeading(this.mc.theWorld.getSpawnPoint()), this.spawnNeedlecolor, true);
            }

            if (currentSetting.getHasStrongholdNeedle() && hasStronghold)
            {
                this.drawNeedle(imageData, 1, this.computeNeedleHeading(this.strongholdCoords), this.strongholdNeedlecolor, false);
            }

            if ((int)this.mc.thePlayer.posX != this.x || (int)this.mc.thePlayer.posY != this.y || (int)this.mc.thePlayer.posZ != this.z)
            {
                this.x = (int)this.mc.thePlayer.posX;
                this.y = (int)this.mc.thePlayer.posY;
                this.z = (int)this.mc.thePlayer.posZ;
                this.updateScan = true;
            }

            if (isNewSecond && this.seccounter > 14)
            {
                this.seccounter = 0;
                is15SecInterval = true;
                
                this.hasStronghold = false;
                askServerForStrongholdCoords();
            }

            int[] configInts;
            AS_FinderCompassIntPair blockInts;
            ChunkCoordinates coords;
            Iterator iter;
            Entry iterEntry;
            if (this.updateScan && isNewSecond)
            {
                this.updateScan = false;
                iter = currentSetting.getCustomNeedles().entrySet().iterator();

                while (iter.hasNext())
                {
                    iterEntry = (Entry)iter.next();
                    blockInts = ((AS_FinderCompassIntPair)iterEntry.getKey());
                    configInts = (int[])iterEntry.getValue();
                    if (is15SecInterval || configInts[7] == 0)
                    {
                        coords = this.findNearestBlockChunkOfIDInRange(blockInts.getBlockID(), blockInts.getDamage(), this.x, this.y, this.z, configInts[3], configInts[4], configInts[5], configInts[6]);
                        if (coords != null && !coords.equals(this.NullChunk))
                        {
                            if (currentSetting.getCustomNeedleTargets().containsKey(blockInts))
                            {
                                currentSetting.getCustomNeedleTargets().remove(blockInts);
                            }

                            currentSetting.getCustomNeedleTargets().put(blockInts, coords);
                        }
                        else
                        {
                            currentSetting.getCustomNeedleTargets().remove(blockInts);
                        }
                    }
                }
            }

            var1 = 3;
            iter = currentSetting.getCustomNeedleTargets().entrySet().iterator();

            while (iter.hasNext())
            {
                iterEntry = (Entry)iter.next();
                blockInts = (AS_FinderCompassIntPair) iterEntry.getKey();
                coords = (ChunkCoordinates)iterEntry.getValue();
                configInts = currentSetting.getCustomNeedles().get(blockInts);
                ++var1;
                this.drawNeedle(imageData, var1, this.computeNeedleHeading(coords), configInts, false);
            }
        }
    }

    public double computeNeedleHeading(ChunkCoordinates coords)
    {
        double var2 = 0.0D;
        if (this.mc.theWorld != null && this.mc.thePlayer != null)
        {
            double var4 = (double)coords.posX+0.5D - this.mc.thePlayer.posX;
            double var6 = (double)coords.posZ+0.5D - this.mc.thePlayer.posZ;
            var2 = (double)(this.mc.thePlayer.rotationYaw - 90.0F) * 3.141592653589793D / 180.0D - Math.atan2(var6, var4);
            if (this.mc.theWorld.provider.isHellWorld)
            {
                var2 = Math.random() * 3.1415927410125732D * 2.0D;
            }
        }

        return var2;
    }

    public void drawNeedle(byte[] imageData, int needleNum, double heading, int[] rgbColors, boolean drawCenter)
    {
    	double needleLength = 0.3D * (this.tileSizeBase / 16);
    	
        double var6;
        for (var6 = heading - this.textureId[needleNum]; var6 < -Math.PI; var6 += 2*Math.PI)
        {
            ;
        }

        while (var6 >= Math.PI)
        {
            var6 -= 2*Math.PI;
        }

        if (var6 < -1.0D)
        {
            var6 = -1.0D;
        }

        if (var6 > 1.0D)
        {
            var6 = 1.0D;
        }

        this.textureId[needleNum] += var6 * 0.1D;
        this.tileSize[needleNum] *= 0.8D;
        this.tileSize[needleNum] += this.textureId[needleNum];
        double var8 = Math.sin(this.textureId[needleNum]);
        double var10 = Math.cos(this.textureId[needleNum]);
        int var12;
        int var13;
        int var14;
        int var15;
        int var17;
        int var16;
        short var19;
        int var18;
        int var21;
        int var20;
        int var22;
        if (drawCenter)
        {            
            for (var12 = this.tileSize_int_compassCrossMin; var12 <= this.tileSize_int_compassCrossMax; ++var12)
            {
                var13 = (int)(this.tileSize_double_compassCenterMax + var10 * (double)var12 * needleLength);
                var14 = (int)(this.tileSize_double_compassCenterMin - var8 * (double)var12 * needleLength * 0.5D);
                var15 = var14 * this.tileSizeBase + var13;
                var16 = 100;
                var17 = 100;
                var18 = 100;
                var19 = 255;

                imageData[var15 * 4 + 0] = (byte)var16;
                imageData[var15 * 4 + 1] = (byte)var17;
                imageData[var15 * 4 + 2] = (byte)var18;
                imageData[var15 * 4 + 3] = (byte)var19;
            }
        }

        for (var12 = this.tileSize_int_compassNeedleMin; var12 <= this.tileSize_int_compassNeedleMax; ++var12)
        {
            var13 = (int)(this.tileSize_double_compassCenterMax + var8 * (double)var12 * needleLength);
            var14 = (int)(this.tileSize_double_compassCenterMin + var10 * (double)var12 * needleLength * 0.5D);
            var15 = var14 * this.tileSizeBase + var13;
            var16 = var12 < 0 ? 100 : rgbColors[0];
            var17 = var12 < 0 ? 100 : rgbColors[1];
            var18 = var12 < 0 ? 100 : rgbColors[2];
            var19 = 255;

            imageData[var15 * 4 + 0] = (byte)var16;
            imageData[var15 * 4 + 1] = (byte)var17;
            imageData[var15 * 4 + 2] = (byte)var18;
            imageData[var15 * 4 + 3] = (byte)var19;
        }
    }
    
    /**
     * Used by the server packet to override the clientside config
     * @param dataIn
     */
    public static void inputOverrideConfig(DataInputStream dataIn)
    {
        mc = FMLClientHandler.instance().getClient();
        settingList = new ArrayList<CompassSetting>();
        parse(new BufferedReader(new InputStreamReader(dataIn)));
        mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded");
    }
    
    private static void parse(BufferedReader var1)
    {
        int curLine = 0;
        try
        {
            String buffer;
            while ((buffer = var1.readLine()) != null)
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
                    else if (settingList.isEmpty())
                    {
                        System.out.println("ERROR: Finder Compass skipping config line because no Setting has been defined yet! Did you update your config file???");
                    }
                    else if (buffer.contentEquals("NoDefaultNeedle"))
                    {
                        currentSetting.setHasDefaultNeedle(false);
                        System.out.println("Disabling Default Needle as per config file");
                    }
                    else if (buffer.contentEquals("NoEnderEyeNeedle"))
                    {
                        currentSetting.setHasStrongholdNeedle(false);
                        System.out.println("Disabling Ender Eye Needle as per config file");
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
                        AS_FinderCompassIntPair key = new AS_FinderCompassIntPair(blockID, configInts[8]);
                        currentSetting.getCustomNeedles().put(key, configInts);
                    }
                }
            }

            var1.close();
        }
        catch (Exception var6)
        {
            System.out.println("EXCEPTION BufferedReader: " + var6);
            var6.printStackTrace();
            mc.ingameGUI.getChatGUI().printChatMessage("There was a problem reading the findercompass.cfg, Parser bailed out in line " + curLine);
            FMLClientHandler.instance().haltGame("There was a problem reading the findercompass.cfg, Parser bailed out in line " + curLine, new Throwable("Fix your config file"));
        }
    }
    
    private void initializeSettingsFile()
    {        
        settingsFile = FinderCompassMod.getConfigFile();
        System.out.println("initializeSettingsFile() running");
        
        if (settingsFile.exists())
        {
            System.out.println(settingsFile.getAbsolutePath()+" found and opened");
            try
            {
                parse(new BufferedReader(new FileReader(settingsFile)));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            mc.ingameGUI.getChatGUI().printChatMessage(settingsFile.getAbsolutePath()+" not found, Finder Compass NOT ACTIVE");
            FMLClientHandler.instance().haltGame(settingsFile.getAbsolutePath()+" not found, Finder Compass NOT ACTIVE", new Throwable("Read the installation instructions"));
        }

        mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass config loaded; " + settingList.size() + " custom Setting-Sets loaded");
        System.out.println("Finder Compass config file reading finished");
        switchSetting();
    }
    
    /**
     * Is a worker setter/getter for each blockID/damage combo. If a worker is present and busy, it does nothing, if a worker is not present, it makes one,
     * and if a worker found something, it retrieves and puts the found target into the "display" Coordinates Map
     */
    private ChunkCoordinates findNearestBlockChunkOfIDInRange(int blockID, int meta, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
    {
    	int [] configInts = {blockID, meta, playerX, playerY, playerZ, xzRange, yRange, minY, maxY};
    	AS_FinderCompassIntPair key = new AS_FinderCompassIntPair(blockID, meta);
    	
    	AS_CompassWorker worker = (AS_CompassWorker) currentSetting.getCompassWorkers().get(key);
    	if (worker == null || !worker.isWorking())
    	{
    		worker = new AS_CompassWorker(mc, this);
    		worker.setPriority(Thread.MIN_PRIORITY);
    		currentSetting.getCompassWorkers().put(key, worker);
    		
    		worker.setupValues(configInts);
    		worker.start();
    	}
    	
    	ChunkCoordinates result = (ChunkCoordinates) currentSetting.getNewFoundTargets().get(key);
    	if (result == null)
    	{
    	    //System.out.println("Did not find saved coords for "+key.getBlockID()+", "+key.getDamage());
    		result = (ChunkCoordinates) currentSetting.getCustomNeedleTargets().get(key);
    	}
    	else
    	{
    	    //System.out.println("Retrieved found coords for "+key.getBlockID()+", "+key.getDamage());
    	    currentSetting.getNewFoundTargets().remove(key);
    	}
    	
    	return result;
    }
    
    public static void onFoundChunkCoordinates(ChunkCoordinates input, int[] intArray)
    {
        //System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+intArray[0]+", damage "+intArray[1]);
        AS_FinderCompassIntPair key = new AS_FinderCompassIntPair(intArray[0], intArray[1]);
        currentSetting.getNewFoundTargets().put(key, input);
    }
    
    public static void switchSetting()
    {
		if (currentSetting != null)
		{
		    currentSetting.onDisableThisConfig();
        
			int nextIndex = settingList.indexOf(currentSetting) + 1;
			if (nextIndex >= settingList.size())
			{
				nextIndex = 0;
			}
			
			currentSetting = settingList.get(nextIndex);
			
			if (mc.theWorld != null)
			{
	            mc.theWorld.playSound(mc.thePlayer.posX+0.5D, mc.thePlayer.posY+0.5D, mc.thePlayer.posZ+0.5D, "random.click", 0.3F, 0.6F, false);
	            mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass Mode: "+currentSetting.getName());
			}
		}
		else
		{
			mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass config error - no Settings defined! YOU FOOL!");
		}
    }

}
