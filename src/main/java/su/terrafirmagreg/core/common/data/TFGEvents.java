package su.terrafirmagreg.core.common.data;

import net.minecraftforge.common.MinecraftForge;

import su.terrafirmagreg.core.common.event.*;

public class TFGEvents {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new HarvesterEvent());
        MinecraftForge.EVENT_BUS.register(new WorldgenVersionEvents());
        MinecraftForge.EVENT_BUS.register(new FishingNetEvent());
    }
}
