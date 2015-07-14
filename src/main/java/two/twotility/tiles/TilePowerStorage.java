/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package two.twotility.tiles;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockPowerStorage;
import two.twotility.container.ContainerBase;
import two.twotility.container.ContainerPowerStorage;
import two.twotility.gui.GUIPowerStorage;

/**
 *
 * @author Two
 */
public class TilePowerStorage extends TileWithInventory implements IEnergyHandler {

  public static final int INVENTORY_START_STORAGE = 0;
  public static final int INVENTORY_SIZE_STORAGE = 5 * 2;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_STORAGE;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE_STORAGE];

  static {
    int index = 0;
    for (int slot = INVENTORY_START_STORAGE; slot < INVENTORY_SIZE_STORAGE; ++slot) {
      ACCESSIBLE_SLOTS[index++] = slot;
    }
  }

  protected int energyStoredCurrently = 0;
  protected int energyStoredMax = 0;

  public TilePowerStorage() {
    super(INVENTORY_SIZE);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
    return (itemstack.getItem() instanceof IEnergyContainerItem) && super.isItemValidForSlot(slot, itemstack);
  }

  @Override
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerPowerStorage(player.inventory, this);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUIPowerStorage(player.inventory, this);
  }

  @Override
  public String getInventoryName() {
    return BlockPowerStorage.NAME;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return ACCESSIBLE_SLOTS;
  }

  @Override
  public int receiveEnergy(final ForgeDirection from, final int maxReceive, final boolean simulate) {
    int energyRemainng = maxReceive;
    ItemStack itemStack;
    for (int i = 0; i < this.getSizeInventory(); ++i) {
      if (energyRemainng <= 0) {
        break;
      }
      itemStack = this.getStackInSlot(i);
      if (itemStack != null) {
        if (itemStack.getItem() instanceof IEnergyContainerItem) {
          final IEnergyContainerItem storage = (IEnergyContainerItem) itemStack.getItem();
          energyRemainng -= storage.receiveEnergy(itemStack, energyRemainng, simulate);
        } else {
          FMLLog.log(TwoTility.MOD_ID, Level.WARN, "Found %s inside PowerStorage.", itemStack.getItem() == null ? "null" : itemStack.getItem().getUnlocalizedName());
        }
      }
    }
    final int energyReceived = (maxReceive - energyRemainng);
    if (simulate == false) {
      this.energyStoredCurrently += energyReceived;
    }
    return energyReceived;
  }

  @Override
  public int extractEnergy(final ForgeDirection from, final int maxExtract, final boolean simulate) {
    int energyExtracted = 0;
    ItemStack itemStack;
    for (int i = 0; i < this.getSizeInventory(); ++i) {
      if (energyExtracted >= maxExtract) {
        break;
      }
      itemStack = this.getStackInSlot(i);
      if (itemStack != null) {
        if (itemStack.getItem() instanceof IEnergyContainerItem) {
          final IEnergyContainerItem storage = (IEnergyContainerItem) itemStack.getItem();
          energyExtracted += storage.extractEnergy(itemStack, maxExtract - energyExtracted, simulate);
        } else {
          FMLLog.log(TwoTility.MOD_ID, Level.WARN, "Found %s inside PowerStorage.", itemStack.getItem() == null ? "null" : itemStack.getItem().getUnlocalizedName());
        }
      }
    }
    if (simulate == false) {
      this.energyStoredCurrently -= energyExtracted;
    }
    return energyExtracted;
  }

  @Override
  public int getEnergyStored(final ForgeDirection from) {
    return this.energyStoredCurrently;
  }

  @Override
  public int getMaxEnergyStored(final ForgeDirection from) {
    return this.energyStoredMax;
  }

  protected void updateEnergyStats() {
    this.energyStoredCurrently = 0;
    this.energyStoredMax = 0;
    ItemStack itemStack;
    for (int i = 0; i < this.getSizeInventory(); ++i) {
      itemStack = this.getStackInSlot(i);
      if (itemStack != null) {
        if (itemStack.getItem() instanceof IEnergyContainerItem) {
          final IEnergyContainerItem storage = (IEnergyContainerItem) itemStack.getItem();
          this.energyStoredCurrently += storage.getEnergyStored(itemStack);
          this.energyStoredMax += storage.getMaxEnergyStored(itemStack);
        } else {
          FMLLog.log(TwoTility.MOD_ID, Level.WARN, "Found %s inside PowerStorage.", itemStack.getItem() == null ? "null" : itemStack.getItem().getUnlocalizedName());
        }
      }
    }
  }

  @Override
  public void markDirty() {
    super.markDirty();
    updateEnergyStats();
  }

  @Override
  public boolean canConnectEnergy(final ForgeDirection from) {
    return true;
  }
}
