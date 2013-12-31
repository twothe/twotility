/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.logging.Level;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import two.twotility.InitializableModContent;
import two.twotility.TwoTility;
import two.twotility.gui.GUICallback;
import two.twotility.inventory.ContainerBase;
import two.twotility.tiles.TileWithInventory;
import two.util.BlockSide;
import two.util.InvalidTileEntityException;

/**
 * @author Two
 */
public abstract class BlockWithInventory extends Block implements ITileEntityProvider, InitializableModContent, GUICallback {

  protected final Class<? extends TileWithInventory> tileEntityClass;
  protected final int guiId;

  public BlockWithInventory(final int id, final Material material, final Class<? extends TileWithInventory> tileEntityClass) {
    super(id, material);
    this.tileEntityClass = tileEntityClass;

    GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getName());
    guiId = TwoTility.guiHandler.registerGui(this);
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote == false) {
      FMLNetworkHandler.openGui(player, TwoTility.instance, guiId, world, x, y, z);
    }
    return true;
  }

  /**
   * Create a new Container class appropriate to the sub-class of this.
   *
   * @return the fully created container.
   */
  protected abstract ContainerBase doCreateContainer(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z);

  @Override
  public Container createContainer(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      return (doCreateContainer(player, tileEntityClass.cast(tileEntity), world, x, y, z)).layout();
    } else {
      throw new InvalidTileEntityException(tileEntityClass, tileEntity, x, y, z);
    }
  }

  /**
   * Create a new GUI class appropriate to the sub-class of this.
   *
   * @return the fully created GUI.
   */
  protected abstract Gui doCreateGUI(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z);

  @Override
  public Gui createGUI(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      return doCreateGUI(player, tileEntityClass.cast(tileEntity), world, x, y, z);
    } else {
      throw new InvalidTileEntityException(tileEntityClass, tileEntity, x, y, z);
    }
  }

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final int blockID, final int metadata) {
    final TileEntity tileEntity = (TileEntity) world.getBlockTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      tileEntityClass.cast(tileEntity).spillOutInventory();
    }

    world.func_96440_m(x, y, z, blockID);
    super.breakBlock(world, x, y, z, blockID, metadata);
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    try {
      return tileEntityClass.newInstance();
    } catch (Exception ex) {
      FMLLog.log(Level.SEVERE, ex, "Creation of %s failed!", tileEntityClass.getName());
      return null;
    }
  }
}
