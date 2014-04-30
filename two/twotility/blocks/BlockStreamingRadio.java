/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.Icon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.tiles.TileStreamingRadio;
import two.util.Logging;

/**
 * @author Two
 */
public class BlockStreamingRadio extends BlockWithInventory {

  public static final String NAME = "StreamingRadio";

  public BlockStreamingRadio() {
    super(TwoTility.config.getBlockID(BlockStreamingRadio.class), Material.wood, TileStreamingRadio.class);
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
    LanguageRegistry.addName(this, "Streaming Radio");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              "  I",
              "  I",
              "JRI",
              'J', Block.jukebox,
              'I', Item.ingotIron,
              'R', Item.redstone);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    return Block.jukebox.getIcon(side, metadata);
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
    Logging.logMethodEntry("BlockStreamingRadio", "onBlockPlacedBy", "remote=" + world.isRemote);
    super.onBlockPlacedBy(world, x, y, z, par5EntityLivingBase, par6ItemStack); // TODO
    world.scheduleBlockUpdate(x, y, z, this.blockID, 4);
  }

  @Override
  public boolean removeBlockByPlayer(final World world, final EntityPlayer player, final int x, final int y, final int z) {
    Logging.logMethodEntry("BlockStreamingRadio", "removeBlockByPlayer", "remote=" + world.isRemote);
    if (world.isRemote) {
      final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getBlockTileEntity(x, y, z);
      tileEntity.stopStreaming();
    }
    return super.removeBlockByPlayer(world, player, x, y, z);
  }

  @Override
  public void onBlockExploded(final World world, final int x, final int y, final int z, final Explosion par5Explosion) {
    Logging.logMethodEntry("BlockStreamingRadio", "onBlockExploded", "remote=" + world.isRemote);
    if (world.isRemote) {
      final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getBlockTileEntity(x, y, z);
      tileEntity.stopStreaming();
    }
    super.onBlockExploded(world, x, y, z, par5Explosion);
  }

  @Override
  public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final int metadata) {
    Logging.logMethodEntry("BlockStreamingRadio", "onNeighborBlockChange", "remote=" + world.isRemote);
    world.scheduleBlockUpdate(x, y, z, this.blockID, 4);
  }

  @Override
  public void updateTick(final World world, final int x, final int y, final int z, final Random random) {
    Logging.logMethodEntry("BlockStreamingRadio", "updateTick", "remote=" + world.isRemote);
    final TileStreamingRadio tileEntity = (TileStreamingRadio) world.getBlockTileEntity(x, y, z);
    if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
      tileEntity.startStreaming();
    } else {
      tileEntity.stopStreaming();
    }
  }
}
