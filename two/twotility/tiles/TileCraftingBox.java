/*
 */
package two.twotility.tiles;

import two.twotility.blocks.BlockCraftingBox;
import two.util.BlockSide;

/**
 * @author Two
 */
public class TileCraftingBox extends TileWithInventory {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 2 * 3 * 7;
  public static final int INVENTORY_START_CRAFTING = INVENTORY_START_STORAGE + INVENTORY_SIZE_STORAGE;
  public static final int INVENTORY_SIZE_CRAFTING = 3 * 3;
  public static final int INVENTORY_START_CRAFTING_RESULT = INVENTORY_START_CRAFTING + INVENTORY_SIZE_CRAFTING;
  public static final int INVENTORY_SIZE_CRAFTING_RESULT = 1;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE + INVENTORY_SIZE_CRAFTING + INVENTORY_SIZE_CRAFTING_RESULT;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE_STORAGE];
  protected static final int[] ACCESSIBLE_SLOTS_BOTTOM = {INVENTORY_START_CRAFTING_RESULT};

  static {
    int index = 0;
    for (int slot = INVENTORY_START_STORAGE; slot < INVENTORY_SIZE_STORAGE; ++slot) {
      ACCESSIBLE_SLOTS[index++] = slot;
    }
  }

  public TileCraftingBox() {
    super(INVENTORY_SIZE);
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
