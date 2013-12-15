/*
 */
package two.twotility.blocks;

/**
 * @author Two
 */
public class BlockList {

  public static BlockLavaFurnace lavaForge;
  public static BlockSideTest sideTest;
  public static BlockLavaTank lavaTank;

  public static void initialize() {
    // the order here determines the IDs!
    lavaForge = new BlockLavaFurnace().initialize();
//    sideTest = new BlockSideTest().initialize();
    lavaTank = new BlockLavaTank().initialize();
  }
}
