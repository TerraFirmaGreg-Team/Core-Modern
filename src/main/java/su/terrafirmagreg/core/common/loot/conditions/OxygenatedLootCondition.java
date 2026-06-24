package su.terrafirmagreg.core.common.loot.conditions;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.api.systems.OxygenApi;

import su.terrafirmagreg.core.common.data.TFGLootConditions;

/**
 * Loot condition that checks if the current oxygenated check passes.
 */
public class OxygenatedLootCondition implements LootItemCondition {

    private final boolean isReverse;
    private final boolean isOxygenated;

    public OxygenatedLootCondition(boolean isReverse, boolean isOxygenated) {
        this.isReverse = isReverse;
        this.isOxygenated = isOxygenated;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return TFGLootConditions.OXYGENATED_LOOT_CONDITION.get();
    }

    @Override
    public boolean test(LootContext context) {
        var level = context.getLevel();
        BlockPos pos = getPos(context);

        boolean hasAdjOxygen = hasOxygenOnAnySide(level, pos);
        boolean passes = isOxygenated == hasAdjOxygen;
        return isReverse != passes;
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

    private static boolean hasOxygenOnAnySide(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (OxygenApi.API.hasOxygen(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    public static class ConditionSerializer implements Serializer<OxygenatedLootCondition> {
        @Override
        public void serialize(@NotNull JsonObject json, @NotNull OxygenatedLootCondition instance, @NotNull JsonSerializationContext ctx) {
            json.addProperty("isReverse", instance.isReverse);
            json.addProperty("isOxygenated", instance.isOxygenated);
        }

        @Override
        public @NotNull OxygenatedLootCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx) {
            boolean isReverse = GsonHelper.getAsBoolean(json, "isReverse", false);
            boolean isOxygenated = GsonHelper.getAsBoolean(json, "isOxygenated", true);
            return new OxygenatedLootCondition(isReverse, isOxygenated);
        }
    }
}
