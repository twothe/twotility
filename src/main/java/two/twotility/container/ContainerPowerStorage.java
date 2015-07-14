package two.twotility.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.tiles.TilePowerStorage;
import two.twotility.tiles.TileShelf;

/**
 *
 * @author Two
 */
public class ContainerPowerStorage extends ContainerBase {

  protected final TilePowerStorage tilePowerStorage;

  public ContainerPowerStorage(final InventoryPlayer inventoryPlayer, final TilePowerStorage tilePowerStorage) {
    super(inventoryPlayer, 4, 137, 4, 82);
    this.tilePowerStorage = tilePowerStorage;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots left
    for (int y = 0; y < 2; ++y) {
      for (int x = 0; x < 5; ++x) {
        this.addSlotToContainer(createSlot(tilePowerStorage, slotCount++, 40 + x * 18, 40 + y * 18));
      }
    }
    if (slotCount != tilePowerStorage.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tilePowerStorage.getClass().getName() + " slot-size{" + tilePowerStorage.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tilePowerStorage.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset + TileShelf.INVENTORY_START_STORAGE, slotOffset + TileShelf.INVENTORY_START_STORAGE + TileShelf.INVENTORY_SIZE_STORAGE);
  }
}
