package su.terrafirmagreg.core.utils.commands;

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

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

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
    }

    private static int dumpRecipes(CommandSourceStack source, boolean detailed) {
        var registryAccess = source.getServer().registryAccess();
        RecipeManager rm = source.getServer().getRecipeManager();

        List<Recipe<?>> sorted = new ArrayList<>(rm.getRecipes());
        sorted.sort(Comparator.comparing(r -> r.getId().toString()));

        Path out = Path.of(detailed ? "recipe_dump_detailed.json" : "recipe_dump.json");

        try (BufferedWriter bw = Files.newBufferedWriter(out);
                JsonWriter jw = new JsonWriter(bw)) {

            jw.setIndent("  ");
            jw.beginArray();

            if (detailed) {
                for (Recipe<?> r : sorted) {
                    jw.beginObject();
                    jw.name("id").value(r.getId().toString());
                    jw.name("type").value(r.getType().toString());
                    jw.name("output").value(r.getResultItem(registryAccess).toString());
                    jw.name("ingredients").beginArray();
                    for (var ingredient : r.getIngredients()) {
                        jw.beginArray();
                        for (var stack : ingredient.getItems()) {
                            jw.value(stack.getItem().toString());
                        }
                        jw.endArray();
                    }
                    jw.endArray();
                    jw.endObject();
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
                boolean hasPipeInput = false;
                outer: for (var ingredient : r.getIngredients()) {
                    for (var stack : ingredient.getItems()) {
                        if (pipeItems.contains(stack.getItem())) {
                            hasPipeInput = true;
                            break outer;
                        }
                    }
                }
                if (!hasPipeInput)
                    continue;

                jw.beginObject();
                jw.name("id").value(r.getId().toString());
                jw.name("type").value(r.getType().toString());
                jw.name("output").value(r.getResultItem(registryAccess).toString());
                jw.name("ingredients").beginArray();
                for (var ingredient : r.getIngredients()) {
                    jw.beginArray();
                    for (var stack : ingredient.getItems()) {
                        jw.value(stack.getItem().toString());
                    }
                    jw.endArray();
                }
                jw.endArray();
                jw.endObject();
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
}
