/*
 */
package two.twotility.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import two.twotility.tiles.TileAdvancedFurnace;

/**
 * @author Two
 */
public class ContainerAdvancedFurnace extends Container {

  final InventoryPlayer inventoryPlayer;
  final TileAdvancedFurnace tileAdvancedFurnace;

  public ContainerAdvancedFurnace(final InventoryPlayer inventoryPlayer, final TileAdvancedFurnace tileAdvancedFurnace) {
    this.inventoryPlayer = inventoryPlayer;
    this.tileAdvancedFurnace = tileAdvancedFurnace;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer entityplayer) {
    return tileAdvancedFurnace.isUseableByPlayer(entityplayer);
  }
}
