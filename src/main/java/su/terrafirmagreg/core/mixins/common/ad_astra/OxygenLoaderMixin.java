package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.common.blockentities.base.RecipeMachineBlockEntity;
import earth.terrarium.adastra.common.blockentities.machines.OxygenLoaderBlockEntity;
import earth.terrarium.adastra.common.recipes.machines.OxygenLoadingRecipe;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;

@Mixin(value = OxygenLoaderBlockEntity.class, remap = false)
public abstract class OxygenLoaderMixin extends RecipeMachineBlockEntity<OxygenLoadingRecipe> implements BotariumFluidBlock<WrappedBlockFluidContainer> {

    public OxygenLoaderMixin(BlockPos pos, BlockState state, int containerSize, Supplier<RecipeType<OxygenLoadingRecipe>> recipeType) {
        super(pos, state, containerSize, recipeType);
    }

    /**
     * @author Bumperdo09
     * @reason Fixes the oxygen spreader consuming more oxygen than it can actually use
     * https://github.com/TerraFirmaGreg-Team/Modpack-Modern/issues/1760
     * https://github.com/terrarium-earth/Ad-Astra/issues/544
     */
    @Inject(method = "recipeTick", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$recipeTick(ServerLevel level, WrappedBlockEnergyContainer energyStorage, CallbackInfo ci) {
        if (recipe == null)
            return;
        var fluidContainer = getFluidContainer();
        if (!canCraft() || recipe.result().getFluidAmount() != fluidContainer.internalInsert(recipe.result(), true)) {
            clearRecipe();
            ci.cancel();
        }
    }
}
