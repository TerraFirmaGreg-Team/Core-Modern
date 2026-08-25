package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.MEAssemblerRedstonePort;

public class MEAssemblerMachine extends WorkableElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEAssemblerMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final double BASE_CHANCE = 0.01;
    private static final double CHANCE_INCREMENT = 0.005;
    private static final double MAX_CHANCE = 1;

    private static final double[] BUDDING_SPEED_BONUS = { 0.0, 0.5, 2.0, 4.0, 8.0 };

    @Persisted
    @DescSynced
    @Getter
    private int buddingTier = 0;

    @Persisted
    private int recipesSinceDegrade = 0;

    @Nullable
    private BlockPos buddingPos = null;

    private final List<MEAssemblerRedstonePort> redstonePorts = new ArrayList<>();

    public MEAssemblerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.buddingTier = 0;
        this.buddingPos = null;
        redstonePorts.clear();
        var ctx = getMultiblockState().getMatchContext();
        if (ctx.get("BuddingTier") instanceof Integer tier) {
            this.buddingTier = tier;
        }
        if (ctx.get("BuddingPos") instanceof BlockPos pos) {
            this.buddingPos = pos.immutable();
        }
        for (IMultiPart part : getParts()) {
            if (part instanceof MEAssemblerRedstonePort port) {
                redstonePorts.add(port);
            }
        }
        updateRedstone();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (var port : redstonePorts)
            port.trySetSignal(0);
        redstonePorts.clear();
    }

    private void updateRedstone() {
        for (var port : redstonePorts) {
            port.trySetSignal(buddingTier);
        }
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        tryDegradeBudding();
    }

    private void tryDegradeBudding() {
        if (buddingTier <= 0 || buddingPos == null)
            return;
        if (getLevel() == null || getLevel().isClientSide)
            return;

        var last = getRecipeLogic().getLastRecipe();
        int executions = last != null ? last.getTotalRuns() : 1;
        recipesSinceDegrade += executions;

        double chance = Math.min(BASE_CHANCE + recipesSinceDegrade * CHANCE_INCREMENT, MAX_CHANCE);
        if (getLevel().getRandom().nextDouble() >= chance)
            return;

        Block next = TFGPredicates.getBuddingBlockForTier(buddingTier - 1);
        getLevel().setBlockAndUpdate(buddingPos, next.defaultBlockState());
        recipesSinceDegrade = 0;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed())
            return;

        ChatFormatting color = switch (buddingTier) {
            case 0 -> ChatFormatting.GRAY;
            case 1, 2 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.GREEN;
        };

        int speedBonus = (int) (BUDDING_SPEED_BONUS[buddingTier] * 100);

        textList.add(Component.translatable("tfg.machine.me_assembler.budding_tier",
                Component.literal("+" + speedBonus + "%").withStyle(color)));

    }

    public static @NotNull ModifierFunction buddingOverclock(@NotNull MetaMachine machine,
            @NotNull GTRecipe recipe) {

        if (!(machine instanceof MEAssemblerMachine meMachine)) {
            return RecipeModifier.nullWrongType(MEAssemblerMachine.class, machine);
        }

        int tier = meMachine.getBuddingTier();
        if (tier <= 0) {
            return OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK
                    .getModifier(machine, recipe, meMachine.getOverclockVoltage());
        }

        double factor = 1.0 / (1.0 + BUDDING_SPEED_BONUS[tier]);
        ModifierFunction baseModifier = r -> {
            var copy = r.copy();
            copy.duration = Math.max(1, (int) (copy.duration * factor));
            return copy;
        };

        GTRecipe reduced = baseModifier.apply(recipe);
        var oc = OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK
                .getModifier(machine, reduced, meMachine.getOverclockVoltage());

        return baseModifier.andThen(oc);
    }
}
