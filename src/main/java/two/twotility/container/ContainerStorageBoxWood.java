/*
 */
package two.twotility.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.tiles.TileStorageBoxWood;

/**
 * @author Two
 */
public class ContainerStorageBoxWood extends ContainerBase {

  protected final TileStorageBoxWood tileStorageWood;

  public ContainerStorageBoxWood(final InventoryPlayer inventoryPlayer, final TileStorageBoxWood tileStorageBox) {
    super(inventoryPlayer, 4, 137, 4, 82);
    this.tileStorageWood = tileStorageBox;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots left
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 9; ++x) {
        this.addSlotToContainer(createSlot(tileStorageWood, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }
    if (slotCount != tileStorageWood.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileStorageWood.getClass().getName() + " slot-size{" + tileStorageWood.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileStorageWood.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset, slotOffset + tileStorageWood.getSizeInventory());
  }
}
