package su.terrafirmagreg.core.compat.emi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.common.data.recipes.ArtisanPattern;
import su.terrafirmagreg.core.common.data.recipes.ArtisanRecipe;
import su.terrafirmagreg.core.common.data.recipes.ArtisanType;

/**
 * EMI recipe wrapper for the Artisan Table.
 */
public class ArtisanTableEmiRecipe implements EmiRecipe {

    private final List<EmiIngredient> tools;
    private final List<EmiStack> items;

    private final ArtisanRecipe recipe;

    private final ArtisanType type;
    private final ArtisanPattern pattern;

    /**
     * Constructs an EMI recipe for the Artisan Table from the given ArtisanRecipe.
     * @param recipe The ArtisanRecipe to wrap.
     */
    public ArtisanTableEmiRecipe(ArtisanRecipe recipe) {
        this.recipe = recipe;
        tools = recipe.getTools().stream().map(EmiIngredient::of).toList();
        assert recipe.getIngredient() != null;
        items = Arrays.stream(recipe.getIngredient().getItems()).map(EmiStack::of).toList();

        type = recipe.getArtisanType();
        pattern = recipe.getPattern();
    }

    /**
     * @return The EMI recipe category.
     */
    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ARTISAN_TABLE;
    }

    /**
     * @return The recipe ID ResourceLocation.
     */
    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId().withPath(recipe.getId().getPath());
    }

    /**
     * @return The list of EMI ingredients.
     */
    @Override
    public List<EmiIngredient> getInputs() {
        return Stream.concat(items.stream(), tools.stream()).toList();
    }

    /**
     * @return The list of EMI output stacks.
     */
    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getResult()));
    }

    /**
     * @return The display width in pixels.
     */
    @Override
    public int getDisplayWidth() {
        return 140;
    }

    /**
     * @return The display height in pixels.
     */
    @Override
    public int getDisplayHeight() {
        return 96;
    }

    /**
     * @param holder The widget holder.
     */
    @Override
    public void addWidgets(WidgetHolder holder) {
        int xPos = 92;
        int yPos = 10;
        int xDiff = 21;
        int yDiff = 21;

        ArrayList<EmiIngredient> stdInputs = new ArrayList<>(this.getInputs());
        if (stdInputs.size() < 4)
            stdInputs.add(1, EmiStack.EMPTY);

        for (EmiIngredient input : stdInputs) {
            if (yPos == (10 + yDiff * 2)) {
                yPos = 10;
                xPos += xDiff;
            }

            holder.addSlot(input, xPos, yPos);
            yPos += yDiff;
        }
        holder.addSlot(this.getOutputs().get(0), xPos - xDiff + 11, yPos + 5).recipeContext(this);

        displayPattern(holder);

        // Add border texture over all widgets, with translucency.
        ResourceLocation borderTexture = type.getBorderTexture();
        if (borderTexture != null) {
            int borderX = 3;
            int borderY = 5;
            int borderW = 82;
            int borderH = 82;
            TextureWidget borderWidget = new TextureWidget(borderTexture, borderX, borderY, borderW, borderH, 0, 0, borderW, borderH, borderW, borderH) {
                @Override
                public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
                    super.render(graphics, mouseX, mouseY, delta);
                    graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                }
            };
            holder.add(borderWidget);
        }
    }

    /**
     * Renders the artisan pattern as a grid of textures.
     * @param holder The widget holder.
     */
    private void displayPattern(WidgetHolder holder) {
        long patternData = pattern.getData();
        int patternWidth = pattern.getWidth();
        int patternHeight = pattern.getHeight();

        ResourceLocation activeTexture = type.getActiveTexture();
        ResourceLocation inactiveTexture = type.getInactiveTexture();

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(patternData);

        String patternString = buildPatternString(buffer, patternWidth, patternHeight);

        int xPos = 8;
        int yPos = 10;
        int imgSize = 12;

        for (char bit : patternString.toCharArray()) {
            if (xPos == 8 + imgSize * patternWidth) {
                xPos = 8;
                yPos += imgSize;
            }

            switch (bit) {
                case '1' ->
                    holder.addTexture(activeTexture, xPos, yPos, imgSize, imgSize, 0, 0, imgSize, imgSize, imgSize, imgSize);
                case '0' -> {
                    if (inactiveTexture != null)
                        holder.addTexture(inactiveTexture, xPos, yPos, imgSize, imgSize, 0, 0, imgSize, imgSize, imgSize, imgSize);
                }
            }
            xPos += imgSize;
        }
    }

    /**
     * Builds a string representation of the recipe pattern from the data.
     * @param buffer The ByteBuffer containing pattern data.
     * @param patternWidth The width of the pattern.
     * @param patternHeight The height of the pattern.
     * @return The binary string representing the pattern.
     */
    private static @NotNull String buildPatternString(ByteBuffer buffer, int patternWidth, int patternHeight) {
        StringBuilder builder = new StringBuilder();
        for (byte b : buffer.array()) {
            //Forces it to be unsigned for use in this weird method
            String binaryString = Integer.toBinaryString(b & 0xFF);
            //Converts the byte to 8 chars, String.format is goofy
            String paddedBinaryString = String.format("%8s", binaryString).replace(' ', '0');
            builder.append(paddedBinaryString);
        }

        int unusedBits = 64 - patternWidth * patternHeight;
        builder.delete(0, unusedBits);
        builder.reverse();
        return builder.toString();
    }
}
