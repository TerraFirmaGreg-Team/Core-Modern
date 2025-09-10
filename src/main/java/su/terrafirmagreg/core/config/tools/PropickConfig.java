package su.terrafirmagreg.core.config.tools;

import net.dries007.tfc.util.Metal;
import net.minecraftforge.common.ForgeConfigSpec;

import su.terrafirmagreg.core.config.ConfigHelpers;

/**
 * Used to compactly configure prospector's pick settings in {@link su.terrafirmagreg.core.config.ServerConfig}.
 * 
 * @param searchLength Length of prospector pick's search area.
 * @param searchWidth  Half width (radius) of prospector pick's search area.
 */
public record PropickConfig(ForgeConfigSpec.IntValue searchLength, ForgeConfigSpec.IntValue searchWidth) {
    public static PropickConfig build(ForgeConfigSpec.Builder builder, Metal.Default metal, int searchLength,
            int searchWidth) {
        return new PropickConfig(
                builder.comment(String.format("\nLength of search area. Default = %s", searchLength))
                        .defineInRange(
                                String.format("%sOreProspectorLength",
                                        ConfigHelpers.toTitleCase(metal.getSerializedName(), true)),
                                searchLength, 0, 200),
                builder.comment(String.format("\nHalf the width of the search area. Default = %s", searchWidth),
                        "Example. If you want a 20x20 set the value to 10")
                        .defineInRange(
                                String.format("%sOreProspectorHalfWidth",
                                        ConfigHelpers.toTitleCase(metal.getSerializedName(), true)),
                                searchWidth, 0, 50));
    }
}
