package su.terrafirmagreg.core.client.renderer;

import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.CopperPotBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.MixingBowlBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.SkilletBlockEntity;
import su.terrafirmagreg.core.common.recipe.attachment.MixerAttachment;

@SuppressWarnings({ "unused", "UnnecessaryLocalVariable" })
public class CookingApplianceBlockEntityRenderer<T extends CookingApplianceBlockEntity> implements BlockEntityRenderer<T> {

    private static final Map<CookingApplianceBlockEntity, SpinState> SPIN_STATES = new WeakHashMap<>();

    private static class SpinState {
        float lastRenderTime = -1.0F;
        float currentAngle = 0.0F;
        float currentSpeed = 0.0F;
        float lastRunningSpeed = 0.0F;
        int lastDirection = 1;
    }

    public CookingApplianceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public CookingApplianceBlockEntityRenderer() {
    }

    public static class CopperPot extends CookingApplianceBlockEntityRenderer<CopperPotBlockEntity> {
        public CopperPot(BlockEntityRendererProvider.Context context) {
            super(context);
        }
    }

    public static class Skillet extends CookingApplianceBlockEntityRenderer<SkilletBlockEntity> {
        public Skillet(BlockEntityRendererProvider.Context context) {
            super(context);
        }
    }

    public static class MixingBowl extends CookingApplianceBlockEntityRenderer<MixingBowlBlockEntity> {
        public MixingBowl(BlockEntityRendererProvider.Context context) {
            super(context);
        }
    }

    @Override
    public void render(T appliance, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (appliance.getLevel() == null)
            return;

        int maxTanks = Math.max(appliance.getFluidOutputCount(), appliance.getFluidInputCount());

        boolean hasAnyOutputFluid = false;
        for (int i = 0; i < appliance.getFluidOutputCount(); i++) {
            if (!appliance.getFluidOutputTank(i).isEmpty()) {
                hasAnyOutputFluid = true;
                break;
            }
        }

        // Fluid Plane Renderings
        for (int i = 0; i <= maxTanks; i++) {
            FluidStack fluidToRender = FluidStack.EMPTY;
            float fluidToRenderCapacity = 0.5F;

            // Check if output tank has fluid.
            if (appliance.getFluidOutputCount() > i) {
                FluidStack outFluid = appliance.getFluidOutputTank(i);
                if (!outFluid.isEmpty()) {
                    fluidToRender = outFluid;
                    fluidToRenderCapacity = appliance.getFluidOutputCapacity();
                }
            }

            // Check if input tank has fluid.
            if (fluidToRender.isEmpty() && !hasAnyOutputFluid && appliance.getFluidInputCount() > i) {
                FluidStack inFluid = appliance.getFluidInputTank(i);
                if (!inFluid.isEmpty()) {
                    fluidToRender = inFluid;
                    fluidToRenderCapacity = appliance.getFluidInputCapacity();
                }
            }

            // Render Fluid
            if (!fluidToRender.isEmpty()) {
                int fluidLevel = fluidToRender.getAmount();

                float spacing = 1 / 16F;

                float minX = appliance instanceof MixingBowlBlockEntity ? 3 / 16F : 2 / 16F;
                float maxX = 13 / 16F;

                float minZ = 3 / 16F;
                float maxZ = 13 / 16F;

                float minY = (2 / 16F) + (i * spacing);
                float maxY = (appliance instanceof MixingBowlBlockEntity ? 6 / 16F : 14 / 16F) + (i * spacing);
                float fluidY = Mth.map(fluidLevel, 0, fluidToRenderCapacity, minY, maxY);

                RenderHelpers.renderFluidFace(poseStack, fluidToRender, buffer, minX, minZ, maxX, maxZ, fluidY, combinedOverlay, combinedLight);
            }
        }

        // Don't render items if there is a block above copper pot.
        Level level = appliance.getLevel();
        BlockPos pos = appliance.getBlockPos();
        BlockState above = level.getBlockState(pos.above());
        boolean stopRender = above.canOcclude() && Block.isFaceFull(above.getOcclusionShape(level, pos.above()), Direction.DOWN);
        if (stopRender && appliance instanceof CopperPotBlockEntity) {
            return;
        }

        // Item Inputs and Outputs Rendering
        appliance.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            long posSeed = appliance.getBlockPos().asLong();

            // Count filed slots.
            int totalItemsToRender = 0;

            for (int i = 0; i < appliance.getItemInputCount(); i++) {
                if (!cap.getStackInSlot(i).isEmpty()) {
                    totalItemsToRender++;
                }
            }
            for (int i = 0; i < appliance.getItemOutputCount(); i++) {
                int slot = appliance.getItemOutputStartSlot() + i;
                if (!cap.getStackInSlot(slot).isEmpty()) {
                    totalItemsToRender++;
                }
            }

            int currentRenderIndex = 0;
            int cookTime = appliance.getCookingTicks();
            float rpm = 0;

            MechanicalMixerBlockEntity mixer = MixerAttachment.getMixer(appliance);
            boolean hasMixerAttached = mixer != null;

            // Determine target angular velocity.
            float targetSpeed = 0.0F;
            if (hasMixerAttached) {
                if (mixer.running && mixer.runningTicks >= 20) {
                    float headRpm = mixer.getRenderedHeadRotationSpeed(partialTicks);
                    targetSpeed = headRpm * 3.0F / 10.0F;
                }
            } else if (appliance instanceof MixingBowlBlockEntity mixingBowl) {
                // Manual bowl mixing.
                float bowlAngle = mixingBowl.getRotationAngle(partialTicks);
                SpinState state = SPIN_STATES.computeIfAbsent(appliance, k -> new SpinState());
                state.currentAngle = bowlAngle;
                state.currentSpeed = 0.0F;
                state.lastRenderTime = AnimationTickHolder.getRenderTime(appliance.getLevel());
            }

            float unifiedRotationAngle = 0.0F;
            if (hasMixerAttached) {
                SpinState state = SPIN_STATES.computeIfAbsent(appliance, k -> new SpinState());
                float now = AnimationTickHolder.getRenderTime(appliance.getLevel());

                if (state.lastRenderTime < 0.0F || now < state.lastRenderTime) {
                    state.lastRenderTime = now;
                }

                float dt = Math.min(now - state.lastRenderTime, 2.0F);
                state.lastRenderTime = now;

                if (targetSpeed != 0.0F) {
                    state.lastDirection = targetSpeed > 0.0F ? 1 : -1;
                    state.lastRunningSpeed = targetSpeed;

                    float lerpFactor = 1.0F - (float) Math.pow(0.85F, dt);
                    state.currentSpeed = Mth.lerp(lerpFactor, state.currentSpeed, targetSpeed);
                    state.currentAngle = (state.currentAngle + state.currentSpeed * dt) % 360.0F;
                    if (state.currentAngle < 0.0F) {
                        state.currentAngle += 360.0F;
                    }
                } else if (state.currentAngle != 0.0F || state.currentSpeed != 0.0F) {
                    float desiredReturnSpeed = getDesiredReturnSpeed(state);
                    float lerpFactor = 1.0F - (float) Math.pow(0.80F, dt);
                    state.currentSpeed = Mth.lerp(lerpFactor, state.currentSpeed, desiredReturnSpeed);
                    float nextAngle = state.currentAngle + state.currentSpeed * dt;

                    if (state.lastDirection > 0 && nextAngle >= 360.0F) {
                        state.currentAngle = 0.0F;
                        state.currentSpeed = 0.0F;
                    } else if (state.lastDirection < 0 && nextAngle <= 0.0F) {
                        state.currentAngle = 0.0F;
                        state.currentSpeed = 0.0F;
                    } else {
                        state.currentAngle = nextAngle;
                    }
                }

                unifiedRotationAngle = state.currentAngle;
            }
            if (appliance instanceof MixingBowlBlockEntity mixingBowl && mixingBowl.isManualMixing()) {
                unifiedRotationAngle = mixingBowl.getRotationAngle(partialTicks);
            }

            // Render inputs
            for (int i = 0; i < appliance.getItemInputCount(); i++) {
                ItemStack item = cap.getStackInSlot(i);
                if (!item.isEmpty()) {
                    renderFloatingItem(poseStack, buffer, combinedLight, combinedOverlay, appliance, item, posSeed, currentRenderIndex, totalItemsToRender, unifiedRotationAngle);
                    currentRenderIndex++;
                }
            }

            // Render outputs
            for (int i = 0; i < appliance.getItemOutputCount(); i++) {
                int slot = appliance.getItemOutputStartSlot() + i;
                ItemStack item = cap.getStackInSlot(slot);
                if (!item.isEmpty()) {
                    renderFloatingItem(poseStack, buffer, combinedLight, combinedOverlay, appliance, item, posSeed, currentRenderIndex, totalItemsToRender, unifiedRotationAngle);
                    currentRenderIndex++;
                }
            }

            // Tool Item Rendering
            float toolY = Mth.clamp(getHighestFluidLevel(appliance), 0.4F, 1.0F);
            ItemStack tool = cap.getStackInSlot(appliance.getToolSlot());

            // Base Translations
            float toolScale = 0.8F;
            float tranX = 0;
            float tranY = 0;
            float tranZ = 0;
            float corner = 0;
            float rotX = 0;
            float rotY = 0;
            float rotZ = 0;

            // Copper Pot Translations
            float potTranX = 0.6F;
            float potTranY = toolY;
            float potTranZ = 0.6F;
            float potRotX = 45.0F;
            float potRotY = 45.0F;
            float potRotZ = 110.0F;

            if (appliance instanceof CopperPotBlockEntity) {
                tranX = potTranX;
                tranY = potTranY;
                tranZ = potTranZ;
                rotX = potRotX;
                rotY = potRotY;
                rotZ = potRotZ;
            }

            // Skillet Translations
            float skilletTranX = 0.2F;
            float skilletTranY = 0.2F;
            float skilletTranZ = 0.7F;
            float skilletRotX = 90.0F;
            float skilletRotY = -20.0F;
            float skilletRotZ = 190.0F;

            if (appliance instanceof SkilletBlockEntity) {
                corner = 180;
                tranX = skilletTranX;
                tranY = skilletTranY;
                tranZ = skilletTranZ;
                rotX = skilletRotX;
                rotY = skilletRotY;
                rotZ = skilletRotZ;
            }

            // Mixing Bowl Translations
            float bowlTranX = 0.65F;
            float bowlTranY = 0.4F;
            float bowlTranZ = 0.65F;
            float bowlRotX = 45.0F;
            float bowlRotY = 45.0F;
            float bowlRotZ = 110.0F;

            if (appliance instanceof MixingBowlBlockEntity) {
                tranX = bowlTranX;
                tranY = bowlTranY;
                tranZ = bowlTranZ;
                rotX = bowlRotX;
                rotY = bowlRotY;
                rotZ = bowlRotZ;
            }

            // Render Tool
            if (!tool.isEmpty()) {
                BlockState state = appliance.getBlockState();
                Direction facing = Direction.SOUTH;
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                } else if (state.hasProperty(BlockStateProperties.FACING)) {
                    facing = state.getValue(BlockStateProperties.FACING);
                }

                float horizontalAngle = facing.toYRot();

                poseStack.pushPose();

                // Pivot around center.
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(corner));
                poseStack.mulPose(Axis.YP.rotationDegrees(-horizontalAngle));

                if (unifiedRotationAngle != 0.0F) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(unifiedRotationAngle));
                }

                poseStack.translate(-0.5F, -0.5F, -0.5F);
                poseStack.translate(tranX, tranY, tranZ);
                poseStack.scale(toolScale, toolScale, toolScale);
                poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        tool,
                        ItemDisplayContext.FIXED,
                        combinedLight,
                        combinedOverlay,
                        poseStack,
                        buffer,
                        appliance.getLevel(),
                        0);

                poseStack.popPose();
            }
        });
    }

    private static float getDesiredReturnSpeed(SpinState state) {
        float remaining = state.lastDirection > 0
                ? (360.0F - state.currentAngle)
                : state.currentAngle;

        float decelWindow = 90.0F;
        float idleCreepSpeed = 1.0F * state.lastDirection;

        float desiredReturnSpeed;
        if (remaining > decelWindow) {
            desiredReturnSpeed = state.lastRunningSpeed;
        } else {
            float progress = Mth.clamp(remaining / decelWindow, 0.0F, 1.0F);
            desiredReturnSpeed = Mth.lerp(progress, idleCreepSpeed, state.lastRunningSpeed);
        }
        return desiredReturnSpeed;
    }

    /**
     * Returns the highest fluid level from all tanks, with preference to output tanks.
     * @return Fluid level between 2/16F and 12/16F
     */
    public static float getHighestFluidLevel(CookingApplianceBlockEntity appliance) {
        float minY = 2 / 16F;
        float maxY = appliance instanceof MixingBowlBlockEntity ? 7 / 16F : 14 / 16F;
        float highestLevel = minY;
        boolean foundInOutput = false;

        int outputCount = appliance.getFluidOutputCount();
        for (int i = 0; i < outputCount; i++) {
            FluidStack outFluid = appliance.getFluidOutputTank(i);
            if (!outFluid.isEmpty()) {
                foundInOutput = true;
                float mappedY = Mth.map(outFluid.getAmount(), 0, appliance.getFluidOutputCapacity(), minY, maxY);
                highestLevel = Math.max(highestLevel, mappedY);
            }
        }

        if (!foundInOutput) {
            int inputCount = appliance.getFluidInputCount();
            for (int i = 0; i < inputCount; i++) {
                FluidStack inFluid = appliance.getFluidInputTank(i);
                if (!inFluid.isEmpty()) {
                    float mappedY = Mth.map(inFluid.getAmount(), 0, appliance.getFluidInputCapacity(), minY, maxY);
                    highestLevel = Math.max(highestLevel, mappedY);
                }
            }
        }

        return highestLevel;
    }

    /**
     * Renders an item in the cooking appliance in a circular formation.
     * @param poseStack The pose stack.
     * @param buffer The buffer source for rendering.
     * @param combinedLight The combined light value for rendering.
     * @param combinedOverlay The combined overlay value for rendering.
     * @param appliance The cooking appliance block entity.
     * @param item The item stack to render.
     * @param posSeed The position seed for rendering.
     * @param slot The slot index for rendering.
     * @param totalCount The total number of items to render. Controls the radius of the circular formation.
     */
    private void renderFloatingItem(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            T appliance,
            ItemStack item,
            long posSeed,
            int slot,
            int totalCount,
            float rotationAngle) {

        float angleStep = 360.0F / Math.max(1, totalCount);
        float currentAngleDegrees = slot * angleStep;

        float orbitAngleDegrees;
        float rotY;

        if (totalCount == 1) {
            orbitAngleDegrees = currentAngleDegrees;
            rotY = currentAngleDegrees + rotationAngle;
        } else {
            orbitAngleDegrees = currentAngleDegrees - rotationAngle;
            rotY = currentAngleDegrees;
        }

        float totalAngleRadians = orbitAngleDegrees * ((float) Math.PI / 180.0F);
        float radius = totalCount == 1 ? 0.0F : 0.20F;
        float scale = 0.33F;

        double x = 0.5D + (Mth.cos(totalAngleRadians) * radius);
        double z = 0.5D + (Mth.sin(totalAngleRadians) * radius);

        float highestFluidY = getHighestFluidLevel(appliance);

        RandomSource rand = RandomSource.create(posSeed ^ (slot * 312345L + 17L));
        float randY = (rand.nextFloat() + rand.nextFloat()) * 0.5F;
        double y = highestFluidY + ((randY - 0.5F) * 0.04D);

        float rotX = rand.nextFloat() * 360.0F;
        float rotZ = rand.nextFloat() * 360.0F;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, appliance.getLevel(), 0);
        poseStack.popPose();
    }
}
