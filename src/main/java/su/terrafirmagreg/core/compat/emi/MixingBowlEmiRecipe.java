package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import electrolyte.greate.registry.MechanicalMixers;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.recipe.MixingBowlRecipe;
import su.terrafirmagreg.core.common.recipe.attachment.MixerAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.RecipeAttachment;
import su.terrafirmagreg.core.compat.emi.widgets.EmiBlockWidget;
import su.terrafirmagreg.core.utils.TFGHelpers;

@SuppressWarnings({ "UnnecessaryLocalVariable", "NoTranslation" })
public class MixingBowlEmiRecipe implements EmiRecipe {

    private static final ResourceLocation PB = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/copper_pot_progress_bar.png");
    private static final ResourceLocation POT = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/mixing_bowl_emi.png");

    private static final int WIDTH = 214;
    private static final int HEIGHT = 122;
    private static final int SLOT_SIZE = 18;

    private static final int PB_SIZE = 32;
    private static final int BOWL_X = 0;
    private static final int BOWL_Y = 0;
    private static final int ATTACHMENT_X = BOWL_X + 158;
    private static final int ATTACHMENT_Y = 26;

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
    private static final EmiTexture BOWL_UNDERLAY = new EmiTexture(
            POT,
            0, 0,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT);

    @Getter
    private final MixingBowlRecipe recipe;
    private final List<EmiIngredient> itemInputs;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public MixingBowlEmiRecipe(MixingBowlRecipe recipe) {
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
        // Fluid ingredients
        for (var fsi : recipe.getFluidIngredients()) {
            var fluidStacks = fsi.ingredient().fluids().stream()
                    .map(fluid -> (EmiIngredient) EmiStack.of(fluid, fsi.amount()))
                    .toList();
            if (!fluidStacks.isEmpty()) {
                this.inputs.add(EmiIngredient.of(fluidStacks));
            }
        }
        // Catalyst
        if (!recipe.getCatalyst().isEmpty()) {
            this.inputs.add(EmiIngredient.of(recipe.getCatalyst()).setChance(recipe.getCatalystChance()));
        }
        // Tool
        if (!recipe.getTool().isEmpty()) {
            this.inputs.add(EmiIngredient.of(recipe.getTool()));
        }

        // Attachments
        this.catalysts = new ArrayList<>();
        for (RecipeAttachment attachment : recipe.getAttachments()) {
            if (attachment instanceof MixerAttachment mixerAttachment) {
                Ingredient ingredient = getAttachment(mixerAttachment);
                if (!ingredient.isEmpty()) {
                    this.catalysts.add(EmiIngredient.of(ingredient));
                }
            }
        }

        this.outputs = new ArrayList<>();
        // Item outputs
        for (var provider : recipe.getItemOutputs()) {
            ItemStack stack = provider.getEmptyStack();
            if (!stack.isEmpty()) {
                this.outputs.add(EmiStack.of(stack));
            }
        }
        // Fluid outputs
        for (FluidStack fs : recipe.getFluidOutputs()) {
            if (!fs.isEmpty()) {
                this.outputs.add(EmiStack.of(fs.getFluid(), fs.getTag(), fs.getAmount()));
            }
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.MIXING_BOWL;
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

        holder.addTexture(BOWL_UNDERLAY, BOWL_X, BOWL_Y);

        // Render Item Inputs
        int inputItemX = BOWL_X + 15;
        int inputItemY = BOWL_Y + 26;
        int inputIdx = 0;
        for (EmiIngredient ing : this.itemInputs) {
            holder.addSlot(ing, inputItemX + inputIdx * SLOT_SIZE, inputItemY).drawBack(false);
            inputIdx++;
        }

        // Render Fluid Inputs
        int inputFluidX = inputItemX + 9;
        int inputFluidY = inputItemY + SLOT_SIZE + 2;
        int fluidInIdx = 0;
        for (var fsi : recipe.getFluidIngredients()) {
            var fluidStacks = fsi.ingredient().fluids().stream()
                    .map(fluid -> (EmiIngredient) EmiStack.of(fluid, fsi.amount()))
                    .toList();
            if (!fluidStacks.isEmpty()) {
                holder.addSlot(EmiIngredient.of(fluidStacks), inputFluidX + fluidInIdx * SLOT_SIZE, inputFluidY).drawBack(false);
                fluidInIdx++;
            }
        }

        // Render Mixing Bowl Block
        float rpm = 0;
        int circuit = 0;

        int renderZ = 201;
        int renderScale = 23;
        float renderRotX = -15.5F;
        float renderRotY = 22.5F + 180;
        int attachmentY = ATTACHMENT_Y;

        if (!recipe.getAttachments().isEmpty()) {
            attachmentY += 11;
        }
        ;

        holder.add(new EmiBlockWidget(TFGBlocks.MIXING_BOWL.getDefaultState(), ATTACHMENT_X + 6, attachmentY + SLOT_SIZE, renderZ, renderScale, renderRotX, renderRotY));
        TFGEmiPlugin.createBlankItemWidget(holder, EmiIngredient.of(Ingredient.of(TFGBlocks.MIXING_BOWL.asItem())), ATTACHMENT_X, attachmentY + SLOT_SIZE);

        int circuitX = ATTACHMENT_X + (SLOT_SIZE * 2) + 2;
        int circuitY = attachmentY - SLOT_SIZE;

        // Render Attachment Blocks
        for (RecipeAttachment attachment : recipe.getAttachments()) {
            // Render Mixer
            if (attachment instanceof MixerAttachment mixerAttachment) {
                TFGEmiPlugin.createBasinlessMixerWidget(holder, EmiIngredient.of(getAttachment(mixerAttachment)), ATTACHMENT_X, attachmentY + 8, 0, true);
                if (mixerAttachment.minRpm() != null) {
                    rpm = mixerAttachment.minRpm();
                }
                if (mixerAttachment.circuit() != null) {
                    circuit = mixerAttachment.circuit();
                }
                if (circuit > 0) {
                    TFGEmiPlugin.createCircuitNumberWidget(holder, circuit, circuitX, circuitY);
                }
            }
        }

        // Render Catalyst slot
        int catalystX = inputFluidX + (SLOT_SIZE * 3) + 1;
        int catalystY = inputFluidY - 6;
        if (!recipe.getCatalyst().isEmpty()) {
            holder.addSlot(EmiIngredient.of(recipe.getCatalyst()).setChance(recipe.getCatalystChance()), catalystX, catalystY);
        }

        // Render Tool slot
        int toolX = catalystX;
        int toolY = catalystY + SLOT_SIZE;
        if (!recipe.getTool().isEmpty()) {
            holder.addSlot(EmiIngredient.of(recipe.getTool()), toolX, toolY);
        }

        // Progress Bar
        int pbX = catalystX - 4;
        int pbY = catalystY - 11;

        float seconds = (float) recipe.getDuration() / 20.0f;
        holder.addTexture(PB_BACKGROUND, pbX, pbY);
        if (seconds <= 0) {
            holder.addTexture(PB_FOREGROUND, pbX, pbY);
        } else {
            holder.addAnimatedTexture(PB_FOREGROUND, pbX, pbY, (int) (seconds * 1000), true, false, false);
        }

        // Output Items
        int outputItemX = catalystX + 28;
        int outputItemY = catalystY - 27;

        int outIdx = 0;
        for (var provider : recipe.getItemOutputs()) {
            ItemStack stack = provider.getEmptyStack();
            if (!stack.isEmpty()) {
                int col = outIdx % 3;
                int row = outIdx / 3;
                holder.addSlot(EmiStack.of(stack), outputItemX + col * SLOT_SIZE, outputItemY + row * SLOT_SIZE).recipeContext(this);
                outIdx++;
            }
        }

        // Output Fluids
        int outputFluidX = outputItemX;
        int outputFluidY = outputItemY + SLOT_SIZE + 4;

        int fluidOutIdx = 0;
        for (FluidStack fs : recipe.getFluidOutputs()) {
            if (!fs.isEmpty()) {
                holder.addSlot(EmiStack.of(fs.getFluid(), fs.getTag(), fs.getAmount()), outputFluidX + fluidOutIdx * SLOT_SIZE, outputFluidY).recipeContext(this);
                fluidOutIdx++;
            }
        }

        // Text
        int textY = ATTACHMENT_Y + SLOT_SIZE + 56;
        int heatSourceX = ATTACHMENT_X + 7;

        Component timeText = TFGHelpers.formatTime(recipe.getDuration());
        holder.add(new TextWidget(timeText.getVisualOrderText(), inputItemX + SLOT_SIZE + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(timeText), textY, 0xFFFFFF, true));

        if (rpm > 0) {
            Component rpmText = Component.translatable("tfg.tooltip.unit.min_rpm", Component.literal(String.format("%.0f", rpm)).withStyle(ChatFormatting.GOLD));
            holder.add(new TextWidget(rpmText.getVisualOrderText(), heatSourceX + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(rpmText), textY, 0xFFFFFF, true));
        }
    }

    /**
     * Gets the attachment ingredients for the recipe.
     * @param attachment RecipeAttachment from active recipe.
     * @return Ingredient forms of the attachment.
     */
    public static Ingredient getAttachment(RecipeAttachment attachment) {
        if (attachment instanceof MixerAttachment) {
            return Ingredient.of(MechanicalMixers.MECHANICAL_MIXERS);
        }
        return Ingredient.EMPTY;
    }
}
