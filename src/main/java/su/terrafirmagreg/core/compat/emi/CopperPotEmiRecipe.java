package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.eerussianguy.beneath.common.blocks.BeneathBlocks;
import com.eerussianguy.beneath.common.blocks.HellforgeBlock;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.negodya1.vintageimprovements.VintageBlocks;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import electrolyte.greate.registry.MechanicalMixers;
import electrolyte.greate.registry.MechanicalPresses;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.GasBurnerBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.recipe.CopperPotRecipe;
import su.terrafirmagreg.core.common.recipe.attachment.CondenserAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.MixerAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.PressAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.RecipeAttachment;
import su.terrafirmagreg.core.common.recipe.attachment.VacuumChamberAttachment;
import su.terrafirmagreg.core.compat.emi.widgets.CyclingWidget;
import su.terrafirmagreg.core.compat.emi.widgets.EmiBlockWidget;
import su.terrafirmagreg.core.utils.TFGHelpers;

@SuppressWarnings({ "UnnecessaryLocalVariable", "NoTranslation" })
public class CopperPotEmiRecipe implements EmiRecipe {

    private static final ResourceLocation PB = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/copper_pot_progress_bar.png");
    private static final ResourceLocation POT = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/copper_pot_emi.png");

    private static final int WIDTH = 240;
    private static final int HEIGHT = 136;
    private static final int SLOT_SIZE = 18;

    private static final int PB_SIZE = 32;
    private static final int POT_X = 0;
    private static final int POT_Y = 0;
    private static final int ATTACHMENT_X = POT_X + 178;
    private static final int ATTACHMENT_Y = 30;

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
            POT,
            0, 0,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT,
            WIDTH, HEIGHT);

    @Getter
    private final CopperPotRecipe recipe;
    private final List<EmiIngredient> itemInputs;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public CopperPotEmiRecipe(CopperPotRecipe recipe) {
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
            this.inputs.add(EmiIngredient.of(recipe.getCatalyst()));
        }
        // Tool
        if (!recipe.getTool().isEmpty()) {
            this.inputs.add(EmiIngredient.of(recipe.getTool()));
        }

        // Attachments
        this.catalysts = new ArrayList<>();
        for (RecipeAttachment attachment : recipe.getAttachments()) {
            Ingredient ingredient = getAttachment(attachment);
            if (!ingredient.isEmpty()) {
                this.catalysts.add(EmiIngredient.of(ingredient));
            }

            // Condenser Coolant Input
            if (attachment instanceof CondenserAttachment condenserAttachment) {
                var coolant = condenserAttachment.coolantCondition();
                if (coolant != null && coolant.ingredient() != null) {
                    var fluids = coolant.ingredient().fluids().stream()
                            .map(fluid -> (EmiIngredient) EmiStack.of(fluid, Math.max(1, coolant.amount())))
                            .toList();
                    if (!fluids.isEmpty()) {
                        this.inputs.add(EmiIngredient.of(fluids));
                    }
                }
            }
        }

        // Heaters
        if (recipe.getMinTemp() > 0) {
            this.catalysts.add(TFGEmiPlugin.heatSources());
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
        for (RecipeAttachment attachment : recipe.getAttachments()) {
            if (attachment instanceof CondenserAttachment condenserAttachment) {
                FluidStack fs = condenserAttachment.fluidOutput();
                if (!fs.isEmpty()) {
                    this.outputs.add(EmiStack.of(fs.getFluid(), fs.getTag(), fs.getAmount()));
                }
            }
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.COPPER_POT;
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

        holder.addTexture(POT_UNDERLAY, POT_X, POT_Y);

        // Render Item Inputs
        int inputItemX = POT_X + 17;
        int inputItemY = POT_Y + 21;
        int inputIdx = 0;
        for (EmiIngredient ing : this.itemInputs) {
            int col = inputIdx % 3;
            int row = inputIdx / 3;
            holder.addSlot(ing, inputItemX + col * SLOT_SIZE, inputItemY + row * SLOT_SIZE).drawBack(false);
            inputIdx++;
        }

        // Render Fluid Inputs
        int inputFluidX = inputItemX;
        int inputFluidY = inputItemY + (SLOT_SIZE * 2) + 3;
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

        // Render Copper Pot Block
        float temp = recipe.getMinTemp();
        float rpm = 0;
        int circuit = 0;
        boolean vacuumPresent = false;
        boolean vacuumMode = false;

        int renderZ = 201;
        int renderScale = 23;
        float renderRotX = -15.5F;
        float renderRotY = 22.5F + 180;
        int attachmentY = ATTACHMENT_Y;

        if (temp <= 0) {
            attachmentY += 21;
        }

        holder.add(new EmiBlockWidget(TFGBlocks.COPPER_POT.getDefaultState(), ATTACHMENT_X + 6, attachmentY + SLOT_SIZE, renderZ, renderScale, renderRotX, renderRotY));
        TFGEmiPlugin.createBlankItemWidget(holder, EmiIngredient.of(Ingredient.of(TFGBlocks.COPPER_POT.asItem())), ATTACHMENT_X, attachmentY + SLOT_SIZE);

        // Heat Sources
        int heatSourceX = ATTACHMENT_X + 7;
        int heatSourceY = attachmentY + 41;
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

        // Render Attachment Blocks
        for (RecipeAttachment attachment : recipe.getAttachments()) {

            // Render Condenser
            int coolantX = ATTACHMENT_X + 6;
            int coolantY = attachmentY - 25;
            int condensedFluidX = ATTACHMENT_X + (SLOT_SIZE * 2) + 2;
            int condensedFluidY = attachmentY + 4;
            if (attachment instanceof CondenserAttachment condenserAttachment) {
                var coolant = condenserAttachment.coolantCondition();
                if (coolant != null && coolant.ingredient() != null) {
                    var fluidStacks = coolant.ingredient().fluids().stream()
                            .map(fluid -> (EmiIngredient) EmiStack.of(fluid, Math.max(1, coolant.amount())))
                            .toList();
                    if (!fluidStacks.isEmpty()) {
                        var slot = holder.addSlot(EmiIngredient.of(fluidStacks), coolantX, coolantY);
                        if (coolant.maxTemp() != null) {
                            slot.appendTooltip(Component.translatable("tfg.tooltip.condenser.coolant_info"));
                            slot.appendTooltip(Component.translatable("tfg.tooltip.condenser.coolant_temp", Component.literal(String.format("%.0f", coolant.maxTemp()))));
                        }
                    }
                }
                FluidStack fs = condenserAttachment.fluidOutput();
                if (!fs.isEmpty()) {
                    var slot = holder.addSlot(EmiStack.of(fs.getFluid(), fs.getTag(), fs.getAmount()), condensedFluidX, condensedFluidY).recipeContext(this);
                    slot.appendTooltip(Component.translatable("tfg.tooltip.condenser.condensate_info"));
                }

                holder.add(new EmiBlockWidget(TFGBlocks.CONDENSER.getDefaultState(), ATTACHMENT_X + 6, attachmentY - 4, renderZ + 10, renderScale, renderRotX, renderRotY - 90));
                TFGEmiPlugin.createBlankItemWidget(holder, EmiIngredient.of(Ingredient.of(TFGBlocks.CONDENSER.asItem())), ATTACHMENT_X, attachmentY - 6);
            }

            // Render Mixer
            if (attachment instanceof MixerAttachment mixerAttachment) {
                TFGEmiPlugin.createBasinlessMixerWidget(holder, EmiIngredient.of(getAttachment(mixerAttachment)), ATTACHMENT_X, attachmentY, 1, true);
                if (mixerAttachment.minRpm() != null) {
                    rpm = mixerAttachment.minRpm();
                }
                if (mixerAttachment.circuit() != null) {
                    circuit = mixerAttachment.circuit();
                }
                if (circuit > 0) {
                    TFGEmiPlugin.createCircuitNumberWidget(holder, circuit, condensedFluidX, condensedFluidY);
                }
            }

            // Render Press
            if (attachment instanceof PressAttachment pressAttachment) {
                TFGEmiPlugin.createBasinlessPressWidget(holder, EmiIngredient.of(getAttachment(pressAttachment)), ATTACHMENT_X, attachmentY, 1, true);
                if (pressAttachment.circuit() != null) {
                    circuit = pressAttachment.circuit();
                }
                TFGEmiPlugin.createCircuitNumberWidget(holder, circuit, condensedFluidX, condensedFluidY);
            }

            // Render Vacuum Compressor
            if (attachment instanceof VacuumChamberAttachment vacuumChamberAttachment) {
                TFGEmiPlugin.createBasinlessVacuumChamberWidget(holder, EmiIngredient.of(getAttachment(vacuumChamberAttachment)), ATTACHMENT_X, attachmentY, vacuumChamberAttachment.mode(), true);
                vacuumMode = vacuumChamberAttachment.mode();
                vacuumPresent = true;
            }
        }

        // Render Catalyst slot
        int catalystX = inputFluidX + (SLOT_SIZE * 3) + 8;
        int catalystY = inputFluidY - 6;
        if (!recipe.getCatalyst().isEmpty()) {
            holder.addSlot(EmiIngredient.of(recipe.getCatalyst()), catalystX, catalystY);
        }

        // Render Tool slot
        int toolX = catalystX;
        int toolY = catalystY + SLOT_SIZE;
        if (!recipe.getTool().isEmpty()) {
            holder.addSlot(EmiIngredient.of(recipe.getTool()), toolX, toolY);
        }

        // Progress Bar
        int pbX = catalystX - 4;
        int pbY = catalystY - 12;

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
        int fontSize = Minecraft.getInstance().font.lineHeight + 2;
        int textY = ATTACHMENT_Y + SLOT_SIZE + 60;

        MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
        if (formattedTemp == null) {
            formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
        }
        Component timeText = TFGHelpers.formatTime(recipe.getDuration());
        holder.add(new TextWidget(timeText.getVisualOrderText(), inputItemX + SLOT_SIZE + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(timeText), textY, 0xFFFFFF, true));
        if (temp > 0) {
            Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp != null ? formattedTemp : Component.empty());
            holder.add(new TextWidget(tempText.getVisualOrderText(), heatSourceX + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(tempText), textY, 0xFFFFFF, true));
            textY += fontSize;
        }
        if (rpm > 0) {
            Component rpmText = Component.translatable("tfg.tooltip.unit.min_rpm", Component.literal(String.format("%.0f", rpm)).withStyle(ChatFormatting.GOLD));
            holder.add(new TextWidget(rpmText.getVisualOrderText(), heatSourceX + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(rpmText), textY, 0xFFFFFF, true));
            textY += fontSize;
        }
        if (vacuumPresent) {
            Component vacuumText = Component.translatable("tfg.tooltip.compressor.vacuum_mode").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component pressureText = Component.translatable("tfg.tooltip.compressor.pressure_mode").withStyle(ChatFormatting.AQUA);
            Component compressorText = vacuumMode ? pressureText : vacuumText;
            holder.add(new TextWidget(compressorText.getVisualOrderText(), heatSourceX + (SLOT_SIZE / 2) - TFGEmiPlugin.centeredTextOffset(compressorText), textY, 0xFFFFFF, true));
        }

    }

    /**
     * Gets the attachment ingredients for the given recipe.
     * @param attachment RecipeAttachment from active recipe.
     * @return Ingredient forms of the attachment.
     */
    public static Ingredient getAttachment(RecipeAttachment attachment) {
        if (attachment instanceof CondenserAttachment) {
            return Ingredient.of(TFGBlocks.CONDENSER.get());
        }
        if (attachment instanceof MixerAttachment) {
            return Ingredient.of(MechanicalMixers.MECHANICAL_MIXERS);
        }
        if (attachment instanceof PressAttachment) {
            return Ingredient.of(MechanicalPresses.MECHANICAL_PRESSES);
        }
        if (attachment instanceof VacuumChamberAttachment) {
            return Ingredient.of(VintageBlocks.VACUUM_CHAMBER.get());
        }
        return Ingredient.EMPTY;
    }
}
