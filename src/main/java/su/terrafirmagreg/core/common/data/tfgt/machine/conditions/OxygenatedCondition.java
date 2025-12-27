package su.terrafirmagreg.core.common.data.tfgt.machine.conditions;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import earth.terrarium.adastra.api.systems.OxygenApi;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Checks for oxygen on any adjacent side using ad_astra API.
 * Set `requiresOxygen` to true for "Oxygenated Environment"
 * or false for "Deoxygenated Environment".
 */
public class OxygenatedCondition extends RecipeCondition {

    public static final Codec<OxygenatedCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.BOOL.fieldOf("isOxygenated").forGetter(cond -> cond.isOxygenated))
            .apply(instance, OxygenatedCondition::new));

    private final boolean isOxygenated;

    public OxygenatedCondition() {
        super(false);
        this.isOxygenated = true;
    }

    public OxygenatedCondition(boolean isReverse, boolean requiresOxygen) {
        super(isReverse);
        this.isOxygenated = requiresOxygen;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TFGTRecipeConditions.OXYGENATED;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(
                isOxygenated ? "tfg.tooltip.recipe_condition.oxygenated.true"
                        : "tfg.tooltip.recipe_condition.oxygenated.false");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        BlockPos pos = machine.getPos();
        boolean hasAdjOxygen = hasOxygenOnAnySide(serverLevel, pos);
        boolean passes = isOxygenated == hasAdjOxygen;
        return isReverse != passes;
    }

    private static boolean hasOxygenOnAnySide(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (OxygenApi.API.hasOxygen(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new OxygenatedCondition();
    }
}
