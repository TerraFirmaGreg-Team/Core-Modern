package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum AsphaltRoadDecal implements StringRepresentable {
    NONE("none"),
    LINE_HORIZONTAL("line_horizontal"),
    LINE_VERTICAL("line_vertical"),
    CROSS("cross"),
    ARROW_NORTH("arrow_north"),
    ARROW_EAST("arrow_east"),
    ARROW_SOUTH("arrow_south"),
    ARROW_WEST("arrow_west");

    private final String name;

    AsphaltRoadDecal(String name) {
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
