package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;


public final class TFGConfig {
    public static final CommonConfig COMMON = register(ModConfig.Type.COMMON, CommonConfig::new);
    public static final ServerConfig SERVER = register(ModConfig.Type.SERVER, ServerConfig::new);

    public static void init() {}

    public static boolean enableCreateCompat;
    public static boolean enableTFCAmbientalCompat;

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
