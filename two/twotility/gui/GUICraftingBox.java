/*
 */
package two.twotility.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.blocks.BlockCraftingBox;
import two.twotility.inventory.ContainerCraftingBox;
import two.twotility.tiles.TileCraftingBox;

/**
 * @author Two
 */
public class GUICraftingBox extends GuiContainer {

  public static final int HEIGHT_RECIPE_ROW = 24;
  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockCraftingBox.NAME_BOX);
  protected final TileCraftingBox tileCraftingBox;
  protected final int boxHeight;
  protected final int playerInventoryHeight;
  protected final boolean isTypeBox;

  public GUICraftingBox(final InventoryPlayer inventoryPlayer, final TileCraftingBox tileCraftingBox) {
    super((new ContainerCraftingBox(inventoryPlayer, tileCraftingBox)).layout());
    this.tileCraftingBox = tileCraftingBox;

    isTypeBox = tileCraftingBox.isCraftingBoxType();
    this.xSize = 171;
    this.ySize = isTypeBox ? 198 - HEIGHT_RECIPE_ROW : 198;
    boxHeight = isTypeBox ? 95 : 119;
    playerInventoryHeight = 78;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);

    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, boxHeight);
    drawTexturedModalRect(guiLeft, guiTop + boxHeight + 1, 0, 120, xSize, playerInventoryHeight);

    if (isTypeBox == false) {
      final int selectedRecipeX = 6 + 18 * tileCraftingBox.getSelectedRecipeIndex();
      drawTexturedModalRect(guiLeft + selectedRecipeX, guiTop + 100, 171, 0, 16, 16);
    }
  }
}
