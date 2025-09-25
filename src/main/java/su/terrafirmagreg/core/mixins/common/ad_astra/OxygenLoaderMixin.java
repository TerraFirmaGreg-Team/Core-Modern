package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.common.blockentities.base.RecipeMachineBlockEntity;
import earth.terrarium.adastra.common.blockentities.machines.OxygenLoaderBlockEntity;
import earth.terrarium.adastra.common.recipes.machines.OxygenLoadingRecipe;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;

@Mixin(value = OxygenLoaderBlockEntity.class, remap = false)
public abstract class OxygenLoaderMixin extends RecipeMachineBlockEntity<OxygenLoadingRecipe> implements BotariumFluidBlock<WrappedBlockFluidContainer> {

    public OxygenLoaderMixin(BlockPos pos, BlockState state, int containerSize, Supplier<RecipeType<OxygenLoadingRecipe>> recipeType) {
        super(pos, state, containerSize, recipeType);
    }

    @Shadow
    private WrappedBlockFluidContainer fluidContainer;

    /**
     * @author Bumperdo09
     * @reason Fixes the oxygen spreader consuming more oxygen than it can actually use
     * https://github.com/TerraFirmaGreg-Team/Modpack-Modern/issues/1760
     * https://github.com/terrarium-earth/Ad-Astra/issues/544
     */
    @ModifyExpressionValue(method = "recipeTick", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/blockentities/machines/OxygenLoaderBlockEntity;canCraft()Z"))
    private boolean modifyCanCraftCheck(boolean original) {
        // boolean logic is different here than in the PR because the not operator is applied after the injection
        return original && (recipe.result().getFluidAmount() == fluidContainer.internalInsert(recipe.result(), true));
    }
}
