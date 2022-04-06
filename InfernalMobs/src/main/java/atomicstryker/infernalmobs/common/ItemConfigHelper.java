package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ItemConfigHelper {
    private final List<ItemStack> itemStackList;

    public ItemConfigHelper(List<? extends String> items, Logger logger) {
        itemStackList = new ArrayList<>();
        for (String json : items) {
            try {
                CompoundTag nbt = TagParser.parseTag(json);
                ItemStack itemStack = ItemStack.of(nbt);
                if (!itemStack.isEmpty()) {
                    itemStackList.add(itemStack);
                } else {
                    logger.error("item config parser could not build item: {}", json);
                }
            } catch (CommandSyntaxException e) {
                logger.error("item config parser CommandSyntaxException: {}", json);
                e.printStackTrace();
            }
        }
        logger.info("item config parser finished, item count: {}", itemStackList.size());
    }

    public static String fromItemStack(ItemStack itemStack) {
        itemStack.getOrCreateTag().putString("id", ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
        itemStack.getOrCreateTag().putByte("Count", (byte) itemStack.getCount());
        return itemStack.getOrCreateTag().toString();
    }

    public List<ItemStack> getItemStackList() {
        return itemStackList;
    }
}
