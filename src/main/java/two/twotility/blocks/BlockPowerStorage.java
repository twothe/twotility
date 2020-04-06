package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.TwoTilityAssets;
import two.twotility.tiles.TilePowerStorage;
import two.twotility.util.BlockUtil;

/**
 *
 * @author Two
 */
public class BlockPowerStorage extends BlockWithInventory {

  public static final String NAME = "powerStorage";
//-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected IIcon iconDefault;

  public BlockPowerStorage() {
    super(Material.iron, TilePowerStorage.class);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, soundTypeMetal, 1.5F, BlockUtil.HARVEST_TOOL_PICKAXE, BlockUtil.HARVEST_LEVEL_IRON);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(this),
              "IrI",
              "rSr",
              "IrI",
              'I', "ingotIron",
              'r', "dustRedstone",
              'S', new ItemStack(TwoTilityAssets.blockShelf)
      ));
    }

  }
}
