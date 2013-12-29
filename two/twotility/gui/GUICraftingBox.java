/*
 */

package two.twotility.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.inventory.ContainerCraftingBox;
import two.twotility.items.ItemPouchSmall;
import two.twotility.tiles.TileCraftingBox;

/**
 * @author Two
 */
public class GUICraftingBox extends GuiContainer {

//  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockCraftingBox.NAME_BOX);
  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(ItemPouchSmall.NAME);
  protected final TileCraftingBox tileCraftingBox;

  public GUICraftingBox(final InventoryPlayer inventoryPlayer, final TileCraftingBox tileCraftingBox) {
    super((new ContainerCraftingBox(inventoryPlayer, tileCraftingBox)).layout());
    this.tileCraftingBox = tileCraftingBox;
    // visible gui part
    this.xSize = 168;
    this.ySize = 175;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
  }

}
