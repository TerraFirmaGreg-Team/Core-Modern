package su.terrafirmagreg.core.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import lombok.Getter;

public class WorldgenData extends SavedData {

    public static final String MOUNTAIN_SCALING_ID = "tfg_worldgen_mountain_scaling";

    @Nullable
    public static volatile MountainScaling MOUNTAIN_SCALING = null;

    public static final MountainScaling MOUNTAIN_SCALING_NONE = new MountainScaling(
            "none", 384, 384, 180, 1.0, 4, 4, 0.5,
            1.0, false, 0.0, 0.5, 0.5);
    public static final MountainScaling MOUNTAIN_SCALING_LOW = new MountainScaling(
            "low", 384, 384, 180, 1.0, 4, 6, 0.5,
            1.0, true, 1.0, 1.0, 0.5);
    public static final MountainScaling MOUNTAIN_SCALING_MEDIUM = new MountainScaling(
            "medium", 704, 704, 200, 1.0, 6, 12, 0.7,
            1.5, true, 3.0, 1.0, 1.0);
    public static final MountainScaling MOUNTAIN_SCALING_HIGH = new MountainScaling(
            "high", 1024, 1024, 230, 1.0, 8, 16, 0.85,
            2.0, true, 5.0, 1.0, 1.0);
    public static final MountainScaling MOUNTAIN_SCALING_NEW_WORLD_DEFAULT = MOUNTAIN_SCALING_LOW;

    public static final Map<String, MountainScaling> ALL_SCALINGS = new HashMap<>();
    public static final List<MountainScaling> CREATE_WORLD_SCALING_CYCLE_VALUES = new ArrayList<>();

    // Scaling written at first generation
    @Getter
    @Nullable
    private MountainScaling generatedMountainScaling = null;
    @Getter
    @Nullable
    private MountainScaling knownConfigOverrideMountainScaling = null;

    static {
        ALL_SCALINGS.put(MOUNTAIN_SCALING_NONE.id, MOUNTAIN_SCALING_NONE);
        ALL_SCALINGS.put(MOUNTAIN_SCALING_LOW.id, MOUNTAIN_SCALING_LOW);
        ALL_SCALINGS.put(MOUNTAIN_SCALING_MEDIUM.id, MOUNTAIN_SCALING_MEDIUM);
        ALL_SCALINGS.put(MOUNTAIN_SCALING_HIGH.id, MOUNTAIN_SCALING_HIGH);

        CREATE_WORLD_SCALING_CYCLE_VALUES.add(MOUNTAIN_SCALING_NONE);
        CREATE_WORLD_SCALING_CYCLE_VALUES.add(MOUNTAIN_SCALING_LOW);
        CREATE_WORLD_SCALING_CYCLE_VALUES.add(MOUNTAIN_SCALING_MEDIUM);
        CREATE_WORLD_SCALING_CYCLE_VALUES.add(MOUNTAIN_SCALING_HIGH);
    }

    public static WorldgenData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                WorldgenData::new,
                WorldgenData::new,
                MOUNTAIN_SCALING_ID);
    }

    public WorldgenData() {
    }

    public WorldgenData(CompoundTag tag) {
        generatedMountainScaling = ALL_SCALINGS.get(tag.getCompound("generated").getString(MOUNTAIN_SCALING_ID));
        knownConfigOverrideMountainScaling = ALL_SCALINGS.get(tag.getCompound("config_overrides").getString(MOUNTAIN_SCALING_ID));
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        if (generatedMountainScaling != null) {
            var generated = new CompoundTag();
            generated.putString(MOUNTAIN_SCALING_ID, generatedMountainScaling.id);
            tag.put("generated", generated);
        }

        if (knownConfigOverrideMountainScaling != null) {
            var overrides = new CompoundTag();
            overrides.putString(MOUNTAIN_SCALING_ID, knownConfigOverrideMountainScaling.id);
            tag.put("config_overrides", overrides);
        }

        return tag;
    }

    public void setGeneratedMountainScaling(MountainScaling scaling) {
        if (generatedMountainScaling != null) {
            if (generatedMountainScaling != scaling)
                throw new IllegalStateException("Generated mountain scaling already set to " + generatedMountainScaling + ", cannot set to " + scaling);
            return;
        }
        generatedMountainScaling = scaling;
        setDirty();
    }

    public void setKnowConfigMountainScaling(MountainScaling override) {
        knownConfigOverrideMountainScaling = override;
        setDirty();
    }

    public static MountainScaling createCycleButtonValue() {
        if (MOUNTAIN_SCALING != null)
            return MOUNTAIN_SCALING;
        else
            return MOUNTAIN_SCALING_NEW_WORLD_DEFAULT;
    }

    public static Component createCycleLabel(@NotNull MountainScaling scaling) {
        return Component.translatable("tfg.gui.mountain_scaling." + scaling.id);
    }

    public static Component createTooltipText(@Nullable MountainScaling scaling) {
        String id;
        if (scaling == null)
            id = MOUNTAIN_SCALING_NEW_WORLD_DEFAULT.id;
        else
            id = scaling.id;

        return Component.translatable("tfg.gui.mountain_scaling.tooltip." + id);
    }

    public record MountainScaling(
            String id,
            int dimHeight,
            int dimLogicalHeight,
            int snowLineY,
            double volcanoHeightScale,
            double foothillWidth,
            int heightBlend,
            double mountainHeightScale,
            double mountainHorizontalScale,
            boolean mountainErosionNoise,
            double mountainErosionNoiseStrength,
            double mountainPeakSharpness,
            double mountainRidgePeakIntensity) {
    }
}
