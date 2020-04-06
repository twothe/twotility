/*
 */
package two.twotility.items;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.twotility.entities.EntityGrenade;
import two.twotility.util.ItemUtil;

/**
 * @author Two
 */
public class ItemGrenade extends ItemBase {

  public static final String NAME = "grenade";
  protected static final String KEY_TOOLTIP = TwoTility.getTooltipName(NAME);

  public ItemGrenade() {
    super();
  }

  @Override
  public void initialize() {
    setBaseValues(NAME);
    EntityRegistry.registerModEntity(EntityGrenade.class, TwoTility.getEntityName(NAME), 210, TwoTility.instance, 64, 1, true);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addShapelessRecipe(new ItemStack(this),
              new ItemStack(Items.clay_ball),
              new ItemStack(Items.gunpowder),
              new ItemStack(Items.string));
    }
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer player) {
    if (player.capabilities.isCreativeMode == false) {
      --itemStack.stackSize;
    }
    world.playSoundAtEntity(player, "game.tnt.primed", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

    if (!world.isRemote) {
      world.spawnEntityInWorld(new EntityGrenade(world, player));
    }

    return itemStack;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    final String toolTip = ItemUtil.getCachedTooltip(KEY_TOOLTIP);
    if (toolTip != null) {
      strings.add(toolTip);
    }
  }
}
