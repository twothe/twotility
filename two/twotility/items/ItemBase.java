/*
 */
package two.twotility.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import two.twotility.InitializableModContent;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public abstract class ItemBase extends Item implements InitializableModContent {

  public ItemBase(final int id) {
    super(id);
  }

  /**
   * Returns the metadata of the block which this Item (ItemBlock) can place
   */
  @Override
  public int getMetadata(final int metadata) {
    return metadata;
  }

  @SideOnly(Side.CLIENT)
  /**
   * gets the CreativeTab this item is displayed on
   */
  @Override
  public CreativeTabs getCreativeTab() {
    return TwoTility.creativeTab;
  }
}
