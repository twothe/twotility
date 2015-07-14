/*
 */
package two.util;

import java.util.Objects;
import net.minecraft.util.Vec3;

/**
 * @author Two
 */
public class Vector3d extends Vec3 {

  public Vector3d() {
    this(0.0, 0.0, 0.0);
  }

  public Vector3d(final Vec3 other) {
    this(other.xCoord, other.yCoord, other.zCoord);
  }

  public Vector3d(final double x, final double y, final double z) {
    super(x, y, z);
  }

  public Vector3d set(final Vec3 other) {
    return set(other.xCoord, other.yCoord, other.zCoord);
  }

  public Vector3d set(final double x, final double y, final double z) {
    return (Vector3d) setComponents(x, y, z);
  }

  public Vec3 add(final Vec3 other) {
    return add(other.xCoord, other.yCoord, other.zCoord);
  }

  public Vec3 add(final double x, final double y, final double z) {
    xCoord += x;
    yCoord += y;
    zCoord += z;
    return this;
  }

  public Vec3 sub(final Vec3 other) {
    return sub(other.xCoord, other.yCoord, other.zCoord);
  }

  public Vec3 sub(final double x, final double y, final double z) {
    xCoord -= x;
    yCoord -= y;
    zCoord -= z;
    return this;
  }

  public Vec3 setSub(final Vec3 from, final Vec3 to) {
    return setSub(from.xCoord, from.yCoord, from.zCoord, to.xCoord, to.yCoord, to.zCoord);
  }

  public Vec3 setSub(final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ) {
    xCoord = toX - fromX;
    yCoord = toY - fromY;
    zCoord = toZ - fromZ;
    return this;
  }

  public Vec3 cross(final Vec3 other) {
    this.xCoord = this.yCoord * other.zCoord - this.zCoord * other.yCoord;
    this.yCoord = this.zCoord * other.xCoord - this.xCoord * other.zCoord;
    this.zCoord = this.xCoord * other.yCoord - this.yCoord * other.xCoord;
    return this;
  }

  public double distanceToBlock(final double blockX, final double blockY, final double blockZ) {
    return distanceTo(blockX + 0.5, blockY + 0.5, blockZ + 0.5);
  }

  public double distanceTo(final double x, final double y, final double z) {
    final double distX = x - this.xCoord;
    final double distY = y - this.yCoord;
    final double distZ = z - this.zCoord;
    return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
  }

  public Vector3d copy() {
    return new Vector3d(this);
  }

  @Override
  public String toString() {
    return String.format("(%+8.3f, %+8.3f, %+8.3f)", this.xCoord, this.yCoord, this.zCoord);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xCoord, yCoord, zCoord);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj.getClass().isAssignableFrom(this.getClass())) {
      final Vec3 other = (Vec3) obj;
      return ((this.xCoord == other.xCoord) && (this.yCoord == other.yCoord) && (this.zCoord == other.zCoord));
    }
    return false;
  }
}
