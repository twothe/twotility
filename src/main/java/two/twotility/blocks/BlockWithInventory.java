/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import two.twotility.TwoTility;
import two.twotility.gui.GUICallback;
import two.twotility.tiles.TileWithInventory;
import two.twotility.util.InvalidTileEntityException;

/**
 * @author Two
 */
public abstract class BlockWithInventory extends BlockBase implements ITileEntityProvider, GUICallback {

  protected final Class<? extends TileWithInventory> tileEntityClass;
  protected final int guiId;

  public BlockWithInventory(final Material material, final Class<? extends TileWithInventory> tileEntityClass) {
    super(material);
    this.tileEntityClass = tileEntityClass;

    GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getName());
    guiId = TwoTility.guiHandler.registerGui(this);
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote == false) {
      player.openGui(TwoTility.instance, guiId, world, x, y, z);
    }
    return true;
  }

  /**
   * Create a new Container class appropriate to the sub-class of this.
   *
   * @return the fully created container.
   */
  @Override
  public Container createContainer(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      return tileEntityClass.cast(tileEntity).createContainer(player).layout();
    } else {
      throw new InvalidTileEntityException(tileEntityClass, tileEntity, x, y, z);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      return tileEntityClass.cast(tileEntity).createGUI(player);
    } else {
      throw new InvalidTileEntityException(tileEntityClass, tileEntity, x, y, z);
    }
  }

  @Override
  public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      tileEntityClass.cast(tileEntity).spillOutInventory();
    }

    super.breakBlock(world, x, y, z, block, metadata);
  }

  @Override
  public TileEntity createNewTileEntity(World world, int i) {
    try {
      return tileEntityClass.newInstance();
    } catch (Exception ex) {
      FMLLog.log(Level.ERROR, ex, "Creation of %s failed!", tileEntityClass.getName());
      return null;
    }
  }
}
