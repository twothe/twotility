/*
 */
package two.twotility.inventory;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import two.twotility.gui.SlotWithValidation;
import two.twotility.tiles.TileAdvancedFurnace;

/**
 * @author Two
 */
public class ContainerAdvancedFurnace extends ContainerBase {

  protected final static int UPDATEID_STORED_OPERATIONS = 0;
  protected final static int UPDATEID_SMELTTIME_REMAINING = UPDATEID_STORED_OPERATIONS + 1;
  protected final TileAdvancedFurnace tileAdvancedFurnace;
  protected int lastStoredFuel, lastSmeltTime;

  public ContainerAdvancedFurnace(final InventoryPlayer inventoryPlayer, final TileAdvancedFurnace tileAdvancedFurnace) {
    super(inventoryPlayer, 4, 155, 4, 99);
    this.tileAdvancedFurnace = tileAdvancedFurnace;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }
    // fuel slots
    this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 61, 58));
    this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 61, 58 + 18));

    this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 91, 58));
    this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 91, 58 + 18));

    // output slots
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 112 + x * 18, 4 + y * 18));
      }
    }

    // in-progress slot
    this.addSlotToContainer(createSlot(tileAdvancedFurnace, slotCount++, 76, 22));

    if (slotCount != tileAdvancedFurnace.getSizeInventory()) {
      throw new RuntimeException("Mismatch between container slot-size{" + slotCount + "} and " + tileAdvancedFurnace.getClass().getName() + " slot-size{" + tileAdvancedFurnace.getSizeInventory() + "}");
    }

    return this;
  }

  @Override
  protected Slot createSlot(final IInventory inventory, final int slotIndex, final int x, final int y) {
    if (inventory != tileAdvancedFurnace) {
      return super.createSlot(inventory, slotIndex, x, y);
    } else if ((slotIndex >= TileAdvancedFurnace.INVENTORY_START_OUTPUT) && (slotIndex < TileAdvancedFurnace.INVENTORY_START_OUTPUT + TileAdvancedFurnace.INVENTORY_SIZE_OUTPUT)) {
      return new SlotFurnace(inventoryPlayer.player, tileAdvancedFurnace, slotIndex, x, y);
    } else {
      return new SlotWithValidation(tileAdvancedFurnace, slotIndex, x, y);
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
  protected boolean mergeItemStackWithInventory(final ItemStack newItem, final int slotOffset) {
    return mergeItemStack(newItem, slotOffset + TileAdvancedFurnace.INVENTORY_START_INPUT, slotOffset + TileAdvancedFurnace.INVENTORY_START_INPUT + TileAdvancedFurnace.INVENTORY_SIZE_INPUT + TileAdvancedFurnace.INVENTORY_SIZE_FUEL);
  }
}
