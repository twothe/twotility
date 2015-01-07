/*
 */
package two.sound;

import cpw.mods.fml.common.FMLLog;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

/**
 * @author Two
 */
public class PaulsCodeDevice implements AudioDevice {

  protected static final ConcurrentHashMap<URL, AudioFormat> audioFormatByURL = new ConcurrentHashMap<URL, AudioFormat>();
  protected static final int BUFFER_SIZE = 1024 * 1024;
  protected static final int STREAM_BUFFER_SIZE = SoundSystemConfig.getStreamingBufferSize();
  protected static final Random random = new Random();
  protected static final AudioFormat noiseFormat = new AudioFormat(44100, 16, 1, true, false);
  /* Static source of noise. Having 4 buffers reduce the repetition you hear with only 1 buffer. */
  protected static final SoundBuffer[] noises = new SoundBuffer[]{generateNoiseBuffer(), generateNoiseBuffer(), generateNoiseBuffer(), generateNoiseBuffer()};

  protected static SoundBuffer generateNoiseBuffer() {
    final byte[] data = new byte[STREAM_BUFFER_SIZE];
    random.nextBytes(data);
    for (int i = 0; i < data.length; ++i) {
      data[i] >>= 4;
    }
    return new SoundBuffer(data, noiseFormat);
  }
  protected final ByteBuffer byteBuffer;
  protected final ShortBuffer shortBuffer;
  protected final LinkedBlockingQueue<SoundBuffer> buffers = new LinkedBlockingQueue<SoundBuffer>();
  protected final URL targetURL;
  protected Decoder decoder;
  protected AudioFormat format = null;
  protected boolean updateFormat = true;

  public PaulsCodeDevice(final URL url) {
    this.targetURL = url;
    this.format = audioFormatByURL.get(targetURL); // last format or null
    FMLLog.info("Guessed audio format for url %s as %s", targetURL.toString(), String.valueOf(format));
    byteBuffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.nativeOrder()); // correct byte order is very important here!
    shortBuffer = byteBuffer.asShortBuffer();
    byteBuffer.limit(0);
  }

  @Override
  public void write(final short[] samples, final int offs, final int len) throws JavaLayerException {
    try {
      if (updateFormat) {
        format = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
        audioFormatByURL.put(targetURL, format);
        FMLLog.info("Updated audio format for url %s as %s", targetURL.toString(), String.valueOf(format));
        updateFormat = false;
      }
      if (len > 0) {
        shortBuffer.put(samples, offs, len);
        byteBuffer.limit(shortBuffer.position() * 2);
        if (byteBuffer.limit() >= STREAM_BUFFER_SIZE) {
          final byte[] data = new byte[STREAM_BUFFER_SIZE];
          byteBuffer.get(data);
          buffers.put(new SoundBuffer(data, format));
          byteBuffer.compact();
          byteBuffer.limit(0);
          shortBuffer.position(shortBuffer.position() - STREAM_BUFFER_SIZE / 2);
        }
      }
    } catch (Exception e) {
      FMLLog.log(Level.WARN, e, "Failed to write audio data: %s", e.getLocalizedMessage());
    }
  }

  @Override
  public void close() {
    this.decoder = null;
  }

  @Override
  public void flush() {
  }

  @Override
  public int getPosition() {
    return 0;
  }

  @Override
  public void open(final Decoder decoder) throws JavaLayerException {
    if (this.decoder == null) {
      this.decoder = decoder;
    }
  }

  @Override
  public boolean isOpen() {
    return (this.decoder != null);
  }

  public AudioFormat getAudioFormat() {
    return format;
  }

  public SoundBuffer take() {
    try {
      final SoundBuffer result = buffers.poll(1, TimeUnit.MILLISECONDS); // 1 second delay for some reason avoids noise on first load
      if (result == null) {
        final SoundBuffer noiseBuffer = noises[random.nextInt(noises.length)];
        return new SoundBuffer(noiseBuffer.audioData, noiseBuffer.audioFormat);
      } else {
        return result;
      }
    } catch (InterruptedException ex) {
      return null;
    }
  }
}
