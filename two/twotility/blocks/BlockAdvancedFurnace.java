/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.block.Block;
import static net.minecraft.block.Block.soundStoneFootstep;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import two.twotility.Config;
import two.twotility.TwoTility;
import two.twotility.gui.GuiHandler;
import two.twotility.tiles.TileAdvancedFurnace;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockAdvancedFurnace extends Block implements ITileEntityProvider {

  public static final String NAME = "AdvancedFurnace";
  protected static final int STATE_EMPTY = 0;
  protected static final int STATE_FILLED = STATE_EMPTY + 1;
  protected static final int STATE_HAS_FUEL = STATE_FILLED + 1;
  protected static final int STATE_WORKING = STATE_HAS_FUEL + 1;
  protected static final int NUM_STATES = STATE_WORKING + 1;
  //-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected Icon[] stateIcons = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon iconSide;
  @SideOnly(Side.CLIENT)
  protected Icon iconTop;

  public BlockAdvancedFurnace() {
    super(Config.getBlockID(BlockAdvancedFurnace.class), Material.rock);
  }

  public BlockAdvancedFurnace initialize() {
    setHardness(5F);
    setStepSound(soundStoneFootstep);
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Advanced Furnace");
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
    GameRegistry.registerTileEntity(TileAdvancedFurnace.class, TileAdvancedFurnace.class.getName());

    if (Config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(this, 1),
              " R ",
              "CFC",
              " B ",
              'C', Block.chest,
              'F', Block.furnaceIdle,
              'R', Item.redstone,
              'B', Item.bucketEmpty);
    }
    return this;
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
    return stateIcons[state];
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    final BlockSide rotatedSide = BlockSide.getRotatedSide(side, metadata);
    switch (rotatedSide) {
      case north:
        return getFrontfaceByState(getStateFromMetadata(metadata));
      case top:
      case bottom:
        return iconTop;
      default:
        return iconSide;
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = BlockSide.getDirectionFacing(entity); 
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote == false) {
      System.out.println("[Local] onBlockActivated, opening GUI...");
      FMLNetworkHandler.openGui(player, TwoTility.instance, GuiHandler.ID_ADVANCED_FURNACE, world, x, y, z);
    }
    return true;
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    return new TileAdvancedFurnace();
  }

  protected static int getStateFromMetadata(final int metadata) {
    return (metadata >>> 2) & 3;
  }

  protected static int createState(final int metaCurrent, final int state) {
    return ((state & 3) << 2) | (metaCurrent & 3);
  }

  public static int createState(final int metaCurrent, final boolean hasFuel, final boolean hasWork) {
    return createState(metaCurrent, (hasFuel ? STATE_HAS_FUEL : 0) | (hasWork ? STATE_FILLED : 0));
  }
}
