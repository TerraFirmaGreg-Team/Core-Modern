package su.terrafirmagreg.core.compat.create.display_source;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;

public class TFGDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES = DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, TFGCore.MOD_ID);

    public static final RegistryObject<ThermometerDisplaySource> THERMOMETER = TFGDisplaySources.DISPLAY_SOURCES.register("thermometer", ThermometerDisplaySource::new);
    public static final RegistryObject<CalendarClockDisplaySource> CALENDAR_CLOCK = TFGDisplaySources.DISPLAY_SOURCES.register("calendar_clock", CalendarClockDisplaySource::new);

    public static void init(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
        modEventBus.addListener(TFGDisplaySources::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DisplaySource.BY_BLOCK.add(TFCBlocks.THERMOMETER.get(), THERMOMETER.get());
            DisplaySource.BY_BLOCK.add(TFCBlocks.CALENDAR_CLOCK.get(), CALENDAR_CLOCK.get());
        });
    }
}
