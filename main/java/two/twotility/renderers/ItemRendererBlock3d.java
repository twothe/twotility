/*
 */
package two.twotility.renderers;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import two.twotility.items.ItemBlock3d;
import two.util.BlockSide;

/**
 * @author Two
 */
public class ItemRendererBlock3d implements IItemRenderer {

  static final boolean testflagColour = true;          // if true - render cube with each face a different colour
  static boolean wrongRendererMsgWritten = false;      // if renderer is called with the wrong item, it prints an error msg once only, by setting this flag to stop subsequent prints

  @Override
  public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
    switch (type) {
      case ENTITY:
      case EQUIPPED:
      case EQUIPPED_FIRST_PERSON:
      case INVENTORY:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper) {
    switch (type) {
      case ENTITY: {
        return (helper == ItemRendererHelper.ENTITY_BOBBING
                || helper == ItemRendererHelper.ENTITY_ROTATION
                || helper == ItemRendererHelper.BLOCK_3D);
      }
      case EQUIPPED: {
        return (helper == ItemRendererHelper.BLOCK_3D || helper == ItemRendererHelper.EQUIPPED_BLOCK);
      }
      case EQUIPPED_FIRST_PERSON: {
        return helper == ItemRendererHelper.EQUIPPED_BLOCK;
      }
      case INVENTORY: {
        return helper == ItemRendererHelper.INVENTORY_BLOCK;
      }
      default: {
        return false;
      }
    }
  }

  private enum TransformationTypes {

    NONE, DROPPED, INVENTORY
  };

  @Override
  public void renderItem(final ItemRenderType type, final ItemStack itemStack, Object... data) {
    final Item itemFromStack = itemStack.getItem();
    if (!(itemFromStack instanceof ItemBlock3d)) {
      FMLLog.warning("ItemRendererBlock3d called for item{%s}, which is not a sub-class of ItemBlock3d!", itemFromStack == null ? "null" : itemFromStack.getClass().getName());
      return;
    }
    final ItemBlock3d item = (ItemBlock3d) itemFromStack;
    final int metadata = item.getMetadata(itemStack.getItemDamage());
    final Tessellator tessellator = Tessellator.instance;
    tessellator.startDrawingQuads();

    // adjust rendering space to match what caller expects
    TransformationTypes transformationToBeUndone = TransformationTypes.NONE;
    switch (type) {
      case EQUIPPED:
      case EQUIPPED_FIRST_PERSON: {
        break; // caller expects us to render over [0,0,0] to [1,1,1], no transformation necessary
      }
      case INVENTORY: {  // caller expects [-0.5, -0.5, -0.5] to [0.5, 0.5, 0.5]
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        break;
      }
      case ENTITY: {
        // translate our coordinates and scale so that [0,0,0] to [1,1,1] translates to the [-0.25, -0.25, -0.25] to [0.25, 0.25, 0.25] expected by the caller.
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        transformationToBeUndone = TransformationTypes.DROPPED;
        break;
      }
      default:
        break; // never here
    }

    IIcon icon = item.getIcon(BlockSide.WEST.ordinal(), metadata);
    tessellator.setNormal(1.0F, 0.0F, 0.0F);
    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double) icon.getMinU(), (double) icon.getMaxV());

    icon = item.getIcon(BlockSide.EAST.ordinal(), metadata);
    tessellator.setNormal(-1.0F, 0.0F, 0.0F);
    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double) icon.getMinU(), (double) icon.getMaxV());

    icon = item.getIcon(BlockSide.SOUTH.ordinal(), metadata);
    tessellator.setNormal(0.0F, 0.0F, -1.0F);
    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double) icon.getMinU(), (double) icon.getMaxV());

    icon = item.getIcon(BlockSide.NORTH.ordinal(), metadata);
    tessellator.setNormal(0.0F, 0.0F, 1.0F);
    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double) icon.getMinU(), (double) icon.getMaxV());

    icon = item.getIcon(BlockSide.TOP.ordinal(), metadata);
    tessellator.setNormal(0.0F, 1.0F, 0.0F);
    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double) icon.getMinU(), (double) icon.getMaxV());

    icon = item.getIcon(BlockSide.BOTTOM.ordinal(), metadata);
    tessellator.setNormal(0.0F, -1.0F, 0.0F);
    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double) icon.getMaxU(), (double) icon.getMaxV());
    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double) icon.getMaxU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double) icon.getMinU(), (double) icon.getMinV());
    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double) icon.getMinU(), (double) icon.getMaxV());

    tessellator.draw();

    switch (transformationToBeUndone) {
      case NONE: {
        break;
      }
      case DROPPED: {
        GL11.glTranslatef(0.5F, 0.5F, 0.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        break;
      }
      case INVENTORY: {
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        break;
      }
      default:
        break;
    }
  }
}
