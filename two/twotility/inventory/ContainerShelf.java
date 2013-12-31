/*
 */
package two.twotility.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.tiles.TileShelf;

/**
 * @author Two
 */
public class ContainerShelf extends ContainerBase {

  protected final TileShelf tileShelf;

  public ContainerShelf(final InventoryPlayer inventoryPlayer, final TileShelf tileShelf) {
    super(inventoryPlayer, 4, 137, 4, 82);
    this.tileShelf = tileShelf;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots left
    for (int y = 0; y < 4; ++y) {
      for (int x = 0; x < 5; ++x) {
        this.addSlotToContainer(createSlot(tileShelf, slotCount++, 40 + x * 18, 4 + y * 18));
      }
    }
    if (slotCount != tileShelf.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileShelf.getClass().getName() + " slot-size{" + tileShelf.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileShelf.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset + TileShelf.INVENTORY_START_STORAGE, slotOffset + TileShelf.INVENTORY_START_STORAGE + TileShelf.INVENTORY_SIZE_STORAGE);
  }
}
