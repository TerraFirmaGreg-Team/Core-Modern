package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

/**
 * base of TFG config options. Use {@link ServerConfig} and {@link CommonConfig} instead for side-specific configuration.
 * @see net.dries007.tfc.config.TFCConfig Original inspiration for this config structure
 */
public final class TFGConfig {
    public static final ClientConfig CLIENT = register(ModConfig.Type.CLIENT, ClientConfig::new);
    public static final CommonConfig COMMON = register(ModConfig.Type.COMMON, CommonConfig::new);
    public static final ServerConfig SERVER = register(ModConfig.Type.SERVER, ServerConfig::new);

    public static void init() {}

    @Deprecated
    public static boolean enableCreateCompat;

    @Deprecated
    public static boolean enableTFCAmbientalCompat;

    /**
     *
     * @param event please ensure that the correct {@link ModConfigEvent} type is being checked against.
     * @deprecated Replaced by {@link ServerConfig} and {@link CommonConfig} as of {@code 0.7.10}. This method remains to ensure backwards compatibility with the old configuration system. In all future cases, use the appropriate sided config file to add configuration settings. Usage of this file for storing config values may introduce instability and cause crashes on load.
     */
    @Deprecated
    public static void onLoad(final ModConfigEvent event) {
        enableCreateCompat = COMMON.ENABLE_CREATE_COMPAT.get();
        enableTFCAmbientalCompat = COMMON.ENABLE_TFC_AMBIENTAL_COMPAT.get();
    }


    @SuppressWarnings("removal")
    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getKey();
    }

}
