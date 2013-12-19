/*
 */
package two.twotility;

import java.util.ArrayList;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.blocks.BlockLavaTank;

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
  }

  protected void createContent() {
    blockAdvancedFurnace = new BlockAdvancedFurnace();
    blockLavaTank = new BlockLavaTank();

    pendingInitialization.add(blockAdvancedFurnace);
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
