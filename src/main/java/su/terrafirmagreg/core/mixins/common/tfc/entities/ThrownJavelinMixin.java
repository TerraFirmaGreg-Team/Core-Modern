package su.terrafirmagreg.core.mixins.common.tfc.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import su.terrafirmagreg.core.common.entity.projectile.ILeashedJavelin;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.minecraft.AbstractArrowAccessor;

/**
 *  Mixin for ThrownJavelin entity to add leashing and recall functionality.
 */
@Mixin(value = ThrownJavelin.class)
public abstract class ThrownJavelinMixin extends AbstractArrow implements ILeashedJavelin {

    @Unique
    private static final EntityDataAccessor<Boolean> TFG$DATA_LEASHED = SynchedEntityData.defineId(ThrownJavelin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> TFG$DATA_RECALLING = SynchedEntityData.defineId(ThrownJavelin.class, EntityDataSerializers.BOOLEAN);

    protected ThrownJavelinMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSynchedData(CallbackInfo ci) {
        this.entityData.define(TFG$DATA_LEASHED, false);
        this.entityData.define(TFG$DATA_RECALLING, false);
    }

    @Override
    public boolean tfg$isLeashed() {
        return this.entityData.get(TFG$DATA_LEASHED);
    }

    @Override
    public void tfg$setLeashed(@Nullable Player player) {
        this.entityData.set(TFG$DATA_LEASHED, player != null);
        if (player != null) {
            this.setOwner(player);
        }
    }

    @Override
    @Nullable
    public Player tfg$getLeasher() {
        Entity owner = this.getOwner();
        return owner instanceof Player ? (Player) owner : null;
    }

    @Override
    public void tfg$setRecalling(boolean recalling) {
        this.entityData.set(TFG$DATA_RECALLING, recalling);
        if (recalling) {
            this.noPhysics = true;
        }
    }

    @Override
    public boolean tfg$isRecalling() {
        return this.entityData.get(TFG$DATA_RECALLING);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tfg$onTick(CallbackInfo ci) {
        if (tfg$isLeashed()) {
            Player leasher = tfg$getLeasher();
            if (leasher == null || !leasher.isAlive() || leasher.level() != this.level() || !(leasher.getMainHandItem().is(Items.LEAD) || leasher.getOffhandItem().is(Items.LEAD))) {
                if (!this.level().isClientSide) {
                    tfg$setLeashed(null);
                    tfg$setRecalling(false);
                    this.setNoGravity(false);
                    this.noPhysics = false;
                }
                return;
            }

            // When Recalling.
            if (tfg$isRecalling()) {
                // Recall velocity and acceleration.
                double speedFactor = 0.5D;
                double accel = 0.2D;

                this.noPhysics = true;
                this.setNoGravity(true);

                this.xo = this.getX();
                this.yo = this.getY();
                this.zo = this.getZ();

                Vec3 targetPos = leasher.getEyePosition();
                Vec3 direction = targetPos.subtract(this.position());

                this.setPos(this.getX() + direction.x * speedFactor, this.getY() + direction.y * speedFactor, this.getZ() + direction.z * speedFactor);

                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(direction.normalize().scale(accel)));

                // Update rotation to face the player.
                Vec3 movement = direction.normalize();
                this.setYRot((float) (Mth.atan2(movement.x, movement.z) * (180F / Math.PI)));
                this.setXRot((float) (Mth.atan2(movement.y, movement.horizontalDistance()) * (180F / Math.PI)));

                // Forceful disconnect at double max distance or at zero distance.
                if (!this.level().isClientSide && (this.distanceToSqr(leasher) < 4.0D || this.getBoundingBox().intersects(leasher.getBoundingBox()))) {
                    this.shakeTime = 0;
                    this.playerTouch(leasher);
                    if (!this.isRemoved()) {
                        tfg$setLeashed(null);
                        tfg$setRecalling(false);
                        this.noPhysics = false;
                        this.setNoGravity(false);
                    }
                }
                ci.cancel();

                // When not Recalling.
            } else {
                double distanceSq = this.distanceToSqr(leasher);
                int maxDist = TFGConfig.SERVER.javelinLeashMaxDistance.get();

                // Forceful disconnect at double max distance.
                if (distanceSq > (maxDist * 2.0) * (maxDist * 2.0)) {
                    if (!this.level().isClientSide) {
                        tfg$setLeashed(null);
                        tfg$setRecalling(false);
                        this.setNoGravity(false);
                        this.noPhysics = false;
                    }
                    return;
                }

                if (distanceSq > maxDist * maxDist) {
                    Vec3 motion = this.getDeltaMovement();
                    Vec3 toLeasher = leasher.getEyePosition().subtract(this.position());
                    // If moving away from player, halt and recoil.
                    if (motion.dot(toLeasher) < 0) {
                        this.setDeltaMovement(toLeasher.normalize().scale(0.2D));

                        // Auto recall if reached limit in air
                        if (!((AbstractArrowAccessor) this).is$inGround()) {
                            tfg$setRecalling(true);
                        }
                    }
                }

                if (this.isNoGravity() && !((AbstractArrowAccessor) this).is$inGround()) {
                    this.setNoGravity(false);
                }
            }
        }
    }
}
