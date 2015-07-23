/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.twotility.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.blocks.BlockStorageBoxIron;
import two.twotility.container.ContainerStorageBoxIron;
import two.twotility.tiles.TileStorageBoxIron;

/**
 *
 * @author Two
 */
public class GUIStorageBoxIron extends GuiContainer {

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockStorageBoxIron.NAME);
  protected final TileStorageBoxIron tileStorageBoxIron;

  public GUIStorageBoxIron(final InventoryPlayer inventoryPlayer, final TileStorageBoxIron tileStorageBoxIron) {
    super((new ContainerStorageBoxIron(inventoryPlayer, tileStorageBoxIron)).layout());
    this.tileStorageBoxIron = tileStorageBoxIron;
    // visible gui part
    this.xSize = 167;
    this.ySize = 193;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
  }

}
