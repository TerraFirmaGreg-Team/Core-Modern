package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import su.terrafirmagreg.core.TFGCore;

/**
 * Setup hooks for TFG GT addons.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class TFGTSetupHooks {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TFGTItems.COVER_ROTTEN_VOIDING.get().attachComponents(new CoverPlaceBehavior(TFGTCovers.ITEM_VOIDING_ROTTEN));
        });
    }
}
