/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package two.twotility.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import two.twotility.GuiHandler;
import two.twotility.blocks.BlockPowerStorage;
import two.twotility.container.ContainerPowerStorage;
import two.twotility.tiles.TilePowerStorage;

/**
 *
 * @author Two
 */
public class GUIPowerStorage extends GuiContainer {

  protected static final ResourceLocation background = GuiHandler.loadGuiPNG(BlockPowerStorage.NAME);
  protected final TilePowerStorage tilePowerStorage;

  public GUIPowerStorage(final InventoryPlayer inventoryPlayer, final TilePowerStorage tilePowerStorage) {
    super((new ContainerPowerStorage(inventoryPlayer, tilePowerStorage)).layout());
    this.tilePowerStorage = tilePowerStorage;
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
