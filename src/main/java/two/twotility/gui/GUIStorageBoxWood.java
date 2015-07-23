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
import two.twotility.blocks.BlockStorageBoxWood;
import two.twotility.container.ContainerStorageBoxWood;
import two.twotility.tiles.TileStorageBoxWood;

/**
 *
 * @author Two
 */
public class GUIStorageBoxWood extends GuiContainer {

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockStorageBoxWood.NAME);
  protected final TileStorageBoxWood tileStorageBoxWood;

  public GUIStorageBoxWood(final InventoryPlayer inventoryPlayer, final TileStorageBoxWood tileStorageBoxWood) {
    super((new ContainerStorageBoxWood(inventoryPlayer, tileStorageBoxWood)).layout());
    this.tileStorageBoxWood = tileStorageBoxWood;
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
