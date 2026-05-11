package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum AsphaltRoadMarkingMask implements StringRepresentable {
    NONE("none"),
    LINE("line"),
    CROSS("cross"),
    ARROW("arrow");

    private final String name;

    AsphaltRoadMarkingMask(String name) {
        this.name = name;
    }

    public boolean isNone() {
        return this == NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
