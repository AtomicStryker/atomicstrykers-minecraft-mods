package atomicstryker.petbat.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler
{

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        EntityPlayer p = (EntityPlayer) player;
        if (PetBatMod.instance().getPetBatManualEnabled() && PetBatMod.instance().hasPlayerGotManual())
        {
            ItemStack book = new ItemStack(Item.writtenBook);
            book.stackTagCompound = new NBTTagCompound();
            NBTTagList pages = new NBTTagList();
            pages.appendTag(new NBTTagString("", "- Bats are attracted to Pumpking Pies being slapped against Blocks\n" +
                                                 "- To tame a bat right click it with a Pumpkin Pie\n" +
            		                             "- Pet Bats can be picked up by left clicking them\n"));
            pages.appendTag(new NBTTagString("", "- Pet Bats can be deployed by removing them from your inventory\n" +
                                                 "- instead of dying, Pet Bats return to your inventory and remain Items\n" +
                                                 "- until you drop them on the ground with Pumpkin Pie to fix them\n"));
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
