/*
 */

package two.twotility.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.blocks.BlockShelf;
import two.twotility.container.ContainerShelf;
import two.twotility.tiles.TileShelf;

/**
 * @author Two
 */
public class GUIShelf extends GuiContainer {

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockShelf.NAME);
  protected final TileShelf tileShelf;

  public GUIShelf(final InventoryPlayer inventoryPlayer, final TileShelf tileShelf) {
    super((new ContainerShelf(inventoryPlayer, tileShelf)).layout());
    this.tileShelf = tileShelf;
    // visible gui part
    this.xSize = 167;
    this.ySize = 156;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
  }

}
