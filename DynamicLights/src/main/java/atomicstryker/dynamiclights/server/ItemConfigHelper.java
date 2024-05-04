package atomicstryker.dynamiclights.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemConfigHelper {
    private Map<ItemStack, Integer> itemStackList;

    public ItemConfigHelper(List<? extends String> items, Logger logger, RegistryAccess registryAccess) {
        itemStackList = new HashMap<>();
        for (String json : items) {
            try {
                CompoundTag nbt = TagParser.parseTag(json);
                ItemStack itemStack = ItemStack.parseOptional(registryAccess, nbt);

                if (!itemStack.isEmpty()) {
                    int lightLevel = 15;
                    if (nbt.contains("lightLevel")) {
                        lightLevel = nbt.getShort("lightLevel");
                        nbt.remove("lightLevel");
                    }
                    itemStackList.put(itemStack, lightLevel);
                    logger.info("item config parser identified itemstack {}", itemStack);
                } else {
                    logger.error("item config parser could not create itemStack from {}", json);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        logger.info("item config parser finished, item count: {}", itemStackList.size());
    }

    public static String fromItemStack(ItemStack itemStack, int lightLevel, RegistryAccess registryAccess) {
        CompoundTag resultTag = (CompoundTag) itemStack.save(registryAccess);
        if (lightLevel > 0) {
            resultTag.putShort("lightLevel", (short) lightLevel);
        }
        resultTag.remove("count");
        resultTag.putBoolean("anyNbtMatch", true);
        return resultTag.toString();
    }

    public int getLightLevel(ItemStack stack, RegistryAccess registryAccess) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        for (Map.Entry<ItemStack, Integer> entry : itemStackList.entrySet()) {
            ItemStack is = entry.getKey();
            if (is.getItem() == stack.getItem() && tagsMatchWithWildcard(is, stack, registryAccess)) {
                return entry.getValue();
            }
        }

        return 0;
    }

    private boolean tagsMatchWithWildcard(ItemStack configuredStack, ItemStack ingameStack, RegistryAccess registryAccess) {
        CompoundTag resultTag = (CompoundTag) configuredStack.save(registryAccess);
        if (resultTag.contains("anyNbtMatch")) {
            return true;
        }
        return ItemStack.isSameItem(configuredStack, ingameStack);
    }
}