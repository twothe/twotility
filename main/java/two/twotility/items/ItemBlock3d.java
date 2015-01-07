/*
 */
package two.twotility.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import two.util.BlockSide;

/**
 * @author Two
 */
public abstract class ItemBlock3d extends ItemBase {

  protected final Block block;

  public ItemBlock3d(final Block block) {
    super();
    this.block = block;
  }

  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final int side, final int metadata) {
    return block.getIcon(side, metadata);
  }

  /**
   * Gets an icon index based on an item's damage value
   */
  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIconFromDamage(final int metadata) {
    return block.getIcon(BlockSide.NORTH.ordinal(), metadata);
  }

  /**
   * Returns 0 for /terrain.png, 1 for /gui/items.png
   * @return 
   */
  @SideOnly(Side.CLIENT)
  @Override
  public int getSpriteNumber() {
    return 0;
  }

  /**
   * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
   * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
   */
  @Override
  public boolean onItemUse(final ItemStack itemStackInUse, final EntityPlayer player, final World world, int x, int y, int z, int sideID, float hitX, float hitY, float hitZ) {
    final Block targetBlock = world.getBlock(x, y, z);

    if (targetBlock == Blocks.snow && (world.getBlockMetadata(x, y, z) & 7) < 1) {
      sideID = 1;
    } else if (targetBlock != Blocks.vine && targetBlock != Blocks.tallgrass && targetBlock != Blocks.deadbush
            && (targetBlock == Blocks.air || !targetBlock.isReplaceable(world, x, y, z))) {
      if (sideID == 0) {
        --y;
      }

      if (sideID == 1) {
        ++y;
      }

      if (sideID == 2) {
        --z;
      }

      if (sideID == 3) {
        ++z;
      }

      if (sideID == 4) {
        --x;
      }

      if (sideID == 5) {
        ++x;
      }
    }

    if (itemStackInUse.stackSize == 0) {
      return false;
    } else if (!player.canPlayerEdit(x, y, z, sideID, itemStackInUse)) {
      return false;
    } else if (y == 255 && targetBlock.getMaterial().isSolid()) {
      return false;
    } else if (world.canPlaceEntityOnSide(targetBlock, x, y, z, false, sideID, player, itemStackInUse)) {
      final int metavalue = targetBlock.onBlockPlaced(world, x, y, z, sideID, hitX, hitY, hitZ, this.getMetadata(itemStackInUse.getItemDamage()));

      if (placeBlockAt(itemStackInUse, player, world, x, y, z, sideID, hitX, hitY, hitZ, metavalue)) {
        world.playSoundEffect((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), targetBlock.stepSound.soundName, (targetBlock.stepSound.getVolume() + 1.0F) / 2.0F, targetBlock.stepSound.getPitch() * 0.8F);
        --itemStackInUse.stackSize;
      }

      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the given ItemBlock can be placed on the given side of the given block position.
   */
  @SideOnly(Side.CLIENT)
  public boolean canPlaceItemBlockOnSide(final World world, int x, int y, int z, int sideID, EntityPlayer player, ItemStack itemstack) {
    final Block targetBlock = world.getBlock(x, y, z);

    if (targetBlock == Blocks.snow) {
      sideID = 1;
    } else if (targetBlock != Blocks.vine && targetBlock != Blocks.tallgrass && targetBlock != Blocks.deadbush
            && (targetBlock == Blocks.air || !targetBlock.isReplaceable(world, x, y, z))) {
      if (sideID == 0) {
        --y;
      }

      if (sideID == 1) {
        ++y;
      }

      if (sideID == 2) {
        --z;
      }

      if (sideID == 3) {
        ++z;
      }

      if (sideID == 4) {
        --x;
      }

      if (sideID == 5) {
        ++x;
      }
    }

    return world.canPlaceEntityOnSide(block, x, y, z, false, sideID, (Entity) null, itemstack);
  }

  /**
   * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
   * different names based on their damage or NBT.
   */
  @Override
  public String getUnlocalizedName(final ItemStack par1ItemStack) {
    return block.getUnlocalizedName();
  }

  /**
   * Returns the unlocalized name of this item.
   */
  @Override
  public String getUnlocalizedName() {
    return block.getUnlocalizedName();
  }

  /**
   * Called to actually place the block, after the location is determined
   * and all permission checks have been made.
   *
   * @param stack The item stack that was used to place the block. This can be changed inside the method.
   * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
   * @param side The side the player (or machine) right-clicked on.
   */
  public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
    if (!world.setBlock(x, y, z, block, metadata, 3)) {
      return false;
    }

    if (world.getBlock(x, y, z) == block) {
      block.onBlockPlacedBy(world, x, y, z, player, stack);
      block.onPostBlockPlaced(world, x, y, z, metadata);
    }

    return true;
  }
}
