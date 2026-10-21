package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.FLStateProperties;
import com.eerussianguy.firmalife.common.blocks.greenhouse.PlanterType;
import com.eerussianguy.firmalife.common.util.GreenhouseType;
import com.eerussianguy.firmalife.common.util.Plantable;

import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;

import su.terrafirmagreg.core.compat.emi.widgets.EmiBlockWidget;

@SuppressWarnings({ "UnnecessaryLocalVariable", "NoTranslation" })
public class GreenhousePlantableEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 50;

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
    private final List<EmiIngredient> catalysts;
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
        this.catalysts = List.of(EmiIngredient.of(Ingredient.of(resolvePlanterBlock(planter).asItem())));

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
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/greenhouse/" + id.getPath());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
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

    /**
     * Gets the planter block by {@link PlanterType}
     * @param type Planter type.
     * @return Planter block.
     */
    public static Block resolvePlanterBlock(PlanterType type) {
        var planterBlock = switch (type) {
            case QUAD -> FLBlocks.QUAD_PLANTER;
            case LARGE -> FLBlocks.LARGE_PLANTER;
            case HANGING -> FLBlocks.HANGING_PLANTER;
            case TRELLIS -> FLBlocks.TRELLIS_PLANTER;
            case BONSAI -> FLBlocks.BONSAI_PLANTER;
            case HYDROPONIC -> FLBlocks.HYDROPONIC_PLANTER;
        };
        return planterBlock.get();
    }

    /**
     * Gets the fertilizer translation string by {@link FarmlandBlockEntity.NutrientType}.
     * @param type Nutrient type.
     * @return Nutrient translation key.
     */
    public static String resolveFertilizer(FarmlandBlockEntity.NutrientType type) {
        return switch (type) {
            case NITROGEN -> "tfg.tooltip.fertilizer.nitrogen";
            case PHOSPHOROUS -> "tfg.tooltip.fertilizer.phosphorus";
            case POTASSIUM -> "tfg.tooltip.fertilizer.potassium";
        };
    }

    public FarmlandBlockEntity.NutrientType getPrimaryNutrient() {
        return nutrient;
    }

    /**
     * Gets the greenhouse component name by tier
     * @param tier Tier number of the greenhouse.
     * @return Component name of the greenhouse.
     */
    public static Component getGreenhouseNameByTier(int tier) {
        for (GreenhouseType type : GreenhouseType.MANAGER.getValues()) {
            if (type.tier == tier) {
                return type.getTitle();
            }
        }
        if (tier == 0) {
            return Component.translatable("tfg.emi.ore_veins.biome_any");
        }
        return Component.literal(String.valueOf(tier));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        Font font = Minecraft.getInstance().font;
        Component nutrientType = Component.translatable(resolveFertilizer(nutrient));
        Component tierText = Component.translatable("tfg.tooltip.greenhouse.minimum_tier").append(getGreenhouseNameByTier(tier));

        int offset = 6;
        int slotSize = 18;
        int textHeight = font.lineHeight;
        int slotCentering = (offset / 2) + (slotSize / 2);
        int slotSpacing = offset + slotSize;

        int ingredientX = offset * 2;
        int ingredientY = ((HEIGHT / 2) - (slotSize / 2)) - slotCentering;
        int planterX = ingredientX;
        int planterY = ingredientY + slotSize;

        int nutrientTextWidth = font.width(nutrientType);
        int nutrientX = (WIDTH / 2) - (nutrientTextWidth / 2);
        int nutrientY = (HEIGHT / 3) - textHeight - 2;

        int arrowX = (WIDTH / 2) - (slotSize / 2);
        int arrowY = (HEIGHT / 2) - (slotSize / 2);

        int tierTextWidth = font.width(tierText);
        int tierX = arrowX + (slotSize / 2) - (tierTextWidth / 2);
        int tierY = HEIGHT - (HEIGHT / 3) + 2;

        int cropX = WIDTH - (slotSize * 2) - (offset * 2);
        int cropY = (HEIGHT / 2) - (slotSize / 2);

        int seed1X = cropX + slotSpacing;
        int seed1Y = cropY - slotCentering;
        int seed2X = seed1X;
        int seed2Y = seed1Y + slotSpacing;

        // Re-align crop slot if there is no seed output.
        if (seed.isEmpty()) {
            cropX += slotSize;
        }
        // Re-align seed slot if there is no extra seed chance.
        if (!seed.isEmpty() && extraSeedChance == 0) {
            seed1Y = cropY;
        }

        // Hanging planters switch the planter and ingredient positions.
        if (planter == PlanterType.HANGING) {
            planterY = ingredientY;
            ingredientY = planterY + slotSize;
        }

        // Trellis planters get separated a bit more to look better.
        if (planter == PlanterType.TRELLIS) {
            planterY += (offset / 2);
            ingredientY -= (offset / 2);
        }

        // Input Seed Display.
        widgets.addSlot(EmiIngredient.of(ingredient), ingredientX, ingredientY);

        // Planter Display.
        TFGEmiPlugin.createBlankItemWidget(widgets, EmiIngredient.of(Ingredient.of(resolvePlanterBlock(planter).asItem())), planterX, planterY);
        widgets.add(new EmiBlockWidget(resolvePlanterBlock(planter).defaultBlockState().setValue(FLStateProperties.WATERED, true), planterX, planterY, 100, 20));

        // Fertilizer Display.
        widgets.add(new TextWidget(nutrientType.getVisualOrderText(), nutrientX, nutrientY, 0xFFFFFF, true) {
            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                return List.of(ClientTooltipComponent.create(Component.translatable("tfg.tooltip.planter.fertilizer_info").getVisualOrderText()));
            }
        });

        // Arrow Display.
        widgets.addFillingArrow(arrowX, arrowY, 2000);

        // Greenhouse Tier Display.
        widgets.addText(tierText, tierX, tierY, 0xFFFFFF, true);

        // Output Crop Display.
        widgets.addSlot(EmiStack.of(crop), cropX, cropY).recipeContext(this);

        // Output Seed Display.
        if (!seed.isEmpty()) {
            widgets.addSlot(EmiStack.of(seed), seed1X, seed1Y).recipeContext(this);
        }

        // Bonus Output Seed Display.
        if (!seed.isEmpty() && extraSeedChance > 0) {
            widgets.addSlot(EmiStack.of(seed).setChance(extraSeedChance), seed2X, seed2Y).recipeContext(this);
        }

    }
}
