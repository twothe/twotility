/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.net.URL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import two.twotility.blocks.BlockStreamingRadio;
import two.twotility.container.ContainerBase;

/**
 * @author Two
 */
public class TileStreamingRadio extends TileWithInventory {

  protected final static int[] ACCESSIBLE_SLOTS = new int[0];
  protected String soundName = null;
  protected URL url = null;

  public TileStreamingRadio() {
    super(1);
  }

  public void startStreaming() {
//    try {
//      final SoundSystem soundSystem = Minecraft.getMinecraft().sndManager.sndSystem;
////      final String urlString = "http://50.7.77.114:8082/stream"; // Radio Free Gaia
////      final String urlString = "http://arstwo.de/zeug/test.mp3"; // mp3
////      final String urlString = "http://www.signalogic.com/melp/EngSamples/Orig/male.wav"; // 8-bit mono wav
////      final String urlString = "http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/Samples/AFsp/M1F1-int16-AFsp.wav"; // 16-bit stereo wav
////      final String urlString = "https://upload.wikimedia.org/wikipedia/en/2/2e/Countdown%28example%29.ogg"; // ogg
////      final String urlString = "http://pub8.di.fm:80/di_trance?4b703927dca10221af93699e"; // mp3 stream, 44100, 16B stereo
////      final String urlString = "http://www.humpa.com/images/uniphone_mono.mp3"; // mp3 mono
//      final String urlString = "http://dradio_mp3_dokdeb_s.akacast.akamaistream.net/7/725/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dokdeb_s"; // mp3 stream mono
//
//      final String soundIdent = getSoundIdentityName(urlString);
//      soundSystem.newStreamingSource(true, getSoundName(), new URL(urlString), soundIdent, false, xCoord, yCoord, zCoord, SoundSystemConfig.ATTENUATION_LINEAR, 48.0f);
//      soundSystem.play(getSoundName());
//      FMLLog.info("Started streaming as %s", getSoundName());
//    } catch (Exception ex) {
//      FMLLog.log(Level.ERROR, "Failed to play stream", ex);
//    }
  }

  protected String getSoundIdentityName(final String urlString) {
    final String lowerString = urlString.toLowerCase();
    if (lowerString.endsWith(".mp3") || lowerString.endsWith(".ogg") || lowerString.endsWith(".wav")) {
      return urlString;
    } else {
      return urlString + ".mp3";
    }
  }

  @Override
  public void invalidate() {
    stopStreaming();
    super.invalidate();
  }

  public void stopStreaming() {
//    final SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
//    soundHandler.stop(getSoundName());
//    FMLLog.info("Stopped streaming of %s", getSoundName());
  }

  public String getSoundName() {
    if (soundName == null) {
      soundName = BlockStreamingRadio.NAME + "," + xCoord + "," + yCoord + "," + zCoord;
    }
    return soundName;
  }

  @Override
  public ContainerBase createContainer(EntityPlayer player) {
    throw new UnsupportedOperationException("Not supported yet."); // TODO: implement
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(EntityPlayer player) {
    throw new UnsupportedOperationException("Not supported yet."); // TODO: implement
  }

  @Override
  public int[] getAccessibleSlotsFromSide(int var1) {
    return ACCESSIBLE_SLOTS;
  }

  @Override
  public String getInventoryName() {
    return BlockStreamingRadio.NAME;
  }
}
