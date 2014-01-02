/*
 */
package two.util;

import net.minecraft.item.ItemStack;

/**
 * @author Two
 */
public class Items {

  public static boolean areEqualType(final ItemStack itemStack, final ItemStack other) {
    if (other == itemStack) {
      return true;
    }
    if ((other == null) || (itemStack == null)) {
      return false;
    }
    if (other.itemID != itemStack.itemID) {
      return false;
    }
    if (itemStack.getHasSubtypes()) {
      if ((other.getHasSubtypes() == false) || (itemStack.getItemDamage() != other.getItemDamage())) {
        return false;
      }
    }
    return ItemStack.areItemStackTagsEqual(itemStack, other);
  }
}
