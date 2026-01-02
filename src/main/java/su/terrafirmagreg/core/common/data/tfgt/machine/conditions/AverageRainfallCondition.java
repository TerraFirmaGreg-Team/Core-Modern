package su.terrafirmagreg.core.common.data.tfgt.machine.conditions;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Recipe condition that checks TFC average rainfall at the machine position.
 * <p>Modes:
 * <p>- GT: passes when rainfall > value
 * <p>- LT: passes when rainfall < value
 * <p>- BETWEEN: passes when start < rainfall < end
 */
public class AverageRainfallCondition extends RecipeCondition {

    public enum Mode {
        GT,
        LT,
        BETWEEN
    }

    /**
     * Codec for serializing recipes.
     * <p>- mode: GT|LT|BETWEEN
     * <p>- optional value, start, end bounds
     */
    public static final Codec<AverageRainfallCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.STRING.xmap(Mode::valueOf, Enum::name).fieldOf("mode").forGetter(c -> c.mode))
            .and(Codec.FLOAT.optionalFieldOf("value").forGetter(c -> c.valuePresent ? java.util.Optional.of(c.value) : java.util.Optional.empty()))
            .and(Codec.FLOAT.optionalFieldOf("start").forGetter(c -> c.startPresent ? java.util.Optional.of(c.start) : java.util.Optional.empty()))
            .and(Codec.FLOAT.optionalFieldOf("end").forGetter(c -> c.endPresent ? java.util.Optional.of(c.end) : java.util.Optional.empty()))
            .apply(instance, (isReverse, mode, valueOpt, startOpt, endOpt) -> {
                float v = valueOpt.orElse(0f);
                float s = startOpt.orElse(0f);
                float e = endOpt.orElse(0f);
                return new AverageRainfallCondition(isReverse, mode, v, valueOpt.isPresent(), s, startOpt.isPresent(), e, endOpt.isPresent());
            }));

    private final Mode mode;
    private final float value;
    private final boolean valuePresent;
    private final float start;
    private final boolean startPresent;
    private final float end;
    private final boolean endPresent;

    // Default template.
    public AverageRainfallCondition() {
        super(false);
        this.mode = Mode.BETWEEN;
        this.value = 0f;
        this.valuePresent = false;
        this.start = 0f;
        this.startPresent = true;
        this.end = 500f;
        this.endPresent = true;
    }

    /**
     * Constructor.
     *
     * @param isReverse invert result.
     * @param mode comparison mode.
     * @param value single bound.
     * @param valuePresent whether value is provided.
     * @param start range start.
     * @param startPresent whether start is provided.
     * @param end range end.
     * @param endPresent whether end is provided.
     */
    public AverageRainfallCondition(boolean isReverse, Mode mode, float value, boolean valuePresent, float start, boolean startPresent, float end, boolean endPresent) {
        super(isReverse);
        this.mode = mode;
        this.value = value;
        this.valuePresent = valuePresent;
        this.start = start;
        this.startPresent = startPresent;
        this.end = end;
        this.endPresent = endPresent;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TFGTRecipeConditions.CLIMATE_AVG_RAINFALL;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    // Tooltip (with rounded values)
    @Override
    public Component getTooltips() {
        Component label = Component.translatable("tfg.tooltip.recipe_condition.climate_rain");
        switch (mode) {
            case GT -> {
                long rounded = Math.round(value);
                return label.copy().append(Component.literal(" > " + rounded));
            }
            case LT -> {
                long rounded = Math.round(value);
                return label.copy().append(Component.literal(" < " + rounded));
            }
            case BETWEEN -> {
                float s = Math.min(start, end);
                float e = Math.max(start, end);
                long rs = Math.round(s);
                long re = Math.round(e);
                return label.copy().append(Component.literal(": " + rs + " - " + re));
            }
            default -> {
                return label;
            }
        }
    }

    /**
     * Condition at the machine position.
     *
     * @param recipe current recipe.
     * @param recipeLogic machine recipe logic.
     * @return result with reverse applied.
     */
    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        BlockPos pos = machine.getPos();
        float climate = Climate.getRainfall(serverLevel, pos);

        boolean passes;
        switch (mode) {
            case GT -> passes = climate > value;
            case LT -> passes = climate < value;
            case BETWEEN -> {
                float s = Math.min(start, end);
                float e = Math.max(start, end);
                passes = climate > s && climate < e;
            }
            default -> passes = false;
        }
        return isReverse != passes;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new AverageRainfallCondition();
    }

    // Template factories.

    public static AverageRainfallCondition greaterThan(float value) {
        return new AverageRainfallCondition(false, Mode.GT, value, true, 0f, false, 0f, false);
    }

    public static AverageRainfallCondition lessThan(float value) {
        return new AverageRainfallCondition(false, Mode.LT, value, true, 0f, false, 0f, false);
    }

    public static AverageRainfallCondition ofRange(boolean reverse, float start, float end) {
        return new AverageRainfallCondition(reverse, Mode.BETWEEN, 0f, false, start, true, end, true);
    }
}
