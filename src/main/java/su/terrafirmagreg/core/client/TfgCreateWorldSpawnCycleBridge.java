package su.terrafirmagreg.core.client;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.utils.CustomSpawnHelper;

/**
 * Client-only: holds the Game Tab spawn {@link CycleButton} and wraps {@link CustomSpawnHelper} text in {@link Tooltip}.
 * Sponge disallows non-private static helpers on mixin classes; label/tooltip strings live in {@link CustomSpawnHelper}.
 */
@OnlyIn(Dist.CLIENT)
public final class TfgCreateWorldSpawnCycleBridge {

    private static CycleButton<CustomSpawnHelper.CustomSpawnCondition> tfg$spawnCycleRef;

    private TfgCreateWorldSpawnCycleBridge() {
    }

    public static void register(CycleButton<CustomSpawnHelper.CustomSpawnCondition> button) {
        tfg$spawnCycleRef = button;
    }

    /** Sync widget from {@link su.terrafirmagreg.core.config.TFGConfig}; safe to call from {@link net.minecraft.client.Minecraft#execute}. */
    public static void syncFromConfig() {
        var b = tfg$spawnCycleRef;
        if (b != null) {
            CustomSpawnHelper.CustomSpawnCondition c = CustomSpawnHelper.getFromConfig();
            b.setValue(CustomSpawnHelper.createWorldSpawnCycleButtonValue());
            b.setTooltip(spawnTooltip(c));
        }
    }

    public static Tooltip spawnTooltip(CustomSpawnHelper.CustomSpawnCondition condition) {
        return Tooltip.create(CustomSpawnHelper.createWorldSpawnTooltipText(condition));
    }
}
