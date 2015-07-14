package two.twotility.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 *
 * @author Two
 */
public class ItemPowerStorageUpgradeWood extends ItemPowerStorageUpgradeBase {

  public ItemPowerStorageUpgradeWood() {
    super("powerStorageUpgradeWood", 100000);
  }

  @Override
  protected ShapedOreRecipe getRecipe(final ItemStack result) {
    return new ShapedOreRecipe(result,
            "wrw",
            "rRr",
            "wrw",
            'w', "plankWood",
            'r', "dustRedstone",
            'R', "blockRedstone"
    );
  }

}
