/*
 */
package two.twotility.tiles;

import two.twotility.blocks.BlockShelf;

/**
 * @author Two
 */
public class TileShelf extends TileWithInventory {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 4 * 5;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE_STORAGE];

  static {
    int index = 0;
    for (int slot = INVENTORY_START_STORAGE; slot < INVENTORY_SIZE_STORAGE; ++slot) {
      ACCESSIBLE_SLOTS[index++] = slot;
    }
  }

  public TileShelf() {
    super(INVENTORY_SIZE);
  }

  @Override
  public String getInvName() {
    return BlockShelf.NAME;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return ACCESSIBLE_SLOTS;
  }
}
