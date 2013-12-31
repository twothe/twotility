/*
 */
package two.twotility.gui.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import two.twotility.container.ContainerBase;

/**
 * @author Two
 */
public class SlotCraftingSource extends Slot {

  protected final ContainerBase handler;

  public SlotCraftingSource(final ContainerBase handler, final IInventory inventory, final int index, final int x, final int y) {
    super(inventory, index, x, y);
    this.handler = handler;
  }

  @Override
  public void onSlotChanged() {
    super.onSlotChanged();
    this.handler.onCraftMatrixChanged(inventory);
  }
}
