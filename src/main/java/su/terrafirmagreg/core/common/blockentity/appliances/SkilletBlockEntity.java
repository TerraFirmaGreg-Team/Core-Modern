package su.terrafirmagreg.core.common.blockentity.appliances;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.container.SkilletContainer;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.recipe.CookingApplianceRecipe;

public class SkilletBlockEntity extends CookingApplianceBlockEntity implements MenuProvider {

    public static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.skillet");

    public static final int ITEM_INPUTS = 6;
    public static final int FLUID_INPUTS = 0;
    public static final int ITEM_OUTPUTS = 3;
    public static final int FLUID_OUTPUTS = 0;
    public static final int FLUID_INPUT_CAPACITY = 0;
    public static final int FLUID_OUTPUT_CAPACITY = 0;

    public SkilletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    public boolean supportsAttachments() {
        return false;
    }

    @Override
    public RecipeType<? extends CookingApplianceRecipe> getRecipeType() {
        return TFGRecipeTypes.SKILLET.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return SkilletContainer.create(this, playerInventory, windowId);
    }
}
