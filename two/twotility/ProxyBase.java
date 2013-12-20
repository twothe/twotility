/*
 */
package two.twotility;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.util.ArrayList;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
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
  /* Sound */
  public final String SOUND_FLUIDSUCKIN = TwoTility.getSoundName("fluidsuckin");
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();

  public ProxyBase() {
  }

  public void onPreInit(final FMLPreInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
    blockAdvancedFurnace = new BlockAdvancedFurnace();
    blockLavaTank = new BlockLavaTank();

    pendingInitialization.add(blockAdvancedFurnace);
    pendingInitialization.add(blockLavaTank);
  }

  public void onInit(final FMLInitializationEvent event) {
    for (final InitializableModContent content : pendingInitialization) {
      content.initialize();
    }
    pendingInitialization.clear();
  }

  public void onPostInit(final FMLPostInitializationEvent event) {
  }

  @ForgeSubscribe
  public void onSoundSetup(final SoundLoadEvent event) {
    Logging.logMethodEntry("ProxyClient", "onSoundSetup", "...");
    event.manager.addSound(SOUND_FLUIDSUCKIN + ".ogg");
  }
}
