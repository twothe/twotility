/*
 */
package two.twotility;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.client.MinecraftForgeClient;
import two.twotility.entities.EntityTNTStick;
import two.twotility.renderers.ItemRendererBlock3d;

/**
 * @author Two
 */
public class ProxyClient extends ProxyBase {

  protected ItemRendererBlock3d itemRendererBlock3d;

  @Override
  protected void registerRenderers() {
    super.registerRenderers();

    itemRendererBlock3d = new ItemRendererBlock3d();
    MinecraftForgeClient.registerItemRenderer(itemLavaTank.itemID, itemRendererBlock3d);
    MinecraftForgeClient.registerItemRenderer(itemCraftingBox.itemID, itemRendererBlock3d);
    RenderingRegistry.registerEntityRenderingHandler(EntityTNTStick.class, new RenderSnowball(itemTNTStick));
  }
}
