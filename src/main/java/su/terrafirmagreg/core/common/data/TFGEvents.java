package su.terrafirmagreg.core.common.data;

import net.minecraftforge.common.MinecraftForge;

import su.terrafirmagreg.core.common.data.events.DnaSyringeEvent;
import su.terrafirmagreg.core.common.data.events.FishingNetEvent;
import su.terrafirmagreg.core.common.data.events.HarvesterEvent;
import su.terrafirmagreg.core.common.data.events.OreProspectorEvent;

public class TFGEvents {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new HarvesterEvent());
        MinecraftForge.EVENT_BUS.register(new OreProspectorEvent());
        MinecraftForge.EVENT_BUS.register(new DnaSyringeEvent());
        MinecraftForge.EVENT_BUS.register(new FishingNetEvent());
    }
}
