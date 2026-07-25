package su.terrafirmagreg.tfc_textile;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.dries007.tfc.common.TFCCreativeTabs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import su.terrafirmagreg.tfc_textile.event.ClientEvent;
import su.terrafirmagreg.tfc_textile.item.TFCTextileItems;
import su.terrafirmagreg.tfc_textile.loot.ModLootModifiers;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TFCtextile.MODID)
public class TFCtextile {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "tfc_textile";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public TFCtextile() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        TFCTextileItems.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ClientEvent.init(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            //I really don't know java, this probably should have been done better.
            //event.accept(TFCTextileItems.COTTON_HAT);
            //event.accept(TFCTextileItems.COTTON_SHIRT);
            //event.accept(TFCTextileItems.COTTON_PANTS);
            //event.accept(TFCTextileItems.COTTON_SOCKS);

            event.accept(TFCTextileItems.CROCODILE_HAT);
            event.accept(TFCTextileItems.CROCODILE_SHIRT);
            event.accept(TFCTextileItems.CROCODILE_PANTS);
            event.accept(TFCTextileItems.CROCODILE_BOOTS);

            event.accept(TFCTextileItems.LINEN_HAT);
            event.accept(TFCTextileItems.LINEN_SHIRT);
            event.accept(TFCTextileItems.LINEN_PANTS);
            event.accept(TFCTextileItems.LINEN_SOCKS);

            event.accept(TFCTextileItems.RAW_HAT);
            event.accept(TFCTextileItems.RAW_SHIRT);
            event.accept(TFCTextileItems.RAW_PANTS);
            event.accept(TFCTextileItems.RAW_SOCKS);

            event.accept(TFCTextileItems.CARIBOU_HAT);
            event.accept(TFCTextileItems.CARIBOU_SHIRT);
            event.accept(TFCTextileItems.CARIBOU_PANTS);
            event.accept(TFCTextileItems.CARIBOU_SOCKS);

            event.accept(TFCTextileItems.POLAR_HAT);
            event.accept(TFCTextileItems.POLAR_SHIRT);
            event.accept(TFCTextileItems.POLAR_PANTS);
            event.accept(TFCTextileItems.POLAR_SOCKS);

            event.accept(TFCTextileItems.COUGAR_PANTS);
            event.accept(TFCTextileItems.COUGAR_SHIRT);
            event.accept(TFCTextileItems.COUGAR_SOCKS);
            event.accept(TFCTextileItems.COUGAR_HAT);

            event.accept(TFCTextileItems.TIGER_PANTS);
            event.accept(TFCTextileItems.TIGER_SHIRT);
            event.accept(TFCTextileItems.TIGER_SOCKS);
            event.accept(TFCTextileItems.TIGER_HAT);

            event.accept(TFCTextileItems.PANTHER_PANTS);
            event.accept(TFCTextileItems.PANTHER_SHIRT);
            event.accept(TFCTextileItems.PANTHER_SOCKS);
            event.accept(TFCTextileItems.PANTHER_HAT);

            event.accept(TFCTextileItems.SABERTOOTH_PANTS);
            event.accept(TFCTextileItems.SABERTOOTH_SHIRT);
            event.accept(TFCTextileItems.SABERTOOTH_SOCKS);
            event.accept(TFCTextileItems.SABERTOOTH_HAT);

            event.accept(TFCTextileItems.COUGAR_PANTS);
            event.accept(TFCTextileItems.COUGAR_SHIRT);
            event.accept(TFCTextileItems.COUGAR_SOCKS);
            event.accept(TFCTextileItems.COUGAR_HAT);

            event.accept(TFCTextileItems.BLACK_BEAR_PANTS);
            event.accept(TFCTextileItems.BLACK_BEAR_SHIRT);
            event.accept(TFCTextileItems.BLACK_BEAR_SOCKS);
            event.accept(TFCTextileItems.BLACK_BEAR_HAT);

            event.accept(TFCTextileItems.GRIZZLY_BEAR_PANTS);
            event.accept(TFCTextileItems.GRIZZLY_BEAR_SHIRT);
            event.accept(TFCTextileItems.GRIZZLY_BEAR_SOCKS);
            event.accept(TFCTextileItems.GRIZZLY_BEAR_HAT);

            event.accept(TFCTextileItems.DIREWOLF_PANTS);
            event.accept(TFCTextileItems.DIREWOLF_SHIRT);
            event.accept(TFCTextileItems.DIREWOLF_SOCKS);
            event.accept(TFCTextileItems.DIREWOLF_HAT);

            event.accept(TFCTextileItems.LION_PANTS);
            event.accept(TFCTextileItems.LION_SHIRT);
            event.accept(TFCTextileItems.LION_SOCKS);
            event.accept(TFCTextileItems.LION_HAT);

        }
        ;
        if (event.getTab() == TFCCreativeTabs.MISC.tab().get()) {
            event.accept(TFCTextileItems.PRIMITIVE_INSULATION);
            //event.accept(TFCTextileItems.COTTON_CLOTH);
            //event.accept(TFCTextileItems.LINEN_CLOTH);
            event.accept(TFCTextileItems.CARIBOU_FUR);
            event.accept(TFCTextileItems.COUGAR_FUR);
            event.accept(TFCTextileItems.BLACK_BEAR_FUR);
            event.accept(TFCTextileItems.COTTON_STRING);
            event.accept(TFCTextileItems.GRIZZLY_BEAR_FUR);
            event.accept(TFCTextileItems.TIGER_FUR);
            event.accept(TFCTextileItems.SABERTOOTH_FUR);
            event.accept(TFCTextileItems.POLAR_BEAR_FUR);
            event.accept(TFCTextileItems.PANTHER_FUR);
            event.accept(TFCTextileItems.LION_FUR);
            event.accept(TFCTextileItems.DIREWOLF_FUR);
            event.accept(TFCTextileItems.CROCODILE_LEATHER);
        }
        ;
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
