/*
 */
package two.twotility.items;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.inventory.ContainerPouchSmall;
import two.twotility.gui.GUICallback;
import two.twotility.gui.GUIPouchSmall;
import two.twotility.inventory.InventoryPouchSmall;
import two.twotility.tiles.TilePouchSmall;
import two.util.InvalidTileEntityException;
import two.util.Logging;

/**
 * @author Two
 */
public class ItemPouchSmall extends ItemBase implements GUICallback {

  public static final String NAME = "PouchSmall";
  protected final int guiId;
  protected final TilePouchSmall tilePouchSmall;

  public ItemPouchSmall() {
    super(TwoTility.config.getItemID(ItemPouchSmall.class));
    GameRegistry.registerItem(this, TwoTility.getItemName(NAME));
    guiId = TwoTility.guiHandler.registerGui(this);
    tilePouchSmall = new TilePouchSmall();
  }

  @Override
  public void initialize() {
    setUnlocalizedName(NAME);
    setMaxStackSize(1);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    LanguageRegistry.addName(this, "Small Pouch");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this),
              " S ",
              "L L",
              " L ",
              'S', Item.silk,
              'L', Item.leather);
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack item, final World world, final EntityPlayer player) {
    if (world.isRemote == false) {
      FMLNetworkHandler.openGui(player, TwoTility.instance, guiId, world, (int) (player.posX + 0.5), (int) (player.posY + 0.5), (int) (player.posZ + 0.5));
    }
    return item;
  }

  @Override
  public Container createContainer(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final ItemStack heldItem = player.getHeldItem();
    if (heldItem.getItem().itemID == this.itemID) {
      return (new ContainerPouchSmall(player.inventory, heldItem)).layout();
    } else {
      throw new IllegalStateException("Container of " + this.getClass().getSimpleName() + " requested, but for a different item (" + heldItem.getDisplayName() + ")!");
    }
  }

  @Override
  public Gui createGUI(final EntityPlayer player, final World world, final int x, final int y, final int z) throws InvalidTileEntityException {
    final ItemStack heldItem = player.getHeldItem();
    if (heldItem.getItem().itemID == this.itemID) {
      return new GUIPouchSmall(player.inventory, heldItem);
    } else {
      throw new IllegalStateException("Container of " + this.getClass().getSimpleName() + " requested, but for a different item (" + heldItem.getDisplayName() + ")!");
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    // called once per frame if shown
    InventoryPouchSmall.addItemsToTooltip(itemStack, strings);
  }
}
