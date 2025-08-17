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

    @Deprecated
    public static boolean enableCreateCompat;

    @Deprecated
    public static boolean enableTFCAmbientalCompat;

//    public static int CopperOreProspectorLength;
//    public static int CopperOreProspectorHalfWidth;
//    public static int BronzeOreProspectorLength;
//    public static int BronzeOreProspectorHalfWidth;
//    public static int WroughtIronOreProspectorLength;
//    public static int WroughtIronOreProspectorHalfWidth;
//    public static int SteelOreProspectorLength;
//    public static int SteelOreProspectorHalfWidth;
//    public static int BlackSteelOreProspectorLength;
//    public static int BlackSteelOreProspectorHalfWidth;
//    public static int BlueSteelOreProspectorLength;
//    public static int BlueSteelOreProspectorHalfWidth;
//    public static boolean BlueSteelOreProspectorRender;
//    public static int RedSteelOreProspectorLength;
//    public static int RedSteelOreProspectorHalfWidth;
//    public static boolean RedSteelOreProspectorRender;
//    public static int PreciseOreProspectorParticleChance;
//    public static int HarvestBasketRange;

    /**
     *
     * @param event please ensure that the correct {@link ModConfigEvent} type is being checked against.
     * @deprecated Replaced by {@link ServerConfig} and {@link CommonConfig} as of {@code 0.7.10}. This method remains to ensure backwards compatibility with the old configuration system. In all future cases, use the appropriate sided config file to add configuration settings. Usage of this file for may introduce instability and cause crashes on load.
     */
    @Deprecated
    public static void onLoad(final ModConfigEvent event) {
        enableCreateCompat = COMMON.ENABLE_CREATE_COMPAT.get();
        enableTFCAmbientalCompat = COMMON.ENABLE_TFC_AMBIENTAL_COMPAT.get();

////        CopperOreProspectorLength = SERVER.COPPER_ORE_PROSPECTOR_LENGTH.get();
////        CopperOreProspectorHalfWidth = SERVER.COPPER_ORE_PROSPECTOR_WIDTH.get();
//
////        BronzeOreProspectorLength = SERVER.BRONZE_ORE_PROSPECTOR_LENGTH.get();
////        BronzeOreProspectorHalfWidth = SERVER.BRONZE_ORE_PROSPECTOR_WIDTH.get();
//
////        WroughtIronOreProspectorLength = SERVER.WROUGHT_IRON_ORE_PROSPECTOR_LENGTH.get();
////        WroughtIronOreProspectorHalfWidth = SERVER.WROUGHT_IRON_ORE_PROSPECTOR_LENGTH.get();
//
////        SteelOreProspectorLength = SERVER.STEEL_ORE_PROSPECTOR_LENGTH.get();
////        SteelOreProspectorHalfWidth = SERVER.STEEL_ORE_PROSPECTOR_WIDTH.get();
//
////        BlackSteelOreProspectorLength = SERVER.BLACK_STEEL_ORE_PROSPECTOR_LENGTH.get();
////        BlackSteelOreProspectorHalfWidth = SERVER.BLACK_STEEL_ORE_PROSPECTOR_WIDTH.get();
//
////        BlueSteelOreProspectorLength = SERVER.BLUE_STEEL_ORE_PROSPECTOR_LENGTH.get();
////        BlueSteelOreProspectorHalfWidth = SERVER.BLUE_STEEL_ORE_PROSPECTOR_WIDTH.get();
//
//        BlueSteelOreProspectorRender = SERVER.BLUE_STEEL_ORE_PROSPECTOR_RENDER.get();
//        RedSteelOreProspectorLength = SERVER.RED_STEEL_ORE_PROSPECTOR_LENGTH.get();
//
//        RedSteelOreProspectorHalfWidth = SERVER.RED_STEEL_ORE_PROSPECTOR_WIDTH.get();
//        RedSteelOreProspectorRender = SERVER.RED_STEEL_ORE_PROSPECTOR_RENDER.get();
//
//        PreciseOreProspectorParticleChance = COMMON.PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE.get();
//        HarvestBasketRange = SERVER.HARVEST_BASKET_RANGE.get();
    }


    @SuppressWarnings("removal")
    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getKey();
    }

}
