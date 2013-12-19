/*
 */
package two.twotility;

import java.util.ArrayList;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.blocks.BlockLavaTank;
import two.util.Logging;

/**
 * @author Two
 */
public class ProxyBase {

  /* Blocks */
  public BlockAdvancedFurnace blockAdvancedFurnace;
  public BlockLavaTank blockLavaTank;
  /* Items */
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();

  public ProxyBase() {
    Logging.logMethodEntry("ProxyBase", "Constructor");
  }

  protected void createContent() {
    blockAdvancedFurnace = new BlockAdvancedFurnace();
    pendingInitialization.add(blockAdvancedFurnace);
    blockLavaTank = new BlockLavaTank();
    pendingInitialization.add(blockLavaTank);
  }

  protected void initializeContent() {
    for (final InitializableModContent content : pendingInitialization) {
      content.initialize();
    }
    pendingInitialization.clear();
  }

  protected void onPreInit() {
    createContent();
  }

  protected void onInit() {
    initializeContent();
  }

  protected void onPostInit() {
  }
}
