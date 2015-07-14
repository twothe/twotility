/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import two.twotility.InitializableModContent;
import two.twotility.TwoTility;

/**
 * @author Two
 */
public abstract class BlockBase extends Block implements InitializableModContent {

  public BlockBase(final Material material) {
    super(material);
  }

  /**
   * Sets a blocks base values.
   *
   * Convenient function wrapper for blocks that require no special tool to harvest.
   *
   * @param name The internal name of the block. Equals texture name and language registry name.
   * @param soundType The sound to make when walking over this block.
   * @param hardness The hardness (artificial Minecraft value) that determines how long it takes to mine this block.
   */
  protected void setBaseValues(final String name, final Block.SoundType soundType, final float hardness) {
    setBaseValues(name, TwoTility.creativeTab, soundType, hardness, null, 0);
  }

  /**
   * Sets a blocks base values.
   *
   * @param name The internal name of the block. Equals texture name and language registry name.
   * @param soundType The sound to make when walking over this block.
   * @param hardness The hardness (artificial Minecraft value) that determines how long it takes to mine this block.
   * @param harvestType The tool required to harvest this block. See BlockUtil for standard constants, but it can be any value.
   * @param harvestLevel The material level of the tool required to harvest this block. See BlockUtil for standard constants, but it can be any value.
   */
  protected void setBaseValues(final String name, final Block.SoundType soundType, final float hardness, final String harvestType, final int harvestLevel) {
    setBaseValues(name, TwoTility.creativeTab, soundType, hardness, harvestType, harvestLevel);
  }

  /**
   * Sets a blocks base values.
   *
   * Use this if the block needs to be in a different creative tab.
   *
   * @param name The internal name of the block. Equals texture name and language registry name.
   * @param creativeTab The creative tab to put this block into.
   * @param soundType The sound to make when walking over this block.
   * @param hardness The hardness (artificial Minecraft value) that determines how long it takes to mine this block.
   * @param harvestType The tool required to harvest this block. See BlockUtil for standard constants, but it can be any value.
   * @param harvestLevel The material level of the tool required to harvest this block. See BlockUtil for standard constants, but it can be any value.
   */
  protected void setBaseValues(final String name, final CreativeTabs creativeTab, final Block.SoundType soundType, final float hardness, final String harvestType, final int harvestLevel) {
    GameRegistry.registerBlock(this, name);
    setBlockName(name);
    setCreativeTab(creativeTab);
    setStepSound(soundType);
    setHardness(hardness);
    setHarvestLevel(harvestType, harvestLevel);

    setBlockTextureName(TwoTility.MOD_ID + ":" + name);
  }

}
