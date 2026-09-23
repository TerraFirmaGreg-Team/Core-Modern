package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.MEAssemblerRedstonePort;

public class MEAssemblerMachine extends WorkableElectricMultiblockMachine {

    private static final int HEALTH_MIN = 100;
    private static final int HEALTH_MAX = 500;

    private static final double[] BUDDING_SPEED_BONUS = { 0.0, 8.0, 32.0, 128.0, 512.0 };

    @SaveField
    @Getter
    private int buddingTier = 0;

    @SaveField
    @Getter
    private int buddingHealth = 0;

    @Nullable
    private BlockPos buddingPos = null;

    private final List<MEAssemblerRedstonePort> redstonePorts = new ArrayList<>();
    private final ConditionalSubscriptionHandler buddingCheckSubscription;

    public MEAssemblerMachine(BlockEntityCreationInfo info) {
        super(info);
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
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);

        var cache = patternStates.get(substructureName).getCache();
        BlockPos buddingBlockPos = null;
        Block buddingBlock = null;

        for (var entry: cache.long2ObjectEntrySet()) {
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
    public void onLoad() {
        super.onLoad();
        buddingCheckSubscription.initialize(getLevel());
    }

    @Override
    public void saveToItem(CompoundTag tag, boolean clone) {
        tag.putInt("buddingHealth", buddingHealth);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        buddingHealth = tag.getInt("buddingHealth");
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!super.beforeWorking(recipe))
            return false;
        refreshBuddingTier();
        if (buddingTier < 0) {
            RecipeLogic.putFailureReason(this, Objects.requireNonNull(recipe),
                    Component.translatable("tfg.machine.budding_missing")
                            .withStyle(ChatFormatting.RED));
            return false;
        }
        tryDegradeBudding(recipe);
        return true;
    }

    @Override
    public void invalidateStructure(String name) {
        if (DEFAULT_STRUCTURE.equals(name)) {
            for (var port : redstonePorts)
                port.trySetSignal(0);
            redstonePorts.clear();
            buddingCheckSubscription.updateSubscription();
        }
    }

    private void updateRedstone() {
        int signal = buddingTier < 0 ? 0 : buddingTier + 1;
        for (var port : redstonePorts) {
            port.trySetSignal(signal);
        }
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
            int dur = recipe.data.contains("nominal_duration")
                    ? recipe.data.getInt("nominal_duration")
                    : recipe.duration;
            long work = (long) dur * Math.max(1, recipe.batchParallels);
            executions = Math.max(1, (int) (work / 100));
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
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        if (!isFormed())
            return widgets;

        IntSyncValue buddingTierValue = new IntSyncValue(this::getBuddingTier);
        IntSyncValue speedBonus = new IntSyncValue(() -> (int) (BUDDING_SPEED_BONUS[buddingTier] * 100));
        syncManager.syncValue("buddingTier", buddingTierValue);
        syncManager.syncValue("speedBonus", speedBonus);

        widgets.add(Text.lang("tfg.machine.budding_missing").withStyle(ChatFormatting.RED).asWidget()
                .setEnabledIf(w -> buddingTierValue.getIntValue() < 0));

        widgets.add(Text.dynamic(() -> {
            ChatFormatting color = switch (buddingTierValue.getIntValue()) {
                case 0 -> ChatFormatting.GRAY;
                case 1, 2 -> ChatFormatting.YELLOW;
                default -> ChatFormatting.GREEN;
            };
            return Component.translatable("tfg.machine.me_assembler.budding_tier",
                    Component.literal(speedBonus.getIntValue() + "%").withStyle(color));
        }).asWidget());

        return widgets;
    }

    // Fully custom Modifier function so you can't speed up through OC, Batchmode is always on because of the insane bonus
    // speed you can get and the speed bonus through the Budding
    public static ModifierFunction buddingModifier(MetaMachine machine,
                                                   GTRecipe recipe) {

        if (!(machine instanceof MEAssemblerMachine meMachine)) {
            return RecipeModifier.nullWrongType(MEAssemblerMachine.class, machine);
        }

        if (RecipeHelper.getRecipeEUtTier(recipe) > meMachine.getTier()) {
            return ModifierFunction.cancel(
                    Component.translatable("gtceu.recipe_modifier.insufficient_voltage"));
        }

        int tier = meMachine.getBuddingTier();
        double factor = tier > 0 ? 1.0 / (1.0 + BUDDING_SPEED_BONUS[tier]) : 1.0;

        ModifierFunction speedModifier = r -> {
            var copy = r.copy();
            copy.data = r.data.copy();
            copy.data.putInt("nominal_duration", r.duration);
            copy.duration = Math.max(1, (int) (r.duration * factor));
            return copy;
        };

        GTRecipe sped = speedModifier.apply(recipe);
        if (sped == null)
            return ModifierFunction.NULL;

        if (sped.duration >= ConfigHolder.INSTANCE.machines.batchDuration) {
            return speedModifier;
        }

        int parallel = ConfigHolder.INSTANCE.machines.batchDuration / sped.duration;
        parallel = ParallelLogic.getParallelAmountWithoutEU(machine, sped, parallel);

        if (parallel == 0)
            return ModifierFunction.NULL;
        if (parallel == 1)
            return speedModifier;

        ModifierFunction batchModifier = ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .durationMultiplier(parallel)
                .batchParallels(parallel)
                .build();

        return speedModifier.andThen(batchModifier);
    }
}
