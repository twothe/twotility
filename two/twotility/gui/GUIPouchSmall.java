/*
 */

package two.twotility.gui;

import two.twotility.inventory.ContainerPouchSmall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.items.ItemPouchSmall;

/**
 * @author Two
 */
public class GUIPouchSmall extends GuiContainer {

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(ItemPouchSmall.NAME);
  protected final ItemStack stackPouchSmall;

  public GUIPouchSmall(final InventoryPlayer inventoryPlayer, final ItemStack stackPouchSmall) {
    super((new ContainerPouchSmall(inventoryPlayer, stackPouchSmall)).layout());
    this.stackPouchSmall = stackPouchSmall;
    // visible gui part
    this.xSize = 167;
    this.ySize = 138;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(background);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
  }
}
