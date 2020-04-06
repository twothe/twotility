/*
 */
package two.twotility.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.tiles.TileStorageBoxIron;

/**
 * @author Two
 */
public class ContainerStorageBoxIron extends ContainerBase {

  protected final TileStorageBoxIron tileStorageIron;

  public ContainerStorageBoxIron(final InventoryPlayer inventoryPlayer, final TileStorageBoxIron tileStorageBoxIron) {
    super(inventoryPlayer, 4, 174, 4, 119);
    this.tileStorageIron = tileStorageBoxIron;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots left
    for (int y = 0; y < 6; ++y) {
      for (int x = 0; x < 9; ++x) {
        this.addSlotToContainer(createSlot(tileStorageIron, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }
    if (slotCount != tileStorageIron.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileStorageIron.getClass().getName() + " slot-size{" + tileStorageIron.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileStorageIron.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset, slotOffset + tileStorageIron.getSizeInventory());
  }
}
