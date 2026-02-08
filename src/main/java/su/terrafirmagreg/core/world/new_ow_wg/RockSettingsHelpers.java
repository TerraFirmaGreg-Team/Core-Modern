package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.HashMap;
import java.util.Map;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.settings.RockSettings;

import su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg.RockSettingsAccessor;

public final class RockSettingsHelpers {

    private static Map<RockSettings, Boolean> isKarstMap = new HashMap<>();
    private static Map<RockSettings, Boolean> isMaficMap = new HashMap<>();

    static {
        var presets = RockSettingsAccessor.getPresets();

        isKarstMap.put(presets.get(Helpers.identifier("limestone")), true);
        isKarstMap.put(presets.get(Helpers.identifier("dolomite")), true);
        isKarstMap.put(presets.get(Helpers.identifier("chalk")), true);
        isKarstMap.put(presets.get(Helpers.identifier("marble")), true);

        isMaficMap.put(presets.get(Helpers.identifier("gabbro")), false);
        isMaficMap.put(presets.get(Helpers.identifier("basalt")), false);
    }

    public static boolean isKarst(RockSettings rock) {
        return isKarstMap.getOrDefault(rock, false);
    }

    public static boolean isMafic(RockSettings rock) {
        return isMaficMap.getOrDefault(rock, false);
    }
}
