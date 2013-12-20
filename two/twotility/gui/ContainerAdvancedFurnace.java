/*
 */
package two.twotility.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import two.twotility.tiles.TileAdvancedFurnace;

/**
 * @author Two
 */
public class ContainerAdvancedFurnace extends Container {

  protected final static int UPDATEID_STORED_OPERATIONS = 0;
  protected final static int UPDATEID_SMELTTIME_REMAINING = UPDATEID_STORED_OPERATIONS + 1;
  final InventoryPlayer inventoryPlayer;
  final TileAdvancedFurnace tileAdvancedFurnace;
  int lastStoredFuel, lastSmeltTime;

  public ContainerAdvancedFurnace(final InventoryPlayer inventoryPlayer, final TileAdvancedFurnace tileAdvancedFurnace) {
    this.inventoryPlayer = inventoryPlayer;
    this.tileAdvancedFurnace = tileAdvancedFurnace;

    // player hotbar
    for (int slot = 0; slot < 9; ++slot) {
      this.addSlotToContainer(new Slot(inventoryPlayer, slot, 4 + slot * 18, 155));
    }

    // player inventory
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 9; ++x) {
        this.addSlotToContainer(new Slot(inventoryPlayer, 9 + x + y * 9, 4 + x * 18, 99 + y * 18));
      }
    }

    int slotCount = 0;
    // input slots
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }
    // fuel slots
    this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 61, 58));
    this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 61, 58 + 18));

    this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 91, 58));
    this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 91, 58 + 18));

    // output slots
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(new SlotFurnace(inventoryPlayer.player, tileAdvancedFurnace, slotCount++, 112 + x * 18, 4 + y * 18));
      }
    }

    // in-progress slot
    this.addSlotToContainer(new SlotWithValidation(tileAdvancedFurnace, slotCount++, 76, 22));

    if (slotCount != tileAdvancedFurnace.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileAdvancedFurnace.getClass().getName() + " slot-size{" + tileAdvancedFurnace.getSizeInventory() + "}");
    }
  }

  @Override
  public void addCraftingToCrafters(ICrafting par1ICrafting) {
    super.addCraftingToCrafters(par1ICrafting);
    par1ICrafting.sendProgressBarUpdate(this, UPDATEID_STORED_OPERATIONS, this.tileAdvancedFurnace.getStoredFuel());
    par1ICrafting.sendProgressBarUpdate(this, UPDATEID_SMELTTIME_REMAINING, this.tileAdvancedFurnace.getRemainingSmeltTime());
  }

  /**
   * Looks for changes made in the container, sends them to every listener.
   */
  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();

    final int newStoredFuel = this.tileAdvancedFurnace.getStoredFuel();
    final int newSmeltTime = this.tileAdvancedFurnace.getRemainingSmeltTime();
    if ((lastStoredFuel != newStoredFuel) || (lastSmeltTime != newSmeltTime)) {
      this.lastStoredFuel = newStoredFuel;
      this.lastSmeltTime = newSmeltTime;
      for (int i = 0; i < this.crafters.size(); ++i) {
        final ICrafting icrafting = (ICrafting) this.crafters.get(i);
        icrafting.sendProgressBarUpdate(this, UPDATEID_STORED_OPERATIONS, newStoredFuel);
        icrafting.sendProgressBarUpdate(this, UPDATEID_SMELTTIME_REMAINING, newSmeltTime);
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void updateProgressBar(final int updateID, final int newValue) {
    switch (updateID) {
      case UPDATEID_STORED_OPERATIONS:
        tileAdvancedFurnace.setStoredFuelForGUI(newValue);
        break;
      case UPDATEID_SMELTTIME_REMAINING:
        tileAdvancedFurnace.setSmeltTimerForGUI(newValue);
        break;
      default:
        FMLLog.warning("%s received update event for unknown ID %d", this.getClass().getSimpleName(), updateID);
        break;
    }
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileAdvancedFurnace.isUseableByPlayer(entityplayer);
  }

  @Override
  public ItemStack transferStackInSlot(final EntityPlayer player, final int slotId) {
    final Slot slot = getSlot(slotId);
    if ((slot != null) && (slot.getHasStack())) {
      final ItemStack itemStack = slot.getStack();
      final ItemStack result = itemStack.copy();

      if (slotId >= 36) {
        if (!mergeItemStack(result, 0, 36)) { // transfer to player's inventory by first match
          return null;
        }
      } else if (!mergeItemStack(result, 36 + TileAdvancedFurnace.INVENTORY_START_INPUT, 36 + TileAdvancedFurnace.INVENTORY_START_INPUT + TileAdvancedFurnace.INVENTORY_SIZE_INPUT + TileAdvancedFurnace.INVENTORY_SIZE_FUEL)) {
        return null;
      }

      final int itemsMoved = itemStack.stackSize - result.stackSize;
      slot.decrStackSize(itemsMoved);
      slot.onPickupFromSlot(player, itemStack);
      return result;
    }

    return null;
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
          final int newSlotStackSize = Math.min(itemInSlot.stackSize + newItem.stackSize, newItem.getMaxStackSize());
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
          slot.putStack(newItem.copy());
          slot.onSlotChanged();
          newItem.stackSize = 0;
          inventoryChanged = true;
          break;
        }
      }
    }

    return inventoryChanged;
  }
}
