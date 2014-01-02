/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.logging.Level;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import two.twotility.TwoTility;
import two.twotility.container.ContainerBase;

/**
 * @author Two
 */
public abstract class TileWithInventory extends TileEntity implements ISidedInventory {

  protected static final String NBT_TAG_ITEMLIST = "items";
  protected static final String NBT_TAG_SLOT = "slot";
  // --- Class -----------------------------------------------------------------
  protected final ItemStack[] inventory;

  public TileWithInventory(final int size) {
    this.inventory = new ItemStack[size];
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
  public boolean isInvNameLocalized() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(final EntityPlayer player) {
    // Called once per tick to verify that the player is still allowed to use the container
    if (player != null) {
      return (player.getDistanceSq(xCoord, yCoord, zCoord) <= 5 * 5);
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
    return ((slot >= 0) && (slot < inventory.length));
  }

  /**
   * Returns true if automation can insert the given item in the given slot from the given side.
   */
  @Override
  public boolean canInsertItem(final int slot, final ItemStack itemstack, final int side) {
    return this.isItemValidForSlot(slot, itemstack);
  }

  /**
   * Returns true if automation can extract the given item in the given slot from the given side.
   */
  @Override
  public boolean canExtractItem(final int slot, final ItemStack itemstack, final int side) {
    return true;
  }

  public abstract ContainerBase createContainer(final EntityPlayer player);

  public abstract Gui createGUI(final EntityPlayer player);

  protected static void readInventoryFromNBT(final NBTTagCompound tagCompound, final String inventoryName, final ItemStack[] inventory) {
    final NBTTagList inventoryList = tagCompound.getTagList(inventoryName);
    for (int tagCount = inventoryList.tagCount() - 1; tagCount >= 0; --tagCount) {
      final NBTTagCompound itemEntry = (NBTTagCompound) inventoryList.tagAt(tagCount);
      final byte slotID = itemEntry.getByte(NBT_TAG_SLOT);
      if ((slotID >= 0) && (slotID < inventory.length)) {
        inventory[slotID] = ItemStack.loadItemStackFromNBT(itemEntry);
      } else {
        FMLLog.warning(TileWithInventory.class.getSimpleName() + " received illegal NBT inventory slot. Valid range: 0-%d but got %d.", inventory.length, slotID);
      }
    }
  }

  @Override
  public void readFromNBT(final NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);

    readInventoryFromNBT(tagCompound, NBT_TAG_ITEMLIST, inventory);
  }

  protected static void writeInventoryToNBT(final NBTTagCompound tagCompound, final String inventoryName, final ItemStack[] inventory) {
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
    tagCompound.setTag(inventoryName, inventoryList);
  }

  @Override
  public void writeToNBT(final NBTTagCompound tagCompound) {
    super.writeToNBT(tagCompound);

    writeInventoryToNBT(tagCompound, NBT_TAG_ITEMLIST, inventory);
  }

  public void spillOutInventory() {
    this.spillOutInventory(inventory);
  }

  public void spillOutInventory(final ItemStack[] inventory) {
    for (final ItemStack itemstack : inventory) {
      if (itemstack != null) {
        final float modX = worldObj.rand.nextFloat() * 0.8F + 0.1F;
        final float modY = worldObj.rand.nextFloat() * 0.8F + 0.1F;
        final float modZ = worldObj.rand.nextFloat() * 0.8F + 0.1F;

        while (itemstack.stackSize > 0) {
          int stackSplit = worldObj.rand.nextInt(21) + 10;

          if (stackSplit > itemstack.stackSize) {
            stackSplit = itemstack.stackSize;
          }

          itemstack.stackSize -= stackSplit;
          final EntityItem entityitem = new EntityItem(worldObj, (double) ((float) xCoord + modX), (double) ((float) yCoord + modY), (double) ((float) zCoord + modZ), new ItemStack(itemstack.itemID, stackSplit, itemstack.getItemDamage()));

          if (itemstack.hasTagCompound()) {
            entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
          }

          final float baseVelocity = 0.05F;
          entityitem.motionX = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity);
          entityitem.motionY = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity + 0.2F);
          entityitem.motionZ = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity);
          worldObj.spawnEntityInWorld(entityitem);
        }
      }
    }
  }
}
