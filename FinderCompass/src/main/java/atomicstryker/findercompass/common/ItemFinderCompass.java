package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.FinderCompassClientTicker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemFinderCompass extends Item
{

    public ItemFinderCompass()
    {
        super();
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote)
        {
            FinderCompassClientTicker.instance.switchSetting();
        }
        return new ActionResult<>(EnumActionResult.PASS, itemStack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return TextFormatting.GOLD + super.getItemStackDisplayName(itemStack);
    }

}
