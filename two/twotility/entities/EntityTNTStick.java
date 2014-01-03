/*
 */
package two.twotility.entities;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.packet.Packet60Explosion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import two.twotility.TwoTility;
import two.twotility.effects.NonBreakingExplosion;

/**
 * @author Two
 */
public class EntityTNTStick extends EntityThrowable {

  public EntityTNTStick(final World world) {
    super(world);
  }

  public EntityTNTStick(final World world, final EntityLivingBase entityLiving) {
    super(world, entityLiving);
  }

  @SideOnly(Side.CLIENT)
  public EntityTNTStick(final World world, final double x, final double y, final double z) {
    super(world, x, y, z);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    this.worldObj.spawnParticle("smoke", this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
  }

  protected void explode() {
    this.setDead();
    final float size = 3f;
    final NonBreakingExplosion explosion = new NonBreakingExplosion(worldObj, this, posX, posY, posZ, size, TwoTility.proxy.configTNTStickDamageMultiplier);
    explosion.doExplosionA();
    explosion.doExplosionB(true);

    if (worldObj.isRemote == false) {
      for (final EntityPlayerMP entityplayer : ((List<EntityPlayerMP>) worldObj.playerEntities)) {
        if (entityplayer.getDistanceSq(posX, posY, posZ) < 4096.0D) {
          entityplayer.playerNetServerHandler.sendPacketToPlayer(new Packet60Explosion(posX, posY, posZ, size, explosion.affectedBlockPositions, (Vec3) explosion.func_77277_b().get(entityplayer)));
        }
      }
    }
  }

  @Override
  protected void onImpact(final MovingObjectPosition movingObjectPosition) {
    if (movingObjectPosition.entityHit != null) {
      movingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
    }

    if (!this.worldObj.isRemote) {
      explode();
    }
  }
}
