/*
 * Copyright (c) by Stefan Feldbinder aka Two
 */
package two.twotility;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import java.util.Locale;

/**
 *
 * @author Two
 */
@Mod(modid = "TwoTility", name = "TwoTility", version = "131220")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class TwoTility {

  public static final String MOD_NAME = "TwoTility";
  public static final String MOD_ID = "TwoTility";
  public static final TwoTilityCreativeTab creativeTab = new TwoTilityCreativeTab();
  @Mod.Instance("TwoTility")
  public static TwoTility instance;
  @SidedProxy(clientSide = "two.twotility.ProxyClient", serverSide = "two.twotility.ProxyServer")
  public static ProxyBase proxy;
  public static final Config config = new Config();
  public static final GuiHandler guiHandler = new GuiHandler();

  public static String getTextureName(final String filePrefix) {
    return TwoTility.MOD_ID + ":" + filePrefix.toLowerCase(Locale.ENGLISH);
  }

  public static String getBlockName(final String blockName) {
    return TwoTility.MOD_ID + ":Block" + blockName;
  }

  public static String getItemName(final String itemName) {
    return TwoTility.MOD_ID + ":Item" + itemName;
  }

  public static String getSoundName(final String soundName) {
    return TwoTility.MOD_ID + ":" + soundName;
  }

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    config.initialize(event.getSuggestedConfigurationFile());

    proxy.onPreInit();
  }

  @Mod.EventHandler
  public void load(final FMLInitializationEvent event) {
    config.load();
    NetworkRegistry.instance().registerGuiHandler(TwoTility.instance, guiHandler);
    proxy.onInit();
    config.readOther();
    config.save();
  }

  @Mod.EventHandler
  public void postInit(final FMLPostInitializationEvent event) {
    proxy.onPostInit();
  }
}
