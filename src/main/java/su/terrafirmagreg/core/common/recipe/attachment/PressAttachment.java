package su.terrafirmagreg.core.common.recipe.attachment;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import electrolyte.greate.content.kinetics.base.ICircuitHolder;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;

/**
 * Cooking appliance attachment for mechanical presses.
 */
public record PressAttachment(@Nullable Integer circuit) implements RecipeAttachment {

    public static final String TYPE = "press";

    public static MechanicalPressBlockEntity getPress(CookingApplianceBlockEntity appliance) {
        Level level = appliance.getLevel();
        if (level == null) {
            return null;
        }
        BlockPos abovePos = appliance.getBlockPos().above();
        if (!level.getBlockState(abovePos).isAir()) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(abovePos.above());
        if (be instanceof MechanicalPressBlockEntity press) {
            return press;
        }
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isPresent(CookingApplianceBlockEntity appliance) {
        return getPress(appliance) != null;
    }

    @Override
    public boolean matches(CookingApplianceBlockEntity appliance, Level level) {
        MechanicalPressBlockEntity press = getPress(appliance);
        if (press == null) {
            return false;
        }
        if (Math.abs(press.getSpeed()) == 0) {
            return false;
        }
        if (circuit != null) {
            return press instanceof ICircuitHolder circuitHolder && circuitHolder.getCircuitNumber() == circuit;
        }
        return true;
    }

    @Override
    public int getErrorFlags(CookingApplianceBlockEntity appliance) {
        if (!matches(appliance, appliance.getLevel())) {
            return CookingApplianceBlockEntity.ERROR_INSUFFICIENT_PRESS;
        }
        return 0;
    }

    @Override
    public void tickActive(CookingApplianceBlockEntity appliance) {
        MechanicalPressBlockEntity press = getPress(appliance);
        if (press != null) {
            var behaviour = press.getPressingBehaviour();
            if (!behaviour.running) {
                press.startProcessingBasin();
                press.sendData();
            }
            if (behaviour.runningTicks > 120) {
                behaviour.runningTicks = 121;
                behaviour.prevRunningTicks = 121;
            }
        }
    }

    @Override
    public float getSpeedMultiplier(CookingApplianceBlockEntity appliance) {
        MechanicalPressBlockEntity press = getPress(appliance);
        if (press != null) {
            float rpm = Math.abs(press.getSpeed());
            if (rpm > 0) {
                return (rpm / 16.0f) * 1.5f;
            }
        }
        return 1.0f;
    }

    @Override
    public boolean canFitOutputs(CookingApplianceBlockEntity appliance) {
        return true;
    }

    @Override
    public void assembleOutputs(CookingApplianceBlockEntity appliance) {
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE);
        if (circuit != null) {
            obj.addProperty("circuit", circuit);
        }
        return obj;
    }

    public static PressAttachment fromJson(JsonObject json) {
        Integer circuit = null;
        if (json.has("circuit") && !json.get("circuit").isJsonNull()) {
            circuit = json.get("circuit").getAsInt();
        }
        return new PressAttachment(circuit);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(circuit != null);
        if (circuit != null) {
            buffer.writeVarInt(circuit);
        }
    }

    public static PressAttachment fromNetwork(FriendlyByteBuf buffer) {
        Integer circuit = buffer.readBoolean() ? buffer.readVarInt() : null;
        return new PressAttachment(circuit);
    }
}
