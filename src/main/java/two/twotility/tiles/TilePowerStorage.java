/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package two.twotility.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import two.twotility.blocks.BlockPowerStorage;
import two.twotility.blocks.BlockShelf;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerPowerStorage;
import two.twotility.gui.GUIPowerStorage;
import two.twotility.gui.GUIShelf;

/**
 *
 * @author Two
 */
public class TilePowerStorage extends TileWithInventory {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 5 * 2;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE_STORAGE];

  static {
    int index = 0;
    for (int slot = INVENTORY_START_STORAGE; slot < INVENTORY_SIZE_STORAGE; ++slot) {
      ACCESSIBLE_SLOTS[index++] = slot;
    }
  }

  public TilePowerStorage() {
    super(INVENTORY_SIZE);
  }

  @Override
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerPowerStorage(player.inventory, this);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUIPowerStorage(player.inventory, this);
  }

  @Override
  public String getInventoryName() {
    return BlockPowerStorage.NAME;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return ACCESSIBLE_SLOTS;
  }
}
