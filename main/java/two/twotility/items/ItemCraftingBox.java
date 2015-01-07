/*
 */
package two.twotility.items;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockCraftingBox;

/**
 * @author Two
 */
public class ItemCraftingBox extends ItemBlock3d {

  public ItemCraftingBox(final Block block) {
    super(block);
  }

  @Override
  public void initialize() {
    GameRegistry.registerItem(this, TwoTility.getItemName(BlockCraftingBox.NAME_BOX));
  }

  @Override
  public String getUnlocalizedName(final ItemStack itemStack) {
    switch (itemStack.getItemDamage()) {
      case BlockCraftingBox.STATE_BOX:
        return BlockCraftingBox.NAME_BOX;
      case BlockCraftingBox.STATE_ADVANCED:
        return BlockCraftingBox.NAME_ADVANCED;
    }
    FMLLog.warning("Unknown state #%d for %s!", itemStack.getItemDamage(), this.getClass().getSimpleName());
    return null;
  }

  @Override
  public boolean getHasSubtypes() {
    return true;
  }
}
