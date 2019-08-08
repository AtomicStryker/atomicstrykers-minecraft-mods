package atomicstryker.petbat.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemBatFlute extends Item {

    protected ItemBatFlute() {
        super((new Item.Properties()).maxStackSize(1).setNoRepair().group(ItemGroup.MISC));
    }

    @Override
    public boolean shouldSyncTag() {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (itemStack.getTag() != null) {
            String batname = itemStack.getTag().getString("batName");
            for (EntityPetBat petBat : world.getEntitiesWithinAABB(EntityPetBat.class, player.getBoundingBox().expand(16 * 8, 256, 16 * 8))) {
                if (petBat.getName().getUnformattedComponentText().equals(batname)) {
                    petBat.recallToOwner();
                    itemStack.setCount(0);
                    break;
                }
            }
            return new ActionResult<>(ActionResultType.PASS, null);
        }
        return new ActionResult<>(ActionResultType.FAIL, null);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack itemStack) {
        String batname = itemStack.getTag() != null ? (": " + itemStack.getTag().getString("batName")) : ": unassigned";
        return new TranslationTextComponent(TextFormatting.GOLD + super.getDisplayName(itemStack).getUnformattedComponentText() + batname);
    }

}
