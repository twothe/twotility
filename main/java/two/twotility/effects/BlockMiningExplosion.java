/*
 */
package two.twotility.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import two.util.TwoMath;
import two.util.Vector3d;

/**
 * @author Two
 */
public class BlockMiningExplosion extends Explosion {

  protected final int searchRange;
  protected final World world;
  protected final boolean destroyBlocks;
  protected final float strengthMultiplier;
  protected final double baseStrength;
  protected final Vector3d explosionSource;
  protected final Map affectedPlayers = new HashMap();

  public BlockMiningExplosion(final World world, final Entity explodingEntity, final double x, final double y, final double z, final float size, final boolean destroyBlocks, final float strengthMultiplier) {
    super(world, explodingEntity, x, y, z, size);
    this.isFlaming = false;
    this.searchRange = MathHelper.ceiling_float_int(size / 2.0f);
    this.world = world;
    this.explosionSource = new Vector3d(x, y, z);
    this.destroyBlocks = destroyBlocks;
    this.baseStrength = size * size;
    this.strengthMultiplier = strengthMultiplier;
  }

  // Might do weird things on air blocks
  protected float getBlockAbsorb(final Block block, final int blockX, final int blockY, final int blockZ) {
    return this.exploder != null ? this.exploder.func_145772_a(this, this.world, blockX, blockY, blockZ, block) : block.getExplosionResistance(this.exploder, world, blockX, blockY, blockZ, explosionX, explosionY, explosionZ);
  }

  protected void findAffectedBlocks() {
    if (!destroyBlocks) {
      return;
    }
    double stepX, stepY, stepZ;
    double blockCheckX, blockCheckY, blockCheckZ, blockExplosionStrength, explosionDistance, totalAbsorb;
    Block block;
    int blockX, blockY, blockZ, lastBlockX, lastBlockY, lastBlockZ;
    final HashSet<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>();
    boolean isAirBlock;

    for (int x = -searchRange; x <= searchRange; ++x) {
      for (int y = -searchRange; y <= searchRange; ++y) {
        for (int z = -searchRange; z <= searchRange; ++z) {
          if ((x == searchRange) || (x == -searchRange) || (y == searchRange) || (y == -searchRange) || (z == searchRange) || (z == -searchRange)) { // only search cube border
            stepX = (double) ((double) x / ((double) searchRange)) / 2.0;
            stepY = (double) ((double) y / ((double) searchRange)) / 2.0;
            stepZ = (double) ((double) z / ((double) searchRange)) / 2.0;

            blockCheckX = this.explosionX;
            blockCheckY = this.explosionY;
            blockCheckZ = this.explosionZ;
            lastBlockX = MathHelper.floor_double(blockCheckX) + 1;
            lastBlockY = MathHelper.floor_double(blockCheckY) + 1;
            lastBlockZ = MathHelper.floor_double(blockCheckZ) + 1;
            blockExplosionStrength = this.baseStrength;
            totalAbsorb = 0.0;
            explosionDistance = 0.0;

            do {
              blockX = MathHelper.floor_double(blockCheckX);
              blockY = MathHelper.floor_double(blockCheckY);
              blockZ = MathHelper.floor_double(blockCheckZ);
              if ((blockX != lastBlockX) || (blockY != lastBlockY) || (blockZ != lastBlockZ)) {
                lastBlockX = blockX;
                lastBlockY = blockY;
                lastBlockZ = blockZ;
                explosionDistance = this.explosionSource.distanceToBlock(blockX, blockY, blockZ);
                block = this.world.getBlock(blockX, blockY, blockZ);
                isAirBlock = block.isAir(world, blockX, blockY, blockZ);

                if (isAirBlock == false) {
                  totalAbsorb += getBlockAbsorb(block, blockX, blockY, blockZ) / 5.0; // transform from the arbitrary resistance value to something useful;
                }
                if (explosionDistance < 1.0) {
                  blockExplosionStrength = this.baseStrength - totalAbsorb;
                } else {
                  blockExplosionStrength = this.baseStrength / (explosionDistance * explosionDistance) - totalAbsorb;
                }

                if (isAirBlock == false
                        && blockExplosionStrength > 0.0F
                        && (this.exploder == null || this.exploder.func_145774_a(this, this.world, blockX, blockY, blockZ, block, (float) blockExplosionStrength))) {
                  affectedBlocks.add(new ChunkPosition(blockX, blockY, blockZ));
                }
              }

              blockCheckX += stepX;
              blockCheckY += stepY;
              blockCheckZ += stepZ;
            } while ((blockExplosionStrength > 0.0) && (explosionDistance < searchRange));
          }
        }
      }
    }

    this.affectedBlockPositions.addAll(affectedBlocks);
  }

  protected void findAffectedEntities() {
    if (this.world.isRemote) {
      return;
    }
    final List<Entity> affectedEntities = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getBoundingBox(
            MathHelper.floor_double(this.explosionX - searchRange - 1.0D),
            MathHelper.floor_double(this.explosionY - searchRange - 1.0D),
            MathHelper.floor_double(this.explosionZ - searchRange - 1.0D),
            MathHelper.floor_double(this.explosionX + searchRange + 1.0D),
            MathHelper.floor_double(this.explosionY + searchRange + 1.0D),
            MathHelper.floor_double(this.explosionZ + searchRange + 1.0D)));
    double distanceToEntity, knockBackStrength;
    float explosionDamage;
    double stepX, stepY, stepZ;
    double blockCheckX, blockCheckY, blockCheckZ, expoisionStrengthAtPos, explosionDistance, totalAbsorb;
    int blockX, blockY, blockZ, lastBlockX, lastBlockY, lastBlockZ;
    Block block;
    final Vector3d entityPos = new Vector3d();
    final Vector3d thisToEntity = new Vector3d();
    final Vector3d velocityChange = new Vector3d();
    boolean isAirBlock;

    for (final Entity entity : affectedEntities) {
      entityPos.set(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
      thisToEntity.setSub(this.explosionSource, entityPos);
      distanceToEntity = thisToEntity.lengthVector();
      stepX = thisToEntity.xCoord / distanceToEntity / 2.0; // check in half-block steps towards the entity
      stepY = thisToEntity.yCoord / distanceToEntity / 2.0;
      stepZ = thisToEntity.zCoord / distanceToEntity / 2.0;

      blockCheckX = this.explosionX;
      blockCheckY = this.explosionY;
      blockCheckZ = this.explosionZ;
      lastBlockX = MathHelper.floor_double(blockCheckX) + 1;
      lastBlockY = MathHelper.floor_double(blockCheckY) + 1;
      lastBlockZ = MathHelper.floor_double(blockCheckZ) + 1;
      expoisionStrengthAtPos = this.baseStrength;
      totalAbsorb = 0.0;
      explosionDistance = 0.0;

      do {
        blockX = MathHelper.floor_double(blockCheckX);
        blockY = MathHelper.floor_double(blockCheckY);
        blockZ = MathHelper.floor_double(blockCheckZ);
        if ((blockX != lastBlockX) || (blockY != lastBlockY) || (blockZ != lastBlockZ)) {
          lastBlockX = blockX;
          lastBlockY = blockY;
          lastBlockZ = blockZ;
          explosionDistance = this.explosionSource.distanceToBlock(blockX, blockY, blockZ);
          block = this.world.getBlock(blockX, blockY, blockZ);
          isAirBlock = block.isAir(world, blockX, blockY, blockZ);

          if (isAirBlock == false) {
            totalAbsorb += getBlockAbsorb(block, blockX, blockY, blockZ) / 5.0; // transform from the arbitrary resistance value to something useful;
          }

          if (explosionDistance < 1.0) {
            expoisionStrengthAtPos = this.baseStrength - totalAbsorb;
          } else {
            expoisionStrengthAtPos = this.baseStrength / (explosionDistance * explosionDistance) - totalAbsorb;
          }
        }
        blockCheckX += stepX;
        blockCheckY += stepY;
        blockCheckZ += stepZ;
      } while ((expoisionStrengthAtPos > 0.0) && (explosionDistance < distanceToEntity));

      if (expoisionStrengthAtPos > 0.0) {
        explosionDamage = (float) ((this.baseStrength * 2.0 / TwoMath.noLessThanOne(distanceToEntity) - totalAbsorb) * strengthMultiplier);
        entity.setFire(1); // causes cows and such to drop cooked meat
        entity.attackEntityFrom(DamageSource.setExplosionSource(this), explosionDamage);
        knockBackStrength = EnchantmentProtection.func_92092_a(entity, explosionDamage / 20.0);
        velocityChange.set(knockBackStrength / TwoMath.noLessThanOne(thisToEntity.xCoord), knockBackStrength / TwoMath.noLessThanOne(thisToEntity.yCoord), knockBackStrength / TwoMath.noLessThanOne(thisToEntity.zCoord));
        entity.addVelocity(velocityChange.xCoord, velocityChange.yCoord, velocityChange.zCoord);

        if (entity instanceof EntityPlayer) {
          this.affectedPlayers.put((EntityPlayer) entity, velocityChange.copy());
        }
      }
    }
  }

  @Override
  public void doExplosionA() {
    findAffectedEntities();
    findAffectedBlocks();
    net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, new ArrayList(this.affectedPlayers.keySet()), this.explosionSize);
  }

  @Override
  public void doExplosionB(final boolean spawnParticles) {
    if (this.world.isRemote) {
      if (this.explosionSize >= 4.0F) {
        this.world.spawnParticle("hugeexplode", this.explosionX, this.explosionY, this.explosionZ, 0.0, 0.0, 0.0);
      } else {
        this.world.spawnParticle("largeexplode", this.explosionX, this.explosionY, this.explosionZ, 0.0, 0.0, 0.0);
      }
    } else {
      this.world.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
    }

    int x, y, z;
    Block block;
    final Random random = this.world.rand;

    final int particleLimit = Math.max(1, affectedBlockPositions.size() / 100);

    for (final ChunkPosition chunkposition : ((List<ChunkPosition>) affectedBlockPositions)) {
      x = chunkposition.chunkPosX;
      y = chunkposition.chunkPosY;
      z = chunkposition.chunkPosZ;
      block = this.world.getBlock(x, y, z);

      if (block.isAir(world, x, y, z) == false) {
        if (this.world.isRemote == false) {
          block.dropBlockAsItemWithChance(this.world, x, y, z, this.world.getBlockMetadata(x, y, z), 1.0F, 0);
          block.onBlockExploded(this.world, x, y, z, this);
        } else if ((particleLimit == 1) || (random.nextInt(particleLimit) == 0)) {
          this.world.spawnParticle("explode", x + random.nextDouble() - 0.5, y + random.nextDouble() - 0.5, z + random.nextDouble() - 0.5, (random.nextDouble() - 0.5) * 0.2, random.nextDouble() * 0.5 * 0.2, (random.nextDouble() - 0.5) * 0.2);
          this.world.spawnParticle("explode", x + random.nextDouble() - 0.5, y + random.nextDouble() - 0.5, z + random.nextDouble() - 0.5, (random.nextDouble() - 0.5) * 0.2, random.nextDouble() * 0.5 * 0.2, (random.nextDouble() - 0.5) * 0.2);
          this.world.spawnParticle("explode", x + random.nextDouble() - 0.5, y + random.nextDouble() - 0.5, z + random.nextDouble() - 0.5, (random.nextDouble() - 0.5) * 0.2, random.nextDouble() * 0.5 * 0.2, (random.nextDouble() - 0.5) * 0.2);
        }
      }
    }
  }

  @Override
  public Map func_77277_b() {
    return this.affectedPlayers;
  }
}
