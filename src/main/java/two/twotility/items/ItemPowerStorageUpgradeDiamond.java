package two.twotility.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 *
 * @author Two
 */
public class ItemPowerStorageUpgradeDiamond extends ItemPowerStorageUpgradeBase {

  public ItemPowerStorageUpgradeDiamond() {
    super("powerStorageUpgradeDiamond", 5000000);
  }

  @Override
  protected ShapedOreRecipe getRecipe(final ItemStack result) {
    return new ShapedOreRecipe(result,
            "drd",
            "rRr",
            "drd",
            'd', "gemDiamond",
            'r', "dustRedstone",
            'R', "blockRedstone"
    );
  }

}
