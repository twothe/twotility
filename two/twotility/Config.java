/*
 */
package two.twotility;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/**
 * @author Two
 */
public class Config {

  protected static final String CATEGORY_VARIOUS_SETTINGS = "other";
  //--- Class ------------------------------------------------------------------
  public static final AtomicInteger blockIDs = new AtomicInteger(745);
  public static final AtomicInteger itemIDs = new AtomicInteger(6928);
  //--- Misc config settings ---------------------------------------------------
  public int LAVA_FLOW_FOLLOW_MAX = 128;
  protected Configuration configuration;

  protected Config() {
  }

  protected void readOther() {
    LAVA_FLOW_FOLLOW_MAX = configuration.get(CATEGORY_VARIOUS_SETTINGS, "Lava flow follow max", LAVA_FLOW_FOLLOW_MAX).getInt(LAVA_FLOW_FOLLOW_MAX);
  }

  protected void initialize(final File configFile) {
    configuration = new Configuration(configFile);
  }

  protected void load() {
    configuration.load();
  }

  protected void save() {
    configuration.save();
  }

  public int getBlockID(final Class<? extends Block> block) {
    final String className = block.getSimpleName();
    final String key = className.startsWith("Block") ? className.substring("Block".length()) : className;
    final int defaultID = blockIDs.getAndIncrement();
    final Property property = configuration.getBlock(key, defaultID);
    return property.getInt(defaultID);
  }

  public int getItemID(final Class<? extends Item> item) {
    final String className = item.getSimpleName();
    final String key = className.startsWith("Item") ? className.substring("Item".length()) : className;
    final int defaultID = itemIDs.getAndIncrement();
    final Property property = configuration.getItem(key, defaultID);
    return property.getInt(defaultID);
  }

  public boolean isCraftingEnabled(final String key) {
    return isCraftingEnabled(key, true);
  }

  public boolean isCraftingEnabled(final String key, final boolean defaultValue) {
    final Property property = configuration.get("Allowed Recipes", key, defaultValue);
    return property.getBoolean(defaultValue);
  }

  public int getMiscInteger(final String key, final int defaultValue) {
    final Property property = configuration.getItem(key, defaultValue);
    return property.getInt(defaultValue);
  }
}
