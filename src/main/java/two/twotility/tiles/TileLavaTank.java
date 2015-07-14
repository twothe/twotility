/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.fluid.FluidTank;
import two.util.ItemUtil;

/**
 * @author Two
 */
public class TileLavaTank extends TileEntity implements IFluidHandler {

  protected final FluidTank tank;

  public TileLavaTank() {
    this(0, 4 * FluidContainerRegistry.BUCKET_VOLUME);
  }

  public TileLavaTank(final int amount, final int capacity) {
    tank = new FluidTank(FluidRegistry.LAVA, amount, capacity, true); // true = tank will clamp to bucket size whenever possible
  }

  @Override
  public void readFromNBT(final NBTTagCompound tag) {
    super.readFromNBT(tag);
    tank.readFromNBT(tag);
  }

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tank.writeToNBT(tag);
  }

  @Override
  public void markDirty() {
    if (worldObj.isRemote) {
      return;
    }
    final int metadataCurrent = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    final int metadataNew = tank.getAmountInBuckets();
    if (metadataCurrent != metadataNew) {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadataNew, 2);
    }
  }

  @Override
  public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
    final int amountTaken = this.tank.fill(resource, doFill);
    if (doFill && (amountTaken > 0)) {
      markDirty();
      FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank, amountTaken));
    }
    return amountTaken;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
    final FluidStack result = this.tank.drain(resource, doDrain);
    if (doDrain && (result != null)) {
      markDirty();
      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank, result.amount));
    }
    return result;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
    final FluidStack result = this.tank.drain(maxDrain, doDrain);
    if (doDrain && (result != null)) {
      markDirty();
      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank, result.amount));
    }
    return result;
  }

  @Override
  public boolean canFill(final ForgeDirection from, final Fluid fluid) {
    return tank.canFill(fluid);
  }

  @Override
  public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
    return tank.canFill(fluid);
  }

  @Override
  public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    return new FluidTankInfo[]{this.tank.getInfo()};
  }

  public int getAmountInBuckets() {
    return this.tank.getAmountInBuckets();
  }

  public int getCapacityInBuckets() {
    return this.tank.getCapacityInBuckets();
  }

  public void onUse(final FillBucketEvent event) {
    final ItemStack itemInUse = event.current;
    if (FluidContainerRegistry.isFilledContainer(itemInUse)) {
      final FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemInUse);
      final int fillAmount = tank.fill(fluidStack, false);
      if (fillAmount >= FluidContainerRegistry.BUCKET_VOLUME) {
        event.setResult(Event.Result.ALLOW);
        event.result = FluidContainerRegistry.drainFluidContainer(itemInUse);
        tank.fill(fluidStack, true);
        markDirty();
      } else {
        event.setCanceled(true);
      }
    } else if (FluidContainerRegistry.isEmptyContainer(itemInUse)) {
      final int capacity = FluidContainerRegistry.getContainerCapacity(tank.getFluid(), itemInUse);
      final FluidStack drainedFluid = tank.drain(capacity, false);
      if ((drainedFluid != null) && (drainedFluid.amount == capacity)) {
        event.setResult(Event.Result.ALLOW);
        event.result = FluidContainerRegistry.fillFluidContainer(drainedFluid, itemInUse);
        if ((event.result == null) && ItemUtil.isSameItem(itemInUse, FluidContainerRegistry.EMPTY_BUCKET)) {
          event.result = new ItemStack(Items.lava_bucket);
        }
        tank.drain(capacity, true);
        markDirty();
      } else {
        event.setCanceled(true);
      }
    } else {
      event.setCanceled(true);
    }
  }
}
