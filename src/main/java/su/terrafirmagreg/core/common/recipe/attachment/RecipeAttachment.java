package su.terrafirmagreg.core.common.recipe.attachment;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;

public interface RecipeAttachment {

    String getType();

    boolean matches(CookingApplianceBlockEntity appliance, Level level);

    boolean matchesInputs(CookingApplianceBlockEntity appliance);

    boolean canFitOutputs(CookingApplianceBlockEntity appliance);

    void assembleOutputs(CookingApplianceBlockEntity appliance);

    JsonObject toJson();

    void toNetwork(FriendlyByteBuf buffer);

    static RecipeAttachment fromJson(JsonObject json) {
        String type = json.has("type") ? json.get("type").getAsString() : "";
        if (CondenserAttachment.TYPE.equalsIgnoreCase(type)) {
            return CondenserAttachment.fromJson(json);
        }
        return null;
    }

    static RecipeAttachment fromNetwork(FriendlyByteBuf buffer) {
        String type = buffer.readUtf();
        if (CondenserAttachment.TYPE.equalsIgnoreCase(type)) {
            return CondenserAttachment.fromNetwork(buffer);
        }
        return null;
    }
}
