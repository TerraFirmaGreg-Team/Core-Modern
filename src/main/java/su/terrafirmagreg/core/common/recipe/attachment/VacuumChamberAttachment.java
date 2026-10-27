package su.terrafirmagreg.core.common.recipe.attachment;

import com.google.gson.JsonObject;
import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.mixins.common.vintageimprovements.VacuumChamberBlockEntityAccessor;

/**
 * Cooking appliance attachment for vacuum chambers.
 */
public record VacuumChamberAttachment(boolean mode) implements RecipeAttachment {

    public static final String TYPE = "vacuum_chamber";

    public static VacuumChamberBlockEntity getChamber(CookingApplianceBlockEntity appliance) {
        Level level = appliance.getLevel();
        if (level == null) {
            return null;
        }
        BlockPos abovePos = appliance.getBlockPos().above();
        if (!level.getBlockState(abovePos).isAir()) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(abovePos.above());
        if (be != null && !level.getBlockState(be.getBlockPos().above()).isAir()) {
            return null;
        }
        if (be instanceof VacuumChamberBlockEntity chamber) {
            return chamber;
        }
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isPresent(CookingApplianceBlockEntity appliance) {
        return getChamber(appliance) != null;
    }

    @Override
    public boolean matches(CookingApplianceBlockEntity appliance, Level level) {
        VacuumChamberBlockEntity chamber = getChamber(appliance);
        if (chamber == null) {
            return false;
        }
        if (Math.abs(chamber.getSpeed()) < 30) {
            return false;
        }
        boolean chamberMode = ((VacuumChamberBlockEntityAccessor) chamber).tfg$getMode();
        return chamberMode == this.mode;
    }

    @Override
    public CookingApplianceBlockEntity.CookingError getError(CookingApplianceBlockEntity appliance) {
        if (!matches(appliance, appliance.getLevel())) {
            return CookingApplianceBlockEntity.CookingError.INSUFFICIENT_VACUUM;
        }
        return CookingApplianceBlockEntity.CookingError.NONE;
    }

    @Override
    public void tickActive(CookingApplianceBlockEntity appliance) {
        VacuumChamberBlockEntity chamber = getChamber(appliance);
        if (chamber != null) {
            if (!chamber.running) {
                chamber.startProcessingBasin();
                chamber.sendData();
            }
            if (chamber.runningTicks >= 20) {
                chamber.runningTicks = 20;
                chamber.processingTicks = 20;
            }
        }
    }

    @Override
    public float getSpeedMultiplier(CookingApplianceBlockEntity appliance) {
        VacuumChamberBlockEntity chamber = getChamber(appliance);
        if (chamber != null) {
            float rpm = Math.abs(chamber.getSpeed());
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
        obj.addProperty("mode", mode);
        return obj;
    }

    public static VacuumChamberAttachment fromJson(JsonObject json) {
        boolean mode = false;
        if (json.has("mode")) {
            if (json.get("mode").isJsonPrimitive() && json.get("mode").getAsJsonPrimitive().isBoolean()) {
                mode = json.get("mode").getAsBoolean();
            } else if (json.get("mode").isJsonPrimitive() && json.get("mode").getAsJsonPrimitive().isString()) {
                String s = json.get("mode").getAsString();
                mode = "pressure".equalsIgnoreCase(s) || "pressurizing".equalsIgnoreCase(s);
            }
        } else if (json.has("pressure")) {
            mode = json.get("pressure").getAsBoolean();
        } else if (json.has("vacuum")) {
            mode = !json.get("vacuum").getAsBoolean();
        }
        return new VacuumChamberAttachment(mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(mode);
    }

    public static VacuumChamberAttachment fromNetwork(FriendlyByteBuf buffer) {
        boolean mode = buffer.readBoolean();
        return new VacuumChamberAttachment(mode);
    }
}
