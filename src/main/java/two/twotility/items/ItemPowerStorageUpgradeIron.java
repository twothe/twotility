package two.twotility.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 *
 * @author Two
 */
public class ItemPowerStorageUpgradeIron extends ItemPowerStorageUpgradeBase {

  public ItemPowerStorageUpgradeIron() {
    super("powerStorageUpgradeIron", 1000000);
  }

  @Override
  protected ShapedOreRecipe getRecipe(final ItemStack result) {
    return new ShapedOreRecipe(result,
            "iri",
            "rRr",
            "iri",
            'i', "ingotIron",
            'r', "dustRedstone",
            'R', "blockRedstone"
    );
  }

}
