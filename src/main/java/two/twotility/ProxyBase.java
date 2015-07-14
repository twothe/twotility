/*
 */
package two.twotility;

import java.util.ArrayList;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.blocks.*;
import two.twotility.items.*;

/**
 * @author Two
 */
public class ProxyBase {

  /* Sound */
  public final String SOUND_FLUIDSUCKIN = TwoTility.getSoundName("fluidsuckin");
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();

  public ProxyBase() {
  }

  protected void loadGlobalConfigValues() {
    TwoTilityAssets.configGrenadeDamageMultiplier = (float) TwoTility.config.getMiscDouble("Grenade damage multiplier", 1.0);
    TwoTilityAssets.configGrenadeDestroysBlocks = TwoTility.config.getMiscBoolean("Grenade can destroy blocks", true);
  }

  protected void registerBlocks() {
    TwoTilityAssets.blockAdvancedFurnace = new BlockAdvancedFurnace();
    pendingInitialization.add(TwoTilityAssets.blockAdvancedFurnace);

    TwoTilityAssets.blockLavaTank = new BlockLavaTank();
    pendingInitialization.add(TwoTilityAssets.blockLavaTank);

    TwoTilityAssets.blockShelf = new BlockShelf();
    pendingInitialization.add(TwoTilityAssets.blockShelf);

    TwoTilityAssets.blockCraftingBox = new BlockCraftingBox();
    pendingInitialization.add(TwoTilityAssets.blockCraftingBox);

    TwoTilityAssets.blockPowerStorage = new BlockPowerStorage();
    pendingInitialization.add(TwoTilityAssets.blockPowerStorage);
  }

  protected void registerItems() {
    TwoTilityAssets.itemLavaTank = new ItemLavaTank(TwoTilityAssets.blockLavaTank);
    pendingInitialization.add(TwoTilityAssets.itemLavaTank);

    TwoTilityAssets.itemPouchSmall = new ItemPouchSmall();
    pendingInitialization.add(TwoTilityAssets.itemPouchSmall);

    TwoTilityAssets.itemCraftingBox = new ItemCraftingBox(TwoTilityAssets.blockCraftingBox);
    pendingInitialization.add(TwoTilityAssets.itemCraftingBox);

    TwoTilityAssets.itemTeddy = new ItemTeddy();
    pendingInitialization.add(TwoTilityAssets.itemTeddy);

    TwoTilityAssets.itemGrenade = new ItemGrenade();
    pendingInitialization.add(TwoTilityAssets.itemGrenade);

    TwoTilityAssets.itemPowerStorageUpgradePotato = new ItemPowerStorageUpgradePotato();
    pendingInitialization.add(TwoTilityAssets.itemPowerStorageUpgradePotato);

    TwoTilityAssets.itemPowerStorageUpgradeWood = new ItemPowerStorageUpgradeWood();
    pendingInitialization.add(TwoTilityAssets.itemPowerStorageUpgradeWood);

    TwoTilityAssets.itemPowerStorageUpgradeIron = new ItemPowerStorageUpgradeIron();
    pendingInitialization.add(TwoTilityAssets.itemPowerStorageUpgradeIron);

    TwoTilityAssets.itemPowerStorageUpgradeDiamond = new ItemPowerStorageUpgradeDiamond();
    pendingInitialization.add(TwoTilityAssets.itemPowerStorageUpgradeDiamond);
  }

  protected void registerRenderers() {
  }

  public void onPreInit() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void onInit() {
    loadGlobalConfigValues();
    registerBlocks();
    registerItems();
    registerRenderers();

    for (final InitializableModContent content : pendingInitialization) {
      content.initialize();
    }
    pendingInitialization.clear();
  }

  public void onPostInit() {
  }
}
