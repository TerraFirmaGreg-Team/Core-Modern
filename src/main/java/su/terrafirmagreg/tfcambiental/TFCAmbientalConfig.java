package su.terrafirmagreg.tfcambiental;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class TFCAmbientalConfig {
    public static CommonImpl COMMON;
    public static ClientImpl CLIENT;

    public static void init() {
        COMMON = register(ModConfig.Type.COMMON, CommonImpl::new);
        CLIENT = register(ModConfig.Type.CLIENT, ClientImpl::new);
    }

    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getLeft();
    }

    public static class CommonImpl {
        public final ForgeConfigSpec.DoubleValue averageTemperature;
        public final ForgeConfigSpec.DoubleValue hotThreshold;
        public final ForgeConfigSpec.DoubleValue coolThreshold;
        public final ForgeConfigSpec.DoubleValue burnThreshold;
        public final ForgeConfigSpec.DoubleValue freezeThreshold;

        public final ForgeConfigSpec.DoubleValue wetnessChangeSpeed;
        public final ForgeConfigSpec.DoubleValue temperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue goodTemperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue badTemperatureChangeSpeed;

        public final ForgeConfigSpec.DoubleValue hotIngotTemperature;

        public final ForgeConfigSpec.IntValue indoorCheckTickModifier;

        CommonImpl(ForgeConfigSpec.Builder builder) {
            averageTemperature = builder
                    .comment("The average point for temperature, the not too warm and not too cool point")
                    .defineInRange("averageTemperature", 15F, 0F, 30F);

            hotThreshold = builder
                    .comment("The point where warmth starts to affect the screen, but only mildly")
                    .defineInRange("hotThreshold", 25F, 5F, 35F);

            coolThreshold = builder
                    .comment("The point where cold starts to affect the screen, but only mildly")
                    .defineInRange("coolThreshold", 5F, -15F, 25F);

            burnThreshold = builder
                    .comment("The point where warmth starts to hurt the player")
                    .defineInRange("burnThreshold", 30F, 15F, 45F);

            freezeThreshold = builder
                    .comment("The point where cold starts to hurt the player")
                    .defineInRange("freezeThreshold", 0F, -15F, 15F);

            wetnessChangeSpeed = builder
                    .comment("How quickly player wetness changes towards the target environment wetness")
                    .defineInRange("wetnessChangeSpeed", 1F, 0F, 50F);

            temperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature")
                    .defineInRange("temperatureChangeSpeed", 1F, 0F, 50F);

            goodTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's beneficial to do so")
                    .defineInRange("goodTemperatureChangeSpeed", 4F, 0F, 50F);

            badTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's not beneficial")
                    .defineInRange("badTemperatureChangeSpeed", 1F, 0F, 50F);

            hotIngotTemperature = builder
                    .comment("How much do items in the forge:hot_ingots tag modify the temperature of the player")
                    .defineInRange("hotIngotTemperature", 1F, 0F, Float.MAX_VALUE);

            indoorCheckTickModifier = builder
                    .comment("A modifier for the number of ticks between checking if a player is indoors. -1 to disable the check.")
                    .defineInRange("indoorCheckTickModifier", 20, -1, Integer.MAX_VALUE);
        }
    }

    public static class ClientImpl {
        public final ForgeConfigSpec.BooleanValue useFahrenheit;

        public final ForgeConfigSpec.DoubleValue guiOffset;
        public final ForgeConfigSpec.IntValue drippiness;

        ClientImpl(ForgeConfigSpec.Builder builder) {
            builder.comment("For all ARGB values, set to 00000000 to disable the feature in that season");

            useFahrenheit = builder
                    .comment("Change temperature display to Fahrenheit.")
                    .define("useFahrenheit", false);

            guiOffset = builder
                    .comment("Offset the GUI elements. Useful if you're using a resource pack that changes the health or hunger bars")
                    .defineInRange("guiOffset", 0f, 0, Float.MAX_VALUE);

            drippiness = builder
                    .comment("Percentage chance for a water particle to spawn each tick that the player is wet. 0 to turn off")
                    .defineInRange("drippiness", 30, 0, 100);
        }
    }
}
