/*
 */
package two.twotility.tiles;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.blocks.BlockAdvancedFurnace;

/**
 * @author Two
 */
public class TileAdvancedFurnace extends TileEntity implements IFluidHandler {

  protected static final int REFILL_TICK_RATE = 20;
  protected static final int OPERATIONS_PER_LAVA_BLOCK = 100;// 1 lava source block = 100 operations
  protected static final int LAVA_PER_OPERATION = FluidContainerRegistry.BUCKET_VOLUME / OPERATIONS_PER_LAVA_BLOCK;  // 1 bucket contains 1 lava source block
  protected static final int STORED_OPERATIONS_MAX = 8; // this is not a limit, depending on the fuel used, this can be exceeded by a lot
  protected static final String NBT_TAG_OPERATIONS_STORED = "AdvancedFurnace_operationsStored";

  /**
   * Storage class for the nearest lava tank
   */
  protected class LavaDrainTarget {

    final IFluidHandler fluidHandler;
    final TileEntity tileEntity;
    final ForgeDirection direction;

    public LavaDrainTarget(IFluidHandler fluidHandler, TileEntity tileEntity, ForgeDirection direction) {
      this.fluidHandler = fluidHandler;
      this.tileEntity = tileEntity;
      this.direction = direction;
    }
  }
  //--- Class ------------------------------------------------------------------
  protected final FluidStack lavaStack = new FluidStack(FluidRegistry.LAVA, STORED_OPERATIONS_MAX * LAVA_PER_OPERATION);
  protected int nextRefillAttempt = 0;
  protected LavaDrainTarget lavaDrainTarget = null;
  protected int storedOperations = 0; // internal buffer for prepared fuel

  @Override
  public void readFromNBT(final NBTTagCompound tag) {
    super.readFromNBT(tag);
    storedOperations = tag.getByte(NBT_TAG_OPERATIONS_STORED);
  }

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setByte(NBT_TAG_OPERATIONS_STORED, (byte) (storedOperations & 0xFF)); // any excess operations will be lost, but >255 requires some strange mod-fuel
  }

  public boolean doSomething() {
    return false;
  }

  @Override
  public void updateEntity() {
    if (worldObj.isRemote) {
      return;
    }
    if (--nextRefillAttempt <= 0) {
      nextRefillAttempt = REFILL_TICK_RATE;
      refill(); // if neccessary
    }
  }

  protected void refill() {
    if (storedOperations < STORED_OPERATIONS_MAX) {
      if (refillWithLava() == false) {
        refillWithInternalFuel();
      }
    }
  }

  @Override
  public void onInventoryChanged() {
    if (worldObj.isRemote) {
      return;
    }
    final int currentMetadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    final int newMetadata = BlockAdvancedFurnace.createState(currentMetadata, this.storedOperations > 0, false);
    if (newMetadata != currentMetadata) {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMetadata, 2);
    }
    super.onInventoryChanged();
  }

  protected void changeStoredOperations(final int change) {
    if (change != 0) {
      this.storedOperations += change;
      onInventoryChanged();
//      System.out.println("Advanced Furnace now has fuel for " + this.storedOperations + " operations");
    }
  }

  protected void refillWithInternalFuel() {
    //TODO
  }

  protected boolean refillWithLava() {
    if ((lavaDrainTarget != null) && (lavaDrainTarget.tileEntity.isInvalid() == false) && tryDrainLava(lavaDrainTarget)) {
      return true;
    } else {
      lavaDrainTarget = null; // this has become invalid somehow
      return tryDrainFromNearestLavaSource();
    }
  }

  protected boolean tryDrainLava(final LavaDrainTarget target) {
    return tryDrainLava(target.fluidHandler, target.direction);
  }

  protected boolean tryDrainLava(final IFluidHandler fluidHandler, final ForgeDirection direction) {
    if (fluidHandler.canDrain(direction, FluidRegistry.LAVA)) {
      final FluidStack drainedFluid = fluidHandler.drain(direction, lavaStack, true);
      if (lavaStack.isFluidEqual(drainedFluid) && (drainedFluid.amount > 0)) {
        changeStoredOperations((drainedFluid.amount / LAVA_PER_OPERATION) * LAVA_PER_OPERATION); // successfully drained enough lava for this many operations
        return true;
      }
    }
    return false;
  }

  protected boolean tryDrainFromNearestLavaSource() {
    TileEntity tileEntity;
    for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
      if ((worldObj.getBlockMaterial(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ) == Material.lava) && // is it lava?
              (worldObj.getBlockMetadata(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ) == 0)) { // is this a source block?
        worldObj.setBlockToAir(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
        changeStoredOperations(OPERATIONS_PER_LAVA_BLOCK); // successfully drained a lava block
        return true;
      }
      tileEntity = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
      if (tileEntity instanceof IFluidHandler) {
        if (tryDrainLava((IFluidHandler) tileEntity, direction)) {
          this.lavaDrainTarget = new LavaDrainTarget((IFluidHandler) tileEntity, tileEntity, direction);
          return true;
        }
      }
    }
    return false;
  }

  //--- IFluidHandler ----------------------------------------------------------
  @Override
  public boolean canFill(final ForgeDirection from, final Fluid fluid) {
    return ((fluid != null) && (fluid.getID() == lavaStack.fluidID));
  }

  @Override
  public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
    int amountTaken = 0;
    if (lavaStack.isFluidEqual(resource) && (this.storedOperations < STORED_OPERATIONS_MAX) && (resource.amount > 0)) {
      final int amountRequired = (STORED_OPERATIONS_MAX * 2 - storedOperations) * LAVA_PER_OPERATION; // get some extra free lava here to not have to refill every tick
      amountTaken = Math.min((resource.amount / LAVA_PER_OPERATION) * LAVA_PER_OPERATION, amountRequired);
      if (doFill) {
        this.changeStoredOperations(amountTaken);
      }
    }
    return amountTaken;
  }

  //--- U NO TAKE MY LAVA!! ----------------------------------------------------
  @Override
  public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
    return null;
  }

  @Override
  public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
    return null;
  }

  @Override
  public boolean canDrain(ForgeDirection from, Fluid fluid) {
    return false;
  }

  @Override
  public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    return new FluidTankInfo[]{new FluidTankInfo(lavaStack.copy(), STORED_OPERATIONS_MAX * LAVA_PER_OPERATION)};
  }
}
