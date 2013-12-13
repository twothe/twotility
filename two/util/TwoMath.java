/*
 */
package two.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

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

}
