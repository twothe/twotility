/*
 */
package two.twotility;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * @author Two
 */
public class TwoTilityCreativeTab extends CreativeTabs {

  public TwoTilityCreativeTab() {
    super(TwoTility.MOD_NAME);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Item getTabIconItem() {
    return TwoTility.proxy.itemTeddy;
  }
}
