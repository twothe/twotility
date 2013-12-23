/*
 */
package two.twotility.items;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public class ItemLavaTank extends ItemBlock3d {

  protected static final String NAME = "LavaTank";

  public ItemLavaTank(final Block block) {
    super(TwoTility.config.getItemID(ItemLavaTank.class), block);
  }

  @Override
  public void initialize() {
    GameRegistry.registerItem(this, TwoTility.getItemName(NAME));
  }

  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    final int numBuckets = getMetadata(itemStack.getItemDamage());
    if (numBuckets == 0) {
      strings.add("Empty");
    } else {
      strings.add(String.format("Contains %d buckets of lava", numBuckets));
    }
  }
}
