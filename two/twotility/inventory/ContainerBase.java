/*
 */
package two.twotility.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author Two
 */
public abstract class ContainerBase extends Container {

  protected final InventoryPlayer inventoryPlayer;
  protected final int hotbarX, hotbarY, playerInventoryX, playerInventoryY;

  public ContainerBase(final InventoryPlayer inventoryPlayer, final int hotbarX, final int hotbarY, final int playerInventoryX, final int playerInventoryY) {
    this.inventoryPlayer = inventoryPlayer;
    this.hotbarX = hotbarX;
    this.hotbarY = hotbarY;
    this.playerInventoryX = playerInventoryX;
    this.playerInventoryY = playerInventoryY;
  }

  public ContainerBase layout() {
    // player hotbar
    for (int slot = 0; slot < 9; ++slot) {
      this.addSlotToContainer(createSlot(inventoryPlayer, slot, hotbarX + slot * 18, hotbarY));
    }

    // player inventory
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 9; ++x) {
        this.addSlotToContainer(createSlot(inventoryPlayer, 9 + x + y * 9, playerInventoryX + x * 18, playerInventoryY + y * 18));
      }
    }
    return this;
  }

  protected Slot createSlot(final IInventory inventory, final int slotIndex, final int x, final int y) {
    return new Slot(inventory, slotIndex, x, y);
  }

  protected boolean mergeItemStack(final ItemStack newItem, int slotStart, int slotEnd) {
    if (newItem == null) {
      return true;
    }
    ItemStack itemInSlot;
    boolean inventoryChanged = false;
    Slot slot;

    if (newItem.isStackable()) { // try to stack with existing items if possible
      for (int slotIndex = slotStart; slotIndex < slotEnd; ++slotIndex) {
        slot = getSlot(slotIndex);
        itemInSlot = slot.getStack();

        if (itemInSlot != null && itemInSlot.itemID == newItem.itemID && (!newItem.getHasSubtypes() || newItem.getItemDamage() == itemInSlot.getItemDamage()) && ItemStack.areItemStackTagsEqual(newItem, itemInSlot)) {
          final int newSlotStackSize = Math.min(Math.min(itemInSlot.stackSize + newItem.stackSize, newItem.getMaxStackSize()), slot.inventory.getInventoryStackLimit());
          final int remaining = newItem.stackSize - (newSlotStackSize - itemInSlot.stackSize);
          if (remaining != newItem.stackSize) { // was something moved?
            inventoryChanged = true;
            newItem.stackSize = remaining;
            itemInSlot.stackSize = newSlotStackSize;
            slot.onSlotChanged();
          }
          if (remaining == 0) {
            break;
          }
        }
      }
    }

    if (newItem.stackSize > 0) { // if there is anything left, put it in the next free slot
      for (int slotIndex = slotStart; slotIndex < slotEnd; ++slotIndex) {
        slot = getSlot(slotIndex);
        itemInSlot = slot.getStack();

        if ((itemInSlot == null) && slot.isItemValid(newItem)) {
          final int newSlotStackSize = Math.min(newItem.stackSize, Math.min(newItem.getMaxStackSize(), slot.inventory.getInventoryStackLimit()));
          final int remaining = newItem.stackSize - newSlotStackSize;
          final ItemStack itemForSlot = newItem.copy();
          itemForSlot.stackSize = newSlotStackSize;
          slot.putStack(itemForSlot);
          newItem.stackSize = remaining;
          slot.onSlotChanged();
          inventoryChanged = true;
          if (remaining == 0) {
            break;
          }
        }
      }
    }

    return inventoryChanged;
  }
}
