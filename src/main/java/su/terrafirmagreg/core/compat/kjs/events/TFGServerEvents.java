package su.terrafirmagreg.core.compat.kjs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface TFGServerEvents {
    EventGroup GROUP = EventGroup.of("TFGServerEvents");

    EventHandler AE2_POWER_CONSUMPTION = GROUP.server("dimensionalPowerConsumption", () -> TFGAE2PowerConsumption.class);
}
