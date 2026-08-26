package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
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

public class BuddingChargerMachine extends WorkableElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            BuddingChargerMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int MAX_TIER = 4;
    private static final int CHARGE_PER_TIER = 100;

    @Persisted
    @DescSynced
    @Getter
    private int buddingTier = 0;

    @Persisted
    @DescSynced
    @Getter
    private int chargeProgress = 0;

    @Nullable
    private BlockPos buddingPos = null;

    private final List<MEAssemblerRedstonePort> redstonePorts = new ArrayList<>();

    public BuddingChargerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        int signal = buddingTier < 0 ? 0 : buddingTier + 1;
        for (var port : redstonePorts) {
            port.trySetSignal(signal);
        }
    }

    private void refreshBuddingTier() {
        if (buddingPos == null || getLevel() == null || getLevel().isClientSide)
            return;
        buddingTier = TFGPredicates.getTierForBlock(getLevel().getBlockState(buddingPos).getBlock());
        updateRedstone();
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

        if (recipe == null)
            return true;

        int maxTier = recipe.data.contains("budding_max_tier")
                ? recipe.data.getInt("budding_max_tier")
                : MAX_TIER;

        if (buddingTier >= maxTier) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("tfg.machine.budding_charger.tier_too_low")
                            .withStyle(ChatFormatting.RED));
            return false;
        }
        return true;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        tryChargeBudding();
    }

    private void tryChargeBudding() {
        if (buddingPos == null || buddingTier >= MAX_TIER)
            return;
        if (getLevel() == null || getLevel().isClientSide)
            return;

        var last = getRecipeLogic().getLastRecipe();
        if (last == null)
            return;

        int maxTier = last.data.contains("budding_max_tier")
                ? last.data.getInt("budding_max_tier")
                : MAX_TIER;
        if (buddingTier >= maxTier)
            return;

        int charge = Math.max(1, last.data.getInt("budding_charge")) * last.getTotalRuns();
        chargeProgress += charge;

        if (chargeProgress < CHARGE_PER_TIER)
            return;

        int actual = TFGPredicates.getTierForBlock(getLevel().getBlockState(buddingPos).getBlock());
        if (actual != buddingTier)
            return;

        chargeProgress = 0;
        Block next = TFGPredicates.getBuddingBlockForTier(buddingTier + 1);
        getLevel().setBlockAndUpdate(buddingPos, next.defaultBlockState());
        getRecipeLogic().markLastRecipeDirty();
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

        if (buddingTier >= MAX_TIER) {
            textList.add(Component.translatable("tfg.machine.budding_charger.max")
                    .withStyle(ChatFormatting.GREEN));
            return;
        }

        int percent = chargeProgress * 100 / CHARGE_PER_TIER;
        textList.add(Component.translatable("tfg.machine.budding_charger.progress",
                Component.literal(percent + "%").withStyle(ChatFormatting.AQUA)));
    }
}
