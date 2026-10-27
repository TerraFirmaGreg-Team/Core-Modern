package su.terrafirmagreg.core.common.recipe.attachment;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import electrolyte.greate.content.kinetics.base.ICircuitHolder;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;

/**
 * Cooking appliance attachment for mixers.
 */
public record MixerAttachment(@Nullable Float minRpm, @Nullable Integer circuit) implements RecipeAttachment {

    public static final String TYPE = "mixer";

    public static MechanicalMixerBlockEntity getMixer(CookingApplianceBlockEntity appliance) {
        Level level = appliance.getLevel();
        if (level == null) {
            return null;
        }
        BlockPos abovePos = appliance.getBlockPos().above();
        if (!level.getBlockState(abovePos).isAir()) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(abovePos.above());
        if (be instanceof MechanicalMixerBlockEntity mixer) {
            return mixer;
        }
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isPresent(CookingApplianceBlockEntity appliance) {
        return getMixer(appliance) != null;
    }

    @Override
    public boolean matches(CookingApplianceBlockEntity appliance, Level level) {
        MechanicalMixerBlockEntity mixer = getMixer(appliance);
        if (mixer == null) {
            return false;
        }
        if (Math.abs(mixer.getSpeed()) == 0) {
            return false;
        }
        if (minRpm != null && Math.abs(mixer.getSpeed()) < minRpm) {
            return false;
        }
        if (circuit != null) {
            return mixer instanceof ICircuitHolder circuitHolder && circuitHolder.getCircuitNumber() == circuit;
        }
        return true;
    }

    @Override
    public CookingApplianceBlockEntity.CookingError getError(CookingApplianceBlockEntity appliance) {
        if (!matches(appliance, appliance.getLevel())) {
            return CookingApplianceBlockEntity.CookingError.INSUFFICIENT_MIXER;
        }
        return CookingApplianceBlockEntity.CookingError.NONE;
    }

    @Override
    public void tickActive(CookingApplianceBlockEntity appliance) {
        MechanicalMixerBlockEntity mixer = getMixer(appliance);
        if (mixer != null) {
            if (!mixer.running) {
                mixer.startProcessingBasin();
                mixer.sendData();
            }
            if (mixer.runningTicks >= 20) {
                mixer.runningTicks = 20;
                mixer.processingTicks = 20;
            }
        }
    }

    @Override
    public float getSpeedMultiplier(CookingApplianceBlockEntity appliance) {
        MechanicalMixerBlockEntity mixer = getMixer(appliance);
        if (mixer != null) {
            float rpm = Math.abs(mixer.getSpeed());
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
        if (minRpm != null) {
            obj.addProperty("min_rpm", minRpm);
        }
        if (circuit != null) {
            obj.addProperty("circuit", circuit);
        }
        return obj;
    }

    public static MixerAttachment fromJson(JsonObject json) {
        Float minRpm = null;
        if (json.has("min_rpm") && !json.get("min_rpm").isJsonNull()) {
            minRpm = json.get("min_rpm").getAsFloat();
        } else if (json.has("rpm") && !json.get("rpm").isJsonNull()) {
            minRpm = json.get("rpm").getAsFloat();
        }

        Integer circuit = null;
        if (json.has("circuit") && !json.get("circuit").isJsonNull()) {
            circuit = json.get("circuit").getAsInt();
        }
        return new MixerAttachment(minRpm, circuit);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(minRpm != null);
        if (minRpm != null) {
            buffer.writeFloat(minRpm);
        }
        buffer.writeBoolean(circuit != null);
        if (circuit != null) {
            buffer.writeVarInt(circuit);
        }
    }

    public static MixerAttachment fromNetwork(FriendlyByteBuf buffer) {
        Float minRpm = buffer.readBoolean() ? buffer.readFloat() : null;
        Integer circuit = buffer.readBoolean() ? buffer.readVarInt() : null;
        return new MixerAttachment(minRpm, circuit);
    }
}
