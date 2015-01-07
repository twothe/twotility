/*
 */
package two.util;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import two.twotility.blocks.BlockLavaTank;

/**
 * @author Two
 */
public class BlockUtil {

  /* Constants for basic harvest tools. */
  public static final String HARVEST_TOOL_AXE = "axe";
  public static final String HARVEST_TOOL_PICKAXE = "pickaxe";
  public static final String HARVEST_TOOL_SHOVEL = "shovel";
  /* Constants for basic harvest material levels */
  public static final int HARVEST_LEVEL_WOOD = 0;
  public static final int HARVEST_LEVEL_STONE = 1;
  public static final int HARVEST_LEVEL_IRON = 2;
  public static final int HARVEST_LEVEL_DIAMOND = 3;
  public static final int HARVEST_LEVEL_GOLD = 0;

  public static final FMLControlledNamespacedRegistry<Block> registry = GameData.getBlockRegistry();

  public static Block findByName(final String name) {
    return registry.getObject(name);
  }

  public static boolean isSameBlockType(final Block block, final Block other) {
    return (block == other);
  }
  
  public static boolean isAir(final Block block) {
    return (block.getMaterial() == Material.air);
  }
}
