package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.PlanterType;
import com.eerussianguy.firmalife.common.util.Plantable;

import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;

@SuppressWarnings({ "unused", "UnnecessaryLocalVariable" })
public class GreenhousePlantableEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 60;

    @Getter
    private final Plantable plantable;
    private final ResourceLocation id;
    @Getter
    private final Ingredient ingredient;
    private final PlanterType planter;
    @Getter
    private final int tier;
    @Getter
    private final int stages;
    @Getter
    private final float extraSeedChance;
    @Getter
    private final ItemStack seed;
    @Getter
    private final ItemStack crop;
    @Getter
    private final FarmlandBlockEntity.NutrientType nutrient;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public GreenhousePlantableEmiRecipe(Plantable plantable) {
        this.plantable = plantable;
        this.id = plantable.getId();
        this.ingredient = Ingredient.of(plantable.getValidItems().toArray(Item[]::new));
        this.planter = plantable.getPlanterType();
        this.tier = plantable.getTier();
        this.stages = plantable.getStages();
        this.extraSeedChance = plantable.getExtraSeedChance();
        this.seed = plantable.getSeed();
        this.crop = plantable.getCrop();
        this.nutrient = plantable.getPrimaryNutrient();

        this.inputs = List.of(EmiIngredient.of(this.ingredient));

        List<EmiStack> outputs = new ArrayList<>();
        if (!this.crop.isEmpty()) {
            outputs.add(EmiStack.of(this.crop));
        }
        if (!this.seed.isEmpty()) {
            outputs.add(EmiStack.of(this.seed));
        }
        this.outputs = outputs;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.GREENHOUSE_PLANTABLE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "synthetic/greenhouse/" + id.getPath());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    public PlanterType getPlanterType() {
        return planter;
    }

    public static Item resolvePlanterItem(PlanterType type) {
        var planterBlock = switch (type) {
            case QUAD -> FLBlocks.QUAD_PLANTER;
            case LARGE -> FLBlocks.LARGE_PLANTER;
            case HANGING -> FLBlocks.HANGING_PLANTER;
            case TRELLIS -> FLBlocks.TRELLIS_PLANTER;
            case BONSAI -> FLBlocks.BONSAI_PLANTER;
            case HYDROPONIC -> FLBlocks.HYDROPONIC_PLANTER;
        };
        return planterBlock.get().asItem();
    }

    public FarmlandBlockEntity.NutrientType getPrimaryNutrient() {
        return nutrient;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        int offset = 6;
        int slotSize = 18;

        int planterX = offset;
        int planterY = (HEIGHT / 2) - (slotSize / 2);
        int ingredientX = planterX + slotSize + offset;
        int ingredientY = planterY;
        int renderX = WIDTH / 2;
        int renderY = planterY;
        int cropX = WIDTH - (slotSize * 2) - (offset * 2);
        int cropY = ingredientY;
        int seed1X = cropX + slotSize + offset;
        int seed1Y = cropY - ((offset / 2) + (slotSize / 2));
        int seed2X = cropX + slotSize + offset;
        int seed2Y = cropY + ((offset / 2) + (slotSize / 2));

        // Re-align crop slot if there is no seed output.
        if (seed.isEmpty())
            cropX += slotSize;

        widgets.addSlot(EmiIngredient.of(Ingredient.of(resolvePlanterItem(planter))), planterX, planterY);

        widgets.addSlot(EmiIngredient.of(ingredient), ingredientX, ingredientY);

        widgets.addSlot(EmiStack.of(crop), cropX, cropY).recipeContext(this);

        if (!seed.isEmpty()) {
            widgets.addSlot(EmiStack.of(seed), seed1X, seed1Y).recipeContext(this);
        }

        if (!seed.isEmpty() && extraSeedChance > 0) {
            widgets.addSlot(EmiStack.of(seed).setChance(extraSeedChance), seed2X, seed2Y).recipeContext(this);
        }

    }
}
