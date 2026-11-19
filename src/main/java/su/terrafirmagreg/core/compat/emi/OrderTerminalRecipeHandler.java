package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.menu.me.common.GridInventoryEntry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import su.terrafirmagreg.core.compat.ae2.terminal.OrderTerminalMenu;

public class OrderTerminalRecipeHandler implements EmiRecipeHandler<OrderTerminalMenu> {

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<OrderTerminalMenu> screen) {
        var stacks = new ArrayList<EmiStack>();
        var repo = screen.getMenu().getClientRepo();
        if (repo != null) {
            for (var entry : repo.getAllEntries()) {
                if (entry.getStoredAmount() > 0) {
                    var stack = EmiStackHelper.toEmiStack(new GenericStack(entry.getWhat(), entry.getStoredAmount()));
                    if (stack != null) {
                        stacks.add(stack);
                    }
                }
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return !recipe.getInputs().isEmpty();
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<OrderTerminalMenu> context) {
        var menu = context.getScreenHandler();
        return menu.hasSelectedDestination() && !resolve(recipe, menu, 1, Screen.hasControlDown()).isEmpty();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<OrderTerminalMenu> context) {
        var menu = context.getScreenHandler();
        boolean craftMissing = Screen.hasControlDown();
        var requests = resolve(recipe, menu, context.getAmount(), craftMissing);
        if (requests.isEmpty()) {
            return false;
        }
        menu.orderBatch(new OrderTerminalMenu.OrderBatch(requests));
        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }

    private static List<OrderTerminalMenu.OrderRequest> resolve(EmiRecipe recipe, OrderTerminalMenu menu, int multiplier, boolean craftMissing) {
        var repo = menu.getClientRepo();
        if (repo == null) {
            return List.of();
        }
        var entries = new HashMap<AEKey, GridInventoryEntry>();
        for (var entry : repo.getAllEntries()) {
            entries.put(entry.getWhat(), entry);
        }
        var amounts = new LinkedHashMap<Long, Long>();
        for (var ingredient : recipe.getInputs()) {
            // gtm marks NC stacks with chance 0
            if (ingredient.isEmpty() || ingredient.getChance() <= 0 || ingredient.getEmiStacks().get(0).getChance() <= 0) {
                continue;
            }
            var match = findMatch(ingredient, entries);
            if (match != null && (match.entry.getStoredAmount() > 0 || craftMissing)) {
                amounts.merge(match.entry.getSerial(), match.amount * multiplier, Long::sum);
            }
        }
        var requests = new ArrayList<OrderTerminalMenu.OrderRequest>();
        for (Map.Entry<Long, Long> amount : amounts.entrySet()) {
            requests.add(new OrderTerminalMenu.OrderRequest(amount.getKey(), amount.getValue(), craftMissing));
        }
        return requests;
    }

    private record Match(GridInventoryEntry entry, long amount) {
    }

    @Nullable
    private static Match findMatch(EmiIngredient ingredient, Map<AEKey, GridInventoryEntry> entries) {
        long needed = Math.max(1, ingredient.getAmount());
        Match best = null;
        for (var alternative : ingredient.getEmiStacks()) {
            var stack = EmiStackHelper.toGenericStack(alternative);
            if (stack == null) {
                continue;
            }
            var entry = entries.get(stack.what());
            if (entry == null) {
                continue;
            }
            if (entry.getStoredAmount() >= needed) {
                return new Match(entry, needed);
            }
            if (best == null || (best.entry.getStoredAmount() == 0 && entry.getStoredAmount() > 0)) {
                best = new Match(entry, needed);
            }
        }
        return best;
    }
}
