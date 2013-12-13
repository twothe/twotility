/*
 */
package two.twotility.items;

import net.minecraft.item.Item;
import two.twotility.Config;

/**
 * @author Two
 */
public class ItemLavaForge extends Item {

  public ItemLavaForge() {
    super(Config.getItemID(ItemLavaForge.class));
  }

  public ItemLavaForge initialize() {

    return this;
  }
}
