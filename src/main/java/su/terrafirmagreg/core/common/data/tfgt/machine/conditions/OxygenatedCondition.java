package su.terrafirmagreg.core.common.data.tfgt.machine.conditions;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.atmosphere.AtmosphereSystem;
import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Recipe condition that requires oxygen adjacency using ad_astra's OxygenApi.
 * <p>
 * <p>- isOxygenated = true: passes when block has oxygen.
 * <p>- isOxygenated = false: passes when block does not have oxygen.
 * <p>
 */
//TODO: wth is going on here some of this smells bad
public class OxygenatedCondition extends RecipeCondition {

    public static final Codec<OxygenatedCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.BOOL.fieldOf("isOxygenated").forGetter(cond -> cond.isOxygenated))
            .apply(instance, OxygenatedCondition::new));

    private final boolean isOxygenated;

    public OxygenatedCondition() {
        super(false);
        this.isOxygenated = true;
    }

    /**
     * Constructor.
     *
     * @param isReverse invert result.
     * @param requiresOxygen true to require oxygen. False to require none.
     */
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

    // Tooltip.
    @Override
    public Component getTooltips() {
        return Component.translatable(
                isOxygenated ? "tfg.tooltip.recipe_condition.oxygenated.true"
                        : "tfg.tooltip.recipe_condition.oxygenated.false");
    }

    /**
     * Checks oxygen on server.
     * Returns false on client.
     */
    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        BlockPos pos = machine.getPos();
        // Check the machine position directly - envelope includes shell blocks
        boolean hasOxygen = AtmosphereSystem.hasOxygen(serverLevel, pos);
        boolean passes = isOxygenated == hasOxygen;
        return isReverse != passes;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new OxygenatedCondition();
    }
}
