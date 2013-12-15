/*
 */
package two.twotility.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.fluid.FluidTank;

/**
 * @author Two
 */
public class TileLavaTank extends TileEntity implements IFluidHandler {

  protected final FluidTank tank = new FluidTank(FluidRegistry.LAVA, 4);

  public TileLavaTank() {
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
  public void onInventoryChanged() {
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
      onInventoryChanged();
      FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank));
    }
    return amountTaken;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
    final FluidStack result = this.tank.drain(resource, doDrain);
    if (doDrain && (result != null)) {
      onInventoryChanged();
      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank));
    }
    return result;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
    final FluidStack result = this.tank.drain(maxDrain, doDrain);
    if (doDrain && (result != null)) {
      onInventoryChanged();
      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(this.tank.getFluid(), worldObj, xCoord, yCoord, zCoord, this.tank));
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
}
