/*
 */
package two.twotility.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import two.util.InvalidTileEntityException;

/**
 *
 * @author Two
 */
public interface GUICallback {

  Container createContainer(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException;

  // @SideOnly(Side.CLIENT)
  Gui createGUI(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException;
}
