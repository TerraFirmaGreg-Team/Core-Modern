package su.terrafirmagreg.core.common.blockentity.appliances;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.PartialFluidHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.container.MixingBowlContainer;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.recipe.CookingApplianceRecipe;
import su.terrafirmagreg.core.common.recipe.attachment.MixerAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.RecipeAttachment;

public class MixingBowlBlockEntity extends CookingApplianceBlockEntity implements MenuProvider {

    public static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.mixing_bowl");

    public static final int ITEM_INPUTS = 3;
    public static final int FLUID_INPUTS = 2;
    public static final int ITEM_OUTPUTS = 2;
    public static final int FLUID_OUTPUTS = 2;
    public static final int FLUID_INPUT_CAPACITY = 4000;
    public static final int FLUID_OUTPUT_CAPACITY = 8000;

    protected boolean manualMixing = false;

    public MixingBowlBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, NAME);
    }

    @Override
    public int getItemInputCount() {
        return ITEM_INPUTS;
    }

    @Override
    public int getFluidInputCount() {
        return FLUID_INPUTS;
    }

    @Override
    public int getItemOutputCount() {
        return ITEM_OUTPUTS;
    }

    @Override
    public int getFluidOutputCount() {
        return FLUID_OUTPUTS;
    }

    @Override
    public int getFluidInputCapacity() {
        return FLUID_INPUT_CAPACITY;
    }

    @Override
    public int getFluidOutputCapacity() {
        return FLUID_OUTPUT_CAPACITY;
    }

    @Override
    public RecipeType<? extends CookingApplianceRecipe> getRecipeType() {
        return TFGRecipeTypes.MIXING_BOWL.get();
    }

    @Override
    public boolean supportsAttachment(RecipeAttachment attachment) {
        return attachment instanceof MixerAttachment;
    }

    @Override
    public float getTemperature() {
        return 0.0F;
    }

    @Override
    public float getTargetTemperature() {
        return 0.0F;
    }

    @Override
    public void setTemperature(float temperature) {
    }

    @Override
    public void setTemperatureIfWarmer(float temperature) {
    }

    @Override
    public void resetHeat() {
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixingBowlBlockEntity appliance) {
        appliance.checkForLastTickSync();
        appliance.checkForCalendarUpdate();
        appliance.updateAndTickRecipe(level);
    }

    @Override
    protected void configureSidedHandlers() {
        int[] inputSlots = new int[getItemInputCount() + 2];
        for (int i = 0; i < getItemInputCount() + 2; i++) {
            inputSlots[i] = i;
        }

        int[] outputSlots = new int[getItemOutputCount()];
        for (int i = 0; i < getItemOutputCount(); i++) {
            outputSlots[i] = getItemOutputStartSlot() + i;
        }

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(inputSlots).extract(outputSlots), Direction.Plane.HORIZONTAL)
                .on(new PartialItemHandler(inventory).insert(inputSlots), Direction.UP)
                .on(new PartialItemHandler(inventory).extract(outputSlots), Direction.DOWN);

        sidedFluidInventory
                .on(new PartialFluidHandler(inventory).insert().extract(), Direction.Plane.HORIZONTAL)
                .on(new PartialFluidHandler(inventory).insert(), Direction.UP)
                .on(new PartialFluidHandler(inventory).extract(), Direction.DOWN);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == HeatCapability.BLOCK_CAPABILITY || cap == HeatCapability.CAPABILITY) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    public boolean isManualMixing() {
        return this.manualMixing && this.cookingTicks > 0;
    }

    public boolean startManualMixing() {
        if (this.level == null || this.level.isClientSide) {
            return !getToolStack().isEmpty();
        }
        if (this.isManualMixing()) {
            return false;
        }
        if (getToolStack().isEmpty()) {
            return false;
        }

        CookingApplianceRecipe matching = level.getRecipeManager().getAllRecipesFor(getRecipeType()).stream()
                .filter(r -> r instanceof CookingApplianceRecipe)
                .map(r -> (CookingApplianceRecipe) r)
                .filter(r -> !r.getTool().isEmpty() && r.matches(this, level))
                .findFirst()
                .orElse(null);

        if (matching != null) {
            this.cachedRecipe = matching;
            this.cachedRecipeId = matching.getId();
            this.manualMixing = true;
            this.cookingTicks = 1;
            this.maxDuration = matching.getEffectiveDuration(this);
            this.recipeError = CookingError.NONE;
            this.markForSync();
            return true;
        }
        return false;
    }

    public float getRotationAngle(float partialTick) {
        if (this.cookingTicks > 0 && this.maxDuration > 0) {
            float progress = (this.cookingTicks + partialTick) / (float) this.maxDuration;
            return (progress % 1.0F) * 360.0F;
        }
        return 0.0F;
    }

    @Override
    protected void updateAndTickRecipe(Level level) {
        if (level == null || level.isClientSide)
            return;

        CookingError prevError = this.recipeError;
        int prevDuration = this.maxDuration;

        // If manual mixing is in progress.
        if (this.manualMixing) {
            if (this.cachedRecipe != null && this.cachedRecipe.matches(this, level)) {
                this.recipeError = CookingError.NONE;
                int effectiveDuration = this.cachedRecipe.getEffectiveDuration(this);
                this.maxDuration = effectiveDuration;
                this.cachedRecipe.tickActive(this);
                this.cookingTicks++;
                if (this.cookingTicks >= effectiveDuration) {
                    finishRecipe(this.cachedRecipe);
                    this.cookingTicks = 0;
                    this.manualMixing = false;
                    this.cachedRecipe = null;
                    this.cachedRecipeId = null;
                }
                markForSync();
                return;
            } else {
                this.manualMixing = false;
                this.cookingTicks = 0;
                this.cachedRecipe = null;
                this.cachedRecipeId = null;
                markForSync();
            }
        }

        // Check if an automated recipe matches.
        CookingApplianceRecipe activeAutomated = level.getRecipeManager().getAllRecipesFor(getRecipeType()).stream()
                .filter(r -> r instanceof CookingApplianceRecipe)
                .map(r -> (CookingApplianceRecipe) r)
                .filter(r -> r.getTool().isEmpty() && r.matches(this, level))
                .findFirst()
                .orElse(null);

        if (activeAutomated != null) {
            if (cachedRecipe != null && cachedRecipe != activeAutomated) {
                cookingTicks = 0;
            }
            cachedRecipe = activeAutomated;
            cachedRecipeId = activeAutomated.getId();
            this.recipeError = CookingError.NONE;
            int effectiveDuration = cachedRecipe.getEffectiveDuration(this);
            this.maxDuration = effectiveDuration;
            cachedRecipe.tickActive(this);
            cookingTicks++;
            if (cookingTicks >= effectiveDuration) {
                finishRecipe(cachedRecipe);
                cookingTicks = 0;
                cachedRecipe = null;
                cachedRecipeId = null;
            }
            markForSync();
            return;
        }

        // Find recipe to report errors or display preparation.
        CookingApplianceRecipe candidate = level.getRecipeManager().getAllRecipesFor(getRecipeType()).stream()
                .filter(r -> r instanceof CookingApplianceRecipe)
                .map(r -> (CookingApplianceRecipe) r)
                .sorted((r1, r2) -> Integer.compare(r2.getAttachments().size(), r1.getAttachments().size()))
                .filter(r -> r.matchesInputs(this))
                .findFirst()
                .orElse(null);

        if (candidate != null) {
            CookingError detectedErrors = candidate.getErrorFlags(this);
            if (detectedErrors != CookingError.NONE) {
                this.recipeError = detectedErrors;
            } else {
                this.recipeError = CookingError.NONE;
                this.maxDuration = candidate.getEffectiveDuration(this);
                this.cachedRecipe = candidate;
                this.cachedRecipeId = candidate.getId();
            }
            this.cookingTicks = 0;
        } else {
            this.recipeError = CookingError.NONE;
            this.cookingTicks = 0;
            this.cachedRecipe = null;
            this.cachedRecipeId = null;
        }

        if (prevError != this.recipeError || prevDuration != this.maxDuration) {
            markForSync();
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        super.loadAdditional(nbt);
        this.manualMixing = nbt.getBoolean("manualMixing");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("manualMixing", this.manualMixing);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return MixingBowlContainer.create(this, playerInventory, windowId);
    }
}
