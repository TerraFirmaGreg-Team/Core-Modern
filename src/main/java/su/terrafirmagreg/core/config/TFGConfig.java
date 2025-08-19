package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

/**
 * base of TFG config options. This is where each side-specific config is initialized and registered.
 * Use {@link ServerConfig}, {@link CommonConfig}, {@link ClientConfig} instead for side-specific configuration.
 * @see net.dries007.tfc.config.TFCConfig original inspiration for this config structure comes from TFC
 */
public final class TFGConfig {
    public static final ClientConfig CLIENT = register(ModConfig.Type.CLIENT, ClientConfig::new);
    public static final CommonConfig COMMON = register(ModConfig.Type.COMMON, CommonConfig::new);
    public static final ServerConfig SERVER = register(ModConfig.Type.SERVER, ServerConfig::new);

    public static void init() {}

    /**
     *  Use this function for listening to specific mod loading events. Do not use this to assign configuration values.
     * @param event please ensure that the correct event type is being checked.
     * @see net.minecraftforge.fml.event.config.ModConfigEvent.Loading
     * @see net.minecraftforge.fml.event.config.ModConfigEvent.Reloading
     * @see net.minecraftforge.fml.event.config.ModConfigEvent.Unloading
     */
    public static void onLoad(final ModConfigEvent event) {}


    @SuppressWarnings("removal")
    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getKey();
    }

}
