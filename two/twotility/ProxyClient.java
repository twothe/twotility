/*
 */
package two.twotility;

import net.minecraftforge.client.MinecraftForgeClient;
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
  }
}
