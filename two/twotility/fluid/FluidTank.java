/*
 */
package two.twotility.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

/**
 * @author Two
 */
public class FluidTank implements IFluidTank {

  protected static final String NBT_TAG_CAPACITY = "BucketFluidTank_capacity";
  protected final FluidStack internalStorage;
  protected int capacity;

  public FluidTank(final Fluid fluid, final int amount, final int capacity) {
    this(fluid.getID(), amount, capacity);
  }

  public FluidTank(final int fluidID, final int amount, final int capacity) {
    this.internalStorage = new FluidStack(fluidID, amount);
    this.capacity = capacity;
  }

  public void readFromNBT(final NBTTagCompound tag) {
    final FluidStack stackRead = FluidStack.loadFluidStackFromNBT(tag);
    this.internalStorage.fluidID = stackRead.fluidID;
    this.internalStorage.amount = stackRead.amount;
    this.internalStorage.tag = stackRead.tag;
    this.capacity = tag.getInteger(NBT_TAG_CAPACITY);
  }

  public void writeToNBT(final NBTTagCompound tag) {
    this.internalStorage.writeToNBT(tag);
    tag.setInteger(NBT_TAG_CAPACITY, this.capacity);
  }

  @Override
  public FluidStack getFluid() {
    return this.internalStorage;
  }

  @Override
  public int getFluidAmount() {
    return this.internalStorage.amount;
  }

  @Override
  public int getCapacity() {
    return this.capacity;
  }

  @Override
  public FluidTankInfo getInfo() {
    return new FluidTankInfo(this);
  }

  @Override
  public int fill(final FluidStack resource, final boolean doFill) {
    if (this.canFill(resource)) {
      return this.fill(resource.amount, doFill);
    }
    return 0;
  }

  public int fill(final int maxFill, final boolean doFill) {
    final int amountToAdd = getEffectiveFillAmount(maxFill);
    if (amountToAdd > 0) {
      if (doFill) {
        internalStorage.amount += amountToAdd;
      }
      return amountToAdd;
    }
    return 0;
  }

  protected int getEffectiveFillAmount(final int requested) {
    final int spaceLeft = this.capacity - internalStorage.amount;
    if (requested <= spaceLeft) {
      return requested;
    } else {
      return spaceLeft;
    }
  }

  /**
   * Drain a specific Fluid and amount from this tank.
   *
   * @param resource a FluidStack describing the Fluid that should be drained and the maximum amount to drain.
   * @param doDrain false if this should only check if such an operation would be successful, true otherwise.
   * @return the Fluid and amount that was/would be drained by this operation, null if no Fluid was/would be drained.
   */
  public FluidStack drain(final FluidStack resource, final boolean doDrain) {
    if (this.canDrain(resource)) {
      return drain(resource.amount, doDrain);
    }
    return null;
  }

  @Override
  public FluidStack drain(final int maxDrain, final boolean doDrain) {
    final int ammountToDrain = getEffectiveDrainAmount(maxDrain);
    if (ammountToDrain > 0) {
      if (doDrain) {
        internalStorage.amount -= ammountToDrain;
      }
      return new FluidStack(internalStorage, ammountToDrain);
    }
    return null;
  }

  protected int getEffectiveDrainAmount(final int requested) {
    if (internalStorage.amount >= requested) {
      return requested;
    } else {
      return internalStorage.amount;
    }
  }

  public int getAmountInBuckets() {
    return internalStorage.amount / FluidContainerRegistry.BUCKET_VOLUME;
  }

  public int getCapacityInBuckets() {
    return this.capacity / FluidContainerRegistry.BUCKET_VOLUME;
  }

  /**
   * Changes the fluid type in this tank to <i>fluid</i>
   *
   * @param fluid the fluid to change to
   * @return true if the tank actually changed, false if it was already set to that fluid.
   */
  public boolean changeFluidType(final Fluid fluid) {
    return changeFluidType(fluid.getID());
  }

  public boolean changeFluidType(final int fluidID) {
    if (this.internalStorage.fluidID != fluidID) {
      this.internalStorage.fluidID = fluidID;
      return true;
    }
    return false;
  }

  /**
   * Checks if this can theoretically fill with the given type of fluid.
   * This ignores whether or not there is actually enough space left to actually do it.
   *
   * @param fluid the fluid in question
   * @return true if this tank is theoretically able to add any amount of the given fluid, false otherwise.
   */
  public boolean canFill(final Fluid fluid) {
    return ((fluid != null) && canFill(fluid.getID()));
  }

  public boolean canFill(final FluidStack fluid) {
    return ((fluid != null) && canFill(fluid.fluidID) && (fluid.amount > 0));
  }

  public boolean canFill(final int fluidID) {
    return (fluidID == this.internalStorage.fluidID);
  }

  /**
   * Checks if this can theoretically drain the given type of fluid.
   * This ignores whether or not there is actually any amount left to actually do it.
   *
   * @param fluid the fluid in question
   * @return true if this tank is theoretically able to return any amount of the given fluid, false otherwise.
   */
  public boolean canDrain(final Fluid fluid) {
    return ((fluid != null) && canDrain(fluid.getID()));
  }

  public boolean canDrain(final FluidStack fluid) {
    return ((fluid != null) && canDrain(fluid.fluidID) && (fluid.amount > 0));
  }

  public boolean canDrain(final int fluidID) {
    return (fluidID == this.internalStorage.fluidID);
  }
}
