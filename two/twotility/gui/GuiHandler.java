/*
 */
package two.twotility.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.util.Logging;

/**
 * @author Two
 */
public class GuiHandler implements IGuiHandler {

  private static final HashMap<Integer, GuiEntry> knownGuis = new HashMap<Integer, GuiEntry>();
  //----------------------------------------------------------------------------
  //--- Register GUI IDs here --------------------------------------------------
  //----------------------------------------------------------------------------
  public static final int ID_ADVANCED_FURNACE = 0;

  static {
    knownGuis.put(ID_ADVANCED_FURNACE, new GuiEntry(ContainerAdvancedFurnace.class, GUIAdvancedFurnace.class));
  }
  //----------------------------------------------------------------------------

  /**
   * Utility function to load GUI PNGs using the appropriate lower-case name
   *
   * @param guiName the name of the gui (without PNG)
   * @return the resource that was created using the given file.
   */
  public static ResourceLocation loadGuiPNG(final String guiName) {
    return new ResourceLocation(TwoTility.MOD_ID.toLowerCase(Locale.ENGLISH), "textures/gui/" + guiName.toLowerCase(Locale.ENGLISH) + ".png");
  }
  /* Instance */
  public static final GuiHandler instance = new GuiHandler();

  private static class GuiEntry {

    final Class<? extends Container> containerClass;
    final Class<? extends Gui> guiClass;

    public GuiEntry(final Class<? extends Container> containerClass, final Class<? extends Gui> guiClass) {
      this.containerClass = containerClass;
      this.guiClass = guiClass;
    }
  }

  private class InvalidTileEntityException extends Exception {

    final Class expected;
    final Class found;

    public InvalidTileEntityException(final Class expected, final Class found) {
      this.expected = expected;
      this.found = found;
    }
  }
  //--- Class ------------------------------------------------------------------

  public void initialize() {
    NetworkRegistry.instance().registerGuiHandler(TwoTility.instance, this);
  }

  private GuiHandler() {
  }

  @Override
  public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
    Logging.logMethodEntry("GuiHandler", "getServerGuiElement", ID);
    final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    final GuiEntry guiEntry = knownGuis.get(ID);

    if (guiEntry.containerClass == null) {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested to create container for unknown GUI %d", ID);
    } else {
      try {
        return tryCreateContainer(guiEntry.containerClass, player, tileEntity);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been %s, but was %s", new Object[]{x, y, z, e.expected.getName(), e.found.getName()});
        world.removeBlockTileEntity(x, y, z);
      } catch (ReflectiveOperationException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Unable to create container for GUI: %s", e.getMessage());
      }
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
    Logging.logMethodEntry("GuiHandler", "getClientGuiElement", ID);
    final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    final GuiEntry guiEntry = knownGuis.get(ID);
    if (guiEntry.containerClass == null) {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested to create unknown GUI %d", ID);
    } else {
      try {
        return tryCreateGUI(guiEntry.guiClass, player, tileEntity);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been %s, but was %s", new Object[]{x, y, z, e.expected.getName(), e.found.getName()});
        world.removeBlockTileEntity(x, y, z);
      } catch (ReflectiveOperationException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Unable to open GUI: %d", e.getMessage());
      }
    }
    return null;
  }

  protected Object tryCreateContainer(final Class<? extends Container> container, final EntityPlayer player, final TileEntity tileEntity) throws ReflectiveOperationException, InvalidTileEntityException {
    Logging.logMethodEntry("GuiHandler", "tryCreateContainer", container.getSimpleName());
    final Constructor[] constructors = container.getConstructors();
    for (final Constructor constructor : constructors) {
      final Class[] parameters = constructor.getParameterTypes();
      if (parameters.length == 2) {
        if (parameters[1].isAssignableFrom(tileEntity.getClass())) { // is this the correct TileEntity?
          if ((tileEntity instanceof IInventory) && (((IInventory) tileEntity).isUseableByPlayer(player) == false)) {
            return null; // player is not allowed to use that GUI, no need to try
          }
          return constructor.newInstance(player.inventory, tileEntity);
        } else if (parameters[1].isAssignableFrom(TileEntity.class)) { // is this any other TileEntity?
          throw new InvalidTileEntityException(tileEntity.getClass(), parameters[1]);
        }
      }
    }
    return null;
  }

  protected Object tryCreateGUI(final Class<? extends Gui> gui, final EntityPlayer player, final TileEntity tileEntity) throws ReflectiveOperationException, InvalidTileEntityException {
    Logging.logMethodEntry("GuiHandler", "tryCreateGUI", gui.getSimpleName());
    for (final Constructor constructor : gui.getConstructors()) {
      final Class[] parameters = constructor.getParameterTypes();
      if (parameters.length == 2) {
        if (parameters[1].isAssignableFrom(tileEntity.getClass())) { // is this the correct TileEntity?
          return constructor.newInstance(player.inventory, tileEntity);
        } else if (parameters[1].isAssignableFrom(TileEntity.class)) { // is this any other TileEntity?
          throw new InvalidTileEntityException(tileEntity.getClass(), parameters[1]);
        }
      }
    }
    return null;
  }
}
