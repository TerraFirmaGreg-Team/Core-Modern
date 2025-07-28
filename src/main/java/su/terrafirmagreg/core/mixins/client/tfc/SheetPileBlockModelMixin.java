package su.terrafirmagreg.core.mixins.client.tfc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.SheetPileBlockModel;
import net.dries007.tfc.client.model.SimpleStaticBlockEntityModel;
import net.dries007.tfc.common.blockentities.SheetPileBlockEntity;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.devices.SheetPileBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import su.terrafirmagreg.core.client.TFGClientEventHandler;
import su.terrafirmagreg.core.client.TFGClientHelpers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(value = SheetPileBlockModel.class, remap = false)
@OnlyIn(Dist.CLIENT)
public abstract class SheetPileBlockModelMixin implements SimpleStaticBlockEntityModel<SheetPileBlockModel, SheetPileBlockEntity> {

    /**
     * Измененный метод рендера кучек с одинарными слитками. Теперь они отрисовываются исходя из цвета материала слитка, инфа берется из GTCEu.
     * */
    @Override
    public TextureAtlasSprite render(SheetPileBlockEntity pile, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay)
    {
        final BlockState state = pile.getBlockState();
        TextureAtlasSprite sprite = null;

        final Function<ResourceLocation, TextureAtlasSprite> textureAtlas = Minecraft.getInstance().getTextureAtlas(RenderHelpers.BLOCKS_ATLAS);

        for (Direction direction : Helpers.DIRECTIONS)
        {
            if ((Boolean)state.getValue(DirectionPropertyBlock.getProperty(direction)))
            { // The properties are authoritative on which sides should be rendered
                final var stack = pile.getSheet(direction);
                final var material = ChemicalHelper.getMaterialStack(stack);
                final int primaryColor = material.material().getMaterialARGB(0);
                final int secondaryColor = material.material().getMaterialARGB(1);
                Metal metalAtPos = pile.getOrCacheMetal(direction);

                boolean shouldUseTFCRender = !(metalAtPos.getId() == Metal.unknown().getId() && !material.isEmpty());
                ResourceLocation metalResource = shouldUseTFCRender ? metalAtPos.getTextureId() : TFGClientEventHandler.TFCMetalBlockTexturePattern;

                sprite = (TextureAtlasSprite)textureAtlas.apply(metalResource);
                this.tfg$renderSheet(poseStack, sprite, buffer, direction, packedLight, packedOverlay, shouldUseTFCRender, primaryColor, secondaryColor);
            }
        }

        if (sprite == null) {
            sprite = RenderHelpers.missingTexture();
        }

        return sprite;
    }

    @Unique
    private void tfg$renderSheet(PoseStack poseStack, TextureAtlasSprite sprite, VertexConsumer buffer, Direction direction, int packedLight, int packedOverlay, boolean shouldUseTFCRender, int primaryColor, int secondaryColor) {
        if (shouldUseTFCRender)
            RenderHelpers.renderTexturedCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, SheetPileBlock.getShapeForSingleFace(direction).bounds());
        else
            TFGClientHelpers.renderTexturedCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, SheetPileBlock.getShapeForSingleFace(direction).bounds(), primaryColor, secondaryColor);
    }

}
