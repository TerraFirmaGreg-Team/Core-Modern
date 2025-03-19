package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CharcoalPileIgniterMachine;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import su.terrafirmagreg.core.common.data.TFGTags;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.BRONZE_HULL;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@Mixin(value = GTMultiMachines.class, remap = false)
public abstract class GTMachinesMixin {

    /**
     * Исправляет странные требования для мультиблока (земля -> земля тфк, дуб -> дерево тфк)
     * */
   @Shadow
   public static final MultiblockMachineDefinition CHARCOAL_PILE_IGNITER = REGISTRATE
           .multiblock("charcoal_pile_igniter", CharcoalPileIgniterMachine::new)
           .rotationState(RotationState.NONE)
           .recipeType(DUMMY_RECIPES)
           .appearanceBlock(BRONZE_HULL)
           .pattern((def) -> FactoryBlockPattern.start()
                   .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                   .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                   .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DSD ")
                   .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                   .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                   .where('S', controller(blocks(def.getBlock())))
                   .where('B', blocks(Blocks.BRICKS))
                   .where('X', blockTag(TFGTags.Blocks.GrassPlantableOn))
                   .where('D', blockTag(TFGTags.Blocks.GrassPlantableOn))
                   .where('C', blockTag(TFGTags.Blocks.Logs))
                   .build())
           .allowFlip(false)
           .allowExtendedFacing(false)
           .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                   GTCEu.id("block/multiblock/charcoal_pile_igniter"))
           .register();

}
