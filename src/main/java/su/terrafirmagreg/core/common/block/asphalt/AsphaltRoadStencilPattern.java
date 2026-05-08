package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum AsphaltRoadStencilPattern implements StringRepresentable {
    LINE("line"),
    CROSS("cross");

    private final String name;

    AsphaltRoadStencilPattern(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
