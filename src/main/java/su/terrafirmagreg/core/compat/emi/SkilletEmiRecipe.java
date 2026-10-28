package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.eerussianguy.beneath.common.blocks.BeneathBlocks;
import com.eerussianguy.beneath.common.blocks.HellforgeBlock;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.eerussianguy.firmalife.common.blocks.OvenType;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.GasBurnerBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.recipe.SkilletRecipe;
import su.terrafirmagreg.core.compat.emi.widgets.CyclingWidget;
import su.terrafirmagreg.core.compat.emi.widgets.EmiBlockWidget;

@SuppressWarnings({ "NoTranslation" })
public class SkilletEmiRecipe implements EmiRecipe {

    private static final ResourceLocation PB = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/skillet_progress_bar.png");
    private static final ResourceLocation SKILLET = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/skillet_emi.png");

    private static final int WIDTH = 220;
    private static final int HEIGHT = 128;
    private static final int SLOT_SIZE = 18;

    private static final int PB_SIZE = 64;
    private static final int SKILLET_X = 0;
    private static final int SKILLET_Y = 0;

    private static final int DISPLAY_X = SKILLET_X + 157;
    private static final int DISPLAY_Y = 20;

    private static final EmiTexture PB_BACKGROUND = new EmiTexture(
            PB,
            0, 0,
            PB_SIZE, PB_SIZE,
            PB_SIZE, PB_SIZE,
            PB_SIZE * 2,
            PB_SIZE);
    private static final EmiTexture PB_FOREGROUND = new EmiTexture(
            PB,
            PB_SIZE, 0,
            PB_SIZE, PB_SIZE,
            PB_SIZE, PB_SIZE,
            PB_SIZE * 2, PB_SIZE);
    private static final EmiTexture POT_UNDERLAY = new EmiTexture(
            SKILLET,
            0, 0,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT);

    @Getter
    private final SkilletRecipe recipe;
    private final List<EmiIngredient> itemInputs;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public SkilletEmiRecipe(SkilletRecipe recipe) {
        this.recipe = recipe;

        this.itemInputs = new ArrayList<>();
        // Item ingredients
        for (Ingredient ing : recipe.getItemIngredients()) {
            if (ing.isEmpty()) {
                continue;
            }
            EmiIngredient emiIng = EmiIngredient.of(ing);
            if (emiIng.isEmpty()) {
                continue;
            }
            boolean merged = false;
            for (EmiIngredient existing : this.itemInputs) {
                if (EmiIngredient.areEqual(existing, emiIng)) {
                    existing.setAmount(existing.getAmount() + emiIng.getAmount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                this.itemInputs.add(emiIng.copy());
            }
        }

        this.inputs = new ArrayList<>(this.itemInputs);
        // Catalyst
        if (!recipe.getCatalyst().isEmpty()) {
            this.inputs.add(EmiIngredient.of(recipe.getCatalyst()));
        }
        // Tool
        if (!recipe.getTool().isEmpty()) {
            this.inputs.add(EmiIngredient.of(recipe.getTool()));
        }

        this.catalysts = new ArrayList<>();

        this.outputs = new ArrayList<>();
        for (var provider : recipe.getItemOutputs()) {
            ItemStack out = provider.getStack(ItemStack.EMPTY);
            if (!out.isEmpty()) {
                this.outputs.add(EmiStack.of(out));
            }
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.SKILLET;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
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

    @Override
    public void addWidgets(WidgetHolder holder) {
        holder.addTexture(POT_UNDERLAY, SKILLET_X, SKILLET_Y);

        // Item Inputs
        int inputItemX = SKILLET_X + 26;
        int inputItemY = SKILLET_Y + 15;
        int inputIdx = 0;
        for (EmiIngredient ing : this.itemInputs) {
            int col = inputIdx % 2;
            int row = inputIdx / 2;
            if (row == 0 || row == 2) {
                holder.addSlot(ing, inputItemX + (col * (SLOT_SIZE + 1)), inputItemY + (row * (SLOT_SIZE + 1))).drawBack(false);
            } else {
                holder.addSlot(ing, inputItemX - 6 + (col * (SLOT_SIZE + 13)), inputItemY + (row * (SLOT_SIZE + 1))).drawBack(false);
            }
            inputIdx++;
        }

        // Catalyst
        int catalystX = inputItemX + (SLOT_SIZE * 3) + 9;
        if (!recipe.getCatalyst().isEmpty()) {
            holder.addSlot(EmiIngredient.of(recipe.getCatalyst()), catalystX, inputItemY + 1).drawBack(false);
        }

        // Tool
        if (!recipe.getTool().isEmpty()) {
            SlotWidget toolSlot = new SlotWidget(EmiIngredient.of(recipe.getTool()), catalystX, inputItemY + (SLOT_SIZE * 2) + 1).drawBack(false);
            if (recipe.getToolDamage() > 0) {
                toolSlot.appendTooltip(Component.translatable("tfg.tooltip.damage_taken", recipe.getToolDamage()));
            }
            holder.add(toolSlot);
        }

        // Progress Bar
        int pbX = catalystX - 23;
        int pbY = inputItemY - 4;
        float seconds = (float) recipe.getDuration() / 20.0f;
        holder.addTexture(PB_BACKGROUND, pbX, pbY);
        if (seconds <= 0) {
            holder.addTexture(PB_FOREGROUND, pbX, pbY);
        } else {
            holder.addAnimatedTexture(PB_FOREGROUND, pbX, pbY, (int) (seconds * 1000), true, false, false);
        }

        // Item Outputs
        int itemOutputX = catalystX + (SLOT_SIZE * 2) + 1;
        int itemOutputY = inputItemY + 1;
        int itemOutIdx = 0;
        for (var provider : recipe.getItemOutputs()) {
            ItemStack out = provider.getStack(ItemStack.EMPTY);
            if (!out.isEmpty()) {
                holder.addSlot(EmiStack.of(out), itemOutputX, itemOutputY + (itemOutIdx * SLOT_SIZE)).recipeContext(this).drawBack(false);
                itemOutIdx++;
            }
        }

        // Block Renders
        float temp = recipe.getMinTemp();

        int renderZ = 201;
        int renderScale = 23;
        float renderRotX = -15.5F;
        float renderRotY = 22.5F + 180;
        int displayY = DISPLAY_Y;

        if (temp <= 0) {
            displayY += 21;
        }

        // Skillet Render
        holder.add(new EmiBlockWidget(TFGBlocks.SKILLET.getDefaultState(), DISPLAY_X + 6, displayY + SLOT_SIZE, renderZ, renderScale, renderRotX, renderRotY + 180));
        TFGEmiPlugin.createBlankItemWidget(holder, EmiIngredient.of(Ingredient.of(TFGBlocks.SKILLET.asItem())), DISPLAY_X, displayY + SLOT_SIZE);

        // Heat Sources
        int heatSourceX = DISPLAY_X + 7;
        int heatSourceY = displayY + 41;
        int heatSourceZ = renderZ - 20;
        if (temp > 0) {
            Bounds widgetBounds = new Bounds(heatSourceX, heatSourceY, WIDTH, HEIGHT);

            holder.add(new CyclingWidget(
                    widgetBounds,
                    new EmiBlockWidget(
                            TFCBlocks.CHARCOAL_FORGE.get().defaultBlockState().setValue(CharcoalForgeBlock.HEAT, 3),
                            heatSourceX, heatSourceY, heatSourceZ, renderScale, renderRotX, renderRotY),
                    new EmiBlockWidget(
                            BeneathBlocks.HELLFORGE.get().defaultBlockState().setValue(HellforgeBlock.HEAT, 3),
                            heatSourceX, heatSourceY, heatSourceZ, renderScale, renderRotX, renderRotY),
                    new EmiBlockWidget(
                            TFGBlocks.GAS_BURNER.get().defaultBlockState().setValue(GasBurnerBlock.HEAT, 3).setValue(GasBurnerBlock.LIT, true),
                            heatSourceX, heatSourceY, heatSourceZ, renderScale, renderRotX, renderRotY),
                    new EmiBlockWidget(
                            FLBlocks.CURED_OVEN_BOTTOM.get(OvenType.BRICK).get().defaultBlockState().setValue(OvenBottomBlock.LOGS, 3).setValue(OvenBottomBlock.LIT, true),
                            heatSourceX, heatSourceY, heatSourceZ, renderScale, renderRotX, renderRotY)));

            holder.add(new SlotWidget(TFGEmiPlugin.heatSources(), heatSourceX, heatSourceY) {
                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
                }
            }).drawBack(false).catalyst(true);
        }

        // Text
        int textY = inputItemY + (SLOT_SIZE * 3) + 38;

        MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
        if (formattedTemp == null) {
            formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
        }
        Component timeText = Component.literal("⌛ ").append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", seconds)));
        holder.add(new TextWidget(timeText.getVisualOrderText(), inputItemX + SLOT_SIZE - TFGEmiPlugin.centeredTextOffset(timeText), textY, 0xFFFFFF, true));
        if (temp > 0) {
            Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp != null ? formattedTemp : Component.empty());
            holder.add(new TextWidget(tempText.getVisualOrderText(), heatSourceX + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(tempText), textY, 0xFFFFFF, true));
        }
    }
}
