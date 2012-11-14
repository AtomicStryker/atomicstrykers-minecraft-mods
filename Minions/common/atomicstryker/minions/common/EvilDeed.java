package atomicstryker.minions.common;

/**
 * Storage class for Evil Deeds loaded from config file
 * 
 * 
 * @author AtomicStryker
 */

public class EvilDeed
{
	private final String buttonText;
	private final String soundFile;
	private final int soundLength;

    public EvilDeed(String text, String file, int length)
    {
    	buttonText = text;
    	soundFile = file;
    	soundLength = length;
    }

    public String getButtonText()
    {
    	return buttonText;
    }
    
    public String getSoundFile()
    {
    	return soundFile;
    }
    
    public int getSoundLength()
    {
    	return soundLength;
    }
}
