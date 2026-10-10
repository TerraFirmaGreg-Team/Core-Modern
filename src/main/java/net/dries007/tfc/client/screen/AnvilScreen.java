/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.screen.button.AnvilPlanButton;
import net.dries007.tfc.client.screen.button.AnvilStepButton;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.container.AnvilContainer;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;

@SuppressWarnings("unused")
public class AnvilScreen extends BlockEntityScreen<AnvilBlockEntity, AnvilContainer>
{
    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/anvil.png");

    public static void drawRule(GuiGraphics graphics, ForgeRule rule, int x, int y, @Nullable ForgeSteps steps)
    {
        // The rule icon
        drawRuleIcon(graphics, rule, x + 5, y + 3);

        // The overlay
        if (steps != null)
        {
            if (rule.matches(steps))
            {
                RenderSystem.setShaderColor(0f, 0.6f, 0.2f, 1f); // Green
            }
            else
            {
                RenderSystem.setShaderColor(1f, 0.4f, 0, 1f); // Red
            }
        }

        graphics.blit(BACKGROUND, x, y, 198, rule.overlayY(), 20, 22);

        if (steps != null)
        {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    public static void drawRule(GuiGraphics graphics, ForgeRule rule, int x, int y)
    {
        drawRule(graphics, rule, x, y, null);
    }

    public static void drawRules(GuiGraphics graphics, ForgeRule[] rules, int x, int y, @Nullable ForgeSteps steps)
    {
        for (int i = 0; i < rules.length; i++)
        {
            final ForgeRule rule = rules[i];
            if (rule != null)
            {
                drawRule(graphics, rule, x + i * 19, y, steps);
            }
        }
    }

    public static void drawRules(GuiGraphics graphics, ForgeRule[] rules, int x, int y)
    {
        drawRules(graphics, rules, x, y, null);
    }

    public static void drawRuleIcon(GuiGraphics graphics, ForgeRule rule, int x, int y)
    {
        graphics.blit(BACKGROUND, x, y, 10, 10, rule.iconX(), rule.iconY(), 32, 32, 256, 256);
    }

    public static void drawStepIcon(GuiGraphics graphics, ForgeStep step, int x, int y)
    {
        graphics.blit(BACKGROUND, x, y, 10, 10, step.iconX(), step.iconY(), 32, 32, 256, 256);
    }

    public static @Nullable ForgeRule getHoveredRule(ForgeRule[] rules, int x, int y, double mouseX, double mouseY)
    {
        for (int i = 0; i < rules.length; i++)
        {
            final ForgeRule rule = rules[i];
            if (rule != null)
            {
                final int ruleX = x + i * 19;
                if (mouseX >= ruleX && mouseX < ruleX + 20 && mouseY >= y && mouseY < y + 22)
                {
                    return rule;
                }
            }
        }
        return null;
    }

    public AnvilScreen(AnvilContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);

        inventoryLabelY += 41;
        imageHeight += 41;
    }

    @Override
    protected void init()
    {
        super.init();

        addRenderableWidget(new AnvilPlanButton(blockEntity, getGuiLeft(), getGuiTop()));

        for (ForgeStep step : ForgeStep.VALUES)
        {
            addRenderableWidget(new AnvilStepButton(step, getGuiLeft(), getGuiTop()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        final Level level = blockEntity.getLevel();
        final int guiLeft = getGuiLeft(), guiTop = getGuiTop();

		graphics.blit(texture, guiLeft + 26, guiTop + 24, 0, 207, 9, 14);

        assert level != null;

        // Draw rule icons
        final @Nullable Forging forging = blockEntity.getMainInputForging();
        if (forging != null)
        {
            // Draw the progress indicators
            final int progress = forging.getWork();
            graphics.blit(texture, guiLeft + 13 + progress, guiTop + 100, 176, 0, 5, 5);

            final int target = forging.getWorkTarget();
            graphics.blit(texture, guiLeft + 13 + target, guiTop + 94, 181, 0, 5, 5);

            final ForgeSteps steps = forging.getSteps();
            final AnvilRecipe recipe = forging.getRecipe(level);
            if (recipe != null)
            {
                drawRules(graphics, recipe.getRules(), guiLeft + 59, guiTop + 7, steps);
            }

            // Draw step icons
            final ForgeStep[] stepSequence = {steps.last(), steps.secondLast(), steps.thirdLast()};
            for (int i = 0; i < 3; i++)
            {
                final ForgeStep step = stepSequence[i];
                if (step != null)
                {
                    drawStepIcon(graphics, step, guiLeft + 64 + i * 19, guiTop + 31);
                }
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

        final Level level = blockEntity.getLevel();
        final @Nullable Forging forging = blockEntity.getMainInputForging();
        if (forging != null && level != null)
        {
            final AnvilRecipe recipe = forging.getRecipe(level);
            if (recipe != null)
            {
                final ForgeRule rule = getHoveredRule(recipe.getRules(), getGuiLeft() + 59, getGuiTop() + 7, mouseX, mouseY);
                if (rule != null)
                {
                    graphics.renderTooltip(font, rule.getDescriptionId(), mouseX, mouseY);
                }
            }
        }
    }
}
