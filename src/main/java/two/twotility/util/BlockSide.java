package two.twotility.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Two
 */
public enum BlockSide {

  BOTTOM, TOP, NORTH, SOUTH, WEST, EAST; // Blockside.ordinal() is the Minecraft side of a Block facing north
  public static final int ROTATION_MASK = 0x0C; // this mask extracts the rotation information from the metadata
  public static final int DATA_MASK = 0x03; // this mask extracts block specific data from the metadata

  public ForgeDirection behind() {
    switch (this) {
      case BOTTOM:
        return ForgeDirection.UP;
      case TOP:
        return ForgeDirection.DOWN;
      case NORTH:
        return ForgeDirection.SOUTH;
      case SOUTH:
        return ForgeDirection.NORTH;
      case WEST:
        return ForgeDirection.EAST;
      case EAST:
        return ForgeDirection.WEST;
    }
    throw new IllegalStateException("Illegal side for behind: " + this.name());
  }

  public BlockSide backSide() {
    switch (this) {
      case BOTTOM:
        return TOP;
      case TOP:
        return BOTTOM;
      case NORTH:
        return SOUTH;
      case SOUTH:
        return NORTH;
      case WEST:
        return EAST;
      case EAST:
        return WEST;
    }
    throw new IllegalStateException("Illegal side for backSide: " + this.name());
  }

  public ForgeDirection infront() {
    switch (this) {
      case BOTTOM:
        return ForgeDirection.DOWN;
      case TOP:
        return ForgeDirection.UP;
      case NORTH:
        return ForgeDirection.NORTH;
      case SOUTH:
        return ForgeDirection.SOUTH;
      case WEST:
        return ForgeDirection.WEST;
      case EAST:
        return ForgeDirection.EAST;
    }
    throw new IllegalStateException("Illegal side for infront: " + this.name());
  }

  public ForgeDirection left() {
    switch (this) {
      case NORTH:
        return ForgeDirection.WEST;
      case SOUTH:
        return ForgeDirection.EAST;
      case WEST:
        return ForgeDirection.NORTH;
      case EAST:
        return ForgeDirection.SOUTH;
    }
    throw new IllegalStateException("Illegal side for left: " + this.name());
  }

  public BlockSide leftSide() {
    switch (this) {
      case NORTH:
        return WEST;
      case SOUTH:
        return EAST;
      case WEST:
        return NORTH;
      case EAST:
        return SOUTH;
    }
    throw new IllegalStateException("Illegal side for leftSide: " + this.name());
  }

  public ForgeDirection right() {
    switch (this) {
      case NORTH:
        return ForgeDirection.EAST;
      case SOUTH:
        return ForgeDirection.WEST;
      case WEST:
        return ForgeDirection.SOUTH;
      case EAST:
        return ForgeDirection.NORTH;
    }
    throw new IllegalStateException("Illegal side for right: " + this.name());
  }

  public BlockSide rightSide() {
    switch (this) {
      case NORTH:
        return EAST;
      case SOUTH:
        return WEST;
      case WEST:
        return SOUTH;
      case EAST:
        return NORTH;
    }
    throw new IllegalStateException("Illegal side for rightSide: " + this.name());
  }

  /**
   * Returns the direction the entity is looking at.
   * This is intended to be used for a block's metadata on placement.
   *
   * @param entity the entity in question
   * @return the direction the entity is looking at as CW 0 (west) to 3 (south)
   */
  public static int getLookDirection(final EntityLivingBase entity) {
    return MathHelper.floor_double(((double) (entity.rotationYaw + 360.0F - 45.0F + 180.0F)) / 90.0) & 3; // Minecraft is -180° (north) to 180° CW, +360 to replace modulo with &
  }

  /**
   * Returns the side the entity is looking at.
   *
   * @param entity the entity in question
   * @return the BlockSide the entity is looking at
   */
  public static BlockSide getLookSide(final EntityLivingBase entity) {
    return fromDirection(getLookDirection(entity));
  }

  /**
   * Returns the direction that is facing the entity
   * This is intended to be used for a block's metadata on placement.
   *
   * @param entity the entity in question
   * @return the direction that is facing the entity as CW 0 (west) to 3 (south)
   */
  public static int getDirectionFacing(final EntityLivingBase entity) {
    return MathHelper.floor_double(((double) (entity.rotationYaw + 360.0F - 45.0F + 180.0F)) / 90.0) & 3; // Minecraft is -180° (north) to 180° CW, +360 to replace modulo with &
  }

  /**
   * Returns the side that is facing the entity
   *
   * @param entity the entity in question
   * @return the BlockSide that is facing the entity.
   */
  public static BlockSide getSideFacing(final EntityLivingBase entity) {
    return fromDirection(getDirectionFacing(entity));
  }

  /**
   * Creates rotation data based on the given BlockSide ordinal.
   *
   * @param direction a direction ordinal.
   * @return rotation data based on the given BlockSide ordinal.
   */
  public static int createRotationData(final int direction) {
    return (direction & 3) << 2;
  }

  public static int createRotationData(final BlockSide direction) {
    return createRotationData(direction.direction());
  }

  public int direction() {
    switch (this) {
      case WEST:
        return 0;
      case NORTH:
        return 1;
      case EAST:
        return 2;
      case SOUTH:
        return 3;
    }
    throw new IllegalArgumentException("No direction for " + this);
  }

  /**
   * Returns the raw rotation data as found in the given metadata.
   * @param metadata the block's metadata.
   * @return the raw rotation data as found in the given metadata.
   */
  public static int getRotationData(final int metadata) {
    return metadata & ROTATION_MASK;
  }
  
  /**
   * Returns the BlockSide according the given metadata.
   *
   * @param metadata metadata created by createRotationData.
   * @return the BlockSide according the given metadata.
   */
  public static int getDirectionFrom(final int metadata) {
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
        switch (getDirectionFrom(metadata)) {
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
        switch (getDirectionFrom(metadata)) {
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
        switch (getDirectionFrom(metadata)) {
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
        switch (getDirectionFrom(metadata)) {
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
   * Returns the side that corresponds to direction as encoded in the metadata.
   *
   * @param metadata the direction to look up.
   * @return the side that corresponds to direction.
   */
  public static BlockSide fromMetadata(final int metadata) {
    return fromDirection(getDirectionFrom(metadata));
  }

  /**
   * Returns the side that corresponds to direction.
   *
   * @param direction the direction to look up.
   * @return the side that corresponds to direction.
   */
  public static BlockSide fromDirection(final int direction) {
    switch (direction & 3) {
      case 0:
        return WEST;
      case 1:
        return NORTH;
      case 2:
        return EAST;
      case 3:
        return SOUTH;
    }
    throw new IllegalArgumentException("Illegal direction " + direction); // impossible to reach
  }

  /**
   * Returns a block's state from its metadata by removing rotation data.
   *
   * @param metadata the block's metadata.
   * @return the block's metadata state.
   */
  public static int getBlockDataFromMetadata(final int metadata) {
    return (metadata & BlockSide.DATA_MASK);
  }

  /**
   * Updates a block's state from its current metadata and block state.
   *
   * @param metaCurrent the block's current metadata.
   * @param state the block's internal state.
   * @return the block's metadata.
   */
  public static int updateState(final int metaCurrent, final int state) {
    return ((metaCurrent & BlockSide.ROTATION_MASK) | (state & BlockSide.DATA_MASK));
  }

  /**
   * Creates a block's metadata using the given rotation data and state.
   *
   * @param metaCurrent the block's rotation data.
   * @param state the block's internal state.
   * @return the block's metadata.
   */
  public static int createState(final int rotationData, final int state) {
    return (createRotationData(rotationData) | (state & BlockSide.DATA_MASK));
  }

  public static int createState(final BlockSide blockFacing, final int state) {
    return createState(blockFacing.direction(), state);
  }
}
