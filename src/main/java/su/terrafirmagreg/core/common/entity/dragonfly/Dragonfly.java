package su.terrafirmagreg.core.common.entity.dragonfly;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

// Taken from Better End, MIT license

public class Dragonfly extends Animal implements FlyingAnimal {
    public Dragonfly(EntityType<Dragonfly> entityType, Level world) {
        super(entityType, world);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new DragonflyLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.xpReward = 1;
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity
                .createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    @Override
    public boolean canBeLeashed(@NotNull Player player) {
        return false;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level world) {
        FlyingPathNavigation birdNavigation = new FlyingPathNavigation(this, world) {
            public boolean isStableDestination(@NotNull BlockPos pos) {
                BlockState state = this.level.getBlockState(pos);
                return state.isAir() || !state.blocksMotion();
            }

            public void tick() {
                super.tick();
            }
        };
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanFloat(false);
        birdNavigation.setCanPassDoors(true);
        return birdNavigation;
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, LevelReader world) {
        return world.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new WanderAroundGoal());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    protected Entity.@NotNull MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.BEE_LOOP;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.hasCustomName();
    }

    class DragonflyLookControl extends LookControl {
        DragonflyLookControl(Mob entity) {
            super(entity);
        }

        protected boolean resetXRotOnTick() {
            return true;
        }
    }

    class WanderAroundGoal extends Goal {
        WanderAroundGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return Dragonfly.this.navigation.isDone() && Dragonfly.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return Dragonfly.this.navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3d = this.getRandomLocation();
            if (vec3d != null) {
                BlockPos pos = new BlockPos((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
                try {
                    Path path = Dragonfly.this.navigation.createPath(pos, 1);
                    if (path != null) {
                        Dragonfly.this.navigation.moveTo(path, 1.0D);
                    }
                } catch (Exception ignored) {
                }
            }
            super.start();
        }

        private Vec3 getRandomLocation() {
            int h = downRay(Dragonfly.this.level(), Dragonfly.this.blockPosition(), 16);
            Vec3 rotation = Dragonfly.this.getViewVector(0.0F);
            Vec3 airPos = HoverRandomPos.getPos(Dragonfly.this, 8, 7, rotation.x, rotation.z, 1.5707964F, 3, 1);
            if (airPos != null) {
                if (h > 5 && airPos.y() >= Dragonfly.this.blockPosition().getY()) {
                    airPos = new Vec3(airPos.x, airPos.y - h * 0.5, airPos.z);
                }
                return airPos;
            }
            return AirAndWaterRandomPos.getPos(
                    Dragonfly.this,
                    8,
                    4,
                    -2,
                    rotation.x,
                    rotation.z,
                    1.5707963705062866D);
        }

        public static int downRay(LevelAccessor world, BlockPos pos, int maxDist) {
            int length = 0;
            for (int j = 1; j < maxDist && (world.isEmptyBlock(pos.below(j))); j++) {
                length++;
            }
            return length;
        }
    }

    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel world, @NotNull AgeableMob entity) {
        return null;
    }
}
