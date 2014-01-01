/*
 */
package two.twotility.tiles;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import two.twotility.blocks.BlockCraftingBox;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerCraftingBox;
import two.twotility.gui.GUICraftingBox;
import two.util.BlockSide;

/**
 * @author Two
 */
public class TileCraftingBox extends TileWithInventory {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 2 * 3 * 5;
  public static final int INVENTORY_START_CRAFTING = INVENTORY_START_STORAGE + INVENTORY_SIZE_STORAGE;
  public static final int INVENTORY_SIZE_CRAFTING = 3 * 3;
  public static final int INVENTORY_START_CRAFTING_RESULT = INVENTORY_START_CRAFTING + INVENTORY_SIZE_CRAFTING;
  public static final int INVENTORY_SIZE_CRAFTING_RESULT = 1;
  public static final int INVENTORY_START_RECIPE = INVENTORY_START_CRAFTING_RESULT + INVENTORY_SIZE_CRAFTING_RESULT;
  public static final int INVENTORY_SIZE_RECIPE = 9;
  public static final int RECIPE_INDEX_OFF = INVENTORY_SIZE_RECIPE / 2;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE + INVENTORY_SIZE_CRAFTING + INVENTORY_SIZE_CRAFTING_RESULT + INVENTORY_SIZE_RECIPE;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE_STORAGE];
  protected static final int[] ACCESSIBLE_SLOTS_BOTTOM = {INVENTORY_START_CRAFTING_RESULT};

  static {
    int index = 0;
    for (int slot = INVENTORY_START_STORAGE; slot < INVENTORY_SIZE_STORAGE; ++slot) {
      ACCESSIBLE_SLOTS[index++] = slot;
    }
  }
  protected final InventoryCrafting craftingMatrix;
  protected int craftingBoxType = BlockCraftingBox.STATE_BOX;
  protected int selectedRecipeIndex = RECIPE_INDEX_OFF;
  protected boolean needInitialization = true;

  public TileCraftingBox() {
    super(INVENTORY_SIZE);
    this.craftingMatrix = new InventoryCrafting(null, 3, 3);
  }

  @Override
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerCraftingBox(player.inventory, this);
  }

  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUICraftingBox(player.inventory, this);
  }

  public int getCraftingBoxType() {
    return BlockSide.getRotationData(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
  }

  public boolean isCraftingBoxType() {
    return this.getCraftingBoxType() == BlockCraftingBox.STATE_BOX;
  }

  public boolean isAdvancedCraftingBoxType() {
    return this.getCraftingBoxType() == BlockCraftingBox.STATE_ADVANCED;
  }

  public boolean isAutocraftingEnabled() {
    return (getSelectedRecipeIndex() != RECIPE_INDEX_OFF);
  }

  public int getSelectedRecipeIndex() {
    return selectedRecipeIndex;
  }

  public void setSelectedRecipeIndex(final int selectedRecipeIndex) {
    this.selectedRecipeIndex = selectedRecipeIndex;
  }

  @Override
  public void onInventoryChanged() {
    if (worldObj.isRemote == false) {
      final int currentMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
      final int newMeta = BlockSide.updateState(currentMeta, TileShelf.calculateFillState(inventory, currentMeta));

      if (newMeta != currentMeta) {
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMeta, 2);
      }
    }
    super.onInventoryChanged();
  }

  @Override
  public String getInvName() {
    return BlockCraftingBox.NAME_BOX;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    if (BlockSide.getSide(side) == BlockSide.BOTTOM) {
      return ACCESSIBLE_SLOTS_BOTTOM;
    } else {
      return ACCESSIBLE_SLOTS;
    }
  }
}
