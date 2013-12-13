/*
 */
package two.twotility;

import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import two.twotility.blocks.BlockList;

/**
 * @author Two
 */
public class TwoTilityCreativeTab extends CreativeTabs {

  public static final String TAB_NAME = TwoTility.MOD_NAME;

  public TwoTilityCreativeTab() {
    super(TAB_NAME);
    LanguageRegistry.instance().addStringLocalization("itemGroup." + TAB_NAME, "en_US", TAB_NAME);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public int getTabIconItemIndex() {
    return BlockList.lavaForge.blockID;
  }
}
