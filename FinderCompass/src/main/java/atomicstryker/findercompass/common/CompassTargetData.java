package atomicstryker.findercompass.common;

import java.util.HashSet;
import java.util.Set;

import atomicstryker.findercompass.common.CompassTargetData.BlockData;
import net.minecraft.block.Block;

public class CompassTargetData
{
	private final Set<BlockData> mBlocks = new HashSet<BlockData>();
    private final String mOreDictName;
    
    public CompassTargetData(String pOreName)
    {
    	this(null, -1, pOreName);
    }
    
    public CompassTargetData(Block a, int b, String pOreName)
    {
    	this(a, b, pOreName, true);
    }
    
    public CompassTargetData(Block a, int b, String pOreName, boolean pWithDamage)
    {
    	mOreDictName = pOreName != null ? pOreName : a.getUnlocalizedName();
    	if (a != null){
    		mBlocks.add(new BlockData(a, b, pWithDamage));
    	}
    }
    
    public CompassTargetData(Set<BlockData> blockData, String pOreDictName) {
    	this(pOreDictName);
    	if (blockData != null){
    		mBlocks.addAll(blockData);
    	}
	}

	public void add(Block pBlock, int pDamage, boolean pUseDamage){
    	if (pBlock != null){
    		mBlocks.add(new BlockData(pBlock, pDamage, pUseDamage));
    	}
    }
   
	public void addAll(CompassTargetData pData) {
		if (pData != null && pData.getOreDictName().equals(mOreDictName)) {
			mBlocks.addAll(pData.mBlocks);
		}
	}

    
    public Set<BlockData> getBlocks()
    {
        return mBlocks;
    }

	public boolean isEmpty() {
		return mBlocks.isEmpty();
	}
    
    public String getOreDictName()
    {
    	return mOreDictName;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof CompassTargetData)
        {
            CompassTargetData comp = (CompassTargetData)o;
            return comp.getOreDictName().equals(mOreDictName) ;
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return mOreDictName.hashCode();
    }
    
    public class BlockData{
        private final Block blockID;
        private final int damage;
        private final boolean mUseDamage;
        
        private BlockData(Block pBlock, int pDamage, boolean pWithDamage){
        	blockID = pBlock;
        	damage = pDamage;
        	mUseDamage = pWithDamage;
        }
        
        public Block getBlockID()
        {
            return blockID;
        }
        
        public boolean useDamage(){
        	return mUseDamage;
        }
        
        public int getDamage()
        {
            return damage;
        }

        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof BlockData)
            {
            	BlockData comp = (BlockData)o;
                return comp.getBlockID() == blockID && comp.getDamage() == damage;
            }
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return (blockID.getClass().getSimpleName() + ":" + damage).hashCode();
        }
    
    }
}
