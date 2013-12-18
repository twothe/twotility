/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.logging.Level;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.util.Logging;

/**
 * @author Two
 */
public class TileAdvancedFurnace extends TileEntity implements IFluidHandler, IInventory {

  protected static final int REFILL_TICK_RATE = 20;
  protected static final int OPERATIONS_PER_LAVA_BLOCK = 100;// 1 lava source block = 100 operations
  protected static final int LAVA_PER_OPERATION = FluidContainerRegistry.BUCKET_VOLUME / OPERATIONS_PER_LAVA_BLOCK;  // 1 bucket contains 1 lava source block
  protected static final int STORED_OPERATIONS_MAX = 8; // this is not a limit, depending on the fuel used, this can be exceeded by a lot
  protected static final String NBT_TAG_OPERATIONS_STORED = "operationsStored";
  protected static final String NBT_TAG_ITEMLIST = "items";
  protected static final String NBT_TAG_SLOT = "slot";
  //--- Inventory declaration --------------------------------------------------
  protected static final int INVENTORY_SIZE_INPUT = 3 * 5;
  protected static final int INVENTORY_SIZE_OUTPUT = 3 * 5;
  protected static final int INVENTORY_SIZE_FUEL = 4;
  protected static final int INVENTORY_SIZE_PROCESSING = 1;
  protected static final int INVENTORY_SIZE = INVENTORY_SIZE_INPUT + INVENTORY_SIZE_OUTPUT + INVENTORY_SIZE_FUEL + INVENTORY_SIZE_PROCESSING;
  protected static final int INVENTORY_START_INPUT = 0;
  protected static final int INVENTORY_START_FUEL = INVENTORY_START_INPUT + INVENTORY_SIZE_INPUT;
  protected static final int INVENTORY_START_OUTPUT = INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL;
  protected static final int INVENTORY_START_PROCESSING = INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT;

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
  protected final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setByte(NBT_TAG_OPERATIONS_STORED, (byte) (storedOperations & 0xFF)); // any excess operations will be lost, but >255 requires some strange mod-fuel

    final NBTTagList inventoryList = new NBTTagList();
    NBTTagCompound tagCompound;
    for (byte slotCount = 0; slotCount < inventory.length; ++slotCount) {
      if (inventory[slotCount] != null) {
        tagCompound = new NBTTagCompound();
        tagCompound.setByte(NBT_TAG_SLOT, slotCount);
        inventory[slotCount].writeToNBT(tagCompound);
        inventoryList.appendTag(tagCompound);
      }
    }

    tag.setTag(NBT_TAG_ITEMLIST, inventoryList);
  }

  @Override
  public void readFromNBT(final NBTTagCompound tag) {
    super.readFromNBT(tag);
    storedOperations = tag.getByte(NBT_TAG_OPERATIONS_STORED) & 0xFF;

    final NBTTagList nbttaglist = tag.getTagList(NBT_TAG_ITEMLIST);
    for (int tagCount = nbttaglist.tagCount() - 1; tagCount >= 0; --tagCount) {
      final NBTTagCompound itemEntry = (NBTTagCompound) nbttaglist.tagAt(tagCount);
      final byte slotID = itemEntry.getByte(NBT_TAG_SLOT);
      if ((slotID >= 0) && (slotID < inventory.length)) {
        inventory[slotID] = ItemStack.loadItemStackFromNBT(itemEntry);
      } else {
        FMLLog.warning("TileAdvancedFurnace received illegal NBT inventory slot. Valid range: 0-%d but got %d.", inventory.length, slotID);
      }
    }
  }

  @Override
  public void updateEntity() {
    if (worldObj.isRemote) {
      //do some fancy effects
    } else {
      if (--nextRefillAttempt <= 0) {
        nextRefillAttempt = REFILL_TICK_RATE;
        refill(); // if neccessary
      }
    }
  }

  protected void refill() {
    if (storedOperations < STORED_OPERATIONS_MAX) {
      if (refillWithLava() == false) {
        refillWithInternalFuel();
      }
    }
  }

  @SideOnly(Side.CLIENT)
  public void setStoredOperationForGUI(final int storedOperations) {
    this.storedOperations = storedOperations;
  }

  protected void changeStoredOperations(final int change) {
    if (change != 0) {
      this.storedOperations += change;
      if (worldObj.isRemote == false) {
        final int currentMetadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        final int newMetadata = BlockAdvancedFurnace.createState(currentMetadata, this.storedOperations > 0, false);
        if (newMetadata != currentMetadata) {
          worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMetadata, 2);
        }
      }
      onInventoryChanged();
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
        changeStoredOperations(drainedFluid.amount / LAVA_PER_OPERATION); // successfully drained enough lava for this many operations (rounded down)
        return true;
      }
    }
    return false;
  }

  protected boolean tryDrainFromNearestLavaSource() {
    boolean foundMovingLava = false;
    TileEntity tileEntity;
    for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
      if (worldObj.getBlockMaterial(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ) == Material.lava) {// is it lava?
        if (worldObj.getBlockMetadata(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ) == 0) { // is this a source block?
          worldObj.setBlockToAir(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
          changeStoredOperations(OPERATIONS_PER_LAVA_BLOCK); // successfully drained a lava block
          return true;
        } else {
          foundMovingLava = true;
        }
      }
      tileEntity = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
      if (tileEntity instanceof IFluidHandler) {
        if (tryDrainLava((IFluidHandler) tileEntity, direction)) {
          this.lavaDrainTarget = new LavaDrainTarget((IFluidHandler) tileEntity, tileEntity, direction);
          return true;
        }
      }
    }

    if (foundMovingLava) {
      return tryDrainFollowingLavaFlow();
    }
    return false;
  }

  protected boolean tryDrainFollowingLavaFlow() {
    return false; // TODO
  }

  public int getStoredOperations() {
    return this.storedOperations;
  }

  public double getStoredOperationsInPercent() {
    if (storedOperations > STORED_OPERATIONS_MAX) {
      return 1.0;
    } else if (storedOperations < 0) {
      return 0.0;
    } else {
      return ((double) storedOperations) / ((double) STORED_OPERATIONS_MAX);
    }
  }

  //----------------------------------------------------------------------------
  //--- IInventory -------------------------------------------------------------
  //----------------------------------------------------------------------------
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
      inventory[slot] = itemStack;
      if ((itemStack != null) && (itemStack.stackSize > getInventoryStackLimit())) {
        itemStack.stackSize = getInventoryStackLimit();
      }
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
    return BlockAdvancedFurnace.NAME; // TODO: check
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
      return (player.getDistanceSq(xCoord, yCoord, zCoord) <= 4 * 4); // TODO
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
    return true; // TODO: limit slot access
  }

  //----------------------------------------------------------------------------
  //--- IFluidHandler ----------------------------------------------------------
  //----------------------------------------------------------------------------
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
