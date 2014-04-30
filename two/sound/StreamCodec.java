/*
 */
package two.sound;

import java.net.URL;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import two.util.Logging;

/**
 * Wrapper class to translate between javazoom and paulscode
 *
 * @author Two
 */
public class StreamCodec implements ICodec {

  protected PaulsCodeDevice device = null;
  protected StreamPlayer player = null;
  protected boolean reverseBytes = false;

  @Override
  public void reverseByteOrder(boolean doReverse) {
    reverseBytes = true;
  }

  @Override
  public boolean initialize(final URL url) {
    Logging.logMethodEntry("StreamCodec", "initialize", url);
    try {
      if (player != null) {
        player.stop();
      }
      device = new PaulsCodeDevice(url);
      player = new StreamPlayer(url, device);
      player.start();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public boolean initialized() {
    Logging.logMethodEntry("StreamCodec", "initialized");
    return player.isPlaying();
  }

  @Override
  public SoundBuffer read() {
    if (Thread.currentThread().isInterrupted()) {
      Thread.interrupted(); // why ever paulscode is doing that, it interferes with the take
    }
    final SoundBuffer result = device.take();
    return result;
  }

  @Override
  public SoundBuffer readAll() {
    return read();
  }

  @Override
  public boolean endOfStream() {
    return player.isComplete();
  }

  @Override
  public void cleanup() {
    if (player != null) {
      player.stop();
      player = null;
    }
  }

  @Override
  public AudioFormat getAudioFormat() {
    Logging.logMethodEntry("StreamCodec", "getAudioFormat");
    if (device == null) {
      return null;
    } else {
      return device.getAudioFormat();
    }
  }
}
