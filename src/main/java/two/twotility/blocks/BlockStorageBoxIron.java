/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.twotility.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.TwoTilityAssets;
import two.twotility.tiles.TileStorageBoxIron;
import two.util.BlockUtil;

/**
 *
 * @author Two
 */
public class BlockStorageBoxIron extends BlockWithInventory {

  public static final String NAME = "storageBoxIron";

  public BlockStorageBoxIron() {
    super(Material.iron, TileStorageBoxIron.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize() {
    setBaseValues(NAME, soundTypeMetal, 3.0F, BlockUtil.HARVEST_TOOL_PICKAXE, BlockUtil.HARVEST_LEVEL_STONE);
    this.blockResistance = 40.0f;
    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(this),
              "III",
              "ISI",
              "III",
              'I', "ingotIron",
              'S', TwoTilityAssets.blockStorageBoxWood
      ));
    }
  }
}
