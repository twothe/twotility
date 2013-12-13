/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.Config;
import two.twotility.TwoTility;
import static two.twotility.blocks.BlockLavaFurnace.NAME;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockSideTest extends Block {

  public static final String NAME = "sidetest";
  protected final Icon[] sides = new Icon[6];

  public BlockSideTest() {
    super(Config.getBlockID(BlockSideTest.class), Material.rock);
  }

  public BlockSideTest initialize() {
    setHardness(3.5F);
    setStepSound(soundStoneFootstep);
    setUnlocalizedName(NAME);
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Side Test Block");
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));

    return this;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    for (int i = 0; i < sides.length; ++i) {
      sides[i] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + i);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    return sides[side];
  }
}
