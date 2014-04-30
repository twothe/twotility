/*
 */
package two.twotility;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import java.util.logging.Level;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import two.sound.StreamCodec;
import two.twotility.entities.EntityTNTStick;
import two.twotility.renderers.ItemRendererBlock3d;

/**
 * @author Two
 */
public class ProxyClient extends ProxyBase {

  protected ItemRendererBlock3d itemRendererBlock3d;

  @Override
  protected void registerRenderers() {
    super.registerRenderers();

    itemRendererBlock3d = new ItemRendererBlock3d();
    MinecraftForgeClient.registerItemRenderer(itemLavaTank.itemID, itemRendererBlock3d);
    MinecraftForgeClient.registerItemRenderer(itemCraftingBox.itemID, itemRendererBlock3d);
    RenderingRegistry.registerEntityRenderingHandler(EntityTNTStick.class, new RenderSnowball(itemTNTStick));
  }

  @ForgeSubscribe
  public void onSoundSetup(final SoundLoadEvent event) {
    try {
      SoundSystemConfig.setCodec("mp3", StreamCodec.class);
      SoundSystemConfig.setCodec("aac", StreamCodec.class);
      FMLLog.log(Level.INFO, "MP3/AAC codec loaded");
    } catch (SoundSystemException ex) {
      FMLLog.log(Level.SEVERE, "Unable to load MP3/AAC codec", ex);
    }
    event.manager.addSound(SOUND_FLUIDSUCKIN + ".ogg");
  }
}
