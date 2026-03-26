package su.terrafirmagreg.core.common.recipe.repair;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Small helpers for item repair: tool tags, repair ingredients, metal ingots, applying durability loss. */
public class RepairRecipeHelper {

    private static final List<TagKey<Item>> TOOL_ITEM_TAGS = buildToolItemTags();

    private static List<TagKey<Item>> buildToolItemTags() {
        String[] ids = {
                "tfc:shears",
                "forge:tools",
                "forge:tools/wrenches",
                "forge:tools/screwdrivers",
                "forge:tools/hammers",
                "forge:tools/crowbars",
                "forge:tools/files",
                "forge:tools/wire_cutters",
                "forge:tools/butchery_knives",
                "forge:tools/plungers",
                "forge:tools/mortars",
                "forge:tools/mallets",
                "forge:tools/chainsaws",
                "forge:tools/buzzsaws",
                "forge:tools/drills",
                "forge:tools/fishing_nets"
        };
        List<TagKey<Item>> list = new ArrayList<>(ids.length);
        for (String id : ids) {
            int colon = id.indexOf(':');
            list.add(ItemTags.create(ResourceLocation.fromNamespaceAndPath(id.substring(0, colon), id.substring(colon + 1))));
        }
        return List.copyOf(list);
    }

    /** True if the stack matches common tool tags (hammers, shears, forge:tools, and related). */
    public static boolean isToolItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (TagKey<Item> tag : TOOL_ITEM_TAGS) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /** True if the ingredient is non-empty and accepts this stack. */
    public static boolean isRepairMaterial(ItemStack stack, @Nullable Ingredient repairIngredient) {
        return repairIngredient != null && repairIngredient.test(stack);
    }

    /** Copy of the stack with extra damage applied, capped at max damage. */
    public static ItemStack damageToolItem(ItemStack tool, int damage) {
        if (tool.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack damagedTool = tool.copy();
        int newDamage = Math.min(damagedTool.getMaxDamage(), damagedTool.getDamageValue() + damage);
        damagedTool.setDamageValue(newDamage);
        return damagedTool;
    }

}
