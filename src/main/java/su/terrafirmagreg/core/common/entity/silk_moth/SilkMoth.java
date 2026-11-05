package su.terrafirmagreg.core.common.entity.silk_moth;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

// Taken from Better End, MIT license

public class SilkMoth extends Animal implements FlyingAnimal {

    public SilkMoth(EntityType<? extends SilkMoth> entityType, Level world) {
        super(entityType, world);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new MothLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.xpReward = 1;
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity
                .createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    @Override
    public boolean canBeLeashed(@NotNull Player player) {
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WanderAroundGoal());
        // TODO: figure out how to get moths to hang out near their nests?
        //        this.goalSelector.addGoal(3, new MoveToBlockGoal(this, 1f, 16) {
        //            @Override
        //            protected boolean isValidTarget(@NotNull LevelReader levelReader, @NotNull BlockPos blockPos) {
        //                BlockState state = levelReader.getBlockState(blockPos);
        //                return Helpers.isBlock(state, TFGTags.Blocks.SILK_MOTH_BLOCKS);
        //            }
        //        });
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
    public AgeableMob getBreedOffspring(@NotNull ServerLevel world, @NotNull AgeableMob entity) {
        return null;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.hasCustomName();
    }

    class MothLookControl extends LookControl {
        MothLookControl(Mob entity) {
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

        @Override
        public boolean canUse() {
            return SilkMoth.this.navigation.isDone() && SilkMoth.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return SilkMoth.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 vec3d = this.getRandomLocation();

            if (vec3d != null) {
                try {
                    SilkMoth.this.navigation.moveTo(SilkMoth.this.navigation.createPath(new BlockPos((int) vec3d.x, (int) vec3d.y, (int) vec3d.z), 1), 1.0D);
                } catch (Exception ignored) {
                }
            }
        }

        @Nullable
        private Vec3 getRandomLocation() {
            Vec3 vec3d3 = SilkMoth.this.getViewVector(0.0F);
            Vec3 vec3d4 = HoverRandomPos.getPos(SilkMoth.this, 8, 7, vec3d3.x, vec3d3.z, 1.5707964F, 3, 1);
            return vec3d4 != null ? vec3d4
                    : AirAndWaterRandomPos.getPos(
                            SilkMoth.this,
                            8,
                            4,
                            -2,
                            vec3d3.x,
                            vec3d3.z,
                            1.5707963705062866D);
        }
    }

}
