package two.twotility.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 *
 * @author Two
 */
public class ItemPowerStorageUpgradePotato extends ItemPowerStorageUpgradeBase {

  public ItemPowerStorageUpgradePotato() {
    super("powerStorageUpgradePotato", 10000);
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

}
