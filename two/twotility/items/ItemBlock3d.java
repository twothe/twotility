/*
 */
package two.twotility.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import two.twotility.InitializableModContent;
import two.util.BlockSide;

/**
 * @author Two
 */
public abstract class ItemBlock3d extends ItemBlockWithMetadata implements InitializableModContent {

  protected final Block block;
  protected final int blockID;

  public ItemBlock3d(final int id, final Block block) {
    super(id, block);
    this.block = block;
    this.blockID = block.blockID;
  }

  @Override
  public void getSubItems(final int itemID, final CreativeTabs creativeTab, final List itemlist) {
    // done by the block
  }

  /**
   * Returns the blockID for this Item
   */
  @Override
  public int getBlockID() {
    return blockID;
  }

  @SideOnly(Side.CLIENT)
  public Icon getIcon(final int side, final int metadata) {
    return block.getIcon(side, metadata);
  }

  @SideOnly(Side.CLIENT)
  /**
   * Returns 0 for /terrain.png, 1 for /gui/items.png
   */
  @Override
  public int getSpriteNumber() {
    return 0;
  }

  @SideOnly(Side.CLIENT)
  /**
   * Gets an icon index based on an item's damage value
   */
  @Override
  public Icon getIconFromDamage(final int metadata) {
    return block.getIcon(BlockSide.NORTH.ordinal(), metadata);
  }

  /**
   * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
   * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
   */
  @Override
  public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
    int i1 = par3World.getBlockId(par4, par5, par6);

    if (i1 == Block.snow.blockID && (par3World.getBlockMetadata(par4, par5, par6) & 7) < 1) {
      par7 = 1;
    } else if (i1 != Block.vine.blockID && i1 != Block.tallGrass.blockID && i1 != Block.deadBush.blockID
            && (Block.blocksList[i1] == null || !Block.blocksList[i1].isBlockReplaceable(par3World, par4, par5, par6))) {
      if (par7 == 0) {
        --par5;
      }

      if (par7 == 1) {
        ++par5;
      }

      if (par7 == 2) {
        --par6;
      }

      if (par7 == 3) {
        ++par6;
      }

      if (par7 == 4) {
        --par4;
      }

      if (par7 == 5) {
        ++par4;
      }
    }

    if (par1ItemStack.stackSize == 0) {
      return false;
    } else if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7, par1ItemStack)) {
      return false;
    } else if (par5 == 255 && block.blockMaterial.isSolid()) {
      return false;
    } else if (par3World.canPlaceEntityOnSide(blockID, par4, par5, par6, false, par7, par2EntityPlayer, par1ItemStack)) {
      final int metavalue = block.onBlockPlaced(par3World, par4, par5, par6, par7, par8, par9, par10, this.getMetadata(par1ItemStack.getItemDamage()));

      if (placeBlockAt(par1ItemStack, par2EntityPlayer, par3World, par4, par5, par6, par7, par8, par9, par10, metavalue)) {
        par3World.playSoundEffect((double) ((float) par4 + 0.5F), (double) ((float) par5 + 0.5F), (double) ((float) par6 + 0.5F), block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
        --par1ItemStack.stackSize;
      }

      return true;
    } else {
      return false;
    }
  }

  @SideOnly(Side.CLIENT)
  /**
   * Returns true if the given ItemBlock can be placed on the given side of the given block position.
   */
  @Override
  public boolean canPlaceItemBlockOnSide(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer, ItemStack par7ItemStack) {
    int i1 = par1World.getBlockId(par2, par3, par4);

    if (i1 == Block.snow.blockID) {
      par5 = 1;
    } else if (i1 != Block.vine.blockID && i1 != Block.tallGrass.blockID && i1 != Block.deadBush.blockID
            && (Block.blocksList[i1] == null || !Block.blocksList[i1].isBlockReplaceable(par1World, par2, par3, par4))) {
      if (par5 == 0) {
        --par3;
      }

      if (par5 == 1) {
        ++par3;
      }

      if (par5 == 2) {
        --par4;
      }

      if (par5 == 3) {
        ++par4;
      }

      if (par5 == 4) {
        --par2;
      }

      if (par5 == 5) {
        ++par2;
      }
    }

    return par1World.canPlaceEntityOnSide(blockID, par2, par3, par4, false, par5, (Entity) null, par7ItemStack);
  }

  /**
   * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
   * different names based on their damage or NBT.
   */
  @Override
  public String getUnlocalizedName(ItemStack par1ItemStack) {
    return block.getUnlocalizedName();
  }

  /**
   * Returns the unlocalized name of this item.
   */
  @Override
  public String getUnlocalizedName() {
    return block.getUnlocalizedName();
  }

  @SideOnly(Side.CLIENT)
  /**
   * gets the CreativeTab this item is displayed on
   */
  @Override
  public CreativeTabs getCreativeTab() {
    return block.getCreativeTabToDisplayOn();
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(IconRegister par1IconRegister) {
    // done by the block
  }

  /**
   * Called to actually place the block, after the location is determined
   * and all permission checks have been made.
   *
   * @param stack The item stack that was used to place the block. This can be changed inside the method.
   * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
   * @param side The side the player (or machine) right-clicked on.
   */
  @Override
  public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
    if (!world.setBlock(x, y, z, blockID, metadata, 3)) {
      return false;
    }

    if (world.getBlockId(x, y, z) == blockID) {
      block.onBlockPlacedBy(world, x, y, z, player, stack);
      block.onPostBlockPlaced(world, x, y, z, metadata);
    }

    return true;
  }
}
