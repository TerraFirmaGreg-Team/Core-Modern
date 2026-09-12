package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.DataPackConfig;

import su.terrafirmagreg.core.TFGCore;

/**
 * Repairs the datapack order stored in level.dat when it is scrambled. It happens at pack selection before anything is read from the packs
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerPackOrderMixin {

    private static final String TFG$VANILLA = "vanilla";
    private static final String TFG$MOD_PREFIX = "mod:";

    /**
     * A healthy list is vanilla>mods>packs mods generate at runtime.
     * Vanilla sitting anywhere but first means that the list is scrambled, so we leave every other world alone.
     */
    @ModifyVariable(method = "configurePackRepository", at = @At("HEAD"), argsOnly = true, index = 1)
    private static DataPackConfig tfg$repairScrambledPackOrder(DataPackConfig config) {
        final List<String> enabled = config.getEnabled();
        final int vanillaIndex = enabled.indexOf(TFG$VANILLA);

        // Absent is fine
        if (vanillaIndex <= 0) {
            return config;
        }

        final List<String> mods = new ArrayList<>();
        final List<String> generated = new ArrayList<>();
        for (String pack : enabled) {
            if (TFG$VANILLA.equals(pack)) {
                continue;
            }
            (pack.startsWith(TFG$MOD_PREFIX) ? mods : generated).add(pack);
        }

        final List<String> repaired = new ArrayList<>(enabled.size());
        repaired.add(TFG$VANILLA);
        repaired.addAll(mods);
        repaired.addAll(generated);

        TFGCore.LOGGER.warn(
                "Datapack order in this save was scrambled, vanilla was at position {} of {} and overrode mod data. Reordered it to vanilla>mods>generated packs.",
                vanillaIndex, enabled.size());

        return new DataPackConfig(repaired, config.getDisabled());
    }
}
