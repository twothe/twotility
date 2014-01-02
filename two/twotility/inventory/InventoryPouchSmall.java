/*
 */
package two.twotility.inventory;

import cpw.mods.fml.common.FMLLog;
import java.util.logging.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import two.twotility.TwoTility;
import two.twotility.items.ItemPouchSmall;

/**
 * @author Two
 */
public class InventoryPouchSmall implements ISidedInventory {

  public static final int INVENTORY_START = 0;
  public static final int INVENTORY_SIZE = 3 * 3;
  protected static final int[] ACCESSIBLE_SLOTS = {};
  protected static final String NBT_TAG_ITEMLIST = "items";
  protected static final String NBT_TAG_SLOT = "slot";
  protected static final String NBT_TAG_TOOLTIP = "tooltip";

  public static InventoryPouchSmall fromItemStack(final ItemStack stackPouchSmall, final EntityPlayer player) {
    final InventoryPouchSmall result = new InventoryPouchSmall(stackPouchSmall, player);
    if (player.worldObj.isRemote == false) {
      result.loadNBT();
    }
    return result;
  }
  // --- Class -----------------------------------------------------------------
  protected final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
  protected final ItemStack stackPouchSmall;
  protected final EntityPlayer owner;

  public InventoryPouchSmall(final ItemStack itemStack, final EntityPlayer owner) {
    this.stackPouchSmall = itemStack;
    this.owner = owner;
  }

  @Override
  public int getSizeInventory() {
    return inventory.length;
  }

  @Override
  public ItemStack getStackInSlot(final int slot) {
    if (slot < 0) {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested illegal slot item #%d < 0", slot);
      return null;
    } else if (slot < inventory.length) {
      return inventory[slot];
    } else {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested illegal slot item #%d > total size", slot);
      return null;
    }
  }

  @Override
  public void setInventorySlotContents(final int slot, final ItemStack itemStack) {
    if (slot < 0) {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested illegal slot item #%d < 0", slot);
    } else if (slot < inventory.length) {
      if ((itemStack == null) || (itemStack.stackSize <= getInventoryStackLimit())) {
        inventory[slot] = itemStack;
      } else {
        inventory[slot] = itemStack.splitStack(getInventoryStackLimit());
      }
      onInventoryChanged();
    } else {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested illegal slot item #%d > total size", slot);
    }
  }

  @Override
  public ItemStack decrStackSize(final int slot, final int amount) {
    ItemStack result = getStackInSlot(slot);
    if (result != null) {
      if (amount >= result.stackSize) { // usually left click
        setInventorySlotContents(slot, null);
      } else { // usually right-click
        result = result.splitStack(amount); // create a new reduced stack instead
        onInventoryChanged();
      }
    }

    return result;
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int slot) {
    final ItemStack itemStack = getStackInSlot(slot);
    setInventorySlotContents(slot, null);
    return itemStack;
  }

  @Override
  public String getInvName() {
    return ItemPouchSmall.NAME;
  }

  @Override
  public boolean isInvNameLocalized() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 4;
  }

  @Override
  public boolean isUseableByPlayer(final EntityPlayer player) {
    // Called once per tick to verify that the player is still allowed to use the container
    if (player != null) {
      return (player == this.owner); // TODO
    } else {
      FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "Requested isUseableByPlayer with null player");
      return false;
    }
  }

  @Override
  public void openChest() {
    // not used
  }

  @Override
  public void closeChest() {
    // not used
  }

  @Override
  public boolean isItemValidForSlot(final int slot, final ItemStack itemstack) {
    return ((slot >= INVENTORY_START) && (slot < INVENTORY_SIZE));
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return ACCESSIBLE_SLOTS;
  }

  public EntityPlayer getOwner() {
    return owner;
  }

  /**
   * Returns true if automation can insert the given item in the given slot from the given side.
   */
  @Override
  public boolean canInsertItem(final int slot, final ItemStack itemstack, final int side) {
    return false; // no automation
  }

  /**
   * Returns true if automation can extract the given item in the given slot from the given side.
   */
  @Override
  public boolean canExtractItem(final int slot, final ItemStack itemstack, final int side) {
    return false; // no automation
  }

  @Override
  public void onInventoryChanged() {
    writeNBT();
    this.owner.getHeldItem().setTagCompound(this.stackPouchSmall.getTagCompound());
    this.owner.inventory.onInventoryChanged();
  }

  protected void loadNBT() {
    NBTTagCompound tagCompound = this.stackPouchSmall.getTagCompound();
    if (tagCompound == null) {
      return;
    }

    final NBTTagList nbtTagList = tagCompound.getTagList(NBT_TAG_ITEMLIST);
    for (int tagCount = nbtTagList.tagCount() - 1; tagCount >= 0; --tagCount) {
      final NBTTagCompound itemEntry = (NBTTagCompound) nbtTagList.tagAt(tagCount);
      final byte slotID = itemEntry.getByte(NBT_TAG_SLOT);
      if ((slotID >= 0) && (slotID < inventory.length)) {
        inventory[slotID] = ItemStack.loadItemStackFromNBT(itemEntry);
      } else {
        FMLLog.warning(this.getClass().getSimpleName() + " received illegal NBT inventory slot. Valid range: 0-%d but got %d.", getSizeInventory(), slotID);
      }
    }
  }

  protected void writeNBT() {
    NBTTagCompound tagCompound = this.stackPouchSmall.getTagCompound();
    if (tagCompound == null) {
      tagCompound = new NBTTagCompound();
      this.stackPouchSmall.setTagCompound(tagCompound);
    }

    final NBTTagList inventoryList = new NBTTagList();

    for (byte slot = 0; slot < inventory.length; ++slot) {
      final ItemStack inventorySlot = inventory[slot];
      if (inventorySlot != null) {
        final NBTTagCompound itemEntry = new NBTTagCompound();
        itemEntry.setByte(NBT_TAG_SLOT, slot);
        inventorySlot.writeToNBT(itemEntry);
        inventoryList.appendTag(itemEntry);
      }
    }
    tagCompound.setTag(NBT_TAG_ITEMLIST, inventoryList);
  }
}
