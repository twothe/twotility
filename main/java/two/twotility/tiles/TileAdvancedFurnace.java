/*
 */
package two.twotility.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.container.ContainerAdvancedFurnace;
import two.twotility.container.ContainerBase;
import two.twotility.fluid.FluidDrainTarget;
import two.twotility.gui.GUIAdvancedFurnace;
import two.util.ItemUtil;

/**
 * @author Two
 */
public class TileAdvancedFurnace extends TileWithInventory implements IFluidHandler {

  protected static final int FUEL_PER_LAVA_BLOCK = 20000;// 1 lava source block = 100 operations
  protected static final int SMELTING_DURATION = 160; // in ticks
  protected static final int FUEL_PER_TICK = FUEL_PER_LAVA_BLOCK / SMELTING_DURATION / 100;
  protected static final int REFILL_TICK_RATE = 20;
  protected static final int STORED_FUEL_MAX = 1600; // in ticks. This is not a limit, depending on the fuel used, this can be exceeded by a lot
  protected static final String NBT_TAG_FUEL_STORED = "fuelStored";
  protected static final String NBT_TAG_SMELTTIME_REMAINING = "smeltTimeRemaining";
  //--- Inventory declaration --------------------------------------------------
  public static final int INVENTORY_SIZE_INPUT = 3 * 5;
  public static final int INVENTORY_SIZE_OUTPUT = 3 * 5;
  public static final int INVENTORY_SIZE_FUEL = 4;
  public static final int INVENTORY_SIZE_PROCESSING = 1;
  public static final int INVENTORY_SIZE = INVENTORY_SIZE_INPUT + INVENTORY_SIZE_OUTPUT + INVENTORY_SIZE_FUEL + INVENTORY_SIZE_PROCESSING;
  public static final int INVENTORY_START_INPUT = 0;
  public static final int INVENTORY_START_FUEL = INVENTORY_START_INPUT + INVENTORY_SIZE_INPUT;
  public static final int INVENTORY_START_OUTPUT = INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL;
  public static final int INVENTORY_START_PROCESSING = INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT;
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE - INVENTORY_SIZE_PROCESSING];

  static {
    for (int i = 0; i < INVENTORY_START_PROCESSING; ++i) {
      ACCESSIBLE_SLOTS[i] = i;
    }
  }

  //--- Class ------------------------------------------------------------------
  protected final FluidStack LAVA_FLUIDSTACK = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
  protected int nextRefillAttempt = REFILL_TICK_RATE;
  protected FluidDrainTarget lastLavaSource = null;
  protected int storedFuel = 0; // internal buffer for prepared fuel
  protected int smeltTimer = -1;
  protected int nextSoundEffect = 20;
  protected ItemStack currentSmeltingItem = null; // cache value to prevent unnecessary smeltery lookups

  public TileAdvancedFurnace() {
    super(INVENTORY_SIZE);
  }

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setInteger(NBT_TAG_FUEL_STORED, storedFuel);
    tag.setInteger(NBT_TAG_SMELTTIME_REMAINING, smeltTimer);
  }

  @Override
  public void readFromNBT(final NBTTagCompound tag) {
    super.readFromNBT(tag);
    storedFuel = tag.getInteger(NBT_TAG_FUEL_STORED);
    smeltTimer = tag.getInteger(NBT_TAG_SMELTTIME_REMAINING);
  }

  @Override
  public void updateEntity() {
    if (worldObj.isRemote) {
    } else {
      if (--nextRefillAttempt <= 0) {
        nextRefillAttempt = REFILL_TICK_RATE;
        refill(); // if neccessary
      }

      final ItemStack itemInProgress = getStackInSlot(INVENTORY_START_PROCESSING);
      if (smeltTimer < 0) {
        tryBeginSmelting(itemInProgress);
      } else if (smeltTimer == 0) {
        finishSmelting(itemInProgress);
      } else if (smeltTimer > 0) {
        tryContinueSmelting(itemInProgress);
      }
    }
  }

  protected void tryBeginSmelting(final ItemStack itemInProgress) {
    if (itemInProgress == null) {
      final ItemStack smeltableItem = findSmeltableItem();
      if (smeltableItem != null) {
        setInventorySlotContents(INVENTORY_START_PROCESSING, smeltableItem);
        currentSmeltingItem = smeltableItem;
        updateMetadata();
      }
    } else {
      if (checkCanSmeltItem(itemInProgress)) {
        smeltTimer = SMELTING_DURATION;
      } else {
        tryClearProgressSlot(itemInProgress);
      }
    }
  }

  protected void finishSmelting(final ItemStack itemInProgress) {
    ItemStack smeltResult = ItemUtil.getSmeltingResult(itemInProgress);
    if (smeltResult == null) {
      smeltResult = itemInProgress;
    }
    if (tryClearProgressSlot(smeltResult)) {
      smeltTimer = -1;
    }
  }

  protected void tryContinueSmelting(final ItemStack itemInProgress) {
    if (itemInProgress == null) {
      smeltTimer = -1;
      currentSmeltingItem = null;
    } else {
      if (checkCanSmeltItem(itemInProgress)) {
        if (storedFuel >= FUEL_PER_TICK) {
          --smeltTimer;
          changeStoredFuel(-FUEL_PER_TICK);
        }
      } else {
        if (tryClearProgressSlot(itemInProgress)) {
          smeltTimer = -1;
        }
      }
    }
  }

  protected boolean checkCanSmeltItem(final ItemStack itemInProgress) {
    if (currentSmeltingItem != itemInProgress) {
      if (ItemUtil.canSmelt(itemInProgress)) {
        currentSmeltingItem = itemInProgress;
      } else {
        currentSmeltingItem = null;
      }
    }
    return (itemInProgress == currentSmeltingItem);
  }

  protected ItemStack findSmeltableItem() {
    for (int slot = INVENTORY_START_INPUT; slot < INVENTORY_START_INPUT + INVENTORY_SIZE_INPUT; ++slot) {
      if (ItemUtil.canSmelt(getStackInSlot(slot))) {
        return decrStackSize(slot, 1);
      }
    }
    return null;
  }

  protected boolean tryClearProgressSlot(final ItemStack smeltResult) {
    currentSmeltingItem = null;
    if (smeltResult == null) {
      return true;
    } else if (tryAddToOutput(smeltResult)) {
      decrStackSize(INVENTORY_START_PROCESSING, 1);
      markDirty();
      updateMetadata();
      return true;
    } else {
      return false;
    }
  }

  protected boolean tryAddToOutput(final ItemStack newItem) {
    if (newItem == null) {
      return true;
    }
    ItemStack itemInSlot;
    boolean inventoryChanged = false;

    if (newItem.isStackable()) { // try to stack with existing items if possible
      for (int slot = INVENTORY_START_OUTPUT; slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT; ++slot) {
        itemInSlot = getStackInSlot(slot);

        if (itemInSlot != null && itemInSlot.getItem() == newItem.getItem() && (!newItem.getHasSubtypes() || newItem.getItemDamage() == itemInSlot.getItemDamage()) && ItemStack.areItemStackTagsEqual(newItem, itemInSlot)) {
          final int newSlotStackSize = Math.min(itemInSlot.stackSize + newItem.stackSize, newItem.getMaxStackSize());
          final int remaining = newItem.stackSize - (newSlotStackSize - itemInSlot.stackSize);
          if (remaining != newItem.stackSize) { // was something moved?
            inventoryChanged = true;
            newItem.stackSize = remaining;
            itemInSlot.stackSize = newSlotStackSize;
          }
          if (remaining == 0) {
            break;
          }
        }
      }
    }

    if (newItem.stackSize > 0) { // if there is anything left, put it in the next free slot
      for (int slot = INVENTORY_START_OUTPUT; slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT; ++slot) {
        itemInSlot = getStackInSlot(slot);

        if (itemInSlot == null) {
          setInventorySlotContents(slot, newItem.copy());
          newItem.stackSize = 0;
          inventoryChanged = true;
          break;
        }
      }
    }

    return inventoryChanged;
  }

  protected void refill() {
    if (storedFuel < STORED_FUEL_MAX) {
      if (refillWithLava() == false) {
        refillWithInternalFuel();
      }
    }
  }

  @SideOnly(Side.CLIENT)
  public void setStoredFuelForGUI(final int storedFuel) {
    this.storedFuel = storedFuel;
  }

  @SideOnly(Side.CLIENT)
  public void setSmeltTimerForGUI(final int smeltTime) {
    this.smeltTimer = smeltTime;
  }

  protected void changeStoredFuel(final int change) {
    if (change != 0) {
      this.storedFuel += change;
      updateMetadata();
      markDirty();
    }
  }

  protected void updateMetadata() {
    if (worldObj.isRemote == false) {
      final int currentMetadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
      final int newMetadata = BlockAdvancedFurnace.updateState(currentMetadata, this.storedFuel > 0, this.inventory[INVENTORY_START_PROCESSING] != null);
      if (newMetadata != currentMetadata) {
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMetadata, 3);
      }
    }
  }

  protected boolean refillWithInternalFuel() {
    ItemStack fuelItem;
    int burnTime;
    for (int slot = INVENTORY_START_FUEL; slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL; ++slot) {
      fuelItem = getStackInSlot(slot);
      burnTime = TileEntityFurnace.getItemBurnTime(fuelItem);
      if (burnTime > 0) {
        if ((fuelItem.getItem() != Items.lava_bucket) || tryAddToOutput(new ItemStack(Items.bucket))) {
          decrStackSize(slot, 1);
          changeStoredFuel(burnTime);
          return true;
        }
      }
    }
    return false;
  }

  protected boolean refillWithLava() {
    if (tryRefillFrom(this.lastLavaSource)) {
      return true;
    } else {
      lastLavaSource = null; // this has become invalid or empty somehow
      return tryDrainFromNearestLavaSource();
    }
  }

  protected boolean tryRefillFrom(final FluidDrainTarget target) {
    if ((target != null) && target.isValid()) {
      final FluidStack drainedFluid = target.tryDrainLava(LAVA_FLUIDSTACK);
      if (LAVA_FLUIDSTACK.isFluidEqual(drainedFluid) && (drainedFluid.amount > 0)) {
        changeStoredFuel((int) MBToFuel(drainedFluid.amount)); // successfully drained enough lava for this many operations (rounded down)
        return true;
      }
    }
    return false;
  }

  protected boolean tryDrainFromNearestLavaSource() {
    ForgeDirection movingLavaDirection = ForgeDirection.UNKNOWN;
    TileEntity tileEntity;
    for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
      if (worldObj.getBlock(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ).getMaterial() == Material.lava) {// is it lava?
        if (tryRefillFromLavaBlock(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ)) {
          return true;
        } else {
          movingLavaDirection = direction;
        }
      }
      tileEntity = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
      FluidDrainTarget newTarget = null;
      if (tileEntity instanceof IFluidHandler) {
        newTarget = new FluidDrainTarget((IFluidHandler) tileEntity, tileEntity, direction);
      } else if (tileEntity instanceof IFluidTank) {
        newTarget = new FluidDrainTarget((IFluidTank) tileEntity, tileEntity, direction);
      }
      if (tryRefillFrom(newTarget)) {
        this.lastLavaSource = newTarget;
        return true;
      }
    }

    if (movingLavaDirection != ForgeDirection.UNKNOWN) {
      return tryDrainFollowingLavaFlow(movingLavaDirection);
    }
    return false;
  }

  protected boolean tryRefillFromLavaBlock(final int x, final int y, final int z) {
    if (worldObj.getBlockMetadata(x, y, z) == 0) { // is this a source block?
      worldObj.setBlockToAir(x, y, z);
      changeStoredFuel(FUEL_PER_LAVA_BLOCK); // successfully drained a lava block
      worldObj.playSoundEffect((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D, TwoTility.proxy.SOUND_FLUIDSUCKIN, 0.6F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
      return true;
    } else {
      return false;
    }
  }
  protected final static ForgeDirection[] VALID_LAVA_SEARCH_DIRECTIONS = {ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST};

  protected boolean tryDrainFollowingLavaFlow(final ForgeDirection direction) {
    int x = xCoord + direction.offsetX, y = yCoord + direction.offsetY, z = zCoord + direction.offsetZ;
    int meta, bestMeta = worldObj.getBlockMetadata(x, y, z) + 1; // +1 to make up-flows more important
    ForgeDirection bestLavaDirection;
    boolean hasLavaUp;

    for (int loop = TwoTility.proxy.blockAdvancedFurnace.lavaFlowSearchMax; loop > 0; --loop) {
      if (tryRefillFromLavaBlock(x, y, z)) {
        return true; // we found a source block
      } else {
        bestLavaDirection = ForgeDirection.UNKNOWN;
        hasLavaUp = false;
        for (final ForgeDirection dir : VALID_LAVA_SEARCH_DIRECTIONS) {
          if (worldObj.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getMaterial() == Material.lava) {
            meta = worldObj.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) + 1;// +1 to make up-flows more important
            if (meta > 8) {
              meta = 1; // up-flow is most important flow
            }
            if (meta < bestMeta) {
              bestMeta = meta;
              bestLavaDirection = dir;
            }
            if (dir == ForgeDirection.UP) {
              hasLavaUp = true;
            }
          }
        }

        if (hasLavaUp) { // if there was laval upwards, always go there first
          bestLavaDirection = ForgeDirection.UP;
          bestMeta = Integer.MAX_VALUE;
        } else if (bestLavaDirection == ForgeDirection.UNKNOWN) {
          return false; // there was no lava with better flow value around
        }

        x += bestLavaDirection.offsetX;
        y += bestLavaDirection.offsetY;
        z += bestLavaDirection.offsetZ;
      }
    }
    return false;
  }

  public int getStoredFuel() {
    return this.storedFuel;
  }

  /**
   * Returns the time left till the current item is smelted.
   *
   * @return the time left till the current item is smelted, or -1 if no item is in progress.
   */
  public int getRemainingSmeltTime() {
    return this.smeltTimer;
  }

  public double getStoredOperationsInPercent() {
    if (storedFuel > STORED_FUEL_MAX) {
      return 1.0;
    } else if (storedFuel < 0) {
      return 0.0;
    } else {
      return ((double) storedFuel) / ((double) STORED_FUEL_MAX);
    }
  }

  @Override
  public ContainerBase createContainer(final EntityPlayer player) {
    return new ContainerAdvancedFurnace(player.inventory, this);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Gui createGUI(final EntityPlayer player) {
    return new GUIAdvancedFurnace(player.inventory, this);
  }
  //----------------------------------------------------------------------------
  //--- ISidedInventory --------------------------------------------------------
  //----------------------------------------------------------------------------

  @Override
  public String getInventoryName() {
    return BlockAdvancedFurnace.NAME;
  }

  @Override
  public boolean isItemValidForSlot(final int slot, final ItemStack itemstack) {
    if ((slot >= INVENTORY_START_PROCESSING) && (slot < INVENTORY_START_PROCESSING + INVENTORY_SIZE_PROCESSING)) {
      return false;
    } else if ((slot >= INVENTORY_START_OUTPUT) && (slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT)) {
      return false;
    } else if ((slot >= INVENTORY_START_FUEL) && (slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL)) {
      return TileEntityFurnace.isItemFuel(itemstack);
    } else {
      return ItemUtil.canSmelt(itemstack);
    }
  }

  /**
   * Returns true if automation can extract the given item in the given slot from the given side.
   */
  @Override
  public boolean canExtractItem(final int slot, final ItemStack itemstack, final int side) {
    if ((slot >= INVENTORY_START_OUTPUT) && (slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT)) {
      return true;
    } else if ((slot >= INVENTORY_START_FUEL) && (slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL)) {
      return ItemUtil.isStackHolding(itemstack, Items.bucket);
    } else {
      return false;
    }
  }

  @Override
  public int[] getAccessibleSlotsFromSide(int var1) {
    return ACCESSIBLE_SLOTS;
  }

  //----------------------------------------------------------------------------
  //--- IFluidHandler ----------------------------------------------------------
  //----------------------------------------------------------------------------
  @Override
  public boolean canFill(final ForgeDirection from, final Fluid fluid) {
    return ((fluid != null) && (fluid.getID() == LAVA_FLUIDSTACK.fluidID));
  }

  @Override
  public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
    int amountTaken = 0;
    if (LAVA_FLUIDSTACK.isFluidEqual(resource) && (this.storedFuel < STORED_FUEL_MAX) && (resource.amount > 0)) {
      final int amountRequired = (int) Math.ceil(fuelToMB(STORED_FUEL_MAX * 2 - storedFuel)); // get some extra free lava here to not have to refill every tick
      amountTaken = Math.min((int) fuelToMB(Math.floor(MBToFuel(resource.amount))), amountRequired);
      if (doFill) {
        changeStoredFuel((int) MBToFuel(amountTaken));
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
  //--- Convenience functions --------------------------------------------------
  protected static final double FUEL_PER_LAVA_MB = ((double) FUEL_PER_LAVA_BLOCK) / ((double) FluidContainerRegistry.BUCKET_VOLUME);  // 1 bucket contains 1 lava source block
  protected static final int MB_CAPACITY = (int) Math.ceil(STORED_FUEL_MAX * FUEL_PER_LAVA_MB); // in ticks. This is not a limit, depending on the fuel used, this can be exceeded by a lot

  @Override
  public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    return new FluidTankInfo[]{new FluidTankInfo(LAVA_FLUIDSTACK.copy(), MB_CAPACITY)};
  }

  public static double fuelToMB(final double fuel) {
    return fuel / FUEL_PER_LAVA_MB;
  }

  public static double MBToFuel(final double milliBuckets) {
    return milliBuckets * FUEL_PER_LAVA_MB;
  }
}
