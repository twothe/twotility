/*
 */
package two.twotility.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockLavaTank;
import two.twotility.util.ItemUtil;

/**
 * @author Two
 */
public class ItemLavaTank extends ItemBlock3d {

  protected static final String KEY_TOOLTIP_EMPTY = TwoTility.getTooltipName(BlockLavaTank.NAME, "empty"); // Empty
  protected static final String KEY_TOOLTIP_FILLED = TwoTility.getTooltipName(BlockLavaTank.NAME, "filled"); // Contains %d buckets of lava

  public ItemLavaTank(final Block block) {
    super(block);
  }

  @Override
  public void initialize() {
    setBaseValues(BlockLavaTank.NAME);
    setHasSubtypes(true);
  }

  @Override
  public void getSubItems(final Item item, final CreativeTabs creativeTab, final List itemList) {
    itemList.add(new ItemStack(item, 1, BlockLavaTank.STATE_EMPTY));
    itemList.add(new ItemStack(item, 1, BlockLavaTank.STATE_4_4));
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    final int numBuckets = getMetadata(itemStack.getItemDamage());
    if (numBuckets == 0) {
      final String tooltip = ItemUtil.getCachedTooltip(KEY_TOOLTIP_EMPTY);
      if (tooltip != null) {
        strings.add(tooltip);
      }
    } else {
      final String tooltip = ItemUtil.getCachedTooltip(KEY_TOOLTIP_FILLED);
      if (tooltip != null) {
        strings.add(String.format(tooltip, numBuckets));
      }
    }
  }
}
