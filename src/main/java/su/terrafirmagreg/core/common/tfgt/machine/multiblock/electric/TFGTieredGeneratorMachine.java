package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.Map;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.BatteryDynamoHatchPartMachine;

/**
 * To upgrade a Large Boiler you need to have the same tier for Battery Dynamo Hatch and the Exhaust Casing
 */

public class TFGTieredGeneratorMachine extends WorkableElectricMultiblockMachine {

    private final Map<Integer, ? extends Supplier<? extends Block>> casings;

    @Getter
    private int casingTier = -1;

    public TFGTieredGeneratorMachine(BlockEntityCreationInfo info,
            Map<Integer, ? extends Supplier<? extends Block>> casings) {
        super(info);
        this.casings = casings;
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);

        int tier = -1;
        for (var entry : patternStates.get(substructureName).getCache().long2ObjectEntrySet()) {
            int blockTier = TFGPredicates.getCasingTier(casings, entry.getValue().getBlockState().getBlock());
            if (blockTier == -1)
                continue;
            if (tier == -1) {
                tier = blockTier;
            } else if (blockTier != tier) {
                failFormation(substructureName,
                        Component.translatable("tfg.multiblock.pattern.error.mixed_casing_tiers"));
                return;
            }
        }

        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof BatteryDynamoHatchPartMachine hatch && hatch.getTier() != tier) {
                failFormation(substructureName,
                        Component.translatable("tfg.multiblock.pattern.error.battery_dynamo_tier",
                                GTValues.VNF[tier]));
                return;
            }
        }

        casingTier = tier;
    }

    private void failFormation(String substructureName, Component error) {
        patternStates.get(substructureName).setError(new PatternStringError(error));
        invalidateStructure(substructureName);
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        casingTier = -1;
    }

}
