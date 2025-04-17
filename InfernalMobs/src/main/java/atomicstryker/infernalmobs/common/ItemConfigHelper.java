package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemConfigHelper {
    private final List<ItemStack> itemStackList;

    public ItemConfigHelper(List<? extends String> items, Logger logger, RegistryAccess registryAccess) {
        itemStackList = new ArrayList<>();
        for (String json : items) {
            try {
                CompoundTag nbt = TagParser.parseCompoundFully(json);
                Optional<ItemStack> itemStack = ItemStack.parse(registryAccess, nbt);

                if (itemStack.isPresent()) {
                    itemStackList.add(itemStack.get());
                    logger.info("item config parser identified itemstack {}", itemStack.get());
                } else {
                    logger.error("item config parser could not create itemStack from {}", json);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        logger.info("item config parser finished, item count: {}", itemStackList.size());
    }

    public static String fromItemStack(ItemStack itemStack, RegistryAccess registryAccess) {
        CompoundTag resultTag = (CompoundTag) itemStack.save(registryAccess);
        return resultTag.toString();
    }

    public List<ItemStack> getItemStackList() {
        return itemStackList;
    }
}