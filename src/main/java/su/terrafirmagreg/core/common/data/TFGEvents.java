package su.terrafirmagreg.core.common.data;

import net.minecraftforge.common.MinecraftForge;

import su.terrafirmagreg.core.common.event.HarvesterEvent;
import su.terrafirmagreg.core.common.event.WorldgenVersionEvents;

public class TFGEvents {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new HarvesterEvent());
        MinecraftForge.EVENT_BUS.register(new WorldgenVersionEvents());
    }
}
