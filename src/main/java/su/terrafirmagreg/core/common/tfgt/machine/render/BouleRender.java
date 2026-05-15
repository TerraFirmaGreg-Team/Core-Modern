package su.terrafirmagreg.core.common.tfgt.machine.render;

import java.util.Arrays;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.data.loading.DatagenModLoader;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import su.terrafirmagreg.core.common.data.TFGTags;

public class BouleRender extends DynamicRender<IWorkableMultiController, BouleRender> {
    public static DynamicRender<?, ?> makeRender() {
        return new BouleRender();
    }

    public static final Codec<BouleRender> CODEC = Codec.unit(BouleRender::new);
    public static final DynamicRenderType<IWorkableMultiController, BouleRender> TYPE = new DynamicRenderType<>(BouleRender.CODEC);

    private final FluidBlockRenderer fluidBlockRenderer;
    private ItemRenderer itemRenderer;
    private @Nullable ResourceLocation cachedRecipe;
    private @Nullable Fluid cachedFluid;

    public BouleRender() {
        fluidBlockRenderer = FluidBlockRenderer.Builder.create().getRenderer();
        if (!DatagenModLoader.isRunningDataGen()) {
            itemRenderer = Minecraft.getInstance().getItemRenderer();
        }
    }

    @Override
    public @NotNull DynamicRenderType<IWorkableMultiController, BouleRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 16;
    }

    @Override
    public void render(IWorkableMultiController machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids)
            return;
        if (!machine.isFormed())
            return;

        var recipeLogic = machine.getRecipeLogic();
        var recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            cachedFluid = null;
            cachedRecipe = null;
            return;
        }

        final MultiblockControllerMachine machineSelf = machine.self();
        if (machineSelf.getOffsetTimer() % 20 == 0 || recipe.id != cachedRecipe) {
            cachedRecipe = recipe.id;

            if (machine.isActive()) {
                cachedFluid = RenderUtil.getRecipeFluidToRender(recipe);
            } else {
                cachedFluid = null;
            }
        }

        BlockPos pos = machineSelf.getPos();
        BlockPos center = pos.relative(machineSelf.getFrontFacing().getOpposite());
        center = center.subtract(pos);

        double progress = recipeLogic.getProgressPercent();

        // Render the fluid
        if (cachedFluid != null) {
            var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
            var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

            poseStack.pushPose();

            var pose = poseStack.last().pose();
            double translate = Math.max(progress, 0.05);
            poseStack.translate(0, -translate, 0);

            fluidBlockRenderer.drawPlane(Direction.UP, Collections.singletonList(center), pose, consumer, cachedFluid,
                    RenderUtil.FluidTextureType.STILL, packedOverlay, machineSelf.getPos());

            poseStack.popPose();
        }

        // Find the rod and dipped item
        var contents = new ObjectArrayList<Content>();
        contents.addAll(recipe.getInputContents(ItemRecipeCapability.CAP));
        var ingredients = contents.stream()
                .map(Content::getContent)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .flatMap(Arrays::stream).toList();
        var rod = ingredients.stream().filter(i -> i.is(TFGTags.Items.PrecisionFabricatorHolderRods)).findFirst();
        var dipped = ingredients.stream().filter(i -> i.is(TFGTags.Items.PrecisionFabricatorDippedItems)).findFirst();
        var output = ItemRecipeCapability.CAP.of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).getContent()).getItems()[0];

        // Rotate the pose stack to match the direction that the machine is facing
        float facingYRot = machineSelf.getFrontFacing().toYRot();
        float rotationOffset = 180f - facingYRot;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationOffset));
        poseStack.translate(-0.5f, 0, -0.5f);

        // Then render
        if (progress < 0.05) {
            if (rod.isPresent()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 2.45 - (progress * 20), 1.5);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) (progress * 36000f)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
                poseStack.scale(1.05f, 1.05f, 1.05f);
                //poseStack.rotateAround(direction, 0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(rod.get(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, machine.self().getLevel(), 0);
                poseStack.popPose();
            }

            if (dipped.isPresent()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 1.85 - (progress * 20), 1.5);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) (progress * 36000f) + 90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.scale(0.25f, 0.25f, 0.25f);
                //poseStack.rotateAround(direction, 0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(dipped.get(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, machine.self().getLevel(), 0);
                poseStack.popPose();
            }
        } else {
            if (rod.isPresent()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 1.45 + (progress - 0.05), 1.5);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) (progress * 36000f)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
                //poseStack.rotateAround(direction, 0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(rod.get(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, machine.self().getLevel(), 0);
                poseStack.popPose();
            }

            poseStack.pushPose();
            poseStack.translate(0.5, 0.45 + (progress - 0.05), 1.5);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (progress * 36000f) + 90));
            poseStack.scale(0.9f, 0.9f, 0.9f);
            //poseStack.rotateAround(direction, 0.5f, 0.5f, 0.5f);
            itemRenderer.renderStatic(output, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, machine.self().getLevel(), 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
