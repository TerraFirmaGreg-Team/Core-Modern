package su.terrafirmagreg.core.common.recipe.attachment;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;

@SuppressWarnings("unused")
public interface RecipeAttachment {

    String getType();

    boolean matches(CookingApplianceBlockEntity appliance, Level level);

    default boolean matchesInputs(CookingApplianceBlockEntity appliance) {
        return true;
    }

    default boolean isPresent(CookingApplianceBlockEntity appliance) {
        return true;
    }

    default CookingApplianceBlockEntity.CookingError getError(CookingApplianceBlockEntity appliance) {
        return CookingApplianceBlockEntity.CookingError.NONE;
    }

    default CookingApplianceBlockEntity.CookingError getErrorFlags(CookingApplianceBlockEntity appliance) {
        return getError(appliance);
    }

    default void tickActive(CookingApplianceBlockEntity appliance) {
    }

    default float getSpeedMultiplier(CookingApplianceBlockEntity appliance) {
        return 1.0f;
    }

    boolean canFitOutputs(CookingApplianceBlockEntity appliance);

    void assembleOutputs(CookingApplianceBlockEntity appliance);

    JsonObject toJson();

    void toNetwork(FriendlyByteBuf buffer);

    static RecipeAttachment fromJson(JsonObject json) {
        String type = json.has("type") ? json.get("type").getAsString() : "";
        if (CondenserAttachment.TYPE.equalsIgnoreCase(type)) {
            return CondenserAttachment.fromJson(json);
        } else if (MixerAttachment.TYPE.equalsIgnoreCase(type) || "mechanical_mixer".equalsIgnoreCase(type) || "greate_mixer".equalsIgnoreCase(type)) {
            return MixerAttachment.fromJson(json);
        } else if (PressAttachment.TYPE.equalsIgnoreCase(type) || "mechanical_press".equalsIgnoreCase(type) || "greate_press".equalsIgnoreCase(type)) {
            return PressAttachment.fromJson(json);
        } else if (VacuumChamberAttachment.TYPE.equalsIgnoreCase(type) || "vacuum".equalsIgnoreCase(type) || "condenser_vacuum".equalsIgnoreCase(type) || "vacuum_condenser".equalsIgnoreCase(type)) {
            return VacuumChamberAttachment.fromJson(json);
        }
        return null;
    }

    static RecipeAttachment fromNetwork(FriendlyByteBuf buffer) {
        String type = buffer.readUtf();
        if (CondenserAttachment.TYPE.equalsIgnoreCase(type)) {
            return CondenserAttachment.fromNetwork(buffer);
        } else if (MixerAttachment.TYPE.equalsIgnoreCase(type)) {
            return MixerAttachment.fromNetwork(buffer);
        } else if (PressAttachment.TYPE.equalsIgnoreCase(type)) {
            return PressAttachment.fromNetwork(buffer);
        } else if (VacuumChamberAttachment.TYPE.equalsIgnoreCase(type)) {
            return VacuumChamberAttachment.fromNetwork(buffer);
        }
        return null;
    }
}
