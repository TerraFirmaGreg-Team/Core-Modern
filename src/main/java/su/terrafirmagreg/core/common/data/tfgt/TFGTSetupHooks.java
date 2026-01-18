package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Setup hooks for TFG GT addons.
 */
public final class TFGTSetupHooks {

    private TFGTSetupHooks() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(TFGTSetupHooks::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TFGTItems.COVER_ROTTEN_VOIDING.get().attachComponents(new CoverPlaceBehavior(TFGTCovers.ITEM_VOIDING_ROTTEN));
        });
    }
}
