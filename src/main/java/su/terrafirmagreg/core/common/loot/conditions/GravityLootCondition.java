package su.terrafirmagreg.core.common.loot.conditions;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.api.systems.GravityApi;
import earth.terrarium.adastra.common.constants.PlanetConstants;

import su.terrafirmagreg.core.common.data.TFGLootConditions;

/**
 * Loot condition that checks if the current gravity matches.
 */
public class GravityLootCondition implements LootItemCondition {

    private static final float EARTH_GRAVITY = PlanetConstants.EARTH_GRAVITY;

    public enum Mode {
        GT,
        LT,
        BETWEEN
    }

    private final boolean isReverse;
    private final Mode mode;
    private final float value;
    private final boolean valuePresent;
    private final float start;
    private final boolean startPresent;
    private final float end;
    private final boolean endPresent;

    public GravityLootCondition(boolean isReverse, Mode mode, float value, boolean valuePresent, float start, boolean startPresent, float end, boolean endPresent) {
        this.isReverse = isReverse;
        this.mode = mode;
        this.value = value;
        this.valuePresent = valuePresent;
        this.start = start;
        this.startPresent = startPresent;
        this.end = end;
        this.endPresent = endPresent;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return TFGLootConditions.GRAVITY_LOOT_CONDITION.get();
    }

    @Override
    public boolean test(LootContext context) {
        var level = context.getLevel();
        BlockPos pos = getPos(context);
        float gravityRatio = GravityApi.API.getGravity(level, pos);

        boolean passes = isPasses(gravityRatio);
        return isReverse != passes;
    }

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
                if (startPresent && endPresent) {
                    float s = Math.min(start, end);
                    float e = Math.max(start, end);
                    float sRatio = s / EARTH_GRAVITY;
                    float eRatio = e / EARTH_GRAVITY;
                    passes = gravityRatio > sRatio && gravityRatio < eRatio;
                } else {
                    passes = false;
                }
            }
            default -> passes = false;
        }
        return passes;
    }

    private BlockPos getPos(LootContext context) {
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin != null) {
            return BlockPos.containing(origin);
        }
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null) {
            return entity.blockPosition();
        }
        return BlockPos.ZERO;
    }

    public static class ConditionSerializer implements Serializer<GravityLootCondition> {
        @Override
        public void serialize(@NotNull JsonObject json, @NotNull GravityLootCondition instance, @NotNull JsonSerializationContext ctx) {
            json.addProperty("isReverse", instance.isReverse);
            json.addProperty("mode", instance.mode.name());
            if (instance.valuePresent) {
                json.addProperty("value", instance.value);
            }
            if (instance.startPresent) {
                json.addProperty("start", instance.start);
            }
            if (instance.endPresent) {
                json.addProperty("end", instance.end);
            }
        }

        @Override
        public @NotNull GravityLootCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx) {
            boolean isReverse = GsonHelper.getAsBoolean(json, "isReverse", false);
            Mode mode = Mode.valueOf(GsonHelper.getAsString(json, "mode", Mode.BETWEEN.name()));
            boolean valuePresent = json.has("value");
            float value = valuePresent ? GsonHelper.getAsFloat(json, "value") : 0f;
            boolean startPresent = json.has("start");
            float start = startPresent ? GsonHelper.getAsFloat(json, "start") : 0f;
            boolean endPresent = json.has("end");
            float end = endPresent ? GsonHelper.getAsFloat(json, "end") : 0f;
            return new GravityLootCondition(isReverse, mode, value, valuePresent, start, startPresent, end, endPresent);
        }
    }
}
