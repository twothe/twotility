/*
 */
package two.twotility;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.client.MinecraftForgeClient;
import two.twotility.entities.EntityGrenade;
import two.twotility.renderers.ItemRendererBlock3d;
import two.util.ItemUtil;

/**
 * @author Two
 */
public class ProxyClient extends ProxyBase {

  protected ItemRendererBlock3d itemRendererBlock3d;

  @Override
  public void onInit() {
    super.onInit();
    
    ItemUtil.clearCachedTooltips();
  }

  @Override
  protected void registerRenderers() {
    super.registerRenderers();

    itemRendererBlock3d = new ItemRendererBlock3d();
    MinecraftForgeClient.registerItemRenderer(itemLavaTank, itemRendererBlock3d);
    MinecraftForgeClient.registerItemRenderer(itemCraftingBox, itemRendererBlock3d);
    RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, new RenderSnowball(itemGrenade));
  }

}
