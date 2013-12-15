/*
 */
package two.twotility.blocks;

/**
 * @author Two
 */
public class BlockList {

  public static BlockAdvancedFurnace advancedFurnace;
  public static BlockSideTest sideTest;
  public static BlockLavaTank lavaTank;

  public static void initialize() {
    // the order here determines the IDs!
    advancedFurnace = new BlockAdvancedFurnace().initialize();
//    sideTest = new BlockSideTest().initialize();
    lavaTank = new BlockLavaTank().initialize();
  }
}
