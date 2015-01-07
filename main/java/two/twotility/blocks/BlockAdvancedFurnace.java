/*
 */
package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.twotility.tiles.TileAdvancedFurnace;
import two.util.BlockSide;
import two.util.BlockUtil;

/**
 * @author Two
 */
public class BlockAdvancedFurnace extends BlockWithInventory {

  protected static final String CONFIG_KEY_LAVA_FLOW_MAX = "Lava flow search max";
  public static final String NAME = "advancedFurnace";
  protected static final int STATE_EMPTY = 0;
  protected static final int STATE_FILLED = STATE_EMPTY + 1;
  protected static final int STATE_HAS_FUEL = STATE_FILLED + 1;
  protected static final int STATE_WORKING = STATE_HAS_FUEL + 1;
  protected static final int NUM_STATES = STATE_WORKING + 1;
  //-- Class -------------------------------------------------------------------
  public final int lavaFlowSearchMax;
  @SideOnly(Side.CLIENT)
  protected IIcon[] stateIcons;
  @SideOnly(Side.CLIENT)
  protected IIcon iconSide;
  @SideOnly(Side.CLIENT)
  protected IIcon iconTop;

  public BlockAdvancedFurnace() {
    super(Material.rock, TileAdvancedFurnace.class);
    lavaFlowSearchMax = TwoTility.config.getMiscInteger(CONFIG_KEY_LAVA_FLOW_MAX, 128);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, soundTypeStone, 2.5F, BlockUtil.HARVEST_TOOL_PICKAXE, BlockUtil.HARVEST_LEVEL_STONE);
    setResistance(9.0f);
    setLightLevel(100.0f); // in percent

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this, 1),
              " R ",
              "CFC",
              " B ",
              'C', Blocks.chest,
              'F', Blocks.furnace,
              'R', Items.redstone,
              'B', Items.bucket);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister iconRegister) {
    stateIcons = new IIcon[NUM_STATES];
    stateIcons[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    stateIcons[STATE_FILLED] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_filled");
    stateIcons[STATE_HAS_FUEL] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_fuel");
    stateIcons[STATE_WORKING] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_working");

    iconSide = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_side");
    iconTop = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_top");
  }

  @SideOnly(Side.CLIENT)
  protected IIcon getFrontfaceByState(final int state) {
    if ((state >= 0) && (state < stateIcons.length)) {
      return stateIcons[state];
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    final BlockSide rotatedSide = BlockSide.getRotatedSide(side, metadata);
    switch (rotatedSide) {
      case NORTH:
        return getFrontfaceByState(BlockSide.getBlockDataFromMetadata(metadata));
      case TOP:
      case BOTTOM:
        return iconTop;
      default:
        return iconSide;
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = BlockSide.createState(BlockSide.getDirectionFacing(entity), 0);
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
    super.onBlockPlacedBy(world, x, y, z, entity, itemStack);
  }

  public static int updateState(final int metaCurrent, final boolean hasFuel, final boolean hasWork) {
    return BlockSide.updateState(metaCurrent, (hasFuel ? STATE_HAS_FUEL : 0) | (hasWork ? STATE_FILLED : 0));
  }
}
