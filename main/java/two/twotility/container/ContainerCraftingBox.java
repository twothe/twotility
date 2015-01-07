/*
 */
package two.twotility.container;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import two.twotility.gui.GUICraftingBox;
import two.twotility.gui.slots.SlotCraftingSource;
import two.twotility.inventory.CraftingInventoryView;
import two.twotility.tiles.TileCraftingBox;

/**
 * @author Two
 */
public class ContainerCraftingBox extends ContainerBase {

  protected final static int UPDATEID_SELECTED_RECIPE = 0;
  protected final TileCraftingBox tileCraftingBox;
  protected final CraftingInventoryView craftingMatrix;
  protected Slot craftingResult;
  protected int lastSelectedRecipe = 0;

  public ContainerCraftingBox(final InventoryPlayer inventoryPlayer, final TileCraftingBox tileCraftingBox) {
    super(inventoryPlayer, 6, tileCraftingBox.isCraftingBoxType() ? 179 - GUICraftingBox.HEIGHT_RECIPE_ROW : 179, 6, tileCraftingBox.isCraftingBoxType() ? 124 - GUICraftingBox.HEIGHT_RECIPE_ROW : 124);
    this.tileCraftingBox = tileCraftingBox;
    this.craftingMatrix = new CraftingInventoryView(tileCraftingBox, this, TileCraftingBox.INVENTORY_START_CRAFTING, 3, 3, tileCraftingBox.isAutocraftingEnabled());
    this.lastSelectedRecipe = tileCraftingBox.getSelectedRecipeIndex();
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
    this.craftingResult = createSlot(tileCraftingBox, slotCount++, 78, 76);
    this.addSlotToContainer(craftingResult);

    if (tileCraftingBox.isAdvancedCraftingBoxType()) {
      for (int x = 0; x < TileCraftingBox.INVENTORY_SIZE_RECIPE; ++x) {
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
      return new SlotCraftingSource(this, tileCraftingBox, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_CRAFTING_RESULT) && (slotIndex < TileCraftingBox.INVENTORY_START_CRAFTING_RESULT + TileCraftingBox.INVENTORY_SIZE_CRAFTING_RESULT)) {
      return new SlotCrafting(inventoryPlayer.player, craftingMatrix, tileCraftingBox, slotIndex, x, y);
    } else if ((slotIndex >= TileCraftingBox.INVENTORY_START_RECIPE) && (slotIndex < TileCraftingBox.INVENTORY_START_RECIPE + TileCraftingBox.INVENTORY_SIZE_RECIPE)) {
      return super.createSlot(tileCraftingBox, slotIndex, x, y);
    } else {
      throw new IllegalArgumentException("Slot index #" + slotIndex + " is invalid for " + tileCraftingBox.getClass().getSimpleName());
    }
  }

  @Override
  public void onCraftMatrixChanged(final IInventory inventory) {
    this.craftingResult.putStack(CraftingManager.getInstance().findMatchingRecipe(this.craftingMatrix, tileCraftingBox.getWorldObj()));
    super.onCraftMatrixChanged(inventory);
  }

  @Override
  public void addCraftingToCrafters(final ICrafting crafting) {
    super.addCraftingToCrafters(crafting);
    crafting.sendProgressBarUpdate(this, UPDATEID_SELECTED_RECIPE, this.tileCraftingBox.getSelectedRecipeIndex());
  }

  /**
   * Looks for changes made in the container, sends them to every listener.
   */
  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();

    final int newSelectedRecipe = this.tileCraftingBox.getSelectedRecipeIndex();
    if (lastSelectedRecipe != newSelectedRecipe) {
      this.lastSelectedRecipe = newSelectedRecipe;
      for (int i = 0; i < this.crafters.size(); ++i) {
        final ICrafting icrafting = (ICrafting) this.crafters.get(i);
        icrafting.sendProgressBarUpdate(this, UPDATEID_SELECTED_RECIPE, newSelectedRecipe);
        craftingMatrix.setTakeFromInventory(tileCraftingBox.isAutocraftingEnabled());
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void updateProgressBar(final int updateID, final int newValue) {
    switch (updateID) {
      case UPDATEID_SELECTED_RECIPE:
        tileCraftingBox.setSelectedRecipeIndex(newValue);
        craftingMatrix.setTakeFromInventory(tileCraftingBox.isAutocraftingEnabled());
        break;
      default:
        FMLLog.warning("%s received update event for unknown ID %d", this.getClass().getSimpleName(), updateID);
        break;
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
