/*
 */
package two.twotility.items;

import net.minecraft.item.Item;
import two.twotility.Config;

/**
 * @author Two
 */
public class ItemLavaTank extends Item {

  public ItemLavaTank() {
    super(Config.getItemID(ItemLavaTank.class));
  }

  public ItemLavaTank initialize() {

    return this;
  }
}
