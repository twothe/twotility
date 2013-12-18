/*
 */
package two.twotility.gui;

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
import two.util.Logging;

/**
 * @author Two
 */
public class ContainerAdvancedFurnace extends Container {

  protected final static int UPDATEID_STORED_OPERATIONS = 0;
  
  final InventoryPlayer inventoryPlayer;
  final TileAdvancedFurnace tileAdvancedFurnace;
  int lastStoredFuel;

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
        this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }
    // fuel slots
    this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 61, 58));
    this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 61, 58 + 18));

    this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 91, 58));
    this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 91, 58 + 18));

    // output slots
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(new Slot(tileAdvancedFurnace, slotCount++, 112 + x * 18, 4 + y * 18));
      }
    }

    // in-progress slot
    this.addSlotToContainer(new SlotFurnace(inventoryPlayer.player, tileAdvancedFurnace, slotCount++, 76, 22));

    if (slotCount != tileAdvancedFurnace.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileAdvancedFurnace.getClass().getName() + " slot-size{" + tileAdvancedFurnace.getSizeInventory() + "}");
    }
  }

  @Override
  public void addCraftingToCrafters(ICrafting par1ICrafting) {
    super.addCraftingToCrafters(par1ICrafting);
    par1ICrafting.sendProgressBarUpdate(this, UPDATEID_STORED_OPERATIONS, this.tileAdvancedFurnace.getStoredFuel());
  }

  /**
   * Looks for changes made in the container, sends them to every listener.
   */
  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();

    final int newStoredFuel = this.tileAdvancedFurnace.getStoredFuel();
    if (lastStoredFuel != newStoredFuel) {
      this.lastStoredFuel = newStoredFuel;
      for (int i = 0; i < this.crafters.size(); ++i) {
        final ICrafting icrafting = (ICrafting) this.crafters.get(i);
        icrafting.sendProgressBarUpdate(this, UPDATEID_STORED_OPERATIONS, newStoredFuel);
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void updateProgressBar(final int updateID, final int newValue) {
    switch (updateID) {
      case UPDATEID_STORED_OPERATIONS: tileAdvancedFurnace.setStoredFuelForGUI(newValue);
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
      ItemStack result = itemStack.copy();

      if (slotId >= 36) {
        if (!mergeItemStack(itemStack, 0, 36, false)) { // transfer to player's inventory by first match
          return null;
        }
      } else if (!mergeItemStack(itemStack, 36, 36 + tileAdvancedFurnace.getSizeInventory(), false)) {
        return null;
      }

      if (itemStack.stackSize == 0) {
        slot.putStack(null);
      } else {
        slot.onSlotChanged();
      }
      slot.onPickupFromSlot(player, itemStack);
      return result;
    }

    return null;
  }
}
