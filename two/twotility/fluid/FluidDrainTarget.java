/*
 */
package two.twotility.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

/**
 * @author Two
 */
public class FluidDrainTarget {

  final IFluidHandler fluidHandler;
  final IFluidTank fluidTank;
  final TileEntity tileEntity;
  final ForgeDirection direction;

  public FluidDrainTarget(final IFluidHandler fluidHandler, final TileEntity tileEntity, final ForgeDirection direction) {
    this(fluidHandler, null, tileEntity, direction);
  }

  public FluidDrainTarget(final IFluidTank fluidTank, final TileEntity tileEntity, final ForgeDirection direction) {
    this(null, fluidTank, tileEntity, direction);
  }

  public FluidDrainTarget(final IFluidHandler fluidHandler, final IFluidTank fluidTank, final TileEntity tileEntity, final ForgeDirection direction) {
    this.fluidHandler = fluidHandler;
    this.tileEntity = tileEntity;
    this.direction = direction;
    this.fluidTank = fluidTank;
  }

  public FluidStack tryDrainLava(final FluidStack amount) {
    if (fluidHandler != null) {
      return tryDrainLavaFromHandler(amount);
    } else {
      return tryDrainLavaFromTank(amount);
    }
  }

  public boolean isValid() {
    return (tileEntity.isInvalid() == false);
  }
  
  public TileEntity getTileEntity() {
    return tileEntity;
  }

  public ForgeDirection getDirection() {
    return direction;
  }

  protected FluidStack tryDrainLavaFromHandler(final FluidStack amount) {
    return fluidHandler.canDrain(direction, amount.getFluid())
            ? fluidHandler.drain(direction, amount, true)
            : null;
  }

  protected FluidStack tryDrainLavaFromTank(final FluidStack amount) {
    return (fluidTank.getFluid().fluidID == amount.fluidID)
            ? fluidTank.drain(amount.amount, true)
            : null;
  }
}
