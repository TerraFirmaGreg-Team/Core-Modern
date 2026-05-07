package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum AsphaltRoadTopLayer implements StringRepresentable {
    HORIZONTAL("horizontal"),
    VERTICAL("vertical");

    private final String name;

    AsphaltRoadTopLayer(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
