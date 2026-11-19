package su.terrafirmagreg.core.compat.jade;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.integration.jade.GTJadePlugin;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.*;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.GasBurnerBlock;
import su.terrafirmagreg.core.common.block.TierLockedAcidBlock;
import su.terrafirmagreg.core.common.block.TierLockedBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmClusterBlock;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.GasWellMachine;

@WailaPlugin
public class TFGJadePlugin implements IWailaPlugin {

    public static final ResourceLocation TLB_Info = TFGCore.id("tier_locked_block_info");
    public static final ResourceLocation TLBA_Info = TFGCore.id("tier_locked_block_acid_info");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TierLockedProvider.INSTANCE, TierLockedBlock.class);
        registration.registerBlockComponent(TierLockedAcidProvider.INSTANCE, TierLockedAcidBlock.class);
        registration.registerBlockComponent(DynamicAgeProvider.INSTANCE, PalmClusterBlock.class);
        registration.registerBlockComponent(GasBurnerProvider.INSTANCE, GasBurnerBlock.class);
        GTJadePlugin.register(registration, new BatteryDynamoHatchInfoProvider());
        registration.registerBlockComponent(GasWellProvider.INSTANCE, MetaMachineBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(GasWellProvider.INSTANCE, GasWellMachine.class);
        GTJadePlugin.register(registration, new BatteryDynamoHatchInfoProvider());
    }
}
