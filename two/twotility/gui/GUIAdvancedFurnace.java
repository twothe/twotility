/*
 */
package two.twotility.gui;

import two.twotility.GuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.tiles.TileAdvancedFurnace;

/**
 * @author Two
 */
public class GUIAdvancedFurnace extends GuiContainer {

  protected final static int LAVA_TEXTURE_HEIGHT = 36;
  
  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockAdvancedFurnace.NAME);
  protected final TileAdvancedFurnace tileAdvancedFurnace;

  public GUIAdvancedFurnace(final InventoryPlayer inventoryPlayer, final TileAdvancedFurnace tileAdvancedFurnace) {
    super(new ContainerAdvancedFurnace(inventoryPlayer, tileAdvancedFurnace));
    this.tileAdvancedFurnace = tileAdvancedFurnace;
    // visible gui part
    this.xSize = 168;
    this.ySize = 175;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    final int yLavaSize = (int) (LAVA_TEXTURE_HEIGHT * tileAdvancedFurnace.getStoredOperationsInPercent());
    if (yLavaSize > 0) {
      drawTexturedModalRect(guiLeft + 80, guiTop + 57 + LAVA_TEXTURE_HEIGHT - yLavaSize, 169, LAVA_TEXTURE_HEIGHT - yLavaSize, 7, yLavaSize);
    }
  }
}
