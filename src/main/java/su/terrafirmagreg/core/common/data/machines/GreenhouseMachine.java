package su.terrafirmagreg.core.common.data.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.GTRecipe.ActionResult;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.data.ForgeRecipeProvider;
import net.minecraftforge.items.IItemHandler;
import su.terrafirmagreg.core.TFGCore;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    private IItemHandler itemInput;
    private IItemHandler itemOutput;

    public GreenhouseMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new GreenhouseRecipeLogic(this);
    }

    @Override
    public GreenhouseRecipeLogic getRecipeLogic() {
        return (GreenhouseRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        itemInput = (IItemHandler) getParts().stream().filter(p -> PartAbility.IMPORT_ITEMS.isApplicable(p.self().getBlockState().getBlock())).findFirst().get();
        itemOutput = (IItemHandler) getParts().stream().filter(p -> PartAbility.EXPORT_ITEMS.isApplicable(p.self().getBlockState().getBlock())).findFirst().get();
    }

    @Override
    public void onStructureInvalid() {
        itemInput = null;
        itemOutput = null;
        super.onStructureInvalid();
    }

    public class GreenhouseRecipeLogic extends RecipeLogic {
        public GreenhouseRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }
        
        @Override 
        @NotNull
        public GreenhouseMachine getMachine() {
            return (GreenhouseMachine)super.getMachine();
        }

        @Override 
        protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                for (int i = 0; i < itemInput.getSlots(); i++) {
                    ItemStack iStack = itemInput.getStackInSlot(i);
                    var foodCap = iStack.getCapability(FoodCapability.CAPABILITY);

                    if (foodCap.isPresent()) {
                        // Invalidate input items if they are rotten.
                        if (foodCap.resolve().get().isRotten()) {
                            return false;
                        }
                    }
                }
                
            } else if (io == IO.OUT) {
                var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
                for (Content c : items) {
                    if (c.content instanceof ItemStack stack) {
                        insertIntoFreeSlot(stack);
                    } else if (c.content instanceof Item item) {
                        insertIntoFreeSlot(new ItemStack(item, 1));
                    }

                    TFGCore.LOGGER.error("Greenhouse recipe producing output that is not Item|ItemStack: " + recipe.id.toString());

                }
            }
            return false;
        }

        private boolean insertIntoFreeSlot(ItemStack stack) {
            
            boolean isFood = false;
            if (stack.is(TFCTags.Items.FOODS)) {
                stack = FoodCapability.setCreationDate(stack, FoodCapability.getRoundedCreationDate());
                isFood = true;
            }
            for (int i = 0; i < itemOutput.getSlots(); i++) {
                if (!isFood || itemOutput.getStackInSlot(i).isEmpty()) {
                    stack = itemOutput.insertItem(i, stack, false);
                    if (stack.isEmpty()) return true;
                } else {
                    if (ItemStack.isSameItem(stack, itemOutput.getStackInSlot(i))) {
                        stack = itemOutput.insertItem(i, stack, false);
                        if (stack.isEmpty()) return true;
                    }
                }
            }
            return false;
        }
    }
}
