/*
 */
package two.twotility.gui.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author Two
 */
public class SlotWithValidation extends Slot {

  protected final boolean canTake;

  public SlotWithValidation(final IInventory inventory, final int slotIndex, final int x, final int y, boolean canTake) {
    super(inventory, slotIndex, x, y);
    this.canTake = canTake;
  }

  public SlotWithValidation(final IInventory inventory, final int slotIndex, final int x, final int y) {
    this(inventory, slotIndex, x, y, true);
  }

  @Override
  public boolean isItemValid(final ItemStack itemStack) {
    return inventory.isItemValidForSlot(getSlotIndex(), itemStack); // TODO
  }

  @Override
  public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
    return canTake;
  }
}
