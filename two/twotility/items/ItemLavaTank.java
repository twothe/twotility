/*
 */
package two.twotility.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public class ItemLavaTank extends ItemBlock3d {

  protected static final String NAME = "LavaTank";

  public ItemLavaTank(final Block block) {
    super(TwoTility.config.getItemID(ItemLavaTank.class), block);
  }

  @Override
  public void initialize() {
    GameRegistry.registerItem(this, TwoTility.getItemName(NAME));    
  }
}
