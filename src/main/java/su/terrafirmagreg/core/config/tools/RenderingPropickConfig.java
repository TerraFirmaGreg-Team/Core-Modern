package su.terrafirmagreg.core.config.tools;

import net.dries007.tfc.util.Metal;
import net.minecraftforge.common.ForgeConfigSpec;

import su.terrafirmagreg.core.config.ConfigHelpers;

/**
 * Used to compactly configure tier VI prospector's pick settings in {@link su.terrafirmagreg.core.config.ServerConfig}.
 * 
 * @param inner               base prospector settings.
 * @param preciselyRenderVein if true, ores in an ore vein will each be represented by a particle.
 * @see PropickConfig
 */
public record RenderingPropickConfig(PropickConfig inner, ForgeConfigSpec.BooleanValue preciselyRenderVein) {
    public static RenderingPropickConfig build(ForgeConfigSpec.Builder builder, Metal.Default metal, int searchLength,
            int searchWidth, boolean preciselyRenderVein) {
        return new RenderingPropickConfig(
                PropickConfig.build(builder, metal, searchLength, searchWidth),
                builder.comment(String.format(
                        "\nShould the %s Prospector's Pick render particles per vein (vague)?\nSetting false will render particles per block (precise). Default: %s",
                        ConfigHelpers.toTitleCase(metal.getSerializedName()), preciselyRenderVein))
                        .define(String.format("%sProspectorRender",
                                ConfigHelpers.toTitleCase(metal.getSerializedName(), false)), preciselyRenderVein));
    }
}
