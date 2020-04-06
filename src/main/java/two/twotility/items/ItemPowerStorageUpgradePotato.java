package two.twotility.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 *
 * @author Two
 */
public class ItemPowerStorageUpgradePotato extends ItemPowerStorageUpgradeBase {

  protected static final int POWER_CAPACITY = 10000;

  public ItemPowerStorageUpgradePotato() {
    super("powerStorageUpgradePotato", POWER_CAPACITY);
  }

  @Override
  protected ShapedOreRecipe getRecipe(final ItemStack result) {
    return new ShapedOreRecipe(result,
            "srs",
            "rPr",
            "srs",
            's', "stickWood",
            'r', "dustRedstone",
            'P', "cropPotato"
    );
  }

  @Override
  protected int getCraftedEnergy() {
    return POWER_CAPACITY;
  }

}
