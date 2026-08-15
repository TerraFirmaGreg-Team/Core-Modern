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

import net.minecraft.ChatFormatting;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.utils.ClientData;
import electrolyte.greate.GreateValues;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;
import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGRegistries;
import su.terrafirmagreg.core.common.data.fuel_type.FuelType;

public class CombustionEngineBlockEntity extends GeneratingKineticBlockEntity implements ITieredKineticBlockEntity {
    public static final int DEFAULT_SPEED = 32;
    public ScrollValueBehaviour targetSpeed;

    private final int tier;

    @Getter
    private double remainingTicks = 0;
    private SmartFluidTankBehaviour tank;
    private float lastSpeed;
    private boolean oxygenated;
    private float lastOxygenationCheckTicks = 0;

    public CombustionEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        tier = ((CombustionEngineBlock) state.getBlock()).getTier();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap != ForgeCapabilities.FLUID_HANDLER)
            return super.getCapability(cap, side);
        if (side == null || side == Direction.DOWN)
            return tank.getCapability().cast();
        if (getBlockState().getValue(CombustionEngineBlock.FACING) == Direction.DOWN) {
            if (side.getAxis() == Direction.Axis.X)
		return tank.getCapability().cast();
        } else if (getBlockState().getValue(CombustionEngineBlock.FACING) == Direction.UP) {
            if (side.getAxis() == Direction.Axis.Z)
                return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putDouble("RemainingTicks", remainingTicks);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        remainingTicks = tag.getDouble("RemainingTicks");
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
                this, new CombustionEngineValueBox());
        targetSpeed.between(-max, max);
        targetSpeed.value = DEFAULT_SPEED;
        targetSpeed.withCallback(i -> this.updateTargetRotation());

        tank = SmartFluidTankBehaviour.single(this, 2000 * (((CombustionEngineBlock) getBlockState().getBlock()).getTier() * 2));

        behaviours.add(targetSpeed);
        behaviours.add(tank);
    }

    private void updateTargetRotation() {
        if (hasNetwork()) {
            var network = getOrCreateNetwork();
            if (network != null)
                network.remove(this);
        }

        RotationPropagator.handleRemoved(level, worldPosition, this);
        removeSource();
        reActivateSource = true;
        attachKinetics();
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = GreateValues.getMaxCapacityFromMaterial(((CombustionEngineBlock) getBlockState().getBlock()).getMaterial());
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled() || !oxygenated)
            return 0;

        return convertToDirection(targetSpeed.value, getBlockState().getValue(CombustionEngineBlock.FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.goggles.generator_stats")
                .forGoggles(tooltip);
        CreateLang.translate("tooltip.capacityProvided")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(lastCapacityProvided)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        ITieredKineticBlockEntity.super.addToGoggleTooltip(tooltip, isPlayerSneaking, GreateValues.TM[tier], capacity, stress);
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            if (lastSpeed != getGeneratedSpeed()) {
                reActivateSource = true;
                lastSpeed = getGeneratedSpeed();
            }

            if (lastOxygenationCheckTicks < 2) {
                oxygenated = OxygenApi.API.hasOxygen(level, getBlockPos());
                lastOxygenationCheckTicks = 100;
            }
            lastOxygenationCheckTicks--;
        }

        if (enabled() && oxygenated) {
            if (remainingTicks < 2) {
                remainingTicks += 1.0 / getFuelBurnRate();
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

        // The client can't access a block's oxygenation state, only the player
        boolean oxygenated = true;
        Planet planet = PlanetApi.API.getPlanet(level);
        if (planet != null && !planet.oxygen()) {
            PlanetData localData = ClientData.getLocalData();
            if (localData != null)
                oxygenated = localData.oxygen();
        }

        if (enabled() && oxygenated) {
            if (soundInstance != null) {
                if (soundInstance.isStopped() || Math.abs(targetSpeed.value / 256f) + 0.5f != soundInstance.getPitch()) {
                    soundInstance.release();
                    soundInstance = null;
                }
            } else {
                soundInstance = GTSoundEntries.COMBUSTION.playAutoReleasedSound(
                        () -> enabled() && level.isLoaded(getBlockPos()),
                        getBlockPos(), true, 0, 1, Math.abs(targetSpeed.value / 256f) + 0.5f);
            }
        } else {
            if (soundInstance != null) {
                soundInstance.release();
                soundInstance = null;
            }
        }
    }

    public boolean enabled() {
        return validFluidStack() && !self().getBlockState().getValue(CombustionEngineBlock.POWERED);
    }

    private FuelType getFuelType() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(TFGRegistries.FUEL_TYPE), getTank().getFluid().getFluid());
    }

    public boolean validFluidStack() {
        if (getTank().getFluid().isEmpty())
            return false;
        return getFuelType() != FuelType.EMPTY;
    }

    // Minimum burn of 1mB every 10 sec
    public double getFuelBurnRate() {
        return getFuelType().getFuelBurnRate(targetSpeed.value, tier);
    }

    public SmartBlockEntity self() {
        return this;
    }

    public FluidTank getTank() {
        return tank.getPrimaryHandler();
    }

    @Override
    public boolean renderNormally() {
        return false;
    }
}
