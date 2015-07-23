/*
 */
package two.twotility.container;

import two.twotility.inventory.InventoryPouchSmall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import two.twotility.TwoTility;
import two.twotility.TwoTilityAssets;
import two.twotility.gui.slots.SlotWithValidation;
import two.twotility.util.ItemUtil;

/**
 * @author Two
 */
public class ContainerPouchSmall extends ContainerBase {

  protected final static int UPDATEID_STORED_OPERATIONS = 0;
  protected final static int UPDATEID_SMELTTIME_REMAINING = UPDATEID_STORED_OPERATIONS + 1;
  protected final ItemStack stackPouchSmall;
  protected final IInventory pouchInventory;
  protected int lastStoredFuel, lastSmeltTime;

  public ContainerPouchSmall(final InventoryPlayer inventoryPlayer, final ItemStack stackPouchSmall) {
    super(inventoryPlayer, 4, 119, 4, 64);
    this.stackPouchSmall = stackPouchSmall;
    pouchInventory = InventoryPouchSmall.fromItemStack(stackPouchSmall, inventoryPlayer.player);
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // pouch slots
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(pouchInventory, slotCount++, 58 + x * 18, 4 + y * 18));
      }
    }

    if (slotCount != pouchInventory.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + pouchInventory.getClass().getName() + " slot-size{" + pouchInventory.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  protected Slot createSlot(final IInventory inventory, final int slotIndex, final int x, final int y) {
    final ItemStack itemStackInSlot = inventory.getStackInSlot(slotIndex);
    if (ItemUtil.isSameBaseType(itemStackInSlot, TwoTilityAssets.itemPouchSmall)) {
      return new SlotWithValidation(inventory, slotIndex, x, y, false);
    } else {
      return super.createSlot(inventory, slotIndex, x, y);
    }
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return pouchInventory.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset + InventoryPouchSmall.INVENTORY_START, slotOffset + InventoryPouchSmall.INVENTORY_START + InventoryPouchSmall.INVENTORY_SIZE);
  }
}
