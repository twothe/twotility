/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.twotility.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
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

  @Override
  public boolean onBlockActivated(final World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
    if (world.isRemote == false) {
      world.playSoundEffect(x, (double) y + 0.5D, z, "random.chestopen", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
    }
    return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
  }
}
