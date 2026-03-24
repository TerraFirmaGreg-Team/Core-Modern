package su.terrafirmagreg.core.world;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class BedrockFluidSpoutLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(BedrockFluidSpoutLoader.class);

    public static final Map<String, ResourceLocation> VEIN_TO_FEATURE = new HashMap<>();
    public static final Map<String, String> VEIN_TO_TYPE = new HashMap<>();

    public static final BedrockFluidSpoutLoader INSTANCE = new BedrockFluidSpoutLoader();

    private BedrockFluidSpoutLoader() {
        super(GSON, "bedrock_fluid_spouts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
            ResourceManager manager,
            ProfilerFiller profiler) {

        VEIN_TO_FEATURE.clear();
        VEIN_TO_TYPE.clear();

        for (var fileEntry : objects.entrySet()) {
            if (!fileEntry.getValue().isJsonObject()) {
                LOGGER.warn("[TFG] bedrock_fluid_spouts: {} is not a JSON object, ignoring",
                        fileEntry.getKey());
                continue;
            }

            JsonObject root = fileEntry.getValue().getAsJsonObject();

            if (!root.has("entries")) {
                LOGGER.warn("[TFG] bedrock_fluid_spouts: {} missing 'entries' field, ignoring",
                        fileEntry.getKey());
                continue;
            }

            for (var entryElement : root.getAsJsonArray("entries")) {
                var entry = entryElement.getAsJsonObject();

                if (!entry.has("feature") || !entry.has("vein_ids")) {
                    LOGGER.warn("[TFG] bedrock_fluid_spouts: entry in {} missing 'feature' or 'vein_ids', ignoring",
                            fileEntry.getKey());
                    continue;
                }

                ResourceLocation featureId = ResourceLocation.tryParse(
                        entry.get("feature").getAsString());

                String type = entry.has("type") ? entry.get("type").getAsString() : "spout";

                for (var veinElement : entry.getAsJsonArray("vein_ids")) {
                    String veinId = veinElement.getAsString();
                    VEIN_TO_FEATURE.put(veinId, featureId);
                    VEIN_TO_TYPE.put(veinId, type);
                }
            }
        }

        LOGGER.info("[TFG] BedrockFluidSpoutLoader: {} features loaded", VEIN_TO_FEATURE.size());
    }
}
