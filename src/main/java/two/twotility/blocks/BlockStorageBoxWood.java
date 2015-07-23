/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.twotility.blocks;

import static net.minecraft.block.Block.soundTypeWood;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.tiles.TileStorageBoxWood;
import two.util.BlockUtil;

/**
 *
 * @author Two
 */
public class BlockStorageBoxWood extends BlockWithInventory {

  public static final String NAME = "storageBoxWood";

  public BlockStorageBoxWood() {
    super(Material.wood, TileStorageBoxWood.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize() {
    setBaseValues(NAME, soundTypeWood, 3.0F, BlockUtil.HARVEST_TOOL_AXE, BlockUtil.HARVEST_LEVEL_WOOD);
    this.blockResistance = 20.0f;
    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(this),
              "WSW",
              "S S",
              "WSW",
              'W', "plankWood",
              'S', "stickWood"
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
