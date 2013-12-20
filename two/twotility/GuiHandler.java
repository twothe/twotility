/*
 */
package two.twotility;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * @author Two
 */
public class GuiHandler implements IGuiHandler {

  /**
   * Utility function to load GUI PNGs using the appropriate lower-case name
   *
   * @param guiName the name of the gui (without PNG)
   * @return the resource that was created using the given file.
   */
  public static ResourceLocation loadGuiPNG(final String guiName) {
    return new ResourceLocation(TwoTility.MOD_ID.toLowerCase(Locale.ENGLISH), "textures/gui/" + guiName.toLowerCase(Locale.ENGLISH) + ".png");
  }
  //--- Class ------------------------------------------------------------------
  protected ArrayList<GuiEntry> knownGuis = new ArrayList<GuiEntry>();

  protected GuiHandler() {
  }

  public int registerGui(final Class<? extends Container> containerClass, final Class<? extends Gui> guiClass) {
    final GuiEntry entry = new GuiEntry(containerClass, guiClass);
    if (knownGuis.contains(entry) == false) {
      knownGuis.add(entry);
    } else {
      throw new IllegalStateException("Tried to register GUI with container '" + containerClass.getName() + "' and Gui '" + guiClass.getName() + "' twice!");
    }
    return knownGuis.indexOf(entry);
  }

  @Override
  public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
    if ((ID >= 0) && (ID < knownGuis.size())) {
      final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
      final GuiEntry guiEntry = knownGuis.get(ID);

      try {
        return tryCreateContainer(guiEntry.containerClass, player, tileEntity);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been %s, but was %s", new Object[]{x, y, z, e.expected.getName(), e.found.getName()});
        world.removeBlockTileEntity(x, y, z);
      } catch (ReflectiveOperationException e) {
        FMLLog.log(Level.WARNING, e, "Unable to create container for GUI: %d", ID);
      }
    } else {
      FMLLog.log(Level.WARNING, "Received server request for unknown Gui ID: %d", ID);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
    if ((ID >= 0) && (ID < knownGuis.size())) {
      final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
      final GuiEntry guiEntry = knownGuis.get(ID);
      try {
        return tryCreateGUI(guiEntry.guiClass, player, tileEntity);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been %s, but was %s", new Object[]{x, y, z, e.expected.getName(), e.found.getName()});
        world.removeBlockTileEntity(x, y, z);
      } catch (ReflectiveOperationException e) {
        FMLLog.log(Level.WARNING, e, "Unable to create GUI: %d", ID);
      }
    } else {
      FMLLog.log(Level.WARNING, "Received client request for unknown Gui ID: %d", ID);
    }
    return null;
  }

  protected Object tryCreateContainer(final Class<? extends Container> container, final EntityPlayer player, final TileEntity tileEntity) throws ReflectiveOperationException, InvalidTileEntityException {
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

  /**
   * Private class to notice when the world has a corrupted tile entity.
   */
  protected static class GuiEntry {

    static int calculateHash(final Class<? extends Container> containerClass, final Class<? extends Gui> guiClass) {
      int result = 7;
      result = 83 * result + containerClass.hashCode();
      result = 83 * result + guiClass.hashCode();
      return result;
    }
    final Class<? extends Container> containerClass;
    final Class<? extends Gui> guiClass;
    final int hash;

    GuiEntry(final Class<? extends Container> containerClass, final Class<? extends Gui> guiClass) {
      if (containerClass == null) {
        throw new NullPointerException("Container class cannot be null!");
      }
      if (guiClass == null) {
        throw new NullPointerException("GUI class cannot be null!");
      }
      this.containerClass = containerClass;
      this.guiClass = guiClass;

      this.hash = calculateHash(containerClass, guiClass);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final GuiEntry other = (GuiEntry) obj;
      if (this.containerClass != other.containerClass && !this.containerClass.equals(other.containerClass)) {
        return false;
      }
      if (this.guiClass != other.guiClass && !this.guiClass.equals(other.guiClass)) {
        return false;
      }
      return true;
    }
  }

  protected static class InvalidTileEntityException extends Exception {

    final Class expected;
    final Class found;

    InvalidTileEntityException(final Class expected, final Class found) {
      this.expected = expected;
      this.found = found;
    }
  }
}
