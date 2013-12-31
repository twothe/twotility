/*
 */
package two.twotility.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import two.twotility.gui.GUICraftingBox;
import two.twotility.tiles.TileCraftingBox;

/**
 * @author Two
 */
public class ContainerCraftingBox extends ContainerBase {

  protected final TileCraftingBox tileCraftingBox;

  public ContainerCraftingBox(final InventoryPlayer inventoryPlayer, final TileCraftingBox tileCraftingBox) {
    super(inventoryPlayer, 6, tileCraftingBox.isCraftingBoxType() ? 179 - GUICraftingBox.HEIGHT_RECIPE_ROW : 179, 6, tileCraftingBox.isCraftingBoxType() ? 124 - GUICraftingBox.HEIGHT_RECIPE_ROW : 124);
    this.tileCraftingBox = tileCraftingBox;
  }

  @Override
  public ContainerBase layout() {
    super.layout();

    int slotCount = 0;
    // input slots left
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(tileCraftingBox, slotCount++, 4 + x * 18, 4 + y * 18));
      }
    }

    // input slots right
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(tileCraftingBox, slotCount++, 116 + x * 18, 4 + y * 18));
      }
    }

    // crafting grid
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(createSlot(tileCraftingBox, slotCount++, 60 + x * 18, 4 + y * 18));
      }
    }

    // crafting result
    this.addSlotToContainer(createSlot(tileCraftingBox, slotCount++, 78, 76));

    if (tileCraftingBox.isAdvancedCraftingBoxType()) {
      for (int x = 0; x < 9; ++x) {
        this.addSlotToContainer(createSlot(tileCraftingBox, slotCount++, 6 + x * 18, 100));
      }
    }

    return this;
  }

  @Override
  protected Slot createSlot(final IInventory inventory, final int slotIndex, final int x, final int y) {
    if (inventory != tileCraftingBox) {
      return super.createSlot(inventory, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_STORAGE) && (slotIndex < TileCraftingBox.INVENTORY_START_STORAGE + TileCraftingBox.INVENTORY_SIZE_STORAGE)) {
      return super.createSlot(tileCraftingBox, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_CRAFTING) && (slotIndex < TileCraftingBox.INVENTORY_START_CRAFTING + TileCraftingBox.INVENTORY_SIZE_CRAFTING)) {
      return super.createSlot(tileCraftingBox, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_CRAFTING_RESULT) && (slotIndex < TileCraftingBox.INVENTORY_START_CRAFTING_RESULT + TileCraftingBox.INVENTORY_SIZE_CRAFTING_RESULT)) {
      return super.createSlot(tileCraftingBox, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_RECIPE) && (slotIndex < TileCraftingBox.INVENTORY_START_RECIPE + TileCraftingBox.INVENTORY_SIZE_RECIPE)) {
      return super.createSlot(tileCraftingBox, slotIndex, x, y);
    } else {
      throw new IllegalArgumentException("Slot index #" + slotIndex + " is invalid for " + tileCraftingBox.getClass().getSimpleName());
    }
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileCraftingBox.isUseableByPlayer(entityplayer);
  }

  @Override
  protected boolean mergeItemStackWithInventory(final ItemStack itemStack, final int slotOffset) {
    return mergeItemStack(itemStack, slotOffset + TileCraftingBox.INVENTORY_START_STORAGE, slotOffset + TileCraftingBox.INVENTORY_START_STORAGE + TileCraftingBox.INVENTORY_SIZE_STORAGE);
  }
}
