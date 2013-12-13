/*
 */
package two.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/**
 * @author Two
 */
public enum BlockSide {

  bottom, top, north, south, west, east;

  public static int getLookDirection(final EntityLivingBase entity) {
    return (MathHelper.floor_double(((double) (225.0F - entity.rotationYaw)) / 90.0) % 4) & 3; // Minecraft does everything wrong that can be done wrong
  }

  public static BlockSide getRelativeSide(final int side, final int blockDir) {
    switch (side) {
      case 0:
        return BlockSide.bottom;
      case 1:
        return BlockSide.top;
      case 2: // north side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.north;
          case 1:
            return BlockSide.east;
          case 2:
            return BlockSide.south;
          case 3:
            return BlockSide.west;
        }
      case 3: // south side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.south;
          case 1:
            return BlockSide.west;
          case 2:
            return BlockSide.north;
          case 3:
            return BlockSide.east;
        }
      case 4: // west side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.west;
          case 1:
            return BlockSide.north;
          case 2:
            return BlockSide.east;
          case 3:
            return BlockSide.south;
        }
      case 5: // east side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.east;
          case 1:
            return BlockSide.south;
          case 2:
            return BlockSide.west;
          case 3:
            return BlockSide.north;
        }
    }
    throw new IllegalArgumentException("Illegal side " + side);
  }

  public static int getSideFacingPlacer(final int lookDir) {
    switch (lookDir & 3) {
      case 0:
        return BlockSide.south.ordinal();
      case 1:
        return BlockSide.east.ordinal();
      case 2:
        return BlockSide.north.ordinal();
      case 3:
        return BlockSide.west.ordinal();
    }
    throw new RuntimeException("LookDir " + lookDir + " is invalid!");
  }
}
