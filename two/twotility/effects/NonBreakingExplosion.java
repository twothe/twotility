/*
 */
package two.twotility.effects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/**
 * @author Two
 */
public class NonBreakingExplosion extends Explosion {

  protected final int searchRange;
  protected final World world;
  protected final float strengthMultiplier;
  protected final Map affectedPlayers = new HashMap();

  public NonBreakingExplosion(final World world, final Entity explodingEntity, final double x, final double y, final double z, final float size, final float strengthMultiplier) {
    super(world, explodingEntity, x, y, z, size);
    this.isFlaming = false;
    this.searchRange = MathHelper.ceiling_float_int(size * 2.0f);
    this.world = world;
    this.strengthMultiplier = strengthMultiplier;
  }

  protected void findAffectedBlocks() {
    for (int x = 0; x < searchRange; ++x) {
      for (int y = 0; y < searchRange; ++y) {
        for (int z = 0; z < searchRange; ++z) {
          if (x == 0 || x == searchRange - 1 || y == 0 || y == searchRange - 1 || z == 0 || z == searchRange - 1) { // only search cube border
            double vecX = (double) ((float) x / ((float) searchRange - 1.0F) * 2.0F - 1.0F);
            double vecY = (double) ((float) y / ((float) searchRange - 1.0F) * 2.0F - 1.0F);
            double vecZ = (double) ((float) z / ((float) searchRange - 1.0F) * 2.0F - 1.0F);
            final double distance = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
            vecX /= distance;
            vecY /= distance;
            vecZ /= distance;
            float remainingExplosionStrength = this.explosionSize * (0.7F + this.world.rand.nextFloat() * 0.6F);
            double blockCheckX = this.explosionX;
            double blockCheckY = this.explosionY;
            double blockCheckZ = this.explosionZ;

            for (float f2 = 0.3F; remainingExplosionStrength > 0.0F; remainingExplosionStrength -= f2 * 0.75F) {
              final int blockX = MathHelper.floor_double(blockCheckX);
              final int blockY = MathHelper.floor_double(blockCheckY);
              final int blockZ = MathHelper.floor_double(blockCheckZ);
              final int blockID = this.world.getBlockId(blockX, blockY, blockZ);

              if (blockID > 0) {
                final Block block = Block.blocksList[blockID];
                final float blockAbsorb = this.exploder != null ? this.exploder.getBlockExplosionResistance(this, this.world, blockX, blockY, blockZ, block) : block.getExplosionResistance(this.exploder, world, blockX, blockY, blockZ, explosionX, explosionY, explosionZ);
                remainingExplosionStrength -= (blockAbsorb + 0.3F) * f2;
              }

              if (remainingExplosionStrength > 0.0F && (this.exploder == null || this.exploder.shouldExplodeBlock(this, this.world, blockX, blockY, blockZ, blockID, remainingExplosionStrength))) {
                affectedBlockPositions.add(new ChunkPosition(blockX, blockY, blockZ));
              }

              blockCheckX += vecX * (double) f2;
              blockCheckY += vecY * (double) f2;
              blockCheckZ += vecZ * (double) f2;
            }
          }
        }
      }
    }
  }

  protected void findAffectedEntities() {
    final double entitySearchRadius = this.explosionSize * 2.0;
    final List<Entity> affectedEntities = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getAABBPool().getAABB(
            MathHelper.floor_double(this.explosionX - entitySearchRadius - 1.0D),
            MathHelper.floor_double(this.explosionY - entitySearchRadius - 1.0D),
            MathHelper.floor_double(this.explosionZ - entitySearchRadius - 1.0D),
            MathHelper.floor_double(this.explosionX + entitySearchRadius + 1.0D),
            MathHelper.floor_double(this.explosionY + entitySearchRadius + 1.0D),
            MathHelper.floor_double(this.explosionZ + entitySearchRadius + 1.0D)));
    final Vec3 position = this.world.getWorldVec3Pool().getVecFromPool(this.explosionX, this.explosionY, this.explosionZ);

    double explosionStrength, entityEffectX, entityEffectY, entityEffectZ, distance, blockDensity, effectiveExplosionStrength, knockBackStrength;
    float explosionDamage;
    for (final Entity entity : affectedEntities) {
      explosionStrength = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / entitySearchRadius;

      if (explosionStrength <= 1.0D) {
        entityEffectX = entity.posX - this.explosionX;
        entityEffectY = entity.posY + (double) entity.getEyeHeight() - this.explosionY;
        entityEffectZ = entity.posZ - this.explosionZ;
        distance = Math.sqrt(entityEffectX * entityEffectX + entityEffectY * entityEffectY + entityEffectZ * entityEffectZ);

        if (distance != 0.0D) {
          entityEffectX /= distance;
          entityEffectY /= distance;
          entityEffectZ /= distance;
          blockDensity = (double) this.world.getBlockDensity(position, entity.boundingBox);
          effectiveExplosionStrength = (1.0D - explosionStrength) * blockDensity;
          explosionDamage = (float) ((int) ((effectiveExplosionStrength * effectiveExplosionStrength + effectiveExplosionStrength) / 2.0D * 8.0D * entitySearchRadius * strengthMultiplier + 1.0D));
          entity.setFire(1); // causes cows and such to drop cooked meat
          entity.attackEntityFrom(DamageSource.setExplosionSource(this), explosionDamage);
          knockBackStrength = EnchantmentProtection.func_92092_a(entity, effectiveExplosionStrength) * strengthMultiplier;
          entity.motionX += entityEffectX * knockBackStrength;
          entity.motionY += entityEffectY * knockBackStrength;
          entity.motionZ += entityEffectZ * knockBackStrength;

          if (entity instanceof EntityPlayer) {
            this.affectedPlayers.put((EntityPlayer) entity, this.world.getWorldVec3Pool().getVecFromPool(entityEffectX * effectiveExplosionStrength, entityEffectY * effectiveExplosionStrength, entityEffectZ * effectiveExplosionStrength));
          }
        }
      }
    }
  }

  @Override
  public void doExplosionA() {
    findAffectedBlocks();
    findAffectedEntities();
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
