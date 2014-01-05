/*
 */
package two.twotility.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class NonBreakingExplosion extends Explosion {

  protected final int searchRange;
  protected final World world;
  protected final boolean destroyBlocks;
  protected final float strengthMultiplier;
  protected final double baseStrength;
  protected final Vector3d explosionSource;
  protected final Map affectedPlayers = new HashMap();

  public NonBreakingExplosion(final World world, final Entity explodingEntity, final double x, final double y, final double z, final float size, final boolean destroyBlocks, final float strengthMultiplier) {
    super(world, explodingEntity, x, y, z, size);
    this.isFlaming = false;
    this.searchRange = MathHelper.ceiling_float_int(size / 2.0f);
    this.world = world;
    this.explosionSource = new Vector3d(x, y, z);
    this.destroyBlocks = destroyBlocks;
    this.baseStrength = size * size;
    this.strengthMultiplier = strengthMultiplier;
  }

  protected float getBlockAbsorb(final int blockID, final int blockX, final int blockY, final int blockZ) {
    if (blockID > 0) {
      final Block block = Block.blocksList[blockID];
      return this.exploder != null ? this.exploder.getBlockExplosionResistance(this, this.world, blockX, blockY, blockZ, block) : block.getExplosionResistance(this.exploder, world, blockX, blockY, blockZ, explosionX, explosionY, explosionZ);
    }
    return 0.0f;
  }

  protected void findAffectedBlocks() {
    if (!destroyBlocks) {
      return;
    }
    double stepX, stepY, stepZ;
    double blockCheckX, blockCheckY, blockCheckZ, blockExplosionStrength, explosionDistance, totalAbsorb;
    int blockID, blockX, blockY, blockZ, lastBlockX, lastBlockY, lastBlockZ;
    final HashSet<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>();

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
                blockID = this.world.getBlockId(blockX, blockY, blockZ);

                if (blockID > 0) {
                  totalAbsorb += getBlockAbsorb(blockID, blockX, blockY, blockZ) / 5.0; // transform from the arbitrary resistance value to something useful;
                }
                if (explosionDistance < 1.0) {
                  blockExplosionStrength = this.baseStrength - totalAbsorb;
                } else {
                  blockExplosionStrength = this.baseStrength / (explosionDistance * explosionDistance) - totalAbsorb;
                }

                if (blockID > 0 && blockExplosionStrength > 0.0F && (this.exploder == null || this.exploder.shouldExplodeBlock(this, this.world, blockX, blockY, blockZ, blockID, (float) blockExplosionStrength))) {
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
    final List<Entity> affectedEntities = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getAABBPool().getAABB(
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
    int blockID, blockX, blockY, blockZ, lastBlockX, lastBlockY, lastBlockZ;
    final Vector3d entityPos = new Vector3d();
    final Vector3d thisToEntity = new Vector3d();
    final Vector3d velocityChange = new Vector3d();

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
          blockID = this.world.getBlockId(blockX, blockY, blockZ);

          if (blockID > 0) {
            totalAbsorb += getBlockAbsorb(blockID, blockX, blockY, blockZ) / 5.0; // transform from the arbitrary resistance value to something useful;
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
  }

  @Override
  public void doExplosionB(final boolean spawnParticles) {
    this.world.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

    int x, y, z, blockID;
    Block block;

    for (final ChunkPosition chunkposition : ((List<ChunkPosition>) affectedBlockPositions)) {
      x = chunkposition.x;
      y = chunkposition.y;
      z = chunkposition.z;
      blockID = this.world.getBlockId(x, y, z);

      if (blockID > 0) {
        block = Block.blocksList[blockID];
        block.dropBlockAsItemWithChance(this.world, x, y, z, this.world.getBlockMetadata(x, y, z), 1.0F, 0);
        block.onBlockExploded(this.world, x, y, z, this);
      }
    }
  }

  @Override
  public Map func_77277_b() {
    return this.affectedPlayers;
  }
}
