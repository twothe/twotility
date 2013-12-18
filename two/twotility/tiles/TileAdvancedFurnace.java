/*
 */
package two.twotility.tiles;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import two.twotility.TwoTility;
import two.twotility.blocks.BlockAdvancedFurnace;

/**
 * @author Two
 */
public class TileAdvancedFurnace extends TileEntity implements IFluidHandler, ISidedInventory {

  protected static final int FUEL_PER_LAVA_BLOCK = 20000;// 1 lava source block = 100 operations
  protected static final int SMELTING_DURATION = 200; // in ticks
  protected static final int FUEL_PER_TICK = FUEL_PER_LAVA_BLOCK / SMELTING_DURATION / 100;
  protected static final int REFILL_TICK_RATE = 20;
  protected static final int STORED_FUEL_MAX = 1600; // in ticks. This is not a limit, depending on the fuel used, this can be exceeded by a lot
  protected static final String NBT_TAG_FUEL_STORED = "fuelStored";
  protected static final String NBT_TAG_SMELTTIME_REMAINING = "smeltTimeRemaining";
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
  protected static final int[] ACCESSIBLE_SLOTS = new int[INVENTORY_SIZE - INVENTORY_SIZE_PROCESSING];

  static {
    for (int i = 0; i < INVENTORY_START_PROCESSING; ++i) {
      ACCESSIBLE_SLOTS[i] = i;
    }
  }

  public static boolean canSmelt(final ItemStack item) {
    return ((item != null)
            && (FurnaceRecipes.smelting().getSmeltingList().containsKey(item.itemID)
            || FurnaceRecipes.smelting().getMetaSmeltingList().containsKey(Arrays.asList(item.itemID, item.getItemDamage()))));
  }

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
  protected final FluidStack lavaStack = new FluidStack(FluidRegistry.LAVA, (int) Math.ceil(fuelToMB(STORED_FUEL_MAX)));
  protected int nextRefillAttempt = REFILL_TICK_RATE;
  protected LavaDrainTarget lavaDrainTarget = null;
  protected int storedFuel = 0; // internal buffer for prepared fuel
  protected int smeltTimer = -1;
  protected int nextSoundEffect = 20;
  protected final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

  @Override
  public void writeToNBT(final NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setInteger(NBT_TAG_FUEL_STORED, storedFuel);
    tag.setInteger(NBT_TAG_SMELTTIME_REMAINING, smeltTimer);

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
    storedFuel = tag.getInteger(NBT_TAG_FUEL_STORED);
    smeltTimer = tag.getInteger(NBT_TAG_SMELTTIME_REMAINING);

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
      }
    } else {
      if (canSmelt(itemInProgress)) {
        smeltTimer = SMELTING_DURATION;
      } else {
        tryClearProgressSlot(itemInProgress);
      }
    }
  }

  protected void finishSmelting(final ItemStack itemInProgress) {
    final ItemStack smeltResult = canSmelt(itemInProgress)
            ? FurnaceRecipes.smelting().getSmeltingResult(itemInProgress).copy()
            : itemInProgress;
    if (tryClearProgressSlot(smeltResult)) {
      smeltTimer = -1;
    }
  }

  protected void tryContinueSmelting(final ItemStack itemInProgress) {
    if (itemInProgress == null) {
      smeltTimer = -1;
    } else {
      if (canSmelt(itemInProgress)) {
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

  protected ItemStack findSmeltableItem() {
    for (int slot = INVENTORY_START_INPUT; slot < INVENTORY_START_INPUT + INVENTORY_SIZE_INPUT; ++slot) {
      if (canSmelt(getStackInSlot(slot))) {
        return decrStackSize(slot, 1);
      }
    }
    return null;
  }

  protected boolean tryClearProgressSlot(final ItemStack smeltResult) {
    if (smeltResult == null) {
      return true;
    } else if (tryAddToOutput(smeltResult)) {
      setInventorySlotContents(INVENTORY_START_PROCESSING, null);
      onInventoryChanged();
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

        if (itemInSlot != null && itemInSlot.itemID == newItem.itemID && (!newItem.getHasSubtypes() || newItem.getItemDamage() == itemInSlot.getItemDamage()) && ItemStack.areItemStackTagsEqual(newItem, itemInSlot)) {
          final int newSlotStackSize = Math.min(itemInSlot.stackSize + newItem.stackSize, newItem.getMaxStackSize());
          final int remaining = newItem.stackSize - (newSlotStackSize - itemInSlot.stackSize);
          if (remaining != itemInSlot.stackSize) { // was something moved?
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

  protected void changeStoredFuel(final int change) {
    if (change != 0) {
      this.storedFuel += change;
      if (worldObj.isRemote == false) {
        final int currentMetadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        final int newMetadata = BlockAdvancedFurnace.createState(currentMetadata, this.storedFuel > 0, false);
        if (newMetadata != currentMetadata) {
          worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMetadata, 2);
        }
      }
      onInventoryChanged();
    }
  }

  protected boolean refillWithInternalFuel() {
    ItemStack fuelItem;
    int burnTime;
    for (int slot = INVENTORY_START_FUEL; slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL; ++slot) {
      fuelItem = getStackInSlot(slot);
      burnTime = TileEntityFurnace.getItemBurnTime(fuelItem);
      if (burnTime > 0) {
        if ((fuelItem.itemID != Item.bucketLava.itemID) || tryAddToOutput(new ItemStack(Item.bucketEmpty))) {
          decrStackSize(slot, 1);
          changeStoredFuel(burnTime);
          return true;
        }
      }
    }
    return false;
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
        changeStoredFuel((int) MBToFuel(drainedFluid.amount)); // successfully drained enough lava for this many operations (rounded down)
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
          changeStoredFuel(FUEL_PER_LAVA_BLOCK); // successfully drained a lava block
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

  public int getStoredFuel() {
    return this.storedFuel;
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
    if ((slot >= INVENTORY_START_PROCESSING) && (slot < INVENTORY_START_PROCESSING + INVENTORY_SIZE_PROCESSING)) {
      return false;
    } else if ((slot >= INVENTORY_START_OUTPUT) && (slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT)) {
      return false;
    } else if ((slot >= INVENTORY_START_FUEL) && (slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL)) {
      return TileEntityFurnace.isItemFuel(itemstack);
    } else {
      return canSmelt(itemstack);
    }
  }

  @Override
  public int[] getAccessibleSlotsFromSide(final int side) {
    return ACCESSIBLE_SLOTS;
  }

  /**
   * Returns true if automation can insert the given item in the given slot from the given side.
   */
  @Override
  public boolean canInsertItem(final int slot, final ItemStack itemstack, final int side) {
    return this.isItemValidForSlot(slot, itemstack);
  }

  /**
   * Returns true if automation can extract the given item in the given slot from the given side.
   */
  @Override
  public boolean canExtractItem(final int slot, final ItemStack itemstack, final int side) {
    if ((slot >= INVENTORY_START_OUTPUT) && (slot < INVENTORY_START_OUTPUT + INVENTORY_SIZE_OUTPUT)) {
      return true;
    } else if ((slot >= INVENTORY_START_FUEL) && (slot < INVENTORY_START_FUEL + INVENTORY_SIZE_FUEL)) {
      return itemstack.itemID == Item.bucketEmpty.itemID;
    } else {
      return false;
    }
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
    if (lavaStack.isFluidEqual(resource) && (this.storedFuel < STORED_FUEL_MAX) && (resource.amount > 0)) {
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

  @Override
  public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    return new FluidTankInfo[]{new FluidTankInfo(lavaStack.copy(), (int) Math.ceil(STORED_FUEL_MAX * FUEL_PER_LAVA_MB))};
  }
  //--- Convenience functions --------------------------------------------------
  protected static final double FUEL_PER_LAVA_MB = ((double) FUEL_PER_LAVA_BLOCK) / ((double) FluidContainerRegistry.BUCKET_VOLUME);  // 1 bucket contains 1 lava source block

  public static double fuelToMB(final double fuel) {
    return fuel / FUEL_PER_LAVA_MB;
  }

  public static double MBToFuel(final double milliBuckets) {
    return milliBuckets * FUEL_PER_LAVA_MB;
  }
}
