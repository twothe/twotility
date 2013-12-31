/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.gui.GUICraftingBox;
import two.twotility.inventory.ContainerBase;
import two.twotility.inventory.ContainerCraftingBox;
import two.twotility.tiles.TileCraftingBox;
import two.twotility.tiles.TileWithInventory;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockCraftingBox extends BlockWithInventory {

  public static final String NAME_BOX = "CraftingBox";
  public static final String NAME_ADVANCED = "AdvancedCraftingBox";
  public static final int STATE_BOX = 0;
  public static final int STATE_ADVANCED = STATE_BOX + 1;
  public static final int NUM_STATES = STATE_ADVANCED + 1;
  protected static final String CONFIG_KEY_IS_NOISY = "Crafting Box is noisy";
//-- Class -------------------------------------------------------------------
  public final boolean isNoisy;
  @SideOnly(Side.CLIENT)
  protected Icon[] stateIcons = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon iconSide;
  @SideOnly(Side.CLIENT)
  protected Icon iconTop;
  @SideOnly(Side.CLIENT)
  protected Icon iconBottom;
  @SideOnly(Side.CLIENT)
  protected Icon iconBack;
  protected ItemStack itemBox;
  protected ItemStack itemAdvanced;

  public BlockCraftingBox() {
    super(TwoTility.config.getBlockID(BlockCraftingBox.class), Material.wood, TileCraftingBox.class);
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME_BOX));
    this.isNoisy = TwoTility.config.getMiscBoolean(CONFIG_KEY_IS_NOISY, true);
  }

  @Override
  public void initialize() {
    setHardness(1.5F);
    setResistance(6.0f);
    setStepSound(soundWoodFootstep);
    setUnlocalizedName(NAME_BOX);
    setTextureName(TwoTility.getTextureName(NAME_BOX));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "axe", 0);

    itemBox = new ItemStack(TwoTility.proxy.itemCraftingBox, 1, STATE_BOX);
    LanguageRegistry.addName(itemBox, "Crafting Box");
    itemAdvanced = new ItemStack(TwoTility.proxy.itemCraftingBox, 1, STATE_ADVANCED);
    LanguageRegistry.addName(itemAdvanced, "Advanced Crafting Box");

    if (TwoTility.config.isCraftingEnabled(NAME_BOX)) {
      CraftingManager.getInstance().addRecipe(itemBox,
              "   ",
              "CFC",
              "   ",
              'C', TwoTility.proxy.blockShelf,
              'F', Block.workbench);
    }

    if (TwoTility.config.isCraftingEnabled(NAME_ADVANCED)) {
      CraftingManager.getInstance().addRecipe(itemAdvanced,
              "DCD",
              "BRL",
              "dPd",
              'C', itemBox,
              'D', Item.diamond,
              'd', Item.redstone, // dust
              'B', Item.writableBook,
              'L', Block.lever,
              'P', Block.pistonBase,
              'R', Item.redstoneRepeater);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    stateIcons[STATE_BOX] = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX));
    stateIcons[STATE_ADVANCED] = iconRegister.registerIcon(TwoTility.getTextureName(NAME_ADVANCED));

    iconSide = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX) + "_side");
    iconBack = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX) + "_back");
    iconTop = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX) + "_top");
    iconBottom = iconRegister.registerIcon(TwoTility.getTextureName(NAME_BOX) + "_bottom");
  }

  protected Icon getFrontfaceByState(final int state) {
    if ((state >= 0) && (state < stateIcons.length)) {
      return stateIcons[state];
    } else {
      FMLLog.warning("Illegal front face state #%d for %s.", state, this.getClass().getSimpleName());
      return stateIcons[0];
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    final BlockSide rotatedSide = BlockSide.getRotatedSide(side, metadata);
    switch (rotatedSide) {
      case NORTH:
        return getFrontfaceByState(BlockSide.getStateFromMetadata(metadata));
      case SOUTH:
        return iconBack;
      case TOP:
        return iconTop;
      case BOTTOM:
        return iconBottom;
      default:
        return iconSide;
    }
  }

  @Override
  public void getSubBlocks(final int itemID, final CreativeTabs creativeTab, final List itemlist) {
    itemlist.add(itemBox);
    itemlist.add(itemAdvanced);
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = BlockSide.createState(BlockSide.getDirectionFacing(entity), itemStack.getItemDamage());
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
    super.onBlockPlacedBy(world, z, x, y, entity, itemStack);
  }

  @Override
  protected ContainerBase doCreateContainer(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z) {
    return new ContainerCraftingBox(player.inventory, (TileCraftingBox) tileEntity);
  }

  @Override
  protected Gui doCreateGUI(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z) {
    return new GUICraftingBox(player.inventory, (TileCraftingBox) tileEntity);
  }

  @Override
  public int damageDropped(final int metadata) {
    return BlockSide.getStateFromMetadata(metadata);
  }

  @Override
  public int idDropped(int par1, Random par2Random, int par3) {
    return this.itemBox.itemID;
  }

  @Override
  public int idPicked(World par1World, int par2, int par3, int par4) {
    return this.itemBox.itemID;
  }
}
