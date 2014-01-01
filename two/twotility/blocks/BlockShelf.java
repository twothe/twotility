/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import two.twotility.TwoTility;
import two.twotility.tiles.TileShelf;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockShelf extends BlockWithInventory {

  public static final String NAME = "Shelf";
  public static final int STATE_EMPTY = 0;
  public static final int STATE_2_4 = STATE_EMPTY + 1;
  public static final int STATE_3_4 = STATE_2_4 + 1;
  public static final int STATE_FULL = STATE_3_4 + 1;
  public static final int NUM_STATES = STATE_FULL + 1;
//-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected Icon[] stateIcons = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon iconDefault;

  public BlockShelf() {
    super(TwoTility.config.getBlockID(BlockShelf.class), Material.wood, TileShelf.class);
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
  }

  @Override
  public void initialize() {
    setHardness(Block.bookShelf.blockHardness);
    setStepSound(soundWoodFootstep);
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "axe", 0);
    LanguageRegistry.addName(this, "Shelf");
    OreDictionary.registerOre("Shelf", this);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              "WWW",
              "S S",
              "WWW",
              'W', Block.planks,
              'S', Item.stick);
    }

  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    iconDefault = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    stateIcons[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_empty");
    stateIcons[STATE_2_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_2_4");
    stateIcons[STATE_3_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_3_4");
    stateIcons[STATE_FULL] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_full");
  }

  protected Icon getSideIconByState(final int state) {
    if ((state >= 0) && (state < stateIcons.length)) {
      return stateIcons[state];
    } else {
      FMLLog.warning("Illegal front-face state #%d for %s.", state, this.getClass().getSimpleName());
      return stateIcons[0];
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
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
    super.onBlockPlacedBy(world, z, x, y, entity, itemStack);
  }

  protected BlockSide getValidPlacementDirection(final World world, final int x, final int y, final int z, final EntityLivingBase entity) {
    final BlockSide facingPlayer = BlockSide.getSideFacing(entity);
    ForgeDirection dir = facingPlayer.behind();
    if (world.isBlockSolidOnSide(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer;
    }
    dir = facingPlayer.right();
    if (world.isBlockSolidOnSide(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer.leftSide();
    }
    dir = facingPlayer.left();
    if (world.isBlockSolidOnSide(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
      return facingPlayer.rightSide();
    }
    dir = facingPlayer.infront();
    if (world.isBlockSolidOnSide(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
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
