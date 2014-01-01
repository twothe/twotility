/*
 */
package two.twotility.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public class ItemTeddy extends ItemBase {

  public static final String NAME = "Teddy";

  public ItemTeddy() {
    super(TwoTility.config.getItemID(ItemTeddy.class));
    GameRegistry.registerItem(this, TwoTility.getItemName(NAME));
  }

  @Override
  public void initialize() {
    setUnlocalizedName(NAME);
    setMaxStackSize(1);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    LanguageRegistry.addName(this, "Teddy");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              " W ",
              "WWW",
              "WWW",
              'W', new ItemStack(Block.cloth, 1, 12));
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    strings.add("It's dangerous to go alone...");
  }
}
