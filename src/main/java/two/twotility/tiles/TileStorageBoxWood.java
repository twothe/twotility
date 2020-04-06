/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.twotility.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import two.twotility.blocks.BlockStorageBoxIron;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerStorageBoxWood;
import two.twotility.gui.GUIStorageBoxWood;

/**
 *
 * @author Two
 */
public class TileStorageBoxWood extends TileWithInventory {

  public static final int INVENTORY_SIZE = 3 * 9;
  protected final int[] accessibleSlots;

  public TileStorageBoxWood() {
    super(INVENTORY_SIZE);
    accessibleSlots = new int[INVENTORY_SIZE];
    for (int i = 0; i < INVENTORY_SIZE; ++i) {
      accessibleSlots[i] = i;
    }
  }

  @Override
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerStorageBoxWood(player.inventory, this);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUIStorageBoxWood(player.inventory, this);
  }

  @Override
  public String getInventoryName() {
    return BlockStorageBoxIron.NAME;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return accessibleSlots;
  }
}
