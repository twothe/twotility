/*
 */
package two.twotility.util;

/**
 * @author Two
 */
public class TwoMath {

  public static int withinBounds(final int value, final int lowerBound, final int upperBound) {
    if (value < lowerBound) {
      return lowerBound;
    } else if (value > upperBound) {
      return upperBound;
    } else {
      return value;
    }
  }

  public static double distance(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1) {
    final double xDist = Math.abs(x1 - x0);
    final double yDist = Math.abs(y1 - y0);
    final double zDist = Math.abs(z1 - z0);
    return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
  }

  public static double noLessThanOne(final double value) {
    if ((value > -1.0) && (value < 1.0)) {
      if (value >= 0.0) {
        return 1.0;
      } else {
        return -1.0;
      }
    } else {
      return value;
    }
  }

}
