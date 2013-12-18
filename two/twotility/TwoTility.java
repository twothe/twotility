/*
 * Copyright (c) by Stefan Feldbinder aka Two
 */
package two.twotility;

import cpw.mods.fml.common.FMLLog;
import two.twotility.blocks.BlockList;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraftforge.common.Configuration;
import two.twotility.gui.GuiHandler;

/**
 *
 * @author Two
 */
@Mod(modid = "TwoTility", name = "TwoTility", version = "131213")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class TwoTility {

  public static final String MOD_NAME = "TwoTility";
  public static final String MOD_ID = "TwoTility";
  public static final TwoTilityCreativeTab creativeTab = new TwoTilityCreativeTab();
  @Mod.Instance("TwoTility")
  public static TwoTility instance;
  @SidedProxy(clientSide = "two.twotility.ProxyClient", serverSide = "two.twotility.ProxyServer")
  public static ProxyBase proxy;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    Config.configuration = new Configuration(event.getSuggestedConfigurationFile());
    Config.configuration.load();

    GuiHandler.instance.initialize();

    BlockList.initialize();

    Config.configuration.save();
  }

  @Mod.EventHandler
  public void load(FMLInitializationEvent event) {
    proxy.initialize();
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
  }

  public static String getTextureName(final String filePrefix) {
    return TwoTility.MOD_ID + ":" + filePrefix.toLowerCase(Locale.ENGLISH);
  }

  public static String getBlockName(final String blockName) {
    return TwoTility.MOD_ID + ":" + blockName;
  }
}
