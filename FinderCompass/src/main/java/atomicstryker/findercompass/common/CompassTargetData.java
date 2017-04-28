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
    	mOreDictName = pOreName != null ? pOreName : a.getUnlocalizedName();
    	if (a != null){
    		mBlocks.add(new BlockData(a, b));
    	}
    }
    
    public CompassTargetData(Set<BlockData> blockData, String pOreDictName) {
    	this(pOreDictName);
    	if (blockData != null){
    		mBlocks.addAll(blockData);
    	}
	}

	public void add(Block pBlock, int pDamage){
    	if (pBlock != null){
    		mBlocks.add(new BlockData(pBlock, pDamage));
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
        
        private BlockData(Block pBlock, int pDamage){
        	blockID = pBlock;
        	damage = pDamage;
        }
        
        public Block getBlockID()
        {
            return blockID;
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
            return blockID.getUnlocalizedName().hashCode();
        }
    
    }
}
