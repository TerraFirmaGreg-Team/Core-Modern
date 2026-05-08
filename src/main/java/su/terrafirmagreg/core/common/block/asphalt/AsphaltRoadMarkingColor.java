package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

public enum AsphaltRoadMarkingColor implements StringRepresentable {
    NONE("none", null),
    WHITE("white", DyeColor.WHITE),
    ORANGE("orange", DyeColor.ORANGE),
    MAGENTA("magenta", DyeColor.MAGENTA),
    LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE),
    YELLOW("yellow", DyeColor.YELLOW),
    LIME("lime", DyeColor.LIME),
    PINK("pink", DyeColor.PINK),
    GRAY("gray", DyeColor.GRAY),
    LIGHT_GRAY("light_gray", DyeColor.LIGHT_GRAY),
    CYAN("cyan", DyeColor.CYAN),
    PURPLE("purple", DyeColor.PURPLE),
    BLUE("blue", DyeColor.BLUE),
    BROWN("brown", DyeColor.BROWN),
    GREEN("green", DyeColor.GREEN),
    RED("red", DyeColor.RED),
    BLACK("black", DyeColor.BLACK);

    private final String name;
    private final DyeColor dye;

    AsphaltRoadMarkingColor(String name, DyeColor dye) {
        this.name = name;
        this.dye = dye;
    }

    public static AsphaltRoadMarkingColor fromSerializedName(String name) {
        for (AsphaltRoadMarkingColor color : values()) {
            if (color.name.equals(name)) {
                return color;
            }
        }
        return NONE;
    }

    public int getTextColor() {
        return dye == null ? -1 : dye.getTextColor();
    }

    public boolean isNone() {
        return this == NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
