package su.terrafirmagreg.core.client.renderer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.common.blockentity.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.CopperPotBlockEntity;

@SuppressWarnings({ "UnnecessaryLocalVariable", "unused" })
public class CookingApplianceBlockEntityRenderer<T extends CookingApplianceBlockEntity> implements BlockEntityRenderer<T> {

    public CookingApplianceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public CookingApplianceBlockEntityRenderer() {
    }

    public static class CopperPot extends CookingApplianceBlockEntityRenderer<CopperPotBlockEntity> {
        public CopperPot(BlockEntityRendererProvider.Context context) {
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

                float minX = 2 / 16F;
                float maxX = 13 / 16F;

                float minZ = 3 / 16F;
                float maxZ = 13 / 16F;

                float minY = (2 / 16F) + (i * spacing);
                float maxY = (12 / 16F) + (i * spacing);
                float fluidY = Mth.map(fluidLevel, 0, fluidToRenderCapacity, minY, maxY);

                RenderHelpers.renderFluidFace(poseStack, fluidToRender, buffer, minX, minZ, maxX, maxZ, fluidY, combinedOverlay, combinedLight);
            }
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

            // Render inputs
            for (int i = 0; i < appliance.getItemInputCount(); i++) {
                ItemStack item = cap.getStackInSlot(i);
                if (!item.isEmpty()) {
                    renderFloatingItem(poseStack, buffer, combinedLight, combinedOverlay, appliance, item, posSeed, currentRenderIndex, totalItemsToRender);
                    currentRenderIndex++;
                }
            }

            // Render outputs
            for (int i = 0; i < appliance.getItemOutputCount(); i++) {
                int slot = appliance.getItemOutputStartSlot() + i;
                ItemStack item = cap.getStackInSlot(slot);
                if (!item.isEmpty()) {
                    renderFloatingItem(poseStack, buffer, combinedLight, combinedOverlay, appliance, item, posSeed, currentRenderIndex, totalItemsToRender);
                    currentRenderIndex++;
                }
            }

            // Tool Item Rendering
            float toolScale = 0.8F;
            float toolY = Mth.clamp(getHighestFluidLevel(appliance), 0.4F, 1.0F);
            ItemStack tool = cap.getStackInSlot(appliance.getToolSlot());
            if (!tool.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.6D, toolY, 0.6D);
                poseStack.scale(toolScale, toolScale, toolScale);
                poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(110.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(tool, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, appliance.getLevel(), 0);
                poseStack.popPose();
            }
        });
    }

    /**
     * Returns the highest fluid level from all tanks, with preference to output tanks.
     * @return Fluid level between 2/16F and 12/16F
     */
    public static float getHighestFluidLevel(CookingApplianceBlockEntity appliance) {
        float minY = 2 / 16F;
        float maxY = 12 / 16F;
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
            int totalCount) {

        float angleStep = 360.0F / Math.max(1, totalCount);
        float currentAngleDegrees = slot * angleStep;
        float currentAngleRadians = currentAngleDegrees * ((float) Math.PI / 180.0F);

        float radius = totalCount == 1 ? 0.0F : 0.20F;
        float scale = 0.33F;

        double x = 0.5D + (Mth.cos(currentAngleRadians) * radius);
        double z = 0.5D + (Mth.sin(currentAngleRadians) * radius);

        float highestFluidY = getHighestFluidLevel(appliance);

        RandomSource rand = RandomSource.create(posSeed ^ (slot * 312345L + 17L));
        float randY = (rand.nextFloat() + rand.nextFloat()) * 0.5F;
        double y = highestFluidY + ((randY - 0.5F) * 0.04D);

        float rotX = rand.nextFloat() * 360.0F;
        float rotY = currentAngleDegrees;
        float rotZ = rand.nextFloat() * 360.0F;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, appliance.getLevel(), 0);
        poseStack.popPose();
    }
}
