/*
 */
package two.twotility;

import java.util.ArrayList;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import two.twotility.blocks.BlockCraftingBox;
import two.twotility.blocks.BlockAdvancedFurnace;
import two.twotility.blocks.BlockLavaTank;
import two.twotility.blocks.BlockShelf;
import two.twotility.items.ItemCraftingBox;
import two.twotility.items.ItemLavaTank;
import two.twotility.items.ItemPouchSmall;
import two.twotility.items.ItemTNTStick;
import two.twotility.items.ItemTeddy;

/**
 * @author Two
 */
public class ProxyBase {

  /* Items */
  public ItemLavaTank itemLavaTank;
  public ItemPouchSmall itemPouchSmall;
  public ItemCraftingBox itemCraftingBox;
  public ItemTeddy itemTeddy;
  public ItemTNTStick itemTNTStick;
  /* Blocks */
  public BlockAdvancedFurnace blockAdvancedFurnace;
  public BlockLavaTank blockLavaTank;
  public BlockShelf blockShelf;
  public BlockCraftingBox blockCraftingBox;
  /* Sound */
  public final String SOUND_FLUIDSUCKIN = TwoTility.getSoundName("fluidsuckin");
  /* Global Config vars */
  public float configTNTStickDamageMultiplier;
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();

  public ProxyBase() {
  }

  protected void loadGlobalConfigValues() {
    configTNTStickDamageMultiplier = (float) TwoTility.config.getMiscDouble("TNT-Stick damage multiplier", 0.75);
  }

  protected void registerBlocks() {
    blockAdvancedFurnace = new BlockAdvancedFurnace();
    pendingInitialization.add(blockAdvancedFurnace);

    blockLavaTank = new BlockLavaTank();
    pendingInitialization.add(blockLavaTank);

    blockShelf = new BlockShelf();
    pendingInitialization.add(blockShelf);

    blockCraftingBox = new BlockCraftingBox();
    pendingInitialization.add(blockCraftingBox);
  }

  protected void registerItems() {
    itemLavaTank = new ItemLavaTank(blockLavaTank);
    pendingInitialization.add(itemLavaTank);

    itemPouchSmall = new ItemPouchSmall();
    pendingInitialization.add(itemPouchSmall);

    itemCraftingBox = new ItemCraftingBox(blockCraftingBox);
    pendingInitialization.add(itemCraftingBox);

    itemTeddy = new ItemTeddy();
    pendingInitialization.add(itemTeddy);

    itemTNTStick = new ItemTNTStick();
    pendingInitialization.add(itemTNTStick);
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

  @ForgeSubscribe
  public void onSoundSetup(final SoundLoadEvent event) {
    event.manager.addSound(SOUND_FLUIDSUCKIN + ".ogg");
  }
}
