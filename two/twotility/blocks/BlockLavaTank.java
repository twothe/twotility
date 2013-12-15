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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import two.twotility.Config;
import two.twotility.TwoTility;
import two.twotility.tiles.TileLavaTank;
import two.util.BlockSide;
import static two.util.BlockSide.top;

/**
 * @author Two
 */
public class BlockLavaTank extends Block implements ITileEntityProvider {

  public static final String NAME = "lavatank";
  protected static final int STATE_EMPTY = 0;
  protected static final int STATE_1_4 = STATE_EMPTY + 1;
  protected static final int STATE_2_4 = STATE_1_4 + 1;
  protected static final int STATE_3_4 = STATE_2_4 + 1;
  protected static final int STATE_4_4 = STATE_3_4 + 1;
  protected static final int NUM_STATES = STATE_4_4 + 1;
  protected Icon[] textures = new Icon[NUM_STATES];

  public BlockLavaTank() {
    super(Config.getBlockID(BlockLavaTank.class), Material.glass);
  }

  public BlockLavaTank initialize() {
    setHardness(2.5F);
    setStepSound(soundGlassFootstep);
    setUnlocalizedName(NAME);
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Lava Tank");
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));
    GameRegistry.registerTileEntity(TileLavaTank.class, TileLavaTank.class.getName());

    CraftingManager.getInstance().addRecipe(new ItemStack(this, 1),
            "IGI",
            "G G",
            "IGI",
            'G', Block.thinGlass,
            'I', Item.ingotIron);

    MinecraftForge.EVENT_BUS.register(this);

    return this;
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
        Logger.getLogger(TwoTility.MOD_ID).log(Level.WARNING, "TileEntity at {0}, {1}, {2} should have been a LavaTank, but was {3}", new Object[]{target.blockX, target.blockY, target.blockZ, tileEntity.getClass().getName()});
        event.world.removeBlockTileEntity(target.blockX, target.blockY, target.blockZ);
      }
    }
  }

  @Override
  public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
    super.breakBlock(par1World, par2, par3, par4, par5, par6); // TODO
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    textures[STATE_EMPTY] = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    textures[STATE_1_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_1_4");
    textures[STATE_2_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_half");
    textures[STATE_3_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_3_4");
    textures[STATE_4_4] = iconRegister.registerIcon(TwoTility.getTextureName(NAME) + "_full");
  }

  @Override
  public Icon getIcon(final int side, final int metadata) {
    switch (BlockSide.getSide(side)) {
      case top:
        return (metadata == STATE_4_4) ? textures[STATE_4_4] : textures[STATE_EMPTY];
      case bottom:
        return (metadata == STATE_EMPTY) ? textures[STATE_EMPTY] : textures[STATE_4_4];
      default:
        return textures[metadata];
    }
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    return new TileLavaTank();
  }
}
