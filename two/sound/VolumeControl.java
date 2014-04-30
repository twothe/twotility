/*
 */
package two.sound;

import javax.sound.sampled.FloatControl;

/**
 * @author Two
 */
public class VolumeControl {

  protected final FloatControl gainDB;
  protected final float minDB, maxDB;

  public VolumeControl(final FloatControl gainDB) {
    this.gainDB = gainDB;
    this.minDB = gainDB.getMinimum();
    this.maxDB = gainDB.getMaximum();
  }

  /**
   * Sets a scaled volume, were 0 - 1 maps to -30 - 0 db.
   * Note: scaledVolume can be outside 0-1.
   * @param scaledVolume a scaled volume.
   */
  public void setVolume(final double scaledVolume) {
    setVolumeDB((float) (-30.0 * (1.0 - scaledVolume)));
  }

  public void setVolumeDB(final float volumeDB) {
    if (volumeDB <= this.minDB) {
      this.gainDB.setValue(this.minDB);
    } else if (volumeDB >= this.maxDB) {
      this.gainDB.setValue(this.maxDB);
    } else {
      this.gainDB.setValue(volumeDB);
    }
  }
}
