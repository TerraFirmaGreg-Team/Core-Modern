package su.terrafirmagreg.core.compat.jade;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.*;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.TierLockedBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmClusterBlock;

@WailaPlugin
public class TFGJadePlugin implements IWailaPlugin {

    public static final ResourceLocation TLB_Info = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "tier_locked_block_info");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TierLockedProvider.INSTANCE, TierLockedBlock.class);
        registration.registerBlockComponent(DynamicAgeProvider.INSTANCE, PalmClusterBlock.class);
    }
}
