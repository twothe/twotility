/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package two.twotility.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraftforge.oredict.ShapedOreRecipe;
import two.twotility.TwoTility;
import two.twotility.tiles.TilePowerStorage;
import two.util.BlockUtil;

/**
 *
 * @author Two
 */
public class BlockPowerStorage extends BlockWithInventory {

  public static final String NAME = "powerblock";
//-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected IIcon iconDefault;

  public BlockPowerStorage() {
    super(Material.iron, TilePowerStorage.class);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, soundTypeMetal, 1.5F, BlockUtil.HARVEST_TOOL_PICKAXE, BlockUtil.HARVEST_LEVEL_IRON);

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(this),
              "IrI",
              "rRr",
              "IrI",
              'I', "ingotIron",
              'r', "dustRedstone",
              'R', "blockRedstone"
      ));
    }

  }
}
