package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.recipes.machines.OxygenLoadingRecipe;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

import earth.terrarium.adastra.common.blockentities.machines.OxygenLoaderBlockEntity;

import earth.terrarium.adastra.common.blockentities.base.RecipeMachineBlockEntity;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = OxygenLoaderBlockEntity.class, remap = false)
public abstract class OxygenLoaderMixin extends RecipeMachineBlockEntity<OxygenLoadingRecipe> implements BotariumFluidBlock<WrappedBlockFluidContainer>{

    /**
     * @author Bumperdo09
     * @reason Temp fix for https://github.com/terrarium-earth/Ad-Astra/issues/544
     */
    @Overwrite()
    public void recipeTick(ServerLevel level, WrappedBlockEnergyContainer energyStorage) {
        if (recipe == null) return;
        if (fluidContainer == null) getFluidContainer();
        if (!canCraft() || recipe.result().getFluidAmount() != fluidContainer.internalInsert(recipe.result(), true)) {
            clearRecipe();
            return;
        }

        energyStorage.internalExtract(recipe.energy(), false);

        cookTime++;
        if (cookTime < cookTimeTotal) return;
        craft();
    }
}
