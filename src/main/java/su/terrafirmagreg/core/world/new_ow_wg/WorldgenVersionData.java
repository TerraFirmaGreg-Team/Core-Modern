package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tracks per-dimension initial worldgen versions and last-seen config override.
 */
public class WorldgenVersionData extends SavedData {

    public static final String DATA_ID = "tfg_worldgen_versions";

    /** Version number for the 1.21-backport overworld worldgen. */
    public static final int OVERWORLD_TFC_1_21_BACKPORT = 1;

    /** Effective overworld worldgen version for the current server session. Set on server start. */
    public static volatile int OVERWORLD_VERSION = 0;

    public static WorldgenVersionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                WorldgenVersionData::new,
                WorldgenVersionData::new,
                DATA_ID);
    }

    // version written at first generation
    public final Map<ResourceLocation, Integer> generatedVersions = new HashMap<>();
    // last-seen config override per dimension, absent if no override was set last load
    private final Map<ResourceLocation, Integer> knownConfigOverrides = new HashMap<>();

    public WorldgenVersionData() {
    }

    @SuppressWarnings("removal")
    public WorldgenVersionData(CompoundTag tag) {
        CompoundTag generated = tag.getCompound("generated");
        for (String key : generated.getAllKeys()) {
            generatedVersions.put(new ResourceLocation(key), generated.getInt(key));
        }
        CompoundTag overrides = tag.getCompound("config_overrides");
        for (String key : overrides.getAllKeys()) {
            knownConfigOverrides.put(new ResourceLocation(key), overrides.getInt(key));
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        CompoundTag generated = new CompoundTag();
        generatedVersions.forEach((k, v) -> generated.putInt(k.toString(), v));
        tag.put("generated", generated);

        CompoundTag overrides = new CompoundTag();
        knownConfigOverrides.forEach((k, v) -> overrides.putInt(k.toString(), v));
        tag.put("config_overrides", overrides);
        return tag;
    }

    public OptionalInt getGeneratedVersion(ResourceLocation dimension) {
        Integer v = generatedVersions.get(dimension);
        return v == null ? OptionalInt.empty() : OptionalInt.of(v);
    }

    public void setGeneratedVersion(ResourceLocation dimension, int version) {
        Integer existing = generatedVersions.get(dimension);
        if (existing != null) {
            if (existing != version)
                throw new IllegalStateException("Generated version for " + dimension + " already set to " + existing + ", cannot set to " + version);
            return;
        }
        generatedVersions.put(dimension, version);
        setDirty();
    }

    public OptionalInt getKnownConfigOverride(ResourceLocation dimension) {
        Integer v = knownConfigOverrides.get(dimension);
        return v == null ? OptionalInt.empty() : OptionalInt.of(v);
    }

    public Set<ResourceLocation> knownOverrideDimensions() {
        return knownConfigOverrides.keySet();
    }

    public void setKnownConfigOverride(ResourceLocation dimension, @Nullable Integer override) {
        if (override == null) {
            if (knownConfigOverrides.remove(dimension) != null)
                setDirty();
        } else {
            if (!override.equals(knownConfigOverrides.put(dimension, override)))
                setDirty();
        }
    }
}
