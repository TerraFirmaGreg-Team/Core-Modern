package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = RockSettings.class, remap = false)
public interface RockSettingsAccessor {

    @Accessor("PRESETS")
    static Map<ResourceLocation, RockSettings> getPresets() {
        throw new AssertionError();
    }
}
