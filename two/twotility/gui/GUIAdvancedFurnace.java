/*
 */
package two.twotility.gui;

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

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockAdvancedFurnace.NAME);

  public GUIAdvancedFurnace(final InventoryPlayer inventoryPlayer, final TileAdvancedFurnace tileAdvancedFurnace) {
    super(new ContainerAdvancedFurnace(inventoryPlayer, tileAdvancedFurnace));

    this.xSize = 175; // measured by hand
    this.ySize = 165;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
  }
}
