/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import static net.minecraft.block.Block.soundStoneFootstep;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.TwoTility;
import two.twotility.inventory.ContainerAdvancedFurnace;
import two.twotility.gui.GUIAdvancedFurnace;
import two.twotility.inventory.ContainerBase;
import two.twotility.tiles.TileAdvancedFurnace;
import two.twotility.tiles.TileWithInventory;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockAdvancedFurnace extends BlockWithInventory {

  protected static final String CONFIG_KEY_LAVA_FLOW_MAX = "Lava flow search max";
  public static final String NAME = "AdvancedFurnace";
  protected static final int STATE_EMPTY = 0;
  protected static final int STATE_FILLED = STATE_EMPTY + 1;
  protected static final int STATE_HAS_FUEL = STATE_FILLED + 1;
  protected static final int STATE_WORKING = STATE_HAS_FUEL + 1;
  protected static final int NUM_STATES = STATE_WORKING + 1;
  //-- Class -------------------------------------------------------------------
  public final int lavaFlowSearchMax;
  @SideOnly(Side.CLIENT)
  protected Icon[] stateIcons = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon iconSide;
  @SideOnly(Side.CLIENT)
  protected Icon iconTop;

  public BlockAdvancedFurnace() {
    super(TwoTility.config.getBlockID(BlockAdvancedFurnace.class), Material.rock, TileAdvancedFurnace.class);
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
    lavaFlowSearchMax = TwoTility.config.getMiscInteger(CONFIG_KEY_LAVA_FLOW_MAX, 128);
  }

  @Override
  public void initialize() {
    setHardness(2.5F);
    setResistance(9.0f);
    setStepSound(soundStoneFootstep);
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Advanced Furnace");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this, 1),
              " R ",
              "CFC",
              " B ",
              'C', Block.chest,
              'F', Block.furnaceIdle,
              'R', Item.redstone,
              'B', Item.bucketEmpty);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    stateIcons[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    stateIcons[STATE_FILLED] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_filled");
    stateIcons[STATE_HAS_FUEL] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_fuel");
    stateIcons[STATE_WORKING] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_working");

    iconSide = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_side");
    iconTop = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_top");
  }

  protected Icon getFrontfaceByState(final int state) {
    if ((state >= 0) && (state < stateIcons.length)) {
      return stateIcons[state];
    } else {
      FMLLog.warning("Illegal front face state #%d for %s.", state, this.getClass().getSimpleName());
      return stateIcons[0];
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    final BlockSide rotatedSide = BlockSide.getRotatedSide(side, metadata);
    switch (rotatedSide) {
      case NORTH:
        return getFrontfaceByState(BlockSide.getStateFromMetadata(metadata));
      case TOP:
      case BOTTOM:
        return iconTop;
      default:
        return iconSide;
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = BlockSide.createState(BlockSide.getDirectionFacing(entity), 0);
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
    super.onBlockPlacedBy(world, z, x, y, entity, itemStack);
  }

  @Override
  protected ContainerBase doCreateContainer(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z) {
    return new ContainerAdvancedFurnace(player.inventory, (TileAdvancedFurnace) tileEntity);
  }

  @Override
  protected Gui doCreateGUI(final EntityPlayer player, final TileWithInventory tileEntity, final World world, final int x, final int y, final int z) {
    return new GUIAdvancedFurnace(player.inventory, (TileAdvancedFurnace) tileEntity);
  }

  public static int createState(final int metaCurrent, final boolean hasFuel, final boolean hasWork) {
    return BlockSide.createState(metaCurrent, (hasFuel ? STATE_HAS_FUEL : 0) | (hasWork ? STATE_FILLED : 0));
  }
}
