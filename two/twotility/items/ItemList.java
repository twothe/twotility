/*
 */

package two.twotility.items;

/**
 * @author Two
 */
public class ItemList {
  
  public static ItemLavaForge lavaForge;
  
  public static void initialize() {
    // the order here determines the IDs!
    lavaForge = new ItemLavaForge().initialize();
  }
}
