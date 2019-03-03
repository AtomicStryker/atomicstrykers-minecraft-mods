package atomicstryker.dynamiclights.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ItemConfigHelper {
    private List<ItemStack> itemStackList;

    public ItemConfigHelper(List<? extends String> items, Logger logger) {
        itemStackList = new ArrayList<>();
        for (String json : items) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(json);
                ResourceLocation resourceLocation = new ResourceLocation(nbt.getString("nameId"));
                Item item = IRegistry.ITEM.get(resourceLocation);

                if (item != null) {
                    ItemStack itemStack = new ItemStack(item);
                    nbt.remove("nameId");
                    if (!nbt.isEmpty()) {
                        // only set tag if non empty, otherwise the comparisons fail later!!
                        itemStack.setTag(nbt);
                    }
                    itemStackList.add(itemStack);
                    logger.info("item config parser identified itemstack {}", itemStack);
                } else {
                    logger.error("item config parser could not identify item by resourcelocation {}", resourceLocation);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        logger.info("item config parser finished, item count: {}", itemStackList.size());
    }

    public static String fromItemStack(ItemStack itemStack) {
        itemStack.getOrCreateTag().putString("nameId", IRegistry.ITEM.getKey(itemStack.getItem()).toString());
        return itemStack.getOrCreateTag().toString();
    }

    public boolean contains(ItemStack testee) {
        for (ItemStack is : itemStackList) {
            if (is.getItem() == testee.getItem() && ItemStack.areItemStackTagsEqual(is, testee)) {
                return true;
            }
        }
        return false;
    }
}
