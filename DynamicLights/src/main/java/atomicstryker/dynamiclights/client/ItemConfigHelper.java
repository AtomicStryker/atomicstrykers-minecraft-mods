package atomicstryker.dynamiclights.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemConfigHelper
{
    private List<ItemStack> itemStackList;

    public ItemConfigHelper(List<? extends String> items)
    {
        itemStackList = new ArrayList<>();
        for (String json : items)
        {
            try
            {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(json);
                ResourceLocation resourceLocation = new ResourceLocation(nbt.getString("nameId"));
                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                if (item != null)
                {
                    ItemStack itemStack = new ItemStack(item);
                    nbt.removeTag("nameId");
                    itemStack.setTag(nbt);
                    itemStackList.add(itemStack);
                }
            }
            catch (CommandSyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String fromItemStack(ItemStack itemStack)
    {
        itemStack.getTag().setString("nameId", itemStack.getItem().getRegistryName().toString());
        return itemStack.getTag().toString();
    }

    public boolean contains(ItemStack testee)
    {
        for (ItemStack is : itemStackList)
        {
            if (is.getItem() == testee.getItem() && ItemStack.areItemStackTagsEqual(is, testee))
            {
                return true;
            }
        }
        return false;
    }
}
