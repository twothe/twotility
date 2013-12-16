/*
 */
package two.util;

import cpw.mods.fml.common.FMLLog;
import java.util.logging.Level;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public class Logging {

  public static String methodParametersToString(final Object[] params) {
    if ((params == null) || (params.length == 0)) {
      return "";
    }

    final int iMax = params.length - 1;
    final StringBuilder result = new StringBuilder();
    for (int i = 0;; i++) {
      result.append(String.valueOf(params[i]));
      if (i == iMax) {
        return result.toString();
      }
      result.append(", ");
    }
  }

  public static void logMethodEntry(final String className, final String methodName, final Object... params) {
    FMLLog.log(TwoTility.MOD_ID, Level.INFO, "[Entered] %s.%s(%s)", className, methodName, methodParametersToString(params));
  }
}
