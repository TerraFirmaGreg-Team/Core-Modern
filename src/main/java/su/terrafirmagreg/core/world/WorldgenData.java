package su.terrafirmagreg.core.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import lombok.Getter;

public class WorldgenData extends SavedData {

    public static final String MOUNTAIN_SCALING_ID = "tfg_worldgen_mountain_scaling";

    public static volatile int MOUNTAIN_SCALING = 0;

    public static final int MOUNTAIN_SCALING_UNSET = -1;
    public static final int MOUNTAIN_SCALING_TFC_DEFAULTS = 1;
    public static final int MOUNTAIN_SCALING_LOW = 2;
    public static final int MOUNTAIN_SCALING_MEDIUM = 3;
    public static final int MOUNTAIN_SCALING_HIGH = 4;

    public static final int MOUNTAIN_SCALING_NEW_WORLD_DEFAULT = MOUNTAIN_SCALING_MEDIUM;

    // Scaling written at first generation
    @Getter
    private int generatedMountainScaling = MOUNTAIN_SCALING_UNSET;
    @Getter
    private int knownConfigOverrideMountainScaling = MOUNTAIN_SCALING_UNSET;

    public static WorldgenData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                WorldgenData::new,
                WorldgenData::new,
                MOUNTAIN_SCALING_ID);
    }

    public WorldgenData() {
    }

    public WorldgenData(CompoundTag tag) {
        generatedMountainScaling = tag.getCompound("generated").getInt(MOUNTAIN_SCALING_ID);
        knownConfigOverrideMountainScaling = tag.getCompound("config_overrides").getInt(MOUNTAIN_SCALING_ID);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        var generated = new CompoundTag();
        generated.putInt(MOUNTAIN_SCALING_ID, generatedMountainScaling);
        tag.put("generated", generated);

        var overrides = new CompoundTag();
        overrides.putInt(MOUNTAIN_SCALING_ID, knownConfigOverrideMountainScaling);
        tag.put("config_overrides", overrides);

        return tag;
    }

    public void setGeneratedMountainScaling(int scaling) {
        if (generatedMountainScaling != MOUNTAIN_SCALING_UNSET) {
            if (generatedMountainScaling != scaling)
                throw new IllegalStateException("Generated mountain scaling already set to " + generatedMountainScaling + ", cannot set to " + scaling);
            return;
        }
        generatedMountainScaling = scaling;
        setDirty();
    }

    public void setKnowConfigMountainScaling(int override) {
        knownConfigOverrideMountainScaling = override;
        setDirty();
    }
}
