package ic2.advancedmachines.common;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public interface IProxy
{
    public void load();
    
    public Object getGuiElementForClient(int ID, EntityPlayer player, World world, int x, int y, int z);
}
