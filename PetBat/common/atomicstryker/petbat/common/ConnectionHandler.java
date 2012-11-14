package atomicstryker.petbat.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetLoginHandler;
import net.minecraft.src.Packet1Login;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler
{

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        EntityPlayer p = (EntityPlayer) player;
        if (PetBatMod.instance().getPetBatManualEnabled() && !p.getEntityData().getBoolean("hasPetBatManual"))
        {
            ItemStack book = new ItemStack(Item.writtenBook);
            book.stackTagCompound = new NBTTagCompound();
            NBTTagList pages = new NBTTagList();
            pages.appendTag(new NBTTagString("", "- Bats are attracted to Pumpking Pies being slapped against Blocks\n" +
                                                 "- To tame a bat right click it with a Pumpkin Pie\n" +
            		                             "- Pet Bats can be picked up by left clicking them\n"));
            pages.appendTag(new NBTTagString("", "- Pet Bats can be deployed by removing them from your inventory\n" +
                                                 "- instead of dying, Pet Bats return to your inventory and remain Items\n" +
                                                 "- until you feed them more Pumpkin Pie to heal them\n"));
            pages.appendTag(new NBTTagString("", "- Should you run into bugs or crashes, post your ForgeModLoader logfile on the Forum, along with a description on how it happened."));
            book.stackTagCompound.setTag("pages", pages);
            book.stackTagCompound.setString("title", "Pet Bat Manual");
            book.stackTagCompound.setString("author", "AtomicStryker");
            
            p.getEntityData().setBoolean("hasPetBatManual", true);
            p.inventory.addItemStackToInventory(book);
        }
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
    {
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
    {
    }

    @Override
    public void connectionClosed(INetworkManager manager)
    {
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
    {
    }

}
