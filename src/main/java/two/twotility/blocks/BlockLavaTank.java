/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Level;
import two.twotility.TwoTility;
import two.twotility.TwoTilityAssets;
import two.twotility.items.ItemLavaTank;
import two.twotility.tiles.TileLavaTank;
import two.util.BlockSide;
import static two.util.BlockSide.TOP;
import two.util.BlockUtil;

/**
 * @author Two
 */
public class BlockLavaTank extends BlockBase implements ITileEntityProvider {

  public static final String NAME = "lavaTank";
  public static final int STATE_EMPTY = 0;
  public static final int STATE_1_4 = STATE_EMPTY + 1;
  public static final int STATE_2_4 = STATE_1_4 + 1;
  public static final int STATE_3_4 = STATE_2_4 + 1;
  public static final int STATE_4_4 = STATE_3_4 + 1;
  public static final int NUM_STATES = STATE_4_4 + 1;
  //--- Class ------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected IIcon[] texturesSide;
  @SideOnly(Side.CLIENT)
  protected IIcon textureTopBottom;
  protected ItemLavaTank itemDropped;

  public BlockLavaTank() {
    super(Material.iron);
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, soundTypeMetal, 2.5F, BlockUtil.HARVEST_TOOL_PICKAXE, BlockUtil.HARVEST_LEVEL_STONE);
    GameRegistry.registerTileEntity(TileLavaTank.class, TileLavaTank.class.getName());
    setLightOpacity(0);
    setLightLevel(8.0f / 15.0f);

    itemDropped = TwoTilityAssets.itemLavaTank;

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemDropped, 1, STATE_EMPTY),
              "IGI",
              "G G",
              "IGI",
              'G', "paneGlass",
              'I', "ingotIron"
      ));
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onBucketUse(final FillBucketEvent event) {
    final TileEntity tileEntity = event.world.getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ);
    if (tileEntity instanceof TileLavaTank) {
      ((TileLavaTank) tileEntity).onUse(event);
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(final Item thisItem, final CreativeTabs creativeTab, final List itemlist) {
//    itemlist.add(new ItemStack(itemDropped, 1, STATE_EMPTY));
//    itemlist.add(new ItemStack(itemDropped, 1, STATE_4_4));
  }

  @Override
  public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
    final int metadata = world.getBlockMetadata(x, y, z);
    if (metadata == STATE_EMPTY) {
      world.setBlockToAir(x, y, z);
    } else {
      final TileEntity tileEntity = world.getTileEntity(x, y, z);
      if (tileEntity instanceof TileLavaTank) {
        final TileLavaTank lavaTank = (TileLavaTank) tileEntity;
        final FluidStack fluid = lavaTank.drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, false);
        final Block block = fluid.getFluid().getBlock();
        if (block instanceof BlockStaticLiquid) { // vanilla block
          world.setBlock(x, y, z, getVanillaFluidBlock(block), metadata == STATE_4_4 ? 0 : 2, 3); // a full tank will create a lava source block
        } else if (block instanceof BlockFluidBase) {
          world.setBlock(x, y, z, block, 0, 3);
        } else {
          world.setBlockToAir(x, y, z);
        }
      } else {
        FMLLog.log(TwoTility.MOD_ID, Level.WARN, "TileEntity at %d, %d, %d should have been a %s, but was %s", x, y, z, this.getClass().getSimpleName(), tileEntity.getClass().getName());
      }
    }

    onBlockDestroyedByExplosion(world, x, y, z, explosion);
  }

  protected static Block getVanillaFluidBlock(final Block block) {
    if (block.getMaterial() == Material.water) {
      return Blocks.water;
    } else if (block.getMaterial() == Material.lava) {
      return Blocks.lava;
    }
    return Blocks.air; // air seems safe
  }

  @Override
  public boolean canDropFromExplosion(Explosion par1Explosion) {
    return false;
  }

  @Override
  public int damageDropped(final int metadata) {
    return metadata;
  }

  @Override
  public Item getItemDropped(final int metadata, final Random random, final int fortuneLevel) {
    return itemDropped;
  }

  @Override
  public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y, final int z, final EntityPlayer player) {
    return new ItemStack(itemDropped, 1, world.getBlockMetadata(target.blockX, target.blockY, target.blockZ));
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister iconRegister) {
    texturesSide = new IIcon[NUM_STATES];
    textureTopBottom = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    texturesSide[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME + "_empty"));
    texturesSide[STATE_1_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_1_4");
    texturesSide[STATE_2_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_half");
    texturesSide[STATE_3_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_3_4");
    texturesSide[STATE_4_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_full");
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    switch (BlockSide.getSide(side)) {
      case TOP:
      case BOTTOM:
        return textureTopBottom;
    }
    if ((metadata >= STATE_EMPTY) && (metadata <= STATE_4_4)) {
      return texturesSide[metadata];
    }
    return null;
  }

  @Override
  public TileEntity createNewTileEntity(final World world, final int metadata) {
    return new TileLavaTank(metadata * FluidContainerRegistry.BUCKET_VOLUME, 4 * FluidContainerRegistry.BUCKET_VOLUME);
  }

}
