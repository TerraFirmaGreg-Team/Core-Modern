/*
 * Originally from [Create Diesel Generators] (https://github.com/george8188625/Create-Diesel-Generators)
 * Licensed under the MIT license.
 */

package su.terrafirmagreg.core.common.block.create;

import java.util.List;

import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGRegistries;
import su.terrafirmagreg.core.common.data.fuel_type.FuelType;

public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity {
    public static final int DEFAULT_SPEED = 32;
    public ScrollValueBehaviour targetSpeed;

    private Integer maxRPM = AllConfigs.server().kinetics.maxRotationSpeed.get();

    @Getter
    float remainingTicks = 0;
    SmartFluidTankBehaviour tank;
    private float lastCapacity;
    private float lastSpeed;

    public DieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap != ForgeCapabilities.FLUID_HANDLER)
            return super.getCapability(cap, side);
        if (side == null || side == Direction.DOWN)
            return tank.getCapability().cast();
        if (getBlockState().getValue(DieselEngineBlock.FACING) == Direction.DOWN) {
            if (side.getAxis() == Direction.Axis.X)
                return tank.getCapability().cast();
        } else if (getBlockState().getValue(DieselEngineBlock.FACING) == Direction.UP) {
            if (side.getAxis() == Direction.Axis.Z)
                return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putFloat("RemainingTicks", remainingTicks);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        remainingTicks = tag.getFloat("RemainingTicks");
    }

    @Override
    public void remove() {
        if (level.isClientSide && soundInstance != null) {
            soundInstance.release();
            soundInstance = null;
        }
        super.remove();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        Integer max = AllConfigs.server().kinetics.maxRotationSpeed.get();

        targetSpeed = new KineticScrollValueBehaviour(CreateLang.translateDirect("kinetics.speed_controller.rotation_speed"),
                this, new DieselEngineValueBox());
        targetSpeed.between(-max, max);
        targetSpeed.value = DEFAULT_SPEED;
        targetSpeed.withCallback(i -> this.updateTargetRotation());

        tank = SmartFluidTankBehaviour.single(this, 1000);

        behaviours.add(targetSpeed);
        behaviours.add(tank);
    }

    private void updateTargetRotation() {
        if (level == null)
            return;

        if (hasNetwork())
            getOrCreateNetwork().remove(this);

        RotationPropagator.handleRemoved(level, worldPosition, this);
        removeSource();
        attachKinetics();
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = getFuelCapacity() * (1 / getFuelSpeed()) * getFuelSpeed();
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled())
            return 0;

        return convertToDirection(Math.max(maxRPM, targetSpeed.value * getFuelSpeed()), getBlockState().getValue(DieselEngineBlock.FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getGeneratedSpeed() == 0)
            return false;
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        float fuelCapacity = getFuelCapacity() * (1 / getFuelSpeed()) * getFuelSpeed();
        if (!level.isClientSide && (lastSpeed != getGeneratedSpeed() || lastCapacity != fuelCapacity)) {
            reActivateSource = true;
            lastSpeed = getGeneratedSpeed();
            lastCapacity = fuelCapacity;
        }

        if (enabled()) {
            if (remainingTicks < 2) {
                remainingTicks += 1 / getFuelBurnRate();
                tank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
            }

            if (remainingTicks >= 0)
                remainingTicks--;
        }

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected AutoReleasedSound soundInstance;

    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        if (level == null)
            return;

        if (enabled()) {
            if (soundInstance != null) {
                if (!soundInstance.isStopped())
                    return;

                soundInstance.release();
                soundInstance = null;
            } else {
                soundInstance = GTSoundEntries.COMBUSTION.playAutoReleasedSound(
                        () -> enabled() && level.isLoaded(getBlockPos()),
                        getBlockPos(), true, 0, 1, getFuelSoundPitch());
            }
        } else {
            if (soundInstance != null) {
                soundInstance.release();
                soundInstance = null;
            }
        }
    }

    // IEngine
    public boolean enabled() {
        if (validFS())
            return !self().getBlockState().getValue(DieselEngineBlock.POWERED);
        return false;
    }

    public boolean validFS() {
        if (fs().isEmpty())
            return false;
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), fs().getFluid()) != FuelType.EMPTY;
    }

    public FluidStack fs() {
        return getTank().getFluid();
    }

    public float getFuelSpeed() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), fs().getFluid()).speed();
    }

    public float getFuelCapacity() {
        float speed = getFuelSpeed();
        if (speed == 0)
            return speed;
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), fs().getFluid()).strength() / speed;
    }

    public float getFuelBurnRate() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), fs().getFluid()).burn();
    }

    public float getFuelSoundPitch() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), fs().getFluid()).soundPitch();
    }

    public SmartBlockEntity self() {
        return this;
    }

    public FluidTank getTank() {
        return tank.getPrimaryHandler();
    }
}
