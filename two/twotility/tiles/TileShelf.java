/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import two.twotility.blocks.BlockShelf;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerShelf;
import two.twotility.gui.GUIShelf;
import two.util.BlockSide;

/**
 * @author Two
 */
public class TileShelf extends TileWithInventory {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 4 * 5;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE;
  protected static final float MIN_FILLSTATE_CHANGE = 2.0f / ((float) INVENTORY_SIZE_STORAGE);
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

  protected float getInventoryFillState() {
    int filledSlots = 0;
    for (final ItemStack stack : this.inventory) {
      if (stack != null) {
        ++filledSlots;
      }
    }
    return ((float) filledSlots) / ((float) inventory.length);
  }

  @Override
  public void onInventoryChanged() {
    if (worldObj.isRemote == false) {
      final float fillState = getInventoryFillState();
      final int currentMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
      final int currentState = BlockSide.getBlockDataFromMetadata(currentMeta);
      final float currentFillState = ((float) currentState) / ((float) BlockShelf.NUM_STATES);
      FMLLog.info("if ((new{%5.3f} == 0.0f) || (current{%5.3f} == 0.0f) || (diff{%5.3f} >= %5.3f))", fillState, currentFillState, Math.abs(currentFillState - fillState), MIN_FILLSTATE_CHANGE);
      if ((fillState == 0.0f) || (currentFillState == 0.0f) || (Math.abs(currentFillState - fillState) >= MIN_FILLSTATE_CHANGE)) {
        final int newState = (int) Math.ceil(fillState * ((float) BlockShelf.STATE_FULL));
        if (newState != currentState) {
          worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockSide.updateState(currentMeta, newState), 2);
          FMLLog.info("New shelf state (%5.3f * %d): %d", fillState, BlockShelf.STATE_FULL, newState);
        }
      }
    };
    super.onInventoryChanged();
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
