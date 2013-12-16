/*
 */
package two.twotility;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/**
 * @author Two
 */
public class Config {

  public static final AtomicInteger blockIDs = new AtomicInteger(500);
  public static final AtomicInteger itemIDs = new AtomicInteger(5000);
  protected static Configuration configuration;

  public static int getBlockID(final Class<? extends Block> block) {
    final String className = block.getSimpleName();
    final String key = className.startsWith("Block") ? className.substring("Block".length()) : className;
    final int defaultID = blockIDs.getAndIncrement();
    final Property property = configuration.getBlock(key, defaultID);
    return property.getInt(defaultID);
  }

  public static int getItemID(final Class<? extends Item> item) {
    final String className = item.getSimpleName();
    final String key = className.startsWith("Item") ? className.substring("Item".length()) : className;
    final int defaultID = itemIDs.getAndIncrement();
    final Property property = configuration.getItem(key, defaultID);
    return property.getInt(defaultID);
  }

  public static boolean isCraftingEnabled(final String key) {
    return isCraftingEnabled(key, true);
  }

  public static boolean isCraftingEnabled(final String key, final boolean defaultValue) {
    final Property property = configuration.get("Allowed Recipes", key, defaultValue);
    return property.getBoolean(defaultValue);
  }
}
