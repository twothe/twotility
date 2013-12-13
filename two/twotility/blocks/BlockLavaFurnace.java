/*
 */
package two.twotility.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import static net.minecraft.block.Block.soundStoneFootstep;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import two.twotility.Config;
import two.twotility.TwoTility;
import two.util.BlockSide;
import two.util.TwoMath;

/**
 * @author Two
 */
public class BlockLavaFurnace extends Block {

  public static final String NAME = "lavafurnace";
  public static final int LIQUID_MAX = 3; // counted in buckets
  //-- Class -------------------------------------------------------------------
  @SideOnly(Side.CLIENT)
  protected Icon iconFront;
  @SideOnly(Side.CLIENT)
  protected Icon iconFrontFilled;

  public BlockLavaFurnace() {
    super(Config.getBlockID(BlockLavaFurnace.class), Material.rock);
  }

  public BlockLavaFurnace initialize() {
    setHardness(3.5F);
    setStepSound(soundStoneFootstep);
    setUnlocalizedName(NAME);
    setTextureName(TwoTility.getTextureName(NAME));
    setCreativeTab(TwoTility.creativeTab);

    MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
    LanguageRegistry.addName(this, "Lava Furnace");
    GameRegistry.registerBlock(this, TwoTility.getBlockName(NAME));

    CraftingManager.getInstance().addRecipe(new ItemStack(this, 1, BlockSide.north.ordinal()),
            "CCC",
            "CBC",
            "CCC",
            'C', Block.cobblestone,
            'B', Item.bucketEmpty);

    CraftingManager.getInstance().addRecipe(new ItemStack(this, 1, amountToMetadata(1) | BlockSide.north.ordinal()),
            "CCC",
            "CLC",
            "CCC",
            'C', Block.cobblestone,
            'L', Item.bucketLava);

    MinecraftForge.EVENT_BUS.register(this); // for eventFillBucket

    return this;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(final IconRegister iconRegister) {
    iconFront = iconRegister.registerIcon(TwoTility.getTextureName(NAME));
    iconFrontFilled = iconRegister.registerIcon(TwoTility.getTextureName(NAME + "filled"));
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Icon getIcon(final int side, final int metadata) {
    final int frontSide = BlockSide.getSideFacingPlacer(metadata);
    if (frontSide == side) {
      return amountFromMetadata(metadata) > 0 ? iconFrontFilled : iconFront;
    } else {
      return Block.furnaceIdle.getBlockTextureFromSide(BlockSide.getRelativeSide(side, metadata).ordinal());
    }
  }

  @ForgeSubscribe
  public void eventFillBucket(final FillBucketEvent event) {
    final MovingObjectPosition target = event.target;
    final int targetBlockID = event.world.getBlockId(target.blockX, target.blockY, target.blockZ);
    if (targetBlockID == this.blockID) {
      final int metaData = event.world.getBlockMetadata(target.blockX, target.blockY, target.blockZ);
      final Item item = event.current.getItem();

      if ((item.itemID == Item.bucketEmpty.itemID) && (amountFromMetadata(metaData) > 0)) {
        event.world.setBlockMetadataWithNotify(target.blockX, target.blockY, target.blockZ, changeAmountInMetadata(metaData, -1), 2);
        event.result = new ItemStack(Item.bucketLava);
        event.setResult(Event.Result.ALLOW);
      } else if ((item.itemID == Item.bucketLava.itemID) && (amountFromMetadata(metaData) < LIQUID_MAX)) {
        event.world.setBlockMetadataWithNotify(target.blockX, target.blockY, target.blockZ, changeAmountInMetadata(metaData, 1), 2);
        event.result = new ItemStack(Item.bucketEmpty);
        event.setResult(Event.Result.ALLOW);
      } else {
        event.setCanceled(true); // must cancel here, otherwise lava might be placed in the world
      }
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack itemStack) {
    final int metadata = amountToMetadata(itemStack.getItemDamage()) | BlockSide.getLookDirection(entity);
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
  }

  @Override
  public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
    return false;
  }

  @Override
  public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z) {
    return false;
  }

  private static int amountToMetadata(final int amount) {
    return amount << 2;
  }

  private static int amountFromMetadata(final int metadata) {
    return metadata >>> 2;
  }

  private static int changeAmountInMetadata(final int metadata, final int mod) {
    return amountToMetadata(amountFromMetadata(metadata) + mod) | (metadata & 3);
  }
}
