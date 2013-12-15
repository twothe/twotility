/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.block.Block;
import static net.minecraft.block.Block.soundStoneFootstep;
import net.minecraft.block.BlockContainer;
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
import two.twotility.tiles.TileLavaFurnace;
import two.util.BlockSide;

/**
 * @author Two
 */
public class BlockLavaFurnace extends BlockContainer {

  public static final String NAME = "lavafurnace";
  protected static final int STATE_EMPTY = 0;
  protected static final int STATE_FILLED = STATE_EMPTY + 1;
  protected static final int STATE_LAVA = STATE_FILLED + 1;
  protected static final int STATE_WORKING = STATE_LAVA + 1;
  protected static final int NUM_STATES = STATE_WORKING + 1;
  //-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected Icon[] stateIcons = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon iconSide;
  @SideOnly(Side.CLIENT)
  protected Icon iconTop;

  public BlockLavaFurnace() {
    super(Config.getBlockID(BlockLavaFurnace.class), Material.rock);
  }

  public BlockLavaFurnace initialize() {
    setHardness(5F);
    setStepSound(soundStoneFootstep);
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Lava Furnace");
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
    GameRegistry.registerTileEntity(TileLavaFurnace.class, TileLavaFurnace.class.getName());

    CraftingManager.getInstance().addRecipe(new ItemStack(this, 1),
            " R ",
            "CFC",
            " B ",
            'C', Block.chest,
            'F', Block.furnaceIdle,
            'R', Item.redstone,
            'B', Item.bucketEmpty);

    return this;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    stateIcons[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    stateIcons[STATE_FILLED] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_filled");
    stateIcons[STATE_LAVA] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_lava");
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
    final int metadata = BlockSide.getDirectionFacing(entity); // TODO: search for lava tank
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote == false) {
      final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
      if (tileEntity instanceof TileLavaFurnace) {
        final TileLavaFurnace lavaFurnace = (TileLavaFurnace) tileEntity;
        return lavaFurnace.doSomething();
      } else {
        Logger.getLogger(TwoTility.MOD_ID).log(Level.WARNING, "TileEntity at {0}, {1}, {2} should have been a LavaFurnace, but was {3}", new Object[]{x, y, z, tileEntity.getClass().getName()});
        world.removeBlockTileEntity(x, y, z);
      }
    }
    return false; // TODO: display GUI
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    return new TileLavaFurnace();
  }

  protected static int getStateFromMetadata(final int metadata) {
    return (metadata >>> 2) & 3;
  }

  protected static int createState(final int metaCurrent, final int state) {
    return ((state & 3) << 2) | (metaCurrent & 3);
  }

  public static int createState(final int metaCurrent, final boolean hasLava, final boolean hasWork) {
    return createState(metaCurrent, (hasLava ? STATE_LAVA : 0) | (hasWork ? STATE_FILLED : 0));
  }
}
