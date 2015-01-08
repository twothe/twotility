/*
 */
package two.twotility.entities;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.twotility.effects.BlockMiningExplosion;

/**
 * @author Two
 */
public class EntityGrenade extends EntityThrowable {

  public EntityGrenade(final World world) {
    super(world);
  }

  public EntityGrenade(final World world, final EntityLivingBase entityLiving) {
    super(world, entityLiving);
  }

  @SideOnly(Side.CLIENT)
  public EntityGrenade(final World world, final double x, final double y, final double z) {
    super(world, x, y, z);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (this.worldObj.isRemote) {
      this.worldObj.spawnParticle("smoke", this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
    }
  }

  protected void explode(final MovingObjectPosition movingObjectPosition) {
    this.setDead();
    final float size = 3f;

    final BlockMiningExplosion explosion = new BlockMiningExplosion(worldObj, this, posX, posY, posZ, size, TwoTility.proxy.configGrenadeDestroysBlocks, TwoTility.proxy.configGrenadeDamageMultiplier);
    final boolean cancelExplosion = net.minecraftforge.event.ForgeEventFactory.onExplosionStart(worldObj, explosion);
    if (cancelExplosion == false) {
      explosion.doExplosionA();
      explosion.doExplosionB(true);
    }
  }

  @Override
  protected void onImpact(final MovingObjectPosition movingObjectPosition) {
    if (movingObjectPosition.entityHit != null) {
      movingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
    }

    explode(movingObjectPosition);
  }
}
