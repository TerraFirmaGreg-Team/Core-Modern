/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ConcurrentModificationException;
import java.util.function.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.IngotPileBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.devices.DoubleIngotPileBlock;
import net.dries007.tfc.util.Metal;

public enum DoubleIngotPileBlockModel implements SimpleStaticBlockEntityModel<DoubleIngotPileBlockModel, IngotPileBlockEntity>
{
    INSTANCE;

    @Override
    public TextureAtlasSprite render(IngotPileBlockEntity pile, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay)
    {
        final int ingots = pile.getBlockState().getValue(DoubleIngotPileBlock.DOUBLE_COUNT);
        final Function<ResourceLocation, TextureAtlasSprite> textureAtlas = Minecraft.getInstance().getTextureAtlas(RenderHelpers.BLOCKS_ATLAS);

        TextureAtlasSprite sprite = null;
        for (int i = 0; i < ingots; i++)
        {
            final var stack = pile.getEntryStack(i);
            MaterialStack material;

            try
            {
                material = ChemicalHelper.getMaterialStack(stack);
            }
            catch (ArrayIndexOutOfBoundsException | ConcurrentModificationException ex)
            {
                return RenderHelpers.missingTexture();
            }

            final int primaryColor = material.material().getMaterialARGB(0);
            final int secondaryColor = material.material().getMaterialARGB(1);
            final Metal metalAtPos = pile.getOrCacheMetal(i);

            boolean shouldUseTFCRender = !(metalAtPos.getId() == Metal.unknown().getId() && !material.isEmpty());
            ResourceLocation metalResource = shouldUseTFCRender ? metalAtPos.getSoftTextureId() : RenderHelpers.TFCMetalBlockTexturePattern;

            sprite = textureAtlas.apply(metalResource);

            final int layer = (i + 6) / 6;
            final boolean oddLayer = (layer % 2) == 1;
            final float x = (i % 3) * 0.33f;
            final float y = (layer - 1) * 1f / 6;
            final float z = i % 6 >= 3 ? 0.5f : 0;

            poseStack.pushPose();
            if (oddLayer)
            {
                // Rotate 90 degrees every other layer
                poseStack.translate(0.5f, 0f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                poseStack.translate(-0.5f, 0f, -0.5f);
            }

            poseStack.translate(x, y, z);

            final float scale = 0.0625f / 2f;
            final float minX = scale * 0.5f;
            final float minY = scale * 0f;
            final float minZ = scale * 0.5f;
            final float maxX = scale * (minX + 10);
            final float maxY = scale * (minY + 5);
            final float maxZ = scale * (minZ + 15);

            if (shouldUseTFCRender)
            {
                RenderHelpers.renderTexturedTrapezoidalCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, minX, maxX, minZ, maxZ, minX + scale, maxX - scale, minZ + scale, maxZ - scale, minY, maxY, 10, 5, 15, oddLayer);
            }
            else
            {
                RenderHelpers.renderTexturedTrapezoidalCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, minX, maxX, minZ, maxZ, minX + scale, maxX - scale, minZ + scale, maxZ - scale, minY, maxY, 10, 5, 15, oddLayer, primaryColor, secondaryColor);
            }

            poseStack.popPose();
        }

        if (sprite == null)
        {
            // Use whatever sprite we found in the ingot pile towards the top as the particle texture
            sprite = RenderHelpers.missingTexture();
        }
        return sprite;
    }

    @Override
    public BlockEntityType<IngotPileBlockEntity> type()
    {
        return TFCBlockEntities.INGOT_PILE.get();
    }

    @Override
    public int faces(IngotPileBlockEntity pile)
    {
        return pile.getBlockState().getValue(DoubleIngotPileBlock.DOUBLE_COUNT) * 6;
    }
}
