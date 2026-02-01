package su.terrafirmagreg.core.common.data.tfgt.machine;

import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createTieredHullMachineModel;
import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

import java.util.Locale;
import java.util.function.BiFunction;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidType;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.machine.InterplanetaryLogisticsMonitorMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.electric.*;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunAmmoLoaderMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.SingleItemstackBus;

public class TFGMachines {

    public static void init() {
    }

    // Left here for future reference, but remember that steam machines can't handle fluids

    //	public static final MachineDefinition STEAM_AQUEOUS_ACCUMULATOR =
    //		registerSteamMachine("aqueous_accumulator",
    //			SimpleSteamMachine::new, (pressure, builder) -> builder
    //				.rotationState(RotationState.ALL)
    //				.recipeType(TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
    //				.recipeModifier(SimpleSteamMachine::recipeModifier)
    //				.renderer(() -> new WorkableSteamMachineRenderer(pressure, GTCEu.id("block/machines/aqueous_accumulator")))
    //				.register());

    public static final MachineDefinition BISMUTH_BRONZE_CRATE = GTMachineUtils.registerCrate(GTMaterials.BismuthBronze,
            54, "Bismuth Bronze Crate");
    public static final MachineDefinition BLACK_BRONZE_CRATE = GTMachineUtils.registerCrate(GTMaterials.BlackBronze, 54,
            "Black Bronze Crate");
    public static final MachineDefinition BISMUTH_BRONZE_DRUM = GTMachineUtils.registerDrum(GTMaterials.BismuthBronze,
            32000, "Bismuth Bronze Drum");
    public static final MachineDefinition BLACK_BRONZE_DRUM = GTMachineUtils.registerDrum(GTMaterials.BlackBronze,
            32000, "Black Bronze Drum");

    public static final MachineDefinition[] FOOD_PROCESSOR = registerTieredMachines(REGISTRATE, "food_processor",
            SimpleFoodProcessingMachine::new, (tier, builder) -> builder
                    .langValue("%s Food Processor %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(TFGTRecipeTypes.FOOD_PROCESSOR_RECIPES)
                    .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("food_processor"),
                            TFGTRecipeTypes.FOOD_PROCESSOR_RECIPES))
                    .workableTieredHullModel(GTCEu.id("block/machines/food_processor"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            TFGTRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier),
                            true))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] FOOD_OVEN = registerTieredMachines(REGISTRATE, "food_oven",
            SimpleFoodProcessingMachine::new, (tier, builder) -> builder
                    .langValue("%s Electric Oven %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(TFGTRecipeTypes.FOOD_OVEN_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/food_oven"))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("food_oven"),
                            TFGTRecipeTypes.FOOD_OVEN_RECIPES))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            TFGTRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier),
                            true))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] FOOD_REFRIGERATOR = registerTieredMachines(REGISTRATE, "food_refrigerator",
            FoodRefrigeratorMachine::new, (tier, builder) -> builder
                    .langValue("%s Refrigerator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64)),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    FoodRefrigeratorMachine.INVENTORY_SIZE(tier)))
                    .tooltips(Component.translatable("tfg.machine.food_refrigerator_power_usage",
                            FormattingUtil.formatNumbers(GTValues.VA[GTValues.LV] * (FoodRefrigeratorMachine.INVENTORY_SIZE(tier) / 9))))
                    .workableTieredHullModel(GTCEu.id("block/machines/food_refrigerator"))
                    .register(),
            GTValues.tiersBetween(GTValues.MV, GTValues.IV));

    public static final MachineDefinition[] AQUEOUS_ACCUMULATOR = registerTieredMachines(REGISTRATE, "aqueous_accumulator",
            AqueousAccumulatorMachine::new, (tier, builder) -> builder
                    .langValue("%s Aqueous Accumulator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("aqueous_accumulator"),
                            TFGTRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(TFGTRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/aqueous_accumulator"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            TFGTRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES,
                            GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
                    .tooltips(GTMachineUtils.explosion())
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] GAS_PRESSURIZER = registerTieredMachines(REGISTRATE, "gas_pressurizer",
            GasPressurizerMachine::new, (tier, builder) -> builder
                    .langValue("%s Gas Pressurizer %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(TFGTRecipeTypes.GAS_PRESSURIZER_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("gas_pressurizer"),
                            TFGTRecipeTypes.GAS_PRESSURIZER_RECIPES))
                    .workableTieredHullModel(GTCEu.id("block/machines/gas_pressurizer"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            TFGTRecipeTypes.GAS_PRESSURIZER_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier),
                            true))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] RAILGUN_ITEM_LOADER_IN = registerTieredMachines(REGISTRATE, "railgun_item_loader_in",
            (holder, tier) -> new RailgunItemBusMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(
                            "%s Interplanetary Railgun Loader %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.ALL)
                    .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/overlay_pipe_in_emissive"), null,
                            GTCEu.id("block/overlay/machine/" + OVERLAY_ITEM_HATCH))
                    .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .allowCoverOnFront(true)
                    .register(),
            GTMachineUtils.ALL_TIERS);

    public static final MachineDefinition[] RAILGUN_ITEM_LOADER_OUT = registerTieredMachines(REGISTRATE, "railgun_item_loader_out",
            (holder, tier) -> new RailgunItemBusMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(
                            "%s Interplanetary Railgun Unloader %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .rotationState(RotationState.ALL)
                    .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/overlay_pipe_out_emissive"), null,
                            GTCEu.id("block/overlay/machine/" + OVERLAY_ITEM_HATCH))
                    .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .allowCoverOnFront(true)
                    .register(),
            GTMachineUtils.ALL_TIERS);

    public static final MachineDefinition SINGLE_ITEMSTACK_BUS = REGISTRATE
            .machine("single_itemstack_bus", SingleItemstackBus::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                    Component.translatable("tfg.tooltip.single_itemstack_bus.0"),
                    Component.translatable("tfg.tooltip.single_itemstack_bus.1"))
            .register();

    public static final MachineDefinition RAILGUN_AMMO_LOADER = REGISTRATE
            .machine("railgun_ammo_loader", RailgunAmmoLoaderMachine::new).register();

    public static final MachineDefinition INTERPLANETARY_LOGISTICS_MONITOR = REGISTRATE
            .machine("interplanetary_logistics_monitor", InterplanetaryLogisticsMonitorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .register();

    // LV super chest is 1M, which is already crazy.
    // 10K is about as much as three steel crates holding items that stack to 64
    public static final MachineDefinition ULV_SUPER_CHEST = GTRegistration.REGISTRATE.machine("ulv_super_chest",
            (holder) -> new QuantumChestMachine(holder, GTValues.ULV, 10_000))
            .langValue("ULV Super Chest")
            .blockProp(BlockBehaviour.Properties::dynamicShape)
            .rotationState(RotationState.ALL)
            .allowExtendedFacing(true)
            .model(createTieredHullMachineModel(GTCEu.id("block/machine/template/quantum/quantum_chest"))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createQuantumChestRender)))
            .hasBER(true)
            .tooltipBuilder(GTMachineUtils.CHEST_TOOLTIPS)
            .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.item_storage_total",
                            FormattingUtil.formatNumbers(10_000)))
            .tier(GTValues.ULV)
            .register();

    // LV super tank is 4K, same as a bronze multiblock tank, so this being the same as the wood one feels appropriate
    public static final MachineDefinition ULV_SUPER_TANK = createULVTank();

    private static MachineDefinition createULVTank() {
        long maxAmount = 1000 * FluidType.BUCKET_VOLUME;
        var definition = GTRegistration.REGISTRATE.machine("ulv_super_tank",
                MachineDefinition::new, (holder) -> new QuantumTankMachine(holder, GTValues.ULV, maxAmount),
                MetaMachineBlock::new, QuantumTankMachineItem::new, MetaMachineBlockEntity::new)
                .langValue("ULV Super Tank")
                .blockProp(BlockBehaviour.Properties::dynamicShape)
                .rotationState(RotationState.ALL)
                .allowExtendedFacing(true)
                .model(createTieredHullMachineModel(GTCEu.id("block/machine/template/quantum/quantum_tank"))
                        .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createQuantumTankRender)))
                .hasBER(true)
                .tooltipBuilder(GTMachineUtils.TANK_TOOLTIPS)
                .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil.formatNumbers(maxAmount)))
                .tier(GTValues.ULV)
                .register();

        QuantumTankMachine.TANK_CAPACITY.put(definition, maxAmount);
        return definition;
    }

    public static final BlockEntry<Block> HERMETIC_CASING_ULV = GTRegistration.REGISTRATE.block("ulv_hermetic_casing", Block::new)
            .lang("Basic Hermetic Casing")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, end) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(GTModels.createHermeticCasingModel("ulv"))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();

    // Needed so you can get the full 2A of EV out of the large solar array mk1 while in HV.
    public static final MachineDefinition HV_ENERGY_OUTPUT_HATCH_4A = GTRegistration.REGISTRATE.machine("hv_energy_output_hatch_4a",
            (holder) -> new EnergyHatchPartMachine(holder, GTValues.HV, OUT, 4))
            .langValue(GTValues.VNF[GTValues.HV] + " 4A Dynamo Hatch")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.OUTPUT_ENERGY)
            .modelProperty(IS_FORMED, false)
            .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                    FormattingUtil.formatNumbers(GTValues.V[GTValues.HV]), GTValues.VNF[GTValues.HV]),
                    Component.translatable("gtceu.universal.tooltip.amperage_out", 4),
                    Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                            FormattingUtil
                                    .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(GTValues.HV, 4))),
                    Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
            .overlayTieredHullModel("energy_output_hatch_4a")
            .tier(GTValues.HV)
            .register();

    public static MachineDefinition[] registerTieredMachines(GTRegistrate registrate, String name,
            BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
            BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
            int... tiers) {

        MachineDefinition[] definitions = new MachineDefinition[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var register = registrate.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    holder -> factory.apply(holder, tier)).tier(tier);
            definitions[i] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition registerSteamMachine(String name,
            BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
            BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        return builder.apply(true,
                REGISTRATE.machine("hp_%s".formatted(name), holder -> factory.apply(holder, true))
                        .tier(1));
    }
}
