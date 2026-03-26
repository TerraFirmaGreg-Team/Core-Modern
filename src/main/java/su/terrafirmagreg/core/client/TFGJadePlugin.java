package su.terrafirmagreg.core.client;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.TierLockedBlock;

@WailaPlugin
public class TFGJadePlugin implements IWailaPlugin {

    public static final ResourceLocation TLB_Info = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "tier_locked_block_info");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TierLockedComponent.INSTANCE, TierLockedBlock.class);
    }
}
