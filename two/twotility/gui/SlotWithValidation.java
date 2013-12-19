/*
 */
package two.twotility.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author Two
 */
public class SlotWithValidation extends Slot {

  public SlotWithValidation(final IInventory inventory, final int slotIndex, final int x, final int y) {
    super(inventory, slotIndex, x, y);
  }

  @Override
  public boolean isItemValid(final ItemStack itemStack) {
    return inventory.isItemValidForSlot(getSlotIndex(), itemStack); // TODO
  }
}
