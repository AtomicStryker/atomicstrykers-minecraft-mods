package atomicstryker.minions.common.entity;

/**
 * Minion state Enumeration
 * 
 * 
 * @author AtomicStryker
 */

public enum EnumMinionState
{
    IDLE("IDLE", 0),
    FOLLOWING_PLAYER("FOLLOWING_PLAYER", 1),
    WALKING_TO_COORDS("WALKING_TO_COORDS", 2),
    AWAITING_JOB("AWAITING_JOB", 3),
    RETURNING_GOODS("RETURNING_GOODS", 4),
    THINKING("THINKING", 5),
    MINING("MINING", 6),
    STALKING_TO_GRAB("STALKING_TO_GRAB", 7);

    private String name;
    private int number;
    
    private EnumMinionState(String var1, int var2)
    {
    	this.name = var1;
    	this.number = var2;
    }
    
    public String getName()
    {
    	return this.name;
    }
    
    public int getNumber()
    {
    	return this.number;
    }
    
    public static EnumMinionState getStateByString(String input)
    {
    	for (EnumMinionState check : EnumMinionState.values())
    	{
    		if(check.getName().equals(input))
    		{
    			return check;
    		}
    	}
    	
    	return null;
    }
}
