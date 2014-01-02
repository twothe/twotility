/*
 */
package two.twotility.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import two.util.Items;

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

  protected boolean transferToInventory(final int slotId, final ItemStack pickedUp) {
    if (slotId >= 36) {
      return mergeItemStack(pickedUp, 0, 36);
    } else {
      return mergeItemStackWithInventory(pickedUp, 36);
    }
  }

  /**
   * Tries to move all items from the given slotID to the other inventory.
   * The other inventory is the player's inventory if slotID is part of this continer,
   * or this container if slotID is part of the player's inventory.
   *
   * @param player the player trying to transfere items.
   * @param slotId the source slot from which the items are transfered.
   * @return an ItemStack equal to the items moved away from the given slotId or null if no items were moved.
   */
  @Override
  public ItemStack transferStackInSlot(final EntityPlayer player, final int slotId) {
    final Slot slot = getSlot(slotId);
    final ItemStack itemInSlotBefore = slot.getStack();
    if ((itemInSlotBefore != null) && (itemInSlotBefore.stackSize > 0)) {
      final ItemStack pickedUp = itemInSlotBefore.copy();

      if (transferToInventory(slotId, pickedUp)) {
        final int amountTaken = itemInSlotBefore.stackSize - pickedUp.stackSize;
        final ItemStack itemsTaken = slot.decrStackSize(amountTaken);
        slot.onPickupFromSlot(player, itemsTaken);
        return itemsTaken;
      }
    }

    return null;
  }

  /**
   * Callback for special inventory handling.
   * Must delegate to mergeItemStack with the appropriate slot index, adding the offset.
   *
   * @param itemStack the item to pass to mergeItemStack.
   * @param slotOffset the offset to add to the accessible internal inventory slot index.
   * @return the result of the delegated mergeItemStack.
   */
  protected abstract boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset);

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

        if (Items.areEqualType(itemInSlot, newItem)) {
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
          slot.putStack(newItem.splitStack(newSlotStackSize));
          inventoryChanged = true;
          if (newItem.stackSize == 0) {
            break;
          }
        }
      }
    }

    return inventoryChanged;
  }
}
