/*
 */
package two.twotility.items;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.util.ItemUtil;

/**
 * @author Two
 */
public class ItemTeddy extends ItemBase {

  public static final String NAME = "teddy";
  protected static final String KEY_TOOLTIP = TwoTility.getTooltipName(NAME);

  public ItemTeddy() {
    super();
  }

  @Override
  public void initialize() {
    setBaseValues(NAME);
    setMaxStackSize(1);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              " W ",
              "WWW",
              "WWW",
              'W', new ItemStack(Blocks.wool, 1, 12));
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    final String tooltip = ItemUtil.getCachedTooltip(KEY_TOOLTIP);
    if (tooltip != null) {
      strings.add(tooltip);
    }
  }
}
