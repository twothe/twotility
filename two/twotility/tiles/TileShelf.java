/*
 */
package two.twotility.tiles;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import two.twotility.blocks.BlockShelf;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerShelf;
import two.twotility.gui.GUIShelf;

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
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerShelf(player.inventory, this);
  }

  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUIShelf(player.inventory, this);
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
