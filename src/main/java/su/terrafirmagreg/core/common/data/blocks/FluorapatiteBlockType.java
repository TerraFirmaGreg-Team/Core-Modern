package su.terrafirmagreg.core.common.data.blocks;

import java.util.Locale;

import net.minecraft.world.level.material.MapColor;

import lombok.Getter;

public enum FluorapatiteBlockType {
    WHITE(MapColor.QUARTZ),
    YELLOW(MapColor.COLOR_YELLOW),
    ORANGE(MapColor.COLOR_ORANGE),
    BROWN(MapColor.COLOR_BROWN),
    BLUE(MapColor.COLOR_LIGHT_BLUE),
    GREEN(MapColor.COLOR_LIGHT_GREEN);

    public static final FluorapatiteBlockType[] VALUES = values();

    public static FluorapatiteBlockType valueOf(int i) {
        return i >= 0 ? VALUES[i % VALUES.length] : WHITE;
    }

    @Getter
    private final MapColor mapColor;

    FluorapatiteBlockType(MapColor mapColor) {
        this.mapColor = mapColor;
    }

    public String nameLower() {
        return name().toLowerCase(Locale.ROOT);
    }
}
