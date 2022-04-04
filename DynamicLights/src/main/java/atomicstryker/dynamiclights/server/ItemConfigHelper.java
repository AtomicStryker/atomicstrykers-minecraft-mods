package atomicstryker.dynamiclights.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemConfigHelper {
    private Map<ItemStack, Integer> itemStackList;

    public ItemConfigHelper(List<? extends String> items, Logger logger) {
        itemStackList = new HashMap<>();
        for (String json : items) {
            try {
                CompoundTag nbt = TagParser.parseTag(json);
                ResourceLocation resourceLocation = new ResourceLocation(nbt.getString("nameId"));
                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

                if (item != null) {
                    ItemStack itemStack = new ItemStack(item);
                    nbt.remove("nameId");

                    int lightLevel = 15;
                    if(nbt.contains("lightLevel")) {
                        lightLevel = nbt.getShort("lightLevel");
                        nbt.remove("lightLevel");
                    }

                    if (!nbt.isEmpty()) {
                        // only set tag if non empty, otherwise the comparisons fail later!!
                        itemStack.setTag(nbt);
                    }
                    itemStackList.put(itemStack, lightLevel);
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

    public static String fromItemStack(ItemStack itemStack, int lightLevel) {
        CompoundTag resultTag = itemStack.getOrCreateTag();
        resultTag.putString("nameId", ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
        if(lightLevel > 0) {
            resultTag.putShort("lightLevel", (short) lightLevel);
        }
        return resultTag.toString();
    }

    public int getLightLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        for (Map.Entry<ItemStack, Integer> entry : itemStackList.entrySet()) {
            ItemStack is = entry.getKey();
            if (is.getItem() == stack.getItem() && ItemStack.tagMatches(is, stack)) {
                return entry.getValue();
            }
        }

        return 0;
    }
}