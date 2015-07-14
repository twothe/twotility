/*
 */
package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import static net.minecraft.block.Block.soundTypeWood;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.TwoTilityAssets;
import two.twotility.tiles.TileCraftingBox;
import two.util.BlockSide;
import two.util.BlockUtil;

/**
 * @author Two
 */
public class BlockCraftingBox extends BlockWithInventory {

  public static final String NAME_BOX = "craftingBox";
  public static final String NAME_ADVANCED = "craftingBoxAdvanced";
  public static final int STATE_BOX = 0;
  public static final int STATE_ADVANCED = 0x04;
  protected static final String CONFIG_KEY_IS_NOISY = "Crafting Box is noisy";
//-- Class -------------------------------------------------------------------
  public final boolean isNoisy;
  @SideOnly(Side.CLIENT)
  protected IIcon iconTop;
  @SideOnly(Side.CLIENT)
  protected IIcon iconTopAdvanced;
  @SideOnly(Side.CLIENT)
  protected IIcon iconBottom;

  public BlockCraftingBox() {
    super(Material.wood, TileCraftingBox.class);
    this.isNoisy = TwoTility.config.getMiscBoolean(CONFIG_KEY_IS_NOISY, true);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME_BOX, soundTypeWood, 1.5F, BlockUtil.HARVEST_TOOL_AXE, BlockUtil.HARVEST_LEVEL_WOOD);

    if (TwoTility.config.isCraftingEnabled(NAME_BOX)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(TwoTilityAssets.itemCraftingBox),
              "SFS",
              'S', "shelfWood",
              'F', "craftingTableWood"
      ));
    }
//
//    if (TwoTility.config.isCraftingEnabled(NAME_ADVANCED)) {
//      CraftingManager.getInstance().addRecipe(itemAdvanced,
//              "DCD",
//              "BRL",
//              "dPd",
//              'C', itemBox,
//              'D', Item.diamond,
//              'd', Item.redstone, // dust
//              'B', Item.writableBook,
//              'L', Block.lever,
//              'P', Block.pistonBase,
//              'R', Item.redstoneRepeater);
//    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister iconRegister) {
    iconTop = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX));
    iconTopAdvanced = iconRegister.registerIcon(TwoTility.getTextureName(NAME_ADVANCED));
    iconBottom = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX) + "_bottom");
  }

  @SideOnly(Side.CLIENT)
  protected IIcon getTopIconByState(final int state) {
    switch (state) {
      case STATE_BOX:
        return iconTop;
      case STATE_ADVANCED:
        return iconTopAdvanced;
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    switch (BlockSide.getSide(side)) {
      case TOP:
        return getTopIconByState(BlockSide.getRotationData(metadata));
      case BOTTOM:
        return iconBottom;
      default:
        return TwoTilityAssets.blockShelf.getSideIconByState(BlockSide.getBlockDataFromMetadata(metadata));
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(final Item thisItem, final CreativeTabs creativeTab, final List itemlist) {
//    itemlist.add(itemBox);
//    itemlist.add(itemAdvanced);
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = BlockSide.getRotationData(itemStack.getItemDamage());
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
    super.onBlockPlacedBy(world, x, y, z, entity, itemStack);
  }

  @Override
  public int damageDropped(final int metadata) {
    return BlockSide.getRotationData(metadata);
  }

  @Override
  public Item getItemDropped(final int metadata, final Random random, final int fortuneLevel) {
    return TwoTilityAssets.itemCraftingBox;
  }

  @Override
  public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y, final int z, final EntityPlayer player) {
    return new ItemStack(TwoTilityAssets.itemCraftingBox);
  }
}
