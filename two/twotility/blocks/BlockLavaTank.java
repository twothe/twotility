/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import two.twotility.InitializableModContent;
import two.twotility.TwoTility;
import two.twotility.items.ItemLavaTank;
import two.twotility.tiles.TileLavaTank;
import two.util.BlockSide;
import static two.util.BlockSide.TOP;

/**
 * @author Two
 */
public class BlockLavaTank extends Block implements ITileEntityProvider, InitializableModContent {

  public static final String NAME = "LavaTank";
  public static final int STATE_EMPTY = 0;
  public static final int STATE_1_4 = STATE_EMPTY + 1;
  public static final int STATE_2_4 = STATE_1_4 + 1;
  public static final int STATE_3_4 = STATE_2_4 + 1;
  public static final int STATE_4_4 = STATE_3_4 + 1;
  public static final int NUM_STATES = STATE_4_4 + 1;
  //--- Class ------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected Icon[] texturesSide = new Icon[NUM_STATES];
  @SideOnly(Side.CLIENT)
  protected Icon textureTopBottom;
  protected ItemLavaTank itemDropped;

  public BlockLavaTank() {
    super(TwoTility.config.getBlockID(BlockLavaTank.class), Material.iron);
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
    GameRegistry.registerTileEntity(TileLavaTank.class, TileLavaTank.class.getName());
  }

  @Override
  public void initialize() {
    setHardness(2.5F);
    setLightOpacity(0);
    setStepSound(soundMetalFootstep);
    setUnlocalizedName(NAME);
    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    setCreativeTab(TwoTility.creativeTab);
    
    itemDropped = TwoTility.proxy.itemLavaTank;

    LanguageRegistry.addName(this, "Lava Tank");

    if (TwoTility.config.isCraftingEnabled(NAME)) {
      CraftingManager.getInstance().addRecipe(new ItemStack(itemDropped, 1, STATE_EMPTY),
              "IGI",
              "G G",
              "IGI",
              'G', Block.thinGlass,
              'I', Item.ingotIron);
    }

    MinecraftForge.EVENT_BUS.register(this);
  }

  @ForgeSubscribe
  public void onRightClick(final FillBucketEvent event) {
    final MovingObjectPosition target = event.target;
    final int targetID = event.world.getBlockId(target.blockX, target.blockY, target.blockZ);
    if (targetID == this.blockID) {
      event.setCanceled(true); // in all cases: do not spill lava around!
      if (event.world.isRemote) {
        return;
      }

      final TileEntity tileEntity = event.world.getBlockTileEntity(target.blockX, target.blockY, target.blockZ);
      if (tileEntity instanceof TileLavaTank) {
        final TileLavaTank lavaTank = (TileLavaTank) tileEntity;
        if (event.current.itemID == Item.bucketLava.itemID) {
          if (lavaTank.fill(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), true) > 0) {
            event.result = new ItemStack(Item.bucketEmpty);
            event.setCanceled(false);
            event.setResult(Event.Result.ALLOW);
          }
        } else if (event.current.itemID == FluidContainerRegistry.EMPTY_BUCKET.itemID) {
          final FluidStack drainedFluid = lavaTank.drain(ForgeDirection.UNKNOWN, FluidContainerRegistry.BUCKET_VOLUME, true);
          if ((drainedFluid != null) && (drainedFluid.amount >= FluidContainerRegistry.BUCKET_VOLUME)) {
            event.result = new ItemStack(Item.bucketLava);
            event.setCanceled(false);
            event.setResult(Event.Result.ALLOW);
          }
        }
      } else {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been a %s, but was %s", target.blockX, target.blockY, target.blockZ, this.getClass().getSimpleName(), tileEntity.getClass().getName());
        event.world.removeBlockTileEntity(target.blockX, target.blockY, target.blockZ);
      }
    }
  }

  @Override
  public void getSubBlocks(final int itemID, final CreativeTabs creativeTab, final List itemlist) {
    itemlist.add(new ItemStack(itemDropped.itemID, 1, STATE_EMPTY));
    itemlist.add(new ItemStack(itemDropped.itemID, 1, STATE_4_4));
  }

  @Override
  public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
    final int metadata = world.getBlockMetadata(x, y, z);
    if (metadata == STATE_EMPTY) {
      world.setBlockToAir(x, y, z);
    } else {
      final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
      if (tileEntity instanceof TileLavaTank) {
        final TileLavaTank lavaTank = (TileLavaTank) tileEntity;
        final FluidStack content = lavaTank.drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, false);
        final Block block = Block.blocksList[content.getFluid().getBlockID()];
        if (block instanceof BlockFluid) { // vanilla block
          world.setBlock(x, y, z, getVanillaFluidBlock(block), metadata == STATE_4_4 ? 0 : 2, 3); // a full tank will create a lava source block
        } else if (block instanceof BlockFluidBase) {
          world.setBlock(x, y, z, block.blockID, 0, 3);
        } else {
          world.setBlockToAir(x, y, z);
        }
      } else {
        FMLLog.log(TwoTility.MOD_ID, Level.WARNING, "TileEntity at %d, %d, %d should have been a %s, but was %s", x, y, z, this.getClass().getSimpleName(), tileEntity.getClass().getName());
      }
    }

    onBlockDestroyedByExplosion(world, x, y, z, explosion);
  }

  protected static int getVanillaFluidBlock(final Block block) {
    if (block.blockMaterial == Material.water) {
      return Block.waterMoving.blockID;
    } else if (block.blockMaterial == Material.lava) {
      return Block.lavaMoving.blockID;
    }
    return 0; // air seems safe
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
  public int idDropped(int par1, Random par2Random, int par3) {
    return this.itemDropped.itemID;
  }

  @Override
  public int idPicked(World par1World, int par2, int par3, int par4) {
    return this.itemDropped.itemID;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    textureTopBottom = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    texturesSide[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME + "_empty"));
    texturesSide[STATE_1_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_1_4");
    texturesSide[STATE_2_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_half");
    texturesSide[STATE_3_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_3_4");
    texturesSide[STATE_4_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_full");
  }

  @Override
  public Icon getIcon(final int side, final int metadata) {
    switch (BlockSide.getSide(side)) {
      case TOP:
      case BOTTOM:
        return textureTopBottom;
      default:
        return texturesSide[metadata];
    }
  }

  @Override
  public TileEntity createTileEntity(final World world, final int metadata) {
    return new TileLavaTank(metadata * FluidContainerRegistry.BUCKET_VOLUME, 4 * FluidContainerRegistry.BUCKET_VOLUME);
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    return new TileLavaTank(); // this should never be called in theory
  }
}
