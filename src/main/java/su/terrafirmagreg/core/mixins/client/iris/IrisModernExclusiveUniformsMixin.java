package su.terrafirmagreg.core.mixins.client.iris;

import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.uniforms.IrisExclusiveUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg.OverworldClimateModelAccessor;

/**
 * Exposes more properties (aka uniforms) to shaders.
 * Largely copied from: https://github.com/EuphoriaPatches/EuphoriaPatcher/blob/multiloader/common/src/main/java/com/euphoriapatches/euphoria_patcher/integration/uniforms/IrisUniformBridge.java
 * Thank you SpaceEagle17 for the help!
 */

@OnlyIn(Dist.CLIENT)
@Mixin(value = IrisExclusiveUniforms.class, remap = false)
public class IrisModernExclusiveUniformsMixin {

    @Inject(method = "addIrisExclusiveUniforms(Lnet/irisshaders/iris/gl/uniform/UniformHolder;)V", at = @At("TAIL"), remap = false)
    private static void tfg$addIrisExclusiveUniforms(UniformHolder uniforms, CallbackInfo ci) {
        // SpaceEagle17 suggests always using per frame, and just caching in-between
        uniforms.uniform2f(UniformUpdateFrequency.PER_TICK, "windDirection", IrisModernExclusiveUniformsMixin::tfg$windDirection);
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "precipitation", IrisModernExclusiveUniformsMixin::tfg$precipitation);
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentTemperature", IrisModernExclusiveUniformsMixin::tfg$currentTemperature);
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "averageTemperature", IrisModernExclusiveUniformsMixin::tfg$averageTemperature);
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "worldSize", IrisModernExclusiveUniformsMixin::tfg$temperatureScale);
        uniforms.uniform1i(UniformUpdateFrequency.PER_TICK, "yearDays", IrisModernExclusiveUniformsMixin::tfg$yearDays);
        uniforms.uniform1i(UniformUpdateFrequency.PER_TICK, "currentDay", IrisModernExclusiveUniformsMixin::tfg$currentDay);
    }

    @Unique
    private static Vector2f tfg$windDirection() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            // Fallback
            return new Vector2f();
        }

        var level = player.clientLevel;
        var climate = Climate.model(level);
        var wind = climate.getWindVector(level, player.getOnPos(), Calendars.CLIENT.getTicks());
        return new Vector2f(wind.x, wind.y);
    }

    @Unique
    private static float tfg$precipitation() {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return 0f;

        var level = player.clientLevel;
        var climate = Climate.model(level);
        return climate.getRainfall(level, player.getOnPos());
    }

    @Unique
    private static float tfg$currentTemperature() {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return 0f;

        var level = player.clientLevel;
        var climate = Climate.model(level);
        return climate.getTemperature(level, player.getOnPos(), Calendars.CLIENT.getTicks(), Calendars.CLIENT.getCalendarDaysInMonth());
    }

    @Unique
    private static float tfg$averageTemperature() {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return 0f;

        var level = player.clientLevel;
        var climate = Climate.model(level);
        return climate.getAverageTemperature(level, player.getOnPos());
    }

    @Unique
    private static float tfg$temperatureScale() {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return 20_000f;

        var level = player.clientLevel;
        var dim = level.dimension();
        if (dim == Level.OVERWORLD) {
            var climate = Climate.model(level);
            return ((OverworldClimateModelAccessor) climate).tfg$temperatureScale();
        }
        // These need to match kubejs/startup_scripts/tfg/worldgen/register_climates.js
        else if (dim == Planet.MOON) {
            return 5_000f;
        } else if (dim == Planet.MARS) {
            return 10_000f;
        } else if (dim == Planet.VENUS) {
            return 19_000f;
        } else if (dim == Planet.GLACIO) {
            return 4_500f;
        }
        // Unknown dimension
        else {
            return 20_000f;
        }
    }

    @Unique
    private static int tfg$yearDays() {
        return Calendars.CLIENT.getCalendarDaysInMonth() * 12;
    }

    @Unique
    private static int tfg$currentDay() {
        return Math.toIntExact(Calendars.CLIENT.getTotalCalendarDays() % tfg$yearDays());
    }
}
