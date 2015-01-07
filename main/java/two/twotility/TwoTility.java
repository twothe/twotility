/*
 * Copyright (c) by Stefan Feldbinder aka Two
 */
package two.twotility;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import java.util.Locale;

/**
 *
 * @author Two
 */
@Mod(modid = TwoTility.MOD_ID, name = TwoTility.MOD_NAME, version = TwoTility.MOD_VERSION)
public class TwoTility {

  public static final String MOD_NAME = "TwoTility";
  public static final String MOD_ID = "TwoTility";
  public static final String MOD_VERSION = "172.1.0";
  //----------------------------------------------------------------------------
  public static final TwoTilityCreativeTab creativeTab = new TwoTilityCreativeTab();
  @Mod.Instance("TwoTility")
  public static TwoTility instance;
  @SidedProxy(clientSide = "two.twotility.ProxyClient", serverSide = "two.twotility.ProxyServer")
  public static ProxyBase proxy;
  public static final Config config = new Config();
  public static final GuiHandler guiHandler = new GuiHandler();

  public static String getTextureName(final String filePrefix) {
    return TwoTility.MOD_ID + ":" + filePrefix;
  }

  public static String getSoundName(final String soundName) {
    return TwoTility.MOD_ID + ":" + soundName;
  }

  public static String getTooltipName(final String itemName) {
    return getTooltipName(itemName, null);
  }

  public static String getTooltipName(final String itemName, final String suffix) {
    return "item." + itemName + ".tooltip" + (suffix == null || suffix.length() == 0 ? "" : "." + suffix);
  }

  public static String getEntityName(final String name) {
    return "entity." + name;
  }

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    config.initialize(event.getSuggestedConfigurationFile());

    proxy.onPreInit();
  }

  @Mod.EventHandler
  public void load(final FMLInitializationEvent event) {
    config.load();
    NetworkRegistry.INSTANCE.registerGuiHandler(TwoTility.instance, guiHandler);
    proxy.onInit();
    config.save();
  }

  @Mod.EventHandler
  public void postInit(final FMLPostInitializationEvent event) {
    proxy.onPostInit();
  }
}
