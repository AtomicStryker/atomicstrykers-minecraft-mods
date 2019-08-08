package atomicstryker.petbat.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemPocketedPetBat extends Item {

    protected ItemPocketedPetBat() {
        super((new Item.Properties()).maxStackSize(1).maxDamage(28).group(ItemGroup.COMBAT));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote) {
            PetBatMod.proxy.displayGui(itemStack);
        }

        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    @Override
    public boolean getIsRepairable(ItemStack batStack, ItemStack repairStack) {
        return false;
    }

    @Override
    public boolean shouldSyncTag() {
        return true;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getTag() != null && PetBatMod.instance().getLevelFromExperience(((CompoundNBT) stack.getTag().get("petbatmod")).getInt("BatXP")) > 5;
    }

    public static ItemStack fromBatEntity(EntityPetBat batEnt) {
        if (batEnt.world.isRemote) {
            return ItemStack.EMPTY;
        }

        ItemStack batstack = new ItemStack(PetBatMod.instance().itemPocketedBat);
        writeCompoundStringToItemStack(batstack, "display", "Name", batEnt.getDisplayName().getUnformattedComponentText());
        writeCompoundStringToItemStack(batstack, "petbatmod", "Owner", batEnt.getOwnerName().toString());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatXP", batEnt.getBatExperience());
        writeCompoundFloatToItemStack(batstack, "petbatmod", "health", batEnt.getHealth());
        ((CompoundNBT) batstack.getTag().get("petbatmod")).putFloat("health", batEnt.getHealth());
        batstack.setDamage((int) invertHealthValue(batEnt.getHealth(), batEnt.getMaxHealth()));
        return batstack;
    }

    public static EntityPetBat toBatEntity(World world, ItemStack batStack, PlayerEntity player) {
        EntityPetBat batEnt = new EntityPetBat(world);
        String owner = batStack.getTag() != null ? ((CompoundNBT) batStack.getTag().get("petbatmod")).getString("Owner") : player.getName().getUnformattedComponentText();
        String name = batStack.getTag() != null ? ((CompoundNBT) batStack.getTag().get("display")).getString("Name") : "Battus Genericus";
        int xp = batStack.getTag() != null ? ((CompoundNBT) batStack.getTag().get("petbatmod")).getInt("BatXP") : 0;
        if (owner.equals(""))
            owner = player.getName().getUnformattedComponentText();
        if (name.equals(""))
            name = "Battus Genericus";
        batEnt.setNames(owner, name);
        batEnt.setOwnerEntity(player);
        batEnt.setBatExperience(xp);
        batEnt.setHealth(batStack.getTag() != null ? ((CompoundNBT) batStack.getTag().get("petbatmod")).getFloat("health") : batEnt.getMaxHealth());
        return batEnt;
    }

    public static void writeBatNameToItemStack(ItemStack stack, String name) {
        writeCompoundStringToItemStack(stack, "display", "Name", TextFormatting.DARK_PURPLE + name);
    }

    public static String getBatNameFromItemStack(ItemStack stack) {
        return (stack.getTag() != null ? ((CompoundNBT) stack.getTag().get("display")).getString("Name") : "Battus Genericus");
    }

    /**
     * @param input value to invert
     * @param max   maximum health value
     * @return inverted value
     */
    public static double invertHealthValue(double input, double max) {
        return Math.abs(input - max);
    }

    public static void writeCompoundIntegerToItemStack(ItemStack stack, String tag, String key, int data) {
        checkCompoundNBT(stack, tag);
        ((CompoundNBT) stack.getTag().get(tag)).putInt(key, data);
    }

    public static void writeCompoundFloatToItemStack(ItemStack stack, String tag, String key, float data) {
        checkCompoundNBT(stack, tag);
        ((CompoundNBT) stack.getTag().get(tag)).putFloat(key, data);
    }

    public static void writeCompoundStringToItemStack(ItemStack stack, String tag, String key, String data) {
        checkCompoundNBT(stack, tag);
        ((CompoundNBT) stack.getTag().get(tag)).putString(key, data);
    }

    private static void checkCompoundNBT(ItemStack stack, String tag) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }

        if (stack.getTag().get(tag) == null) {
            stack.getTag().put(tag, new CompoundNBT());
        }
    }

    @Override
    public ITextComponent getDisplayName(ItemStack itemStack) {
        return new TranslationTextComponent(TextFormatting.DARK_PURPLE + super.getDisplayName(itemStack).getUnformattedComponentText());
    }
}