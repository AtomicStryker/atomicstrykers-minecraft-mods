package atomicstryker.multimine.common;

import java.util.PriorityQueue;

public class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock>
{
    private static final long serialVersionUID = -1L;

    @Override
    public boolean offer(PartiallyMinedBlock block)
    {
        if (contains(block))
        {
            this.remove(block);
            return true;
        }
        return super.offer(block);
    }
}
