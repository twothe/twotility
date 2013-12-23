/*
 */
package two.twotility;

import java.util.ArrayList;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.blocks.BlockLavaTank;
import two.twotility.items.ItemLavaTank;
import two.twotility.items.ItemPouchSmall;

/**
 * @author Two
 */
public class ProxyBase {

  /* Items */
  public ItemLavaTank itemLavaTank;
  public ItemPouchSmall itemPouchSmall;
  /* Blocks */
  public BlockAdvancedFurnace blockAdvancedFurnace;
  public BlockLavaTank blockLavaTank;
  /* Sound */
  public final String SOUND_FLUIDSUCKIN = TwoTility.getSoundName("fluidsuckin");
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();

  public ProxyBase() {
  }

  protected void registerBlocks() {
    blockAdvancedFurnace = new BlockAdvancedFurnace();
    pendingInitialization.add(blockAdvancedFurnace);

    blockLavaTank = new BlockLavaTank();
    pendingInitialization.add(blockLavaTank);
  }

  protected void registerItems() {
    itemLavaTank = new ItemLavaTank(blockLavaTank);
    pendingInitialization.add(itemLavaTank);
    
    itemPouchSmall = new ItemPouchSmall();
    pendingInitialization.add(itemPouchSmall);
  }

  protected void registerRenderers() {
  }

  public void onPreInit() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void onInit() {
    registerBlocks();
    registerItems();
    registerRenderers();

    for (final InitializableModContent content : pendingInitialization) {
      content.initialize();
    }
    pendingInitialization.clear();
  }

  public void onPostInit() {
  }

  @ForgeSubscribe
  public void onSoundSetup(final SoundLoadEvent event) {
    event.manager.addSound(SOUND_FLUIDSUCKIN + ".ogg");
  }
}
