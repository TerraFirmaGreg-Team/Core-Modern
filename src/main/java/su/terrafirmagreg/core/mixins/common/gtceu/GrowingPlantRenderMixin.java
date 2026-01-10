package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.*;

import com.gregtechceu.gtceu.client.util.RenderUtil;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.items.FLItems;
import com.eerussianguy.firmalife.common.items.GrapeSeedItem;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.client.renderer.machine.impl.GrowingPlantRender;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.StationaryBerryBushBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@Mixin(value = GrowingPlantRender.class, remap = false)
public class GrowingPlantRenderMixin {

    // Add support for TFC's double crops and berry bushes

    @Unique
    private static final GrowingPlantRender.RenderFunction.ConfigureOnly TFC_DOUBLE_CROP_RENDER = (level, state, progress) -> {
        DoubleCropBlock block = (DoubleCropBlock) state.getBlock();
        IntegerProperty ageProp = block.getAgeProperty();
        int ageValue = (int) (progress * (block.getMaxAge() + 1));

        BlockState bottomState = state;
        bottomState = bottomState.trySetValue(ageProp, ageValue);

        if (progress > 0.5) {
            BlockState topState = state;
            topState = topState.trySetValue(TFCBlockStateProperties.DOUBLE_CROP_PART, DoubleCropBlock.Part.TOP);
            topState = topState.trySetValue(ageProp, ageValue);

            return Arrays.asList(
                    new GrowingPlantRender.StateWithOffset(bottomState),
                    new GrowingPlantRender.StateWithOffset(topState, new Vector3f(0, 1, 0)));
        } else {
            return Collections.singleton(new GrowingPlantRender.StateWithOffset(bottomState));
        }
    };

    @Unique
    private static final GrowingPlantRender.RenderFunction TFC_BERRY_BUSH_RENDER = (level, pos, offset, state, progress, bufferSource, poseStack) -> {

        Lifecycle lc;
        if (progress < 0.33) {
            lc = Lifecycle.HEALTHY;
        } else if (progress < 0.67) {
            lc = Lifecycle.FLOWERING;
        } else {
            lc = Lifecycle.FRUITING;
        }

        state = state.trySetValue(TFCBlockStateProperties.LIFECYCLE, lc);

        poseStack.last().pose().scaleAround((float) progress, 0.5f, 0.0f, 0.5f);
        poseStack.last().normal().scale((float) progress);

        RenderUtil.drawBlock(level, pos, state, bufferSource, poseStack);
    };

    @Unique
    private static final GrowingPlantRender.GrowthMode TFC_DOUBLE_CROP_MODE = new GrowingPlantRender.GrowthMode("double_crop",
            block -> block instanceof DoubleCropBlock, TFC_DOUBLE_CROP_RENDER);

    @Unique
    private static final GrowingPlantRender.GrowthMode TFC_BERRY_BUSH_MODE = new GrowingPlantRender.GrowthMode("berry_bush",
            block -> block instanceof DoubleCropBlock, TFC_BERRY_BUSH_RENDER);

    @Inject(method = "getGrowthModeForBlock", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getGrowthModeForBlock(Block block, CallbackInfoReturnable<GrowingPlantRender.GrowthMode> cir) {
        if (block instanceof DoubleCropBlock) {
            cir.setReturnValue(TFC_DOUBLE_CROP_MODE);
        } else if (block instanceof StationaryBerryBushBlock) {
            cir.setReturnValue(TFC_BERRY_BUSH_MODE);
        }
    }

    // Special case for Grapes, since they aren't normal crops

    @Inject(method = "lambda$static$4", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$recipeBlockCache(GTRecipe recipe, CallbackInfoReturnable<Optional<Block>> cir) {
        var inputGrapes = recipe.getInputContents(ItemRecipeCapability.CAP)
                .stream()
                .map(Content::getContent)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .flatMap(Arrays::stream)
                .map(ItemStack::getItem)
                .filter(GrapeSeedItem.class::isInstance)
                .findFirst()
                .map(GrapeSeedItem.class::cast);

        if (inputGrapes.isPresent()) {
            if (inputGrapes.get() == FLItems.RED_GRAPE_SEEDS.get()) {
                cir.setReturnValue(Optional.of(FLBlocks.WILD_RED_GRAPES.get()));
            } else if (inputGrapes.get() == FLItems.WHITE_GRAPE_SEEDS.get()) {
                cir.setReturnValue(Optional.of(FLBlocks.WILD_WHITE_GRAPES.get()));
            }
        }
    }
}
