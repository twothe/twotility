/*
 */
package two.util;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Two
 */
public class InvalidTileEntityException extends Exception {

  public InvalidTileEntityException(final Class<? extends TileEntity> expected, final TileEntity found, final int x, final int y, final int z) {
    super(String.format("TileEntity at %d, %d, %d should have been %s, but was %s", x, y, z, expected.getName(), found == null ? "null" : found.getClass().getName()));
  }
}
