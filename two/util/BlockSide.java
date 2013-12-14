/*
 */
package two.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/**
 * @author Two
 */
public enum BlockSide {

  bottom, top, north, south, west, east; // Blockside.ordinal() is the Minecraft side of a Block facing north

  /**
   * Returns the direction the entity is facing
   *
   * @param entity the entity in question
   * @return the direction the entity is facing as CW 0 (north) to 3 (west)
   */
  public static int getLookDirection(final EntityLivingBase entity) {
    return (MathHelper.floor_double(((double) (entity.rotationYaw + 180.0F + 45.0F)) / 90.0) % 4) & 3; // Minecraft is -180° (north) to 180° CW
  }

  /**
   * Returns the direction that is facing the entity
   *
   * @param entity the entity in question
   * @return the direction that is facing the entity as CW 0 (north) to 3 (west)
   */
  public static int getDirectionFacing(final EntityLivingBase entity) {
    return (MathHelper.floor_double(((double) (entity.rotationYaw + 180.0F + 225.0F)) / 90.0) % 4) & 3; // Minecraft is -180° (north) to 180° CW
  }

  /**
   * Calculates which side is <i>side</i> given the block's orientation
   *
   * @param side the side that is searched for
   * @param blockDir the facing of the block as CW 0 (north) to 3 (west)
   * @return the side that corresponds to <i>side</i> according to the block's rotation.
   */
  public static BlockSide getRotatedSide(final int side, final int blockDir) {
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
            return BlockSide.west;
          case 2:
            return BlockSide.south;
          case 3:
            return BlockSide.east;
        }
      case 3: // south side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.south;
          case 1:
            return BlockSide.east;
          case 2:
            return BlockSide.north;
          case 3:
            return BlockSide.west;
        }
      case 4: // west side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.west;
          case 1:
            return BlockSide.south;
          case 2:
            return BlockSide.east;
          case 3:
            return BlockSide.north;
        }
      case 5: // east side
        switch (blockDir & 3) {
          case 0:
            return BlockSide.east;
          case 1:
            return BlockSide.north;
          case 2:
            return BlockSide.west;
          case 3:
            return BlockSide.south;
        }
    }
    throw new IllegalArgumentException("Illegal side " + side);
  }

  /**
   * Calculates the side that is the <i>north</i> side given the block's orientation
   * @param blockDir the facing of the block as CW 0 (north) to 3 (west)
   * @return the Minecraft <i>side</i> that corresponds to the north face according to the block's rotation
   */
  public static int getRotatedFrontSide(final int blockDir) {
    switch (blockDir & 3) {
      case 0:
        return BlockSide.north.ordinal();
      case 1:
        return BlockSide.east.ordinal();
      case 2:
        return BlockSide.south.ordinal();
      case 3:
        return BlockSide.west.ordinal();
    }
    throw new RuntimeException("BlockDir " + blockDir + " is invalid!"); // this is impossible to reach
  }
}
