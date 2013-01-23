package atomicstryker.findercompass.client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texturefx.TextureCompassFX;
import net.minecraft.client.texturepacks.ITexturePack;
import net.minecraft.item.Item;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.findercompass.common.AS_FinderCompassIntPair;
import atomicstryker.findercompass.common.ConfigExceptionScreen;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLTextureFX;
import cpw.mods.fml.common.network.PacketDispatcher;

public class AS_FinderCompass extends FMLTextureFX
{
    private static Minecraft mc;
    private int[] tsize;
    private double[] textureId = new double[30];
    private double[] tileSize = new double[30];
    private int[] spawnNeedlecolor = new int[] {255, 20, 20};
    private int[] strongholdNeedlecolor = new int[] {175, 220, 0};    
    static File settingsFile;
    final double HEADING_DOWN = 0.0D;
    final double HEADING_UP = 135.0D;
    private int x;
    private int y;
    private int z;
    private final ChunkCoordinates NullChunk = new ChunkCoordinates(0, 0, 0);
    private long lastTime;
    private int seccounter = 0;
    private boolean updateScan = false;
    public int modState = -1;
    public BufferedImage texture;
    public int tileSize_int_compassCrossMin = -4;
    public int tileSize_int_compassCrossMax = 4;
    public double tileSize_double_compassCenterMin;
    public double tileSize_double_compassCenterMax;
    public int tileSize_int_compassNeedleMin;
    public int tileSize_int_compassNeedleMax;
    public static boolean isHackedIn = false;
    
    public static boolean serverHasFinderCompass = false;
    public static ChunkCoordinates strongholdCoords = new ChunkCoordinates(0, 0, 0);
    public static boolean hasStronghold = false;
    
    private static ArrayList<CompassSetting> settingList;
    private static CompassSetting currentSetting;

    public AS_FinderCompass(Minecraft var1)
    {
        super(Item.compass.getIconFromDamage(0));
        this.tileSize_double_compassCenterMin = (double)(this.tileSizeBase / 2) - 0.5D;
        this.tileSize_double_compassCenterMax = (double)(this.tileSizeBase / 2) + 0.5D;
        this.tileSize_int_compassNeedleMin = -8;
        this.tileSize_int_compassNeedleMax = 16;
		this.mc = var1;
        this.checkModState();
        this.tsize = new int[this.tileSizeSquare];
        this.tileImage = 1;
        BufferedImage var2 = this.texture;
        int var3 = this.iconIndex % 16 * this.tileSizeBase;
        int var4 = this.iconIndex / 16 * this.tileSizeBase;
        var2.getRGB(var3, var4, this.tileSizeBase, this.tileSizeBase, this.tsize, 0, this.tileSizeBase);
        
        lastTime = System.currentTimeMillis();
        settingList = new ArrayList<CompassSetting>();
        initializeSettingsFile();
    }

    private void checkModState()
    {
        try
        {
            this.texture = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
        }
        catch (IOException var9)
        {
            var9.printStackTrace();
        }

        Class foundClass = null;

        try
        {
            foundClass = Class.forName("com.pclewis.mcpatcher.mod.TileSize");
        }
        catch (ClassNotFoundException var8)
        {
            System.out.println("Finder Compass: Did not detect mcpatcher HD Textures");
        }

        if (foundClass != null)
        {
            System.out.println("Finder Compass: mcpatcher HD Textures detected, setting up...");
            this.modState = 1;

            try
            {
                this.tileSizeBase = ((Integer)foundClass.getField("int_size").get((Object)null)).intValue();
                this.tileSizeSquare = ((Integer)foundClass.getField("int_numPixels").get((Object)null)).intValue();
                this.tileSize_int_compassCrossMin = ((Integer)foundClass.getField("int_compassCrossMin").get((Object)null)).intValue();
                this.tileSize_int_compassCrossMax = ((Integer)foundClass.getField("int_compassCrossMax").get((Object)null)).intValue();
                this.tileSize_int_compassNeedleMin = ((Integer)foundClass.getField("int_compassNeedleMin").get((Object)null)).intValue();
                this.tileSize_int_compassNeedleMax = ((Integer)foundClass.getField("int_compassNeedleMax").get((Object)null)).intValue();
                this.tileSize_double_compassCenterMin = ((Double)foundClass.getField("double_compassCenterMin").get((Object)null)).doubleValue();
                this.tileSize_double_compassCenterMax = ((Double)foundClass.getField("double_compassCenterMax").get((Object)null)).doubleValue();
                
                foundClass = Class.forName("com.pclewis.mcpatcher.mod.TextureUtils");
                Method[] var2 = foundClass.getMethods();

                for (int var3 = 0; var3 < var2.length; ++var3)
                {
                    Method var4 = var2[var3];
                    if (var4.getName().equals("getResourceAsBufferedImage") && var4.getParameterTypes().length == 1)
                    {
                        Object[] var5 = new Object[] {new String("/gui/items.png")};
                        imageData = new byte[tileSizeSquare << 2];
                        this.texture = (BufferedImage)var4.invoke((Object)null, var5);
                    }
                }
            }
            catch (Exception var16)
            {
                var16.printStackTrace();
            }
        }
        else
        {
            try
            {
                foundClass = Class.forName("TextureHDCompassFX");
            }
            catch (ClassNotFoundException var7)
            {
                System.out.println("Finder Compass: Did not detect Optifine HD Textures");
            }

            if (foundClass != null)
            {
                System.out.println("Finder Compass: Optifine HD Textures detected, setting up...");
                this.modState = 2;
                                
                Object targetHDCompassobj = null;

                try
                {
        			for(Field f : mc.renderEngine.getClass().getDeclaredFields())
        			{
        				f.setAccessible(true);
        				Object data = f.get(mc.renderEngine);
        				if (data instanceof List)
        				{
        					System.out.println("Found List in RenderEngine...");
        					Iterator itr = ((List) data).iterator();
        					while (itr.hasNext())
        					{
        						Object temp = itr.next();
        						if (temp.getClass().equals(foundClass))
        						{
        							System.out.println("Found TextureHDCompassFX in RenderEngine List: "+temp.getClass());
        							targetHDCompassobj = temp;
        							break;
        						}
        					}
        				}
        			}
					
                    if (targetHDCompassobj == null)
                    {
                        System.out.println("Finder Compass: Optifine detected but HDCompass Object cannot be located, skipping hack");
                        this.modState = 0;
                        return;
                    }
                	
        			Field tilewidth = foundClass.getDeclaredField("tileWidth");
        			tilewidth.setAccessible(true);                    
                    this.tileSizeBase = ((Integer)tilewidth.get(targetHDCompassobj)).intValue();
                    this.tileSizeSquare = this.tileSizeBase * this.tileSizeBase;
                    this.tileSize_double_compassCenterMin = (double)(this.tileSizeBase / 2) - 0.5D;
                    this.tileSize_double_compassCenterMax = (double)(this.tileSizeBase / 2) + 0.5D;
                    imageData = new byte[tileSizeSquare << 2];
                    
                    System.out.println("tilesize_intsize = "+tileSizeBase+"; tilesize_numpixels = "+tileSizeSquare+";");
                    
                    ITexturePack ITexturePack = null;
                    for (Field f : foundClass.getDeclaredFields())
                    {
                        f.setAccessible(true);
                        if (f.getType().equals(ITexturePack.class))
                        {
                            ITexturePack = (ITexturePack) f.get(targetHDCompassobj);
                            System.out.println("ITexturePack of HDCompass found...");
                            break;
                        }
                    }
                    
                    if (ITexturePack == null)
                    {
                        System.out.println("ITexturePack of HDCompass NOT found! Critical failure!");
                        this.modState = 0;
                        return;
                    }
                    
                    for (Method m : ITexturePack.class.getDeclaredMethods())
                    {
                        m.setAccessible(true);
                        if (m.getReturnType().equals(InputStream.class))
                        {
                            InputStream stream = (InputStream)m.invoke(ITexturePack, new Object[] {"/gui/items.png"});
                            if (stream != null)
                            {
                                this.texture = ImageIO.read(stream);
                                System.out.println("Successfully read texture from ITexturePack, texture = "+this.texture);
                                break;
                            }
                            else
                            {
                                System.out.println("ITexturePack getTexture invoke failed, stream is null, Critical failure!");
                                this.modState = 0;
                                return;
                            }
                        }
                    }
                }
                catch (Exception var10)
                {
                    var10.printStackTrace();
                }
            }
            else
            {
                System.out.println("Finder Compass: Did not detect any HD Textures, going with FML FX");
                mc.displayGuiScreen(new ConfigExceptionScreen("Unmodified/Forge textures detected...", "Please use a HD texture fix or Optifine"));
                return;
            }
        }
        isHackedIn = true;
    }

    private void askServerForStrongholdCoords()
    {
        if (serverHasFinderCompass)
        {
            PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("FindrCmps", 1, null));
        }
    }

    @Override
    public void onTick()
    {
        int var1;
        int var2;
        
        for (int pixelIndex = 0; pixelIndex < this.tileSizeSquare; ++pixelIndex)
        {
            int var4 = this.tsize[pixelIndex] >> 24 & 255;
            var1 = this.tsize[pixelIndex] >> 16 & 255;
            int var5 = this.tsize[pixelIndex] >> 8 & 255;
            var2 = this.tsize[pixelIndex] >> 0 & 255;
            if (this.anaglyphEnabled)
            {
                int var6 = (var1 * 30 + var5 * 59 + var2 * 11) / 100;
                int var7 = (var1 * 30 + var5 * 70) / 100;
                int var8 = (var1 * 30 + var2 * 70) / 100;
                var1 = var6;
                var5 = var7;
                var2 = var8;
            }

            this.imageData[pixelIndex * 4 + 0] = (byte)var1;
            this.imageData[pixelIndex * 4 + 1] = (byte)var5;
            this.imageData[pixelIndex * 4 + 2] = (byte)var2;
            this.imageData[pixelIndex * 4 + 3] = (byte)var4;
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
                mc.displayGuiScreen(new ConfigExceptionScreen("Finder Compass config missing!!", "Read the instructions next time."));
                return;
            }

            if (currentSetting.getHasDefaultNeedle())
            {
                this.drawNeedle(0, this.computeNeedleHeading(this.mc.theWorld.getSpawnPoint()), this.spawnNeedlecolor, true);
            }

            if (currentSetting.getHasStrongholdNeedle() && hasStronghold)
            {
                this.drawNeedle(1, this.computeNeedleHeading(this.strongholdCoords), this.strongholdNeedlecolor, false);
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
                this.drawNeedle(var1, this.computeNeedleHeading(coords), configInts, false);
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

    public void drawNeedle(int var1, double var2, int[] ints, boolean drawCenter)
    {
    	double needleLength = modState == 2 ? 0.3D * (this.tileSizeBase / 16) : 0.3D;
    	
        double var6;
        for (var6 = var2 - this.textureId[var1]; var6 < -3.141592653589793D; var6 += 6.283185307179586D)
        {
            ;
        }

        while (var6 >= 3.141592653589793D)
        {
            var6 -= 6.283185307179586D;
        }

        if (var6 < -1.0D)
        {
            var6 = -1.0D;
        }

        if (var6 > 1.0D)
        {
            var6 = 1.0D;
        }

        this.textureId[var1] += var6 * 0.1D;
        this.tileSize[var1] *= 0.8D;
        this.tileSize[var1] += this.textureId[var1];
        double var8 = Math.sin(this.textureId[var1]);
        double var10 = Math.cos(this.textureId[var1]);
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
                if (this.anaglyphEnabled)
                {
                    var20 = (var16 * 30 + var17 * 59 + var18 * 11) / 100;
                    var21 = (var16 * 30 + var17 * 70) / 100;
                    var22 = (var16 * 30 + var18 * 70) / 100;
                    var16 = var20;
                    var17 = var21;
                    var18 = var22;
                }

                this.imageData[var15 * 4 + 0] = (byte)var16;
                this.imageData[var15 * 4 + 1] = (byte)var17;
                this.imageData[var15 * 4 + 2] = (byte)var18;
                this.imageData[var15 * 4 + 3] = (byte)var19;
            }
        }

        for (var12 = this.tileSize_int_compassNeedleMin; var12 <= this.tileSize_int_compassNeedleMax; ++var12)
        {
            var13 = (int)(this.tileSize_double_compassCenterMax + var8 * (double)var12 * needleLength);
            var14 = (int)(this.tileSize_double_compassCenterMin + var10 * (double)var12 * needleLength * 0.5D);
            var15 = var14 * this.tileSizeBase + var13;
            var16 = var12 < 0 ? 100 : ints[0];
            var17 = var12 < 0 ? 100 : ints[1];
            var18 = var12 < 0 ? 100 : ints[2];
            var19 = 255;
            if (this.anaglyphEnabled)
            {
                var20 = (var16 * 30 + var17 * 59 + var18 * 11) / 100;
                var21 = (var16 * 30 + var17 * 70) / 100;
                var22 = (var16 * 30 + var18 * 70) / 100;
                var16 = var20;
                var17 = var21;
                var18 = var22;
            }

            this.imageData[var15 * 4 + 0] = (byte)var16;
            this.imageData[var15 * 4 + 1] = (byte)var17;
            this.imageData[var15 * 4 + 2] = (byte)var18;
            this.imageData[var15 * 4 + 3] = (byte)var19;
        }
    }

    private void initializeSettingsFile()
    {
        settingsFile = FinderCompassMod.getConfigFile();
        System.out.println("initializeSettingsFile() running");
        int curLine = 0;
        
        try
        {
            if (settingsFile.exists())
            {
                System.out.println(settingsFile.getAbsolutePath()+" found and opened");
                BufferedReader var1 = new BufferedReader(new FileReader(settingsFile));

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
                            currentSetting = settingList.get(settingList.size()-1);
                            System.out.println("Created new Compass Setting of the name: "+currentSetting.getName());
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
                            System.out.println("Finder Compass: loaded custom needle of id " + blockID + ", color [" + configInts[0] + "," + configInts[1] + "," + configInts[2]+"]");
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
                            System.out.println("Full readout: "+ blockID + ":" + configInts[0] + ":" + configInts[1] + ":" + configInts[2] + ":" + configInts[3] + ":" + configInts[4] + ":" + configInts[5] + ":" + configInts[6] + ":" + configInts[7] + ":" + configInts[8]);
                            AS_FinderCompassIntPair key = new AS_FinderCompassIntPair(blockID, configInts[8]);
                            currentSetting.getCustomNeedles().put(key, configInts);
                        }
                    }
                }

                var1.close();
            }
            else
            {
                this.mc.ingameGUI.getChatGUI().printChatMessage(settingsFile.getAbsolutePath()+" not found, Finder Compass NOT ACTIVE");
            }
        }
        catch (Exception var6)
        {
            System.out.println("EXCEPTION BufferedReader: " + var6);
            var6.printStackTrace();
            this.mc.ingameGUI.getChatGUI().printChatMessage("There was a problem reading your findercompass.cfg, Parser bailed out in line "+curLine);
        }

        mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass config loaded; " + settingList.size() + " custom Setting-Sets loaded");
        System.out.println("Finder Compass config file reading finished");
        switchSetting();
    }
    
    /**
     * Is a worker setter/getter for each blockID/damage combo. If a worker is present and busy, it does nothing, if a worker is not present, it makes one,
     * and if a worker found something, it retrieves and puts the found target into the "display" Coordinates Map
     */
    ChunkCoordinates findNearestBlockChunkOfIDInRange(int blockID, int meta, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
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
			mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass Mode: "+currentSetting.getName());
		}
		else
		{
			mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass config error - no Settings defined! YOU FOOL!");
		}
    }
}
