package two.twotility.items;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Level;
import two.twotility.TwoTility;
import two.util.ItemUtil;
import two.util.TwoMath;

/**
 *
 * @author Two
 */
public abstract class ItemPowerStorageUpgradeBase extends ItemBase implements IEnergyContainerItem {

  protected static final String NBT_TAG_ENERGY = "Energy";

  protected final String NAME;
  protected final String KEY_TOOLTIP;
  protected final int capacity;
  protected final int maxReceive;
  protected final int maxExtract;
  protected final int damageOnHit;

  public ItemPowerStorageUpgradeBase(final String name, final int defaultCapacity) {
    super();
    this.NAME = name;
    KEY_TOOLTIP = TwoTility.getTooltipName(NAME);
    this.capacity = TwoTility.config.getMiscInteger(NAME + " capacity", defaultCapacity);
    this.maxReceive = this.capacity / (20 * 5);
    this.maxExtract = this.capacity / (20 * 5);
    this.damageOnHit = Math.max(2, this.maxExtract / 5000);
  }

  protected abstract ShapedOreRecipe getRecipe(final ItemStack result);

  @Override
  @SuppressWarnings("unchecked")
  public void initialize() {
    setBaseValues(NAME);
    setMaxStackSize(1);
    setMaxDamage(100 + 1);
    setNoRepair();
    setHasSubtypes(true);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(getRecipe(setEnergyStored(new ItemStack(this), getCraftedEnergy())));
    }
  }
  
  protected int getCraftedEnergy() {
    return 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void getSubItems(final Item item, final CreativeTabs creativeTab, final List itemList) {
    itemList.add(setEnergyStored(new ItemStack(item), 0));
    itemList.add(setEnergyStored(new ItemStack(item), capacity));
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer player) {
    player.setItemInUse(itemStack, 72000);
    return itemStack;
  }

  @Override
  public int getMaxItemUseDuration(final ItemStack itemStack) {
    return 72000;
  }

  @Override
  public EnumAction getItemUseAction(ItemStack p_77661_1_) {
    return EnumAction.bow;
  }

  @Override
  public void onUsingTick(final ItemStack itemStack, final EntityPlayer player, final int count) {
    rechargeItemsInInventory(itemStack, player);
  }

  protected void rechargeItemsInInventory(final ItemStack itemStack, final EntityPlayer player) {
    final InventoryPlayer inventory = player.inventory;
    int energyRemaining = maxExtract;
    for (ItemStack armorStack : inventory.armorInventory) {
      if (energyRemaining <= 0) {
        break;
      }
      if (ItemUtil.isRFPoweredItem(armorStack)) {
        energyRemaining -= ((IEnergyContainerItem) itemStack.getItem()).receiveEnergy(itemStack, energyRemaining, false);
      }
    }
    for (ItemStack inventoryStack : inventory.mainInventory) {
      if (energyRemaining <= 0) {
        break;
      }
      if ((inventoryStack != itemStack) && ItemUtil.isRFPoweredItem(inventoryStack)) {
        energyRemaining -= ((IEnergyContainerItem) inventoryStack.getItem()).receiveEnergy(inventoryStack, energyRemaining, false);
      }
    }
    final int energyTaken = maxReceive - energyRemaining;
    this.changeEnergyStored(itemStack, -energyTaken);
  }

  @Override
  public boolean onLeftClickEntity(final ItemStack itemStack, final EntityPlayer player, final Entity entity) {
    // TODO: deal damage and discharge
    return false;
  }

  /* IEnergyContainerItem */
  @Override
  public int receiveEnergy(final ItemStack itemStack, final int maxReceive, final boolean simulate) {
    final int energyReceived = Math.min(capacity - getEnergyStored(itemStack), Math.min(this.maxReceive, maxReceive));

    if (!simulate) {
      this.changeEnergyStored(itemStack, energyReceived);
    }
    return energyReceived;
  }

  @Override
  public int extractEnergy(final ItemStack itemStack, final int maxExtract, final boolean simulate) {
    final int energyExtracted = Math.min(getEnergyStored(itemStack), Math.min(this.maxExtract, maxExtract));

    if (!simulate) {
      this.changeEnergyStored(itemStack, -energyExtracted);
    }
    return energyExtracted;
  }

  protected void changeEnergyStored(final ItemStack itemStack, final int change) {
    if (change != 0) {
      setEnergyStored(itemStack, getEnergyStored(itemStack) + change);
    }
  }

  protected ItemStack setEnergyStored(final ItemStack itemStack, final int amount) {
    if (itemStack.hasTagCompound()) {
      itemStack.getTagCompound().setInteger(NBT_TAG_ENERGY, TwoMath.withinBounds(amount, 0, capacity));
    } else {
      final NBTTagCompound tagCompound = new NBTTagCompound();
      tagCompound.setInteger(NBT_TAG_ENERGY, amount);
      itemStack.setTagCompound(tagCompound);
    }
    itemStack.setItemDamage(ItemUtil.energyAmountToDamagePercent(amount, capacity));
    return itemStack;
  }

  @Override
  public int getEnergyStored(final ItemStack itemStack) {
    try {
      return itemStack.getTagCompound().getInteger(NBT_TAG_ENERGY);
    } catch (NullPointerException e) {
      FMLLog.log(TwoTility.MOD_ID, Level.WARN, "Found %s without NBT tag.", itemStack == null ? "null" : itemStack.getItem().getUnlocalizedName());
      if ((itemStack != null) && (itemStack.getItem() instanceof ItemPowerStorageUpgradeBase)) {
        final NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger(NBT_TAG_ENERGY, 0);
        itemStack.setTagCompound(tagCompound);
      }
      return 0;
    }
  }

  @Override
  public int getMaxEnergyStored(final ItemStack itemStack) {
    return capacity;
  }

  @SideOnly(Side.CLIENT)
  @Override
  @SuppressWarnings("unchecked")
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    final String toolTip = ItemUtil.getCachedTooltip(KEY_TOOLTIP);
    if (toolTip != null) {
      strings.add(String.format(toolTip, new Object[]{Integer.toString(getEnergyStored(itemStack)), Integer.toString(capacity)}));
    }
  }

  @Override
  public boolean showDurabilityBar(final ItemStack itemStack) {
    return true;
  }

  @Override
  public boolean isDamageable() {
    return true;
  }

  @Override
  public int getMetadata(int metadata) {
    return 0;
  }

}
