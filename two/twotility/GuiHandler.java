/*
 */
package two.twotility;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import two.twotility.gui.GUICallback;
import two.util.InvalidTileEntityException;

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
  protected ArrayList<GUICallback> knownGuis = new ArrayList<GUICallback>();

  protected GuiHandler() {
  }

  public int registerGui(final GUICallback gui) {
    if (knownGuis.contains(gui) == false) {
      knownGuis.add(gui);
    } else {
      throw new IllegalStateException("Tried to register GUI '" + gui.getClass().getName() + "' twice!");
    }
    return knownGuis.indexOf(gui);
  }

  @Override
  public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
    if ((ID >= 0) && (ID < knownGuis.size())) {
      final GUICallback guiClass = knownGuis.get(ID);

      try {
        return guiClass.createContainer(player, world, x, y, z);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, e.getMessage());
        world.removeBlockTileEntity(x, y, z);
      } catch (Exception e) {
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
      final GUICallback guiClass = knownGuis.get(ID);

      try {
        return guiClass.createGUI(player, world, x, y, z);
      } catch (InvalidTileEntityException e) {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, e.getMessage());
        world.removeBlockTileEntity(x, y, z);
      } catch (Exception e) {
        FMLLog.log(Level.WARNING, e, "Unable to create GUI: %d", ID);
      }
    } else {
      FMLLog.log(Level.WARNING, "Received client request for unknown Gui ID: %d", ID);
    }
    return null;
  }
}
