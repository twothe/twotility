/*
 */
package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.tiles.TileShelf;
import two.util.BlockSide;
import two.util.BlockUtil;

/**
 * @author Two
 */
public class BlockShelf extends BlockWithInventory {

  public static final String NAME = "shelf";
  public static final int STATE_EMPTY = 0;
  public static final int STATE_2_4 = STATE_EMPTY + 1;
  public static final int STATE_3_4 = STATE_2_4 + 1;
  public static final int STATE_FULL = STATE_3_4 + 1;
  public static final int NUM_STATES = STATE_FULL + 1;
//-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected IIcon[] stateIcons;
  @SideOnly(Side.CLIENT)
  protected IIcon iconDefault;

  public BlockShelf() {
    super(Material.wood, TileShelf.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize() {
    setBaseValues(NAME, soundTypeWood, 1.5F, BlockUtil.HARVEST_TOOL_AXE, BlockUtil.HARVEST_LEVEL_WOOD);

    OreDictionary.registerOre("shelfWood", this);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(this),
              "WWW",
              "S S",
              "WWW",
              'W', "plankWood",
              'S', "stickWood"
      ));
    }

  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister iconRegister) {
    stateIcons = new IIcon[NUM_STATES];
    iconDefault = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    stateIcons[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_empty");
    stateIcons[STATE_2_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_2_4");
    stateIcons[STATE_3_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_3_4");
    stateIcons[STATE_FULL] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_full");
  }

  @SideOnly(Side.CLIENT)
  protected IIcon getSideIconByState(final int state) {
    if ((state >= 0) && (state < stateIcons.length)) {
      return stateIcons[state];
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    switch (BlockSide.getRotatedSide(side, metadata)) {
      case NORTH:
        return getSideIconByState(BlockSide.getBlockDataFromMetadata(metadata));
      default:
        return iconDefault;
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final BlockSide blockFacing = getValidPlacementDirection(world, x, y, z, entity);
    world.setBlockMetadataWithNotify(x, y, z, BlockSide.createState(blockFacing, 0), 2);
    super.onBlockPlacedBy(world, x, y, z, entity, itemStack);
  }

  protected BlockSide getValidPlacementDirection(final World world, final int x, final int y, final int z, final EntityLivingBase entity) {
    final BlockSide facingPlayer = BlockSide.getSideFacing(entity);
    ForgeDirection dir = facingPlayer.behind();
    if (world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer;
    }
    dir = facingPlayer.right();
    if (world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer.leftSide();
    }
    dir = facingPlayer.left();
    if (world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer.rightSide();
    }
    dir = facingPlayer.infront();
    if (world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer.backSide();
    }
    return facingPlayer;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public void addCollisionBoxesToList(final World world, final int x, final int y, final int z, final AxisAlignedBB boundingBox, final List list, final Entity entity) {
    this.setBlockBoundsBasedOnState(world, x, y, z);
    super.addCollisionBoxesToList(world, x, y, z, boundingBox, list, entity);
  }

  @Override
  public void setBlockBoundsBasedOnState(final IBlockAccess world, final int x, final int y, final int z) {
    final BlockSide facing = BlockSide.fromMetadata(world.getBlockMetadata(x, y, z));
    switch (facing) {
      case NORTH:
        this.setBlockBounds(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);
        break;
      case EAST:
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);
        break;
      case SOUTH:
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);
        break;
      case WEST:
        this.setBlockBounds(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        break;
    }
  }

  @Override
  public void setBlockBoundsForItemRender() {
    this.setBlockBounds(0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F);
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }
}
