package su.terrafirmagreg.core.common.data.tfgt.machine.conditions;

import java.util.Locale;

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

import earth.terrarium.adastra.api.systems.GravityApi;
import earth.terrarium.adastra.common.constants.PlanetConstants;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Recipe condition that checks ad_astra's gravity at the machine position.
 * Compares configured values against Earth ratio.
 * <p>
 * <p>- Modes:
 * <p>- GT: passes when gravity > value
 * <p>- LT: passes when gravity < value
 * <p>- BETWEEN: passes when start < gravity < end
 */
public class GravityCondition extends RecipeCondition {

    private static final float EARTH_GRAVITY = PlanetConstants.EARTH_GRAVITY;

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
    public static final Codec<GravityCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.STRING.xmap(Mode::valueOf, Enum::name).optionalFieldOf("mode").forGetter(c -> c.mode == null ? java.util.Optional.empty() : java.util.Optional.of(c.mode)))
            .and(Codec.FLOAT.optionalFieldOf("value").forGetter(c -> c.valuePresent ? java.util.Optional.of(c.value) : java.util.Optional.empty()))
            .and(Codec.FLOAT.optionalFieldOf("start").forGetter(c -> c.startPresent ? java.util.Optional.of(c.start) : java.util.Optional.empty()))
            .and(Codec.FLOAT.optionalFieldOf("end").forGetter(c -> c.endPresent ? java.util.Optional.of(c.end) : java.util.Optional.empty()))
            .apply(instance, (isReverse, modeOpt, valueOpt, startOpt, endOpt) -> {
                Mode m = modeOpt.orElse(Mode.BETWEEN);
                float v = valueOpt.orElse(0f);
                float s = startOpt.orElse(-1000f);
                float e = endOpt.orElse(1000f);
                return new GravityCondition(isReverse, m, v, valueOpt.isPresent(), s, startOpt.isPresent(), e, endOpt.isPresent());
            }));

    private final Mode mode;
    private final float value;
    private final boolean valuePresent;
    private final float start;
    private final boolean startPresent;
    private final float end;
    private final boolean endPresent;

    // Default template.
    public GravityCondition() {
        super(false);
        this.mode = Mode.BETWEEN;
        this.value = 0f;
        this.valuePresent = false;
        this.start = -1000f;
        this.startPresent = true;
        this.end = 1000f;
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
    public GravityCondition(boolean isReverse, Mode mode, float value, boolean valuePresent, float start, boolean startPresent, float end, boolean endPresent) {
        super(isReverse);
        this.mode = mode == null ? Mode.BETWEEN : mode;
        this.value = value;
        this.valuePresent = valuePresent;
        this.start = start;
        this.startPresent = startPresent;
        this.end = end;
        this.endPresent = endPresent;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TFGTRecipeConditions.GRAVITY;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    // Tooltip with 2 decimal places.
    @Override
    public Component getTooltips() {
        Component label = Component.translatable("tfg.tooltip.recipe_condition.gravity");
        switch (mode) {
            case GT -> {
                String formatted = String.format(Locale.ROOT, "%.2f", value);
                return label.copy().append(Component.literal(" > " + formatted));
            }
            case LT -> {
                String formatted = String.format(Locale.ROOT, "%.2f", value);
                return label.copy().append(Component.literal(" < " + formatted));
            }
            case BETWEEN -> {
                float s = Math.min(start, end);
                float e = Math.max(start, end);
                String fs = String.format(Locale.ROOT, "%.2f", s);
                String fe = String.format(Locale.ROOT, "%.2f", e);
                return label.copy().append(Component.literal(": " + fs + " - " + fe));
            }
            default -> {
                return label;
            }
        }
    }

    /**
     * Evaluates gravity ratio at the machine position.
     */
    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        BlockPos pos = machine.getPos();
        float gravityRatio = GravityApi.API.getGravity(serverLevel, pos);
        boolean passes = isPasses(gravityRatio);
        return isReverse != passes;
    }

    /**
     * Compares the current gravity ratio against configured ranges.
     */
    private boolean isPasses(float gravityRatio) {
        boolean passes;
        switch (mode) {
            case GT -> {
                if (!valuePresent) {
                    passes = false;
                } else {
                    float valueRatio = value / EARTH_GRAVITY;
                    passes = gravityRatio > valueRatio;
                }
            }
            case LT -> {
                if (!valuePresent) {
                    passes = false;
                } else {
                    float valueRatio = value / EARTH_GRAVITY;
                    passes = gravityRatio < valueRatio;
                }
            }
            case BETWEEN -> {
                float s = Math.min(start, end);
                float e = Math.max(start, end);
                float sRatio = s / EARTH_GRAVITY;
                float eRatio = e / EARTH_GRAVITY;
                passes = gravityRatio > sRatio && gravityRatio < eRatio;
            }
            default -> passes = false;
        }
        return passes;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new GravityCondition();
    }

    // Template factories.

    public static GravityCondition greaterThan(float value) {
        return new GravityCondition(false, Mode.GT, value, true, 0f, false, 0f, false);
    }

    public static GravityCondition lessThan(float value) {
        return new GravityCondition(false, Mode.LT, value, true, 0f, false, 0f, false);
    }

    public static GravityCondition ofRange(boolean reverse, float start, float end) {
        return new GravityCondition(reverse, Mode.BETWEEN, 0f, false, start, true, end, true);
    }
}
