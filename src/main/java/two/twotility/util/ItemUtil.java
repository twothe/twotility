/*
 */
package two.twotility.util;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

/**
 * @author Two
 */
public class ItemUtil {

  public static final FMLControlledNamespacedRegistry<Item> registry = GameData.getItemRegistry();

  public static boolean isSameItem(final ItemStack itemStack, final ItemStack other) {
    if (itemStack == other) {
      return true;
    }
    if (itemStack == null) {
      return false;
    }
    return (itemStack.isItemEqual(other) && ItemStack.areItemStackTagsEqual(itemStack, other)); // intenionally no check for stack size
  }

  public static boolean isSameItem(final Item item, final Item other) {
    return (item == other); // is this enough?
  }

  public static boolean isStackHolding(final ItemStack itemStack, final Item item) {
    return ((itemStack != null) && isSameItem(itemStack.getItem(), item) && (itemStack.hasTagCompound() == false));
  }

  public static boolean isSameBaseType(final ItemStack itemStack, final Item item) {
    return ((itemStack != null) && isSameItem(itemStack.getItem(), item));
  }

  public static Item findByName(final String name) {
    return registry.getObject(name);
  }

  /**
   * Fetches the smelting result for a given ingredient.
   * Convenience function that handles all kinds of null checks.
   *
   * @param ingredients the ingredient to smelt.
   * @return the smelting result or null if the ingredient cannot be smelted into anything.
   */
  public static ItemStack getSmeltingResult(final ItemStack ingredients) {
    if ((ingredients == null) || (ingredients.getItem() == null)) {
      return null;
    }
    final ItemStack smeltingResult = FurnaceRecipes.smelting().getSmeltingResult(ingredients);
    if ((smeltingResult == null) || (smeltingResult.getItem() == null)) {
      return null;
    }
    return smeltingResult.copy();
  }

  /**
   * Convenience function to check if a given item can be smelted at all.
   *
   * @param ingredients the ingredient to smelt.
   * @return true if the item can be smelted in any form of furnace, false otherwise.
   */
  public static boolean canSmelt(final ItemStack ingredients) {
    if ((ingredients == null) || (ingredients.getItem() == null)) {
      return false;
    }
    final ItemStack smeltingResult = FurnaceRecipes.smelting().getSmeltingResult(ingredients);
    if ((smeltingResult == null) || (smeltingResult.getItem() == null)) {
      return false;
    }
    return true;
  }

  protected static final Map<String, String> tooltipCache = new HashMap<String, String>();

  @SideOnly(Side.CLIENT)
  public static String getCachedTooltip(final String key) {
    String result = tooltipCache.get(key);
    if (result == null) {
      result = LanguageRegistry.instance().getStringLocalization(key);
        if (result == null) {
          result = ""; // this prevents further lookups
      }
      tooltipCache.put(key, result);
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @SideOnly(Side.CLIENT)
  public static void clearCachedTooltips() {
    tooltipCache.clear();
  }

  public static int energyAmountToDamagePercent(final int amount, final int capacity) {
    final int percent = (amount * 100) / capacity;
    return TwoMath.withinBounds(100 - percent, 0, 100);
  }

  public static boolean isRFPoweredItem(final ItemStack itemStack) {
    return (itemStack != null) && isPoweredItem(itemStack.getItem());
  }

  public static boolean isPoweredItem(final Item item) {
    return (item instanceof IEnergyContainerItem);
  }

  protected static final DecimalFormat energyFormat = new DecimalFormat("#,##0");

  public static String energyValueToString(final long energy) {
    return energyFormat.format(energy);
  }
}
