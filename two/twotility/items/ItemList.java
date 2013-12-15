/*
 */

package two.twotility.items;

/**
 * @author Two
 */
public class ItemList {
  
  public static ItemLavaTank lavaTank;
  
  public static void initialize() {
    // the order here determines the IDs!
    lavaTank = new ItemLavaTank().initialize();
  }
}
