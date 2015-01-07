/*
 */
package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.twotility.tiles.TileStreamingRadio;
import two.util.BlockUtil;
import two.util.Logging;

/**
 * @author Two
 */
public class BlockStreamingRadio extends BlockWithInventory {

  public static final String NAME = "streamingRadio";

  public BlockStreamingRadio() {
    super(Material.wood, TileStreamingRadio.class);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, soundTypeWood, 1.5F, BlockUtil.HARVEST_TOOL_AXE, BlockUtil.HARVEST_LEVEL_WOOD);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              "  I",
              "  I",
              "JRI",
              'J', Blocks.jukebox,
              'I', Items.iron_ingot,
              'R', Items.redstone);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    return Blocks.jukebox.getIcon(side, metadata);
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
    Logging.logMethodEntry("BlockStreamingRadio", "onBlockPlacedBy", "remote=" + world.isRemote);
    super.onBlockPlacedBy(world, x, y, z, par5EntityLivingBase, par6ItemStack); // TODO
    world.scheduleBlockUpdate(x, y, z, this, 4);
  }

  @Override
  public void onBlockDestroyedByPlayer(final World world, final int x, final int y, final int z, final int metadata) {
    Logging.logMethodEntry("BlockStreamingRadio", "removeBlockByPlayer", "remote=" + world.isRemote);
    if (world.isRemote) {
      final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getTileEntity(x, y, z);
      tileEntity.stopStreaming();
    }
  }

  @Override
  public void onBlockPreDestroy(final World world, final int x, final int y, final int z, final int oldMetadata) {
    Logging.logMethodEntry("BlockStreamingRadio", "onBlockPreDestroy", "remote=" + world.isRemote);
    if (world.isRemote) {
      final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getTileEntity(x, y, z);
      tileEntity.stopStreaming();
    }
  }

  @Override
  public void onBlockExploded(final World world, final int x, final int y, final int z, final Explosion par5Explosion) {
    Logging.logMethodEntry("BlockStreamingRadio", "onBlockExploded", "remote=" + world.isRemote);
    if (world.isRemote) {
      final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getTileEntity(x, y, z);
      tileEntity.stopStreaming();
    }
    super.onBlockExploded(world, x, y, z, par5Explosion);
  }

  @Override
  public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final Block neighborBlock) {
    Logging.logMethodEntry("BlockStreamingRadio", "onNeighborBlockChange", "remote=" + world.isRemote);
    world.scheduleBlockUpdate(x, y, z, this, 4);
  }

  @Override
  public void updateTick(final World world, final int x, final int y, final int z, final Random random) {
    Logging.logMethodEntry("BlockStreamingRadio", "updateTick", "remote=" + world.isRemote);
    final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getTileEntity(x, y, z);
    if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
      tileEntity.startStreaming();
    } else {
      tileEntity.stopStreaming();
    }
  }
}
