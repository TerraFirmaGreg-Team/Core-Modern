package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.MEAssemblerRedstonePort;

public class BuddingChargerMachine extends WorkableElectricMultiblockMachine {

    private static final int MAX_TIER = 4;
    private static final int CHARGE_PER_TIER = 100;

    @SaveField
    @Getter
    private int buddingTier = 0;

    @SaveField
    @Getter
    private int chargeProgress = 0;

    @Nullable
    private BlockPos buddingPos = null;

    @SaveField
    private String lastChargeRecipe = "";

    private final List<MEAssemblerRedstonePort> redstonePorts = new ArrayList<>();

    private final ConditionalSubscriptionHandler buddingCheckSubscription;

    public BuddingChargerMachine(BlockEntityCreationInfo info) {
        super(info);
        this.buddingCheckSubscription = new ConditionalSubscriptionHandler(
                this, this::tickBuddingCheck, this::isFormed);
    }

    // Have to updateTick so it checks which Budding Certus is present but won't allow the recipe to sleep
    // so don't add too many recipes to this recipe type
    private void tickBuddingCheck() {
        if (getOffsetTimer() % 20 != 0)
            return;
        refreshBuddingTier();
        if (buddingTier >= 0 && buddingTier < MAX_TIER) {
            getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);

        var cache = patternStates.get(substructureName).getCache();
        BlockPos buddingBlockPos = null;
        Block buddingBlock = null;

        for (var entry : cache.long2ObjectEntrySet()) {
            if (TFGPredicates.isBudding(entry.getValue().getBlockState())) {
                buddingBlock = entry.getValue().getBlockState().getBlock();
                buddingBlockPos = BlockPos.of(entry.getLongKey());
                break;
            }
        }

        buddingTier = TFGPredicates.getTierForBlock(buddingBlock);
        buddingPos = buddingBlockPos;

        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MEAssemblerRedstonePort port) {
                redstonePorts.add(port);
            }
        }
        updateRedstone();
        buddingCheckSubscription.updateSubscription();
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
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
        if (recipe == null)
            return true;

        refreshBuddingTier();
        if (buddingTier < 0) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("tfg.machine.budding_missing")
                            .withStyle(ChatFormatting.RED));
            return false;
        }

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
        if (buddingPos == null || buddingTier < 0 || buddingTier >= MAX_TIER)
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

        String recipeId = last.id.toString();
        if (!recipeId.equals(lastChargeRecipe)) {
            chargeProgress = 0;
            lastChargeRecipe = recipeId;
        }

        int charge = Math.max(1, last.data.getInt("budding_charge")) * last.getTotalRuns();
        chargeProgress += charge;

        int tiersGained = 0;
        while (chargeProgress >= CHARGE_PER_TIER && (buddingTier + tiersGained) < maxTier) {
            chargeProgress -= CHARGE_PER_TIER;
            tiersGained++;
        }

        if (tiersGained == 0)
            return;

        int actual = TFGPredicates.getTierForBlock(getLevel().getBlockState(buddingPos).getBlock());
        if (actual != buddingTier)
            return;

        Block next = TFGPredicates.getBuddingBlockForTier(buddingTier + tiersGained);
        getLevel().setBlockAndUpdate(buddingPos, next.defaultBlockState());
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        IntSyncValue buddingTierValue = new IntSyncValue(this::getBuddingTier);
        IntSyncValue chargeProgressValue = new IntSyncValue(() -> chargeProgress * 100 / CHARGE_PER_TIER);
        syncManager.syncValue("buddingTier", buddingTierValue);
        syncManager.syncValue("chargeProgress", chargeProgressValue);

        widgets.add(Text.lang("tfg.machine.budding_missing").withStyle(ChatFormatting.RED).asWidget()
                .setEnabledIf(w -> buddingTierValue.getIntValue() < 0));

        widgets.add(Text.lang("tfg.machine.budding_charger.max").withStyle(ChatFormatting.GRAY).asWidget()
                .setEnabledIf(w -> buddingTierValue.getIntValue() >= MAX_TIER));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.budding_charger.progress",
                Component.literal(chargeProgressValue.getIntValue() + "%").withStyle(ChatFormatting.AQUA))).asWidget());

        return widgets;
    }
}
