package two.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/**
 * @author Two
 */
public enum BlockSide {

  BOTTOM, TOP, NORTH, SOUTH, WEST, EAST; // Blockside.ordinal() is the Minecraft side of a Block facing north
  public static final int ROTATION_MASK = 0x0C; // this mask extracts the rotation information from the metadata
  public static final int DATA_MASK = 0x03; // this mask extracts block specific data from the metadata

  /**
   * Returns the direction the entity is facing.
   * This is intended to be used for a block's metadata on placement.
   *
   * @param entity the entity in question
   * @return the direction the entity is facing as CW 0 (west) to 3 (south)
   */
  public static int getLookDirection(final EntityLivingBase entity) {
    return createRotationData(MathHelper.floor_double(((double) (entity.rotationYaw + 360.0F - 45.0F + 180.0F)) / 90.0) & 3); // Minecraft is -180° (north) to 180° CW, +360 to replace modulo with &
  }

  /**
   * Returns the direction that is facing the entity
   * This is intended to be used for a block's metadata on placement.
   *
   * @param entity the entity in question
   * @return the direction that is facing the entity as CW 0 (west) to 3 (south)
   */
  public static int getDirectionFacing(final EntityLivingBase entity) {
    return createRotationData(MathHelper.floor_double(((double) (entity.rotationYaw + 360.0F - 45.0F + 180.0F)) / 90.0) & 3); // Minecraft is -180° (north) to 180° CW, +360 to replace modulo with &
  }

  /**
   * Creates rotation data based on the given BlockSide ordinal.
   * @param direction a direction ordinal.
   * @return rotation data based on the given BlockSide ordinal.
   */
  public static int createRotationData(final int direction) {
    return (direction & 3) << 2;
  }

  /**
   * Returns the BlockSide according the given metadata. 
   * @param metadata metadata created by createRotationData.
   * @return the BlockSide according the given metadata.
   */
  public static int extractRotationData(final int metadata) {
    return (metadata & ROTATION_MASK) >>> 2;
  }

  /**
   * Calculates which side is <i>side</i> given the block's orientation
   *
   * @param side the side that is searched for
   * @param metadata the facing of the block.
   * @return the side that corresponds to <i>side</i> according to the block's rotation.
   */
  public static BlockSide getRotatedSide(final int side, final int metadata) {
    switch (side) {
      case 0:
        return BOTTOM;
      case 1:
        return TOP;
      case 2: // north side
        switch (extractRotationData(metadata)) {
          case 0: // facing west
            return EAST;
          case 1: // facing north
            return NORTH;
          case 2: // facing east
            return WEST;
          case 3: // facing south
            return SOUTH;
        }
      case 3: // south side
        switch (extractRotationData(metadata)) {
          case 0: // facing west
            return WEST;
          case 1: // facing north
            return SOUTH;
          case 2: // facing east
            return EAST;
          case 3: // facing south
            return NORTH;
        }
      case 4: // west side
        switch (extractRotationData(metadata)) {
          case 0: // facing west
            return NORTH;
          case 1: // facing north
            return WEST;
          case 2: // facing east
            return SOUTH;
          case 3: // facing south
            return EAST;
        }
      case 5: // east side
        switch (extractRotationData(metadata)) {
          case 0: // facing west
            return SOUTH;
          case 1: // facing north
            return EAST;
          case 2: // facing east
            return NORTH;
          case 3: // facing south
            return WEST;
        }
    }
    throw new IllegalArgumentException("Illegal side " + side);
  }

  /**
   * Returns the "native" block orientation based on side.
   * This is a convenient function for blocks that do not rotate (with a metadata of 0).
   *
   * @param side the side that is searched for.
   * @return the "native" block orientation based on side.
   */
  public static BlockSide getSide(final int side) {
    return getRotatedSide(side, 0);
  }

  /**
   * Returns the side that corresponds to direction.
   *
   * @param metadata the direction to look up.
   * @return the side that corresponds to direction.
   */
  public static BlockSide fromMetadata(final int metadata) {
    switch (extractRotationData(metadata)) {
      case 0:
        return WEST;
      case 1:
        return NORTH;
      case 2:
        return EAST;
      case 3:
        return SOUTH;
    }
    throw new IllegalArgumentException("Illegal direction " + metadata); // impossible to reach
  }
}
