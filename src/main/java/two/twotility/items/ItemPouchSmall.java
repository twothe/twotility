/*
 */
package two.twotility.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.container.ContainerPouchSmall;
import two.twotility.gui.GUICallback;
import two.twotility.gui.GUIPouchSmall;
import two.twotility.tiles.TilePouchSmall;
import two.util.InvalidTileEntityException;
import two.util.ItemUtil;

/**
 * @author Two
 */
public class ItemPouchSmall extends ItemBase implements GUICallback {

  public static final String NAME = "pouchSmall";
  protected final int guiId;
  protected final TilePouchSmall tilePouchSmall;

  public ItemPouchSmall() {
    super();
    guiId = TwoTility.guiHandler.registerGui(this);
    tilePouchSmall = new TilePouchSmall();
  }

  @Override
  public void initialize() {
    setBaseValues(NAME);
    setMaxStackSize(1);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              " S ",
              "L L",
              " L ",
              'S', Items.string,
              'L', Items.leather);
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack item, final World world, final EntityPlayer player) {
    if (world.isRemote == false) {
      player.openGui(TwoTility.instance, guiId, world, (int) (player.posX + 0.5), (int) (player.posY + 0.5), (int) (player.posZ + 0.5));
    }
    return item;
  }

  @Override
  public boolean getHasSubtypes() {
    return true;
  }

  @Override
  public Container createContainer(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final ItemStack heldItem = player.getHeldItem();
    if (ItemUtil.isSameBaseType(heldItem, this)) {
      return (new ContainerPouchSmall(player.inventory, heldItem)).layout();
    } else {
      throw new IllegalStateException("Container of " + this.getClass().getSimpleName() + " requested, but for a different item (" + heldItem.getDisplayName() + ")!");
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final ItemStack heldItem = player.getHeldItem();
    if (ItemUtil.isSameBaseType(heldItem, this)) {
      return new GUIPouchSmall(player.inventory, heldItem);
    } else {
      throw new IllegalStateException("Container of " + this.getClass().getSimpleName() + " requested, but for a different item (" + heldItem.getDisplayName() + ")!");
    }
  }
}
