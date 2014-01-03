/*
 */
package two.twotility.items;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.entities.EntityTNTStick;

/**
 * @author Two
 */
public class ItemTNTStick extends ItemBase {

  public static final String NAME = "TNTStick";

  public ItemTNTStick() {
    super(TwoTility.config.getItemID(ItemTNTStick.class));
    GameRegistry.registerItem(this, TwoTility.getItemName(NAME));
    EntityRegistry.registerModEntity(EntityTNTStick.class, "Entity" + NAME, 210, TwoTility.instance, 64, 1, true);
  }

  @Override
  public void initialize() {
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    LanguageRegistry.addName(this, "TNT Stick");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addShapelessRecipe(new ItemStack(this),
              new ItemStack(Item.clay),
              new ItemStack(Item.gunpowder),
              new ItemStack(Item.silk));
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer player) {
    if (player.capabilities.isCreativeMode == false) {
      --itemStack.stackSize;
    }
    world.playSoundAtEntity(player, "random.fuse", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

    if (!world.isRemote) {
      world.spawnEntityInWorld(new EntityTNTStick(world, player));
    }

    return itemStack;
  }

  @Override
  public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List strings, final boolean verbose) {
    strings.add("Highly sophisticated technology");
  }
}
