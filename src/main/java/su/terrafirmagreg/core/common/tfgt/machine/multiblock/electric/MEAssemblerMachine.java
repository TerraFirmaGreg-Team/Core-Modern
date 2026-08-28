package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
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

    private static final int HEALTH_MIN = 100;
    private static final int HEALTH_MAX = 500;

    private static final double[] BUDDING_SPEED_BONUS = { 0.0, 1.0, 4.0, 8.0, 16.0 };

    @Persisted
    @DescSynced
    @Getter
    private int buddingTier = 0;

    @Persisted
    @DescSynced
    @Getter
    private int buddingHealth = 0;

    @Nullable
    private BlockPos buddingPos = null;

    private final List<MEAssemblerRedstonePort> redstonePorts = new ArrayList<>();
    private final ConditionalSubscriptionHandler buddingCheckSubscription;

    public MEAssemblerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.buddingCheckSubscription = new ConditionalSubscriptionHandler(
                this, this::tickBuddingCheck, this::isFormed);
    }

    private void tickBuddingCheck() {
        if (getOffsetTimer() % 20 != 0)
            return;
        refreshBuddingTier();
    }

    private void refreshBuddingTier() {
        if (buddingPos == null || getLevel() == null || getLevel().isClientSide)
            return;
        buddingTier = TFGPredicates.getTierForBlock(getLevel().getBlockState(buddingPos).getBlock());
        updateRedstone();
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
        buddingCheckSubscription.updateSubscription();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!super.beforeWorking(recipe))
            return false;
        refreshBuddingTier();
        if (buddingTier < 0) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("tfg.machine.budding_missing")
                            .withStyle(ChatFormatting.RED));
            return false;
        }
        tryDegradeBudding(recipe);
        return true;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (var port : redstonePorts)
            port.trySetSignal(0);
        redstonePorts.clear();
        buddingCheckSubscription.updateSubscription();
    }

    private void updateRedstone() {
        int signal = buddingTier < 0 ? 0 : buddingTier + 1;
        for (var port : redstonePorts) {
            port.trySetSignal(signal);
        }
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void rollHealth() {
        buddingHealth = HEALTH_MIN + getLevel().getRandom().nextInt(HEALTH_MAX - HEALTH_MIN + 1);
    }

    private void tryDegradeBudding(@Nullable GTRecipe recipe) {
        if (buddingTier <= 0 || buddingPos == null)
            return;
        if (getLevel() == null || getLevel().isClientSide)
            return;

        if (buddingHealth <= 0)
            rollHealth();

        int executions = 1;
        if (recipe != null) {
            long work = RecipeHelper.getRealEUt(recipe).getTotalEU() * recipe.duration;
            executions = Math.max(1, (int) (work / 1_000_000));
        }

        buddingHealth -= executions;

        int tiersLost = 0;
        while (buddingHealth <= 0 && (buddingTier - tiersLost) > 0) {
            tiersLost++;
            int overflow = -buddingHealth;
            rollHealth();
            buddingHealth -= overflow;
        }

        if (tiersLost == 0)
            return;

        int actual = TFGPredicates.getTierForBlock(getLevel().getBlockState(buddingPos).getBlock());
        if (actual != buddingTier)
            return;

        Block next = TFGPredicates.getBuddingBlockForTier(buddingTier - tiersLost);
        getLevel().setBlockAndUpdate(buddingPos, next.defaultBlockState());
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed())
            return;

        if (buddingTier < 0) {
            textList.add(Component.translatable("tfg.machine.budding_missing")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        ChatFormatting color = switch (buddingTier) {
            case 0 -> ChatFormatting.GRAY;
            case 1, 2 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.GREEN;
        };
        /* Debug Tool for Balance - Also Informations that could be shared but could lead to exploit
        var last = getRecipeLogic().getLastRecipe();
        if (last != null) {
            long work = RecipeHelper.getRealEUt(last).getTotalEU() * last.duration * last.getTotalRuns();
            textList.add(Component.literal("work: " + work
                    + " # runs: " + last.getTotalRuns()
                    + " # dur: " + last.duration
                    + " # eut: " + RecipeHelper.getRealEUt(last).getTotalEU()
                    + " # units: " + Math.max(1, (int) (work / 10_000_000))
                    + " # hp: " + buddingHealth));
        }
        */
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
