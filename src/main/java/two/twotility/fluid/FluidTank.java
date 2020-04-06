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
  protected static final String NBT_TAG_DOCLAMP = "BucketFluidTank_clamp";
  protected FluidStack internalStorage;
  protected boolean clampToBucketSize;
  protected int capacity;

  public FluidTank(final Fluid fluid, final int amount, final int capacity, final boolean clampToBucketSize) {
    this.internalStorage = new FluidStack(fluid, amount);
    this.capacity = capacity;
    this.clampToBucketSize = clampToBucketSize;
  }

  public void readFromNBT(final NBTTagCompound tag) {
    this.internalStorage = FluidStack.loadFluidStackFromNBT(tag);
    this.capacity = tag.getInteger(NBT_TAG_CAPACITY);
    if (tag.hasKey(NBT_TAG_DOCLAMP)) {
      this.clampToBucketSize = tag.getBoolean(NBT_TAG_DOCLAMP);
    }
  }

  public void writeToNBT(final NBTTagCompound tag) {
    this.internalStorage.writeToNBT(tag);
    tag.setInteger(NBT_TAG_CAPACITY, this.capacity);
    tag.setBoolean(NBT_TAG_DOCLAMP, this.clampToBucketSize);
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

  public void setClapToBucketSize(final boolean newClamp) {
    this.clampToBucketSize = newClamp;
  }

  public boolean isClampingToBucketSize() {
    return this.clampToBucketSize;
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
      if (isClampingToBucketSize()) {
        return clampToBucketSize(requested);
      } else {
        return requested;
      }
    } else {
      return internalStorage.amount; // if someone drains everything this should not clamp, as empty is clamped just fine.
    }
  }

  protected int clampToBucketSize(final int amount) {
    return amount / ((int) FluidContainerRegistry.BUCKET_VOLUME) * ((int) FluidContainerRegistry.BUCKET_VOLUME);
  }

  public int getAmountInBuckets() {
    return internalStorage.amount / ((int) FluidContainerRegistry.BUCKET_VOLUME);
  }

  public int getCapacityInBuckets() {
    return this.capacity / ((int) FluidContainerRegistry.BUCKET_VOLUME);
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
    return ((fluid != null) && canFill(fluid.getFluidID()) && (fluid.amount > 0));
  }

  public boolean canFill(final int fluidID) {
    return (fluidID == this.internalStorage.getFluidID());
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
    return ((fluid != null) && canDrain(fluid.getFluidID()) && (fluid.amount > 0));
  }

  public boolean canDrain(final int fluidID) {
    return (fluidID == this.internalStorage.getFluidID());
  }
}
