package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.gson.stream.JsonWriter;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

public class DebugRecipeDump {

    @SuppressWarnings("removal")
    private static final List<TagKey<Item>> PIPE_TAGS = List.of(
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tiny_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "small_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "normal_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "large_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "huge_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "quadruple_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "nonuple_fluid_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "small_item_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "normal_item_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "large_item_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "huge_item_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "small_restrictive_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "normal_restrictive_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "large_restrictive_pipes")),
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "huge_restrictive_pipes")));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("dump_recipes")
                .executes(c -> dumpRecipes(c.getSource(), false)));
        debug.then(literal("dump_recipes_detailed")
                .executes(c -> dumpRecipes(c.getSource(), true)));
        debug.then(literal("dump_pipe_recipes")
                .executes(c -> dumpPipeRecipes(c.getSource())));

        // /tfg debug dump_recipes_where id_starts_with <"prefix">
        // /tfg debug dump_recipes_where has_input <"#forge:tag" | "mod:item_id">
        // /tfg debug dump_recipes_where id_starts_with <"prefix"> has_input <"#forge:tag" | "mod:item_id">
        debug.then(literal("dump_recipes_where")
                .then(literal("id_starts_with")
                        .then(argument("prefix", StringArgumentType.string())
                                .executes(c -> dumpWhere(c.getSource(),
                                        StringArgumentType.getString(c, "prefix"), null))
                                .then(literal("has_input")
                                        .then(argument("input_filter", StringArgumentType.string())
                                                .executes(c -> dumpWhere(c.getSource(),
                                                        StringArgumentType.getString(c, "prefix"),
                                                        StringArgumentType.getString(c, "input_filter")))))))
                .then(literal("has_input")
                        .then(argument("input_filter", StringArgumentType.string())
                                .executes(c -> dumpWhere(c.getSource(),
                                        null, StringArgumentType.getString(c, "input_filter"))))));
    }

    @SuppressWarnings("removal")
    private static int dumpWhere(CommandSourceStack source, String prefix, String inputFilter) {
        var registryAccess = source.getServer().registryAccess();

        Predicate<Recipe<?>> predicate = r -> true;
        var descParts = new ArrayList<String>();

        if (prefix != null) {
            predicate = predicate.and(r -> r.getId().toString().startsWith(prefix));
            descParts.add("id starts with \"" + prefix + "\"");
        }

        if (inputFilter != null) {
            var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);
            Set<Item> matchItems = new HashSet<>();

            if (inputFilter.startsWith("#")) {
                String[] parts = inputFilter.substring(1).split(":", 2);
                if (parts.length != 2) {
                    source.sendFailure(Component.literal("Tag must be in namespace:path format, e.g. \"#forge:small_fluid_pipes\""));
                    return 0;
                }
                var tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(parts[0], parts[1]));
                itemRegistry.getTagOrEmpty(tagKey).forEach(h -> matchItems.add(h.value()));
                if (matchItems.isEmpty()) {
                    source.sendFailure(Component.literal("No items found for tag " + inputFilter));
                    return 0;
                }
            } else {
                String[] parts = inputFilter.split(":", 2);
                if (parts.length != 2) {
                    source.sendFailure(Component.literal("Item/tag must be in namespace:path format, e.g. \"gtceu:copper_cable\" or \"#forge:small_fluid_pipes\""));
                    return 0;
                }
                var item = itemRegistry.get(new ResourceLocation(parts[0], parts[1]));
                if (item == null) {
                    source.sendFailure(Component.literal("Unknown item: " + inputFilter));
                    return 0;
                }
                matchItems.add(item);
            }

            predicate = predicate.and(r -> recipeHasInput(r, matchItems));
            descParts.add("has input " + inputFilter);
        }

        String desc = String.join(" AND ", descParts);
        List<Recipe<?>> sorted = new ArrayList<>(source.getServer().getRecipeManager().getRecipes());
        sorted.sort(Comparator.comparing(r -> r.getId().toString()));

        Path out = Path.of("recipe_dump_filtered.json");
        int count = 0;

        try (BufferedWriter bw = Files.newBufferedWriter(out);
                JsonWriter jw = new JsonWriter(bw)) {

            jw.setIndent("  ");
            jw.beginArray();
            for (Recipe<?> r : sorted) {
                if (!predicate.test(r))
                    continue;
                writeDetailedRecipe(jw, r, registryAccess);
                count++;
            }
            jw.endArray();
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write recipe dump: " + e.getMessage()));
            return 0;
        }

        final int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Dumped " + finalCount + " recipes where " + desc + " to " + out.toAbsolutePath()), true);
        return finalCount;
    }

    private static int dumpRecipes(CommandSourceStack source, boolean detailed) {
        var registryAccess = source.getServer().registryAccess();

        List<Recipe<?>> sorted = new ArrayList<>(source.getServer().getRecipeManager().getRecipes());
        sorted.sort(Comparator.comparing(r -> r.getId().toString()));

        Path out = Path.of(detailed ? "recipe_dump_detailed.json" : "recipe_dump.json");

        try (BufferedWriter bw = Files.newBufferedWriter(out);
                JsonWriter jw = new JsonWriter(bw)) {

            jw.setIndent("  ");
            jw.beginArray();

            if (detailed) {
                for (Recipe<?> r : sorted) {
                    writeDetailedRecipe(jw, r, registryAccess);
                }
            } else {
                for (Recipe<?> r : sorted) {
                    jw.value(r.getId().toString());
                }
            }

            jw.endArray();
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write recipe dump: " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "Dumped " + sorted.size() + " recipes to " + out.toAbsolutePath()), true);
        return sorted.size();
    }

    private static int dumpPipeRecipes(CommandSourceStack source) {
        var registryAccess = source.getServer().registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);

        Set<Item> pipeItems = new HashSet<>();
        for (var tagKey : PIPE_TAGS) {
            itemRegistry.getTagOrEmpty(tagKey).forEach(holder -> pipeItems.add(holder.value()));
        }

        if (pipeItems.isEmpty()) {
            source.sendFailure(Component.literal("No pipe items found"));
            return 0;
        }

        List<Recipe<?>> sorted = new ArrayList<>(source.getServer().getRecipeManager().getRecipes());
        sorted.sort(Comparator.comparing(r -> r.getId().toString()));

        Path out = Path.of("recipe_dump_pipes.json");
        int count = 0;

        try (BufferedWriter bw = Files.newBufferedWriter(out);
                JsonWriter jw = new JsonWriter(bw)) {

            jw.setIndent("  ");
            jw.beginArray();

            for (Recipe<?> r : sorted) {
                if (!recipeHasInput(r, pipeItems))
                    continue;
                if (r.getId().toString().startsWith("greate:milling"))
                    continue;

                writeDetailedRecipe(jw, r, registryAccess);
                count++;
            }

            jw.endArray();
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write pipe recipe dump: " + e.getMessage()));
            return 0;
        }

        final int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Dumped " + finalCount + " pipe recipes (" + pipeItems.size() + " pipe item types) to " + out.toAbsolutePath()), true);
        return finalCount;
    }

    private static boolean recipeHasInput(Recipe<?> r, Set<Item> matchItems) {
        if (r instanceof GTRecipe gtRecipe) {
            var contents = gtRecipe.inputs.get(ItemRecipeCapability.CAP);
            if (contents != null) {
                for (var content : contents) {
                    if (content.content instanceof SizedIngredient si) {
                        for (var stack : si.getItems())
                            if (matchItems.contains(stack.getItem()))
                                return true;
                    } else if (content.content instanceof Ingredient ing) {
                        for (var stack : ing.getItems())
                            if (matchItems.contains(stack.getItem()))
                                return true;
                    }
                }
            }
            return false;
        }
        for (Ingredient ingredient : r.getIngredients())
            for (var stack : ingredient.getItems())
                if (matchItems.contains(stack.getItem()))
                    return true;
        return false;
    }

    private static void writeDetailedRecipe(JsonWriter jw, Recipe<?> r, net.minecraft.core.RegistryAccess registryAccess) throws IOException {
        jw.beginObject();
        jw.name("id").value(r.getId().toString());
        jw.name("type").value(r.getType().toString());

        if (r instanceof GTRecipe gtRecipe) {
            var outputContents = gtRecipe.outputs.get(ItemRecipeCapability.CAP);
            var inputContents = gtRecipe.inputs.get(ItemRecipeCapability.CAP);

            if (outputContents == null || outputContents.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (inputContents == null || inputContents.isEmpty())
                jw.name("warning_no_inputs").value(true);

            jw.name("outputs").beginArray();
            if (outputContents != null) {
                for (var content : outputContents) {
                    if (content.content instanceof SizedIngredient si) {
                        for (var stack : si.getItems())
                            jw.value(stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    } else if (content.content instanceof Ingredient ing) {
                        for (var stack : ing.getItems())
                            jw.value(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    }
                }
            }
            jw.endArray();

            jw.name("ingredients").beginArray();
            if (inputContents != null) {
                for (var content : inputContents) {
                    jw.beginArray();
                    if (content.content instanceof SizedIngredient si) {
                        for (var stack : si.getItems())
                            jw.value(stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    } else if (content.content instanceof Ingredient ing) {
                        for (var stack : ing.getItems())
                            jw.value(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    }
                    jw.endArray();
                }
            }
            jw.endArray();
        } else {
            var output = r.getResultItem(registryAccess);
            var ingredients = r.getIngredients();

            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (ingredients.isEmpty())
                jw.name("warning_no_inputs").value(true);

            jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            for (var ingredient : ingredients) {
                jw.beginArray();
                for (var stack : ingredient.getItems()) {
                    jw.value(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                }
                jw.endArray();
            }
            jw.endArray();
        }

        jw.endObject();
    }
}
