/*
 */
package two.twotility.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.blocks.BlockLavaFurnace;

/**
 * @author Two
 */
public class TileLavaFurnace extends TileEntity {

  protected static final int TICKS_BETWEEN_UPDATE = 10;
  protected static final int LAVA_PER_OPERATION = FluidContainerRegistry.BUCKET_VOLUME / 100; // 1 bucket = 100 operations
  protected static final int STORED_OPERATIONS_MAX = 8; // this is not a limit, depending on the fuel used, this can be exceeded by a lot
  protected static final String NBT_TAG_AMOUNT = "LavaFurnace_amount";

  /**
   * Storage class for the nearest lava tank
   */
  protected class LavaTankTarget {

    final IFluidHandler fluidHandler;
    final TileEntity tileEntity;
    final ForgeDirection direction;

    public LavaTankTarget(IFluidHandler fluidHandler, TileEntity tileEntity, ForgeDirection direction) {
      this.fluidHandler = fluidHandler;
      this.tileEntity = tileEntity;
      this.direction = direction;
    }
  }
  //--- Class ------------------------------------------------------------------
  protected final FluidStack lavaStack = new FluidStack(FluidRegistry.LAVA, LAVA_PER_OPERATION);
  protected int nextUpdate = 0;
  protected LavaTankTarget lavaTank = null;
  protected int storedOperations = 0; // internal buffer for prepared fuel

  @Override
  public void readFromNBT(final NBTTagCompound tag) {
    super.readFromNBT(tag);
    storedOperations = tag.getByte(NBT_TAG_AMOUNT);
  }

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setByte(NBT_TAG_AMOUNT, (byte) (storedOperations & 0xFF)); // any excess operations will be lost, but >255 requires some strange mod-fuel
  }

  public boolean doSomething() {
    return false;
  }

  @Override
  public void updateEntity() {
    if (worldObj.isRemote) {
      return;
    }
    if (--nextUpdate <= 0) {
      nextUpdate = TICKS_BETWEEN_UPDATE;
      refill(); // if neccessary
    }
  }

  protected void refill() {
    if (storedOperations < STORED_OPERATIONS_MAX) {
      if (refillWithLava() == false) {
        refillWithInternalFuel();
      }
      System.out.println("LavaFurnace currently stores " + storedOperations + " operations");
    }
  }

  @Override
  public void onInventoryChanged() {
    if (worldObj.isRemote) {
      return;
    }
    final int currentMetadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    final int newMetadata = BlockLavaFurnace.createState(currentMetadata, lavaTank != null, false);
    if (newMetadata != currentMetadata) {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMetadata, 2);
    }
    super.onInventoryChanged();
  }

  protected void changeStoredOperations(final int change) {
    if (change != 0) {
      this.storedOperations += change;
      onInventoryChanged();
    }
  }

  protected void refillWithInternalFuel() {
  }

  protected boolean refillWithLava() {
    if ((lavaTank == null) || lavaTank.tileEntity.isInvalid() || (tryDrainLava(lavaTank) == false)) {
      this.lavaTank = tryDrainFromNearestLavaTank(); // returns null if none is available
    }
    return (this.lavaStack != null);
  }

  protected boolean tryDrainLava(final LavaTankTarget target) {
    return tryDrainLava(target.fluidHandler, target.direction);
  }

  protected boolean tryDrainLava(final IFluidHandler fluidHandler, final ForgeDirection direction) {
    if (fluidHandler.canDrain(direction, FluidRegistry.LAVA)) {
      final FluidStack drainedFluid = fluidHandler.drain(direction, lavaStack, true);
      if ((drainedFluid != null) && drainedFluid.isFluidStackIdentical(lavaStack)) {
        changeStoredOperations(1); // successfully drained enough lava for one operation
        System.out.println("Valid FluidHandler at [" + (xCoord + direction.offsetX) + ", " + (yCoord + direction.offsetY) + "," + (zCoord + direction.offsetZ) + "] returned " + drainedFluid.amount + " mB Lava");
        return true;
      }
    }
    return false;
  }

  protected LavaTankTarget tryDrainFromNearestLavaTank() {
    TileEntity tileEntity;
    for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
      tileEntity = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
      if (tileEntity instanceof IFluidHandler) {
        if (tryDrainLava((IFluidHandler) tileEntity, direction)) {
          return new LavaTankTarget((IFluidHandler) tileEntity, tileEntity, direction);
        }
      }
    }
    return null;
  }
}
