package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum AsphaltRoadMarkingMask implements StringRepresentable {
    NONE("none"),
    ARROW("arrow"),
    LINE("line", 2),
    DOUBLE_LINE("double_line", 2),
    LINE_SLASH_L("line_slash_l"),
    LINE_SLASH_R("line_slash_r"),
    LINE_SLASH_RL("line_slash_rl"),
    SLASH_L("slash_l"),
    SLASH_R("slash_r"),
    SLASH_RL("slash_rl"),
    FILL("fill", 0),
    CROSS("cross", 0),
    T("t"),
    CORNER("corner"),
    NUM_0("num_0"),
    NUM_1("num_1"),
    NUM_2("num_2"),
    NUM_3("num_3"),
    NUM_4("num_4"),
    NUM_5("num_5"),
    NUM_6("num_6"),
    NUM_7("num_7"),
    NUM_8("num_8"),
    NUM_9("num_9"),
    NUMBER("number");

    private final String name;
    private final int dirs;

    AsphaltRoadMarkingMask(String name) {
        this.name = name;
        this.dirs = 4;
    }

    AsphaltRoadMarkingMask(String name, int dirs) {
        this.name = name;
        this.dirs = dirs;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public static AsphaltRoadMarkingMask fromSerializedName(String name) {
        for (AsphaltRoadMarkingMask mask : values()) {
            if (mask.name.equals(name)) {
                return mask;
            }
        }
        throw new IllegalArgumentException("Unknown asphalt road marking mask: " + name);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public int getDirs() {
        return dirs;
    }
}
