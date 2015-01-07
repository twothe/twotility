/*
 */
package two.twotility.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import two.twotility.container.ContainerBase;

/**
 * @author Two
 */
public class CraftingInventoryView extends InventoryCrafting {

  protected IInventory source;
  protected final ContainerBase eventHandler;
  protected final int offset, width, height, size;
  protected boolean takeFromInventory;

  public CraftingInventoryView(final IInventory source, final ContainerBase container, final int offset, final int width, final int height, final boolean takeFromInventory) {
    super(container, width, height);
    this.eventHandler = container;
    this.width = width;
    this.height = height;
    this.size = width * height;
    this.source = source;
    this.offset = offset;
    this.takeFromInventory = takeFromInventory;
  }

  protected ItemStack takeFromSource(final int index, int amount) {
    final ItemStack requestedItem = getStackInSlot(index).copy();
    requestedItem.stackSize = 0;
    ItemStack itemStack;
    for (int i = 0; i < offset; ++i) {
      itemStack = source.getStackInSlot(i);
      if (itemStack != null && itemStack == requestedItem && (!requestedItem.getHasSubtypes() || requestedItem.getItemDamage() == itemStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(requestedItem, itemStack)) {
        itemStack = source.decrStackSize(i, amount);
        if (itemStack != null) {
          requestedItem.stackSize += itemStack.stackSize;
          if (requestedItem.stackSize >= amount) {
            break;
          }
        }
      }
    }
    return requestedItem;
  }

  public boolean isTakingFromInventory() {
    return takeFromInventory;
  }

  public void setTakeFromInventory(final boolean takeFromInventory) {
    this.takeFromInventory = takeFromInventory;
  }

  @Override
  public int getSizeInventory() {
    return size;
  }

  @Override
  public ItemStack getStackInSlot(final int index) {
    return source.getStackInSlot(index + offset);
  }

  @Override
  public ItemStack getStackInRowAndColumn(final int x, final int y) {
    return this.getStackInSlot(x + y * this.height);
  }

  @Override
  public ItemStack decrStackSize(final int index, final int amount) {
    if (takeFromInventory) {
      final ItemStack taken = takeFromSource(index, amount);
      if (taken.stackSize < amount) {
        taken.stackSize += source.decrStackSize(index + offset, amount - taken.stackSize).stackSize;
      }
      this.eventHandler.onCraftMatrixChanged(source);
      return taken;
    } else {
      final ItemStack taken = source.decrStackSize(index + offset, amount);
      this.eventHandler.onCraftMatrixChanged(source);
      return taken;
    }
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int index) {
    return source.getStackInSlotOnClosing(index + offset);
  }

  @Override
  public void setInventorySlotContents(final int index, final ItemStack itemstack) {
    source.setInventorySlotContents(index + offset, itemstack);
  }

  @Override
  public String getInventoryName() {
    return source.getInventoryName();
  }

  @Override
  public int getInventoryStackLimit() {
    return source.getInventoryStackLimit();
  }

  @Override
  public void markDirty() {
    source.markDirty();
  }

  @Override
  public boolean isUseableByPlayer(final EntityPlayer player) {
    return source.isUseableByPlayer(player);
  }

  @Override
  public void openInventory() {
    source.openInventory();
  }

  @Override
  public void closeInventory() {
    source.closeInventory();
  }

  @Override
  public boolean isItemValidForSlot(final int index, final ItemStack itemstack) {
    return source.isItemValidForSlot(index + offset, itemstack);
  }
}
