/*
 */
package two.sound;

import cpw.mods.fml.common.FMLLog;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.Player;
import org.apache.logging.log4j.Level;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public class StreamPlayer {

  protected static class StreamRunnable implements Runnable {

    final static int STATE_CREATING = 0;
    final static int STATE_RUNNING = STATE_CREATING + 1;
    final static int STATE_STOPPED = STATE_RUNNING + 1;
    final URL url;
    final PaulsCodeDevice device;
    Player player = null;
    final AtomicInteger runState = new AtomicInteger(STATE_CREATING);

    public StreamRunnable(final URL url, final PaulsCodeDevice device) {
      this.url = url;
      this.device = device;
      if (this.device.getAudioFormat() == null) { // PaulsCode requires the AudioFormat to be known. Must stop the game here till it is received. Device uses internal caching to avoid this if possible.
        try {
          player = new Player(url.openStream(), device);
          player.play(1); // preload audio format
        } catch (Exception ex) {
          FMLLog.log(TwoTility.MOD_ID, Level.ERROR, "Failed to initialize player (%s): %s", ex.getClass().getSimpleName(), ex.getMessage());
          runState.set(STATE_STOPPED);
        }
      }
    }

    @Override
    public void run() {
      try {
        if (player == null) {
          player = new Player(url.openStream(), device);
        }
        try {
          if (runState.compareAndSet(STATE_CREATING, STATE_RUNNING)) {
            player.play();
          }
        } catch (Exception e) {
          FMLLog.log(TwoTility.MOD_ID, Level.ERROR, "Failed to play (%s): %s", e.getClass().getSimpleName(), e.getMessage());
        } finally {
          player.close();
        }
      } catch (Exception ex) {
        FMLLog.log(TwoTility.MOD_ID, Level.ERROR, "Failed to initialize player (%s): %s", ex.getClass().getSimpleName(), ex.getMessage());
        runState.set(STATE_STOPPED);
      }
    }

    public void stop() {
      if (!runState.compareAndSet(STATE_CREATING, STATE_STOPPED)) {
        if (runState.compareAndSet(STATE_RUNNING, STATE_STOPPED)) {
          player.close();
        }
      }
    }
  }
  protected final AudioDevice device;
  protected final StreamRunnable runnable;
  protected Thread playbackThread = null;

  public StreamPlayer(final String urlString, final PaulsCodeDevice device) throws MalformedURLException, IOException, JavaLayerException {
    this(new URL(urlString), device);
  }

  public StreamPlayer(final URL url, final PaulsCodeDevice device) throws IOException, JavaLayerException {
    this.device = device;
    this.runnable = new StreamRunnable(url, device);
    this.playbackThread = new Thread(runnable);
    this.playbackThread.setPriority(Thread.NORM_PRIORITY + 1);
    this.playbackThread.setName("StreamPlayer:" + this.playbackThread.getId());
    this.playbackThread.setDaemon(true);
  }

  public void start() {
    if (this.playbackThread != null) {
      this.playbackThread.start();
    }
  }

  public void stop() {
    if (this.playbackThread != null) {
      this.runnable.stop();
      this.playbackThread = null;
    }
  }

  public boolean isPlaying() {
    if (this.runnable.player == null) {
      return false;
    } else {
      return (this.runnable.player.isComplete() == false);
    }
  }

  public boolean isComplete() {
    if (this.runnable.player == null) {
      return false;
    } else {
      return this.runnable.player.isComplete();
    }
  }
}
