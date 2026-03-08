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

import com.eerussianguy.firmalife.common.recipes.MixingBowlRecipe;
import com.eerussianguy.firmalife.common.recipes.OvenRecipe;
import com.eerussianguy.firmalife.common.recipes.VatRecipe;
import com.google.gson.stream.JsonWriter;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.therighthon.rnr.common.recipe.BlockModRecipe;
import com.therighthon.rnr.common.recipe.MattockRecipe;

import net.dries007.tfc.common.recipes.*;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.ingredients.ItemStackIngredient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import xfacthd.framedblocks.common.crafting.FramingSawRecipe;

public class DebugRecipeDump {

    private static final Set<String> RECYCLING_TYPES = Set.of(
            "greate:milling",
            "gtceu:macerator",
            "gtceu:arc_furnace");

    private static final Set<String> PIPE_EXCLUDE_TYPES = Set.of(
            "greate:milling",
            "gtceu:macerator",
            "gtceu:arc_furnace",
            "gtceu:extractor",
            "gtceu:packer");

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
                .executes(c -> dumpPipeRecipes(c.getSource(), false))
                .then(literal("no_recycling")
                        .executes(c -> dumpPipeRecipes(c.getSource(), true))));

        // /tfg debug dump_recipes_where id_starts_with <"prefix">
        // /tfg debug dump_recipes_where has_input <"#mod:tag" | "mod:item_id"> [no_recycling]
        // /tfg debug dump_recipes_where id_starts_with <"prefix"> has_input <"#mod:tag" | "mod:item_id">
        debug.then(literal("dump_recipes_where")
                .then(literal("id_starts_with")
                        .then(argument("prefix", StringArgumentType.string())
                                .executes(c -> dumpWhere(c.getSource(),
                                        StringArgumentType.getString(c, "prefix"), null, false))
                                .then(literal("has_input")
                                        .then(argument("input_filter", StringArgumentType.string())
                                                .executes(c -> dumpWhere(c.getSource(),
                                                        StringArgumentType.getString(c, "prefix"),
                                                        StringArgumentType.getString(c, "input_filter"), false))))))
                .then(literal("has_input")
                        .then(argument("input_filter", StringArgumentType.string())
                                .executes(c -> dumpWhere(c.getSource(),
                                        null, StringArgumentType.getString(c, "input_filter"), false))
                                .then(literal("no_recycling")
                                        .executes(c -> dumpWhere(c.getSource(),
                                                null, StringArgumentType.getString(c, "input_filter"), true))))));
    }

    @SuppressWarnings("removal")
    private static int dumpWhere(CommandSourceStack source, String prefix, String inputFilter, boolean noRecycling) {
        var registryAccess = source.getServer().registryAccess();

        Predicate<Recipe<?>> predicate = r -> true;
        var descParts = new ArrayList<String>();

        if (prefix != null) {
            predicate = predicate.and(r -> r.getId().toString().startsWith(prefix));
            descParts.add("id starts with \"" + prefix + "\"");
        }

        if (inputFilter != null) {
            var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);
            var fluidRegistry = registryAccess.registryOrThrow(net.minecraftforge.registries.ForgeRegistries.Keys.FLUIDS);
            Set<Item> matchItems = new HashSet<>();
            Set<net.minecraft.world.level.material.Fluid> matchFluids = new HashSet<>();

            if (inputFilter.startsWith("#")) {
                String[] parts = inputFilter.substring(1).split(":", 2);
                if (parts.length != 2) {
                    source.sendFailure(Component.literal("Tag must be in namespace:path format, e.g. \"#forge:small_fluid_pipes\""));
                    return 0;
                }
                var rl = new ResourceLocation(parts[0], parts[1]);
                var itemTag = TagKey.create(Registries.ITEM, rl);
                itemRegistry.getTagOrEmpty(itemTag).forEach(h -> matchItems.add(h.value()));
                var fluidTag = TagKey.create(net.minecraftforge.registries.ForgeRegistries.Keys.FLUIDS, rl);
                fluidRegistry.getTagOrEmpty(fluidTag).forEach(h -> matchFluids.add(h.value()));
                if (matchItems.isEmpty() && matchFluids.isEmpty()) {
                    source.sendFailure(Component.literal("No items or fluids found for tag " + inputFilter));
                    return 0;
                }
            } else {
                String[] parts = inputFilter.split(":", 2);
                if (parts.length != 2) {
                    source.sendFailure(Component.literal("Filter must be in namespace:path format, e.g. \"gtceu:copper_cable\" or \"#forge:small_fluid_pipes\""));
                    return 0;
                }
                var rl = new ResourceLocation(parts[0], parts[1]);
                var item = itemRegistry.get(rl);
                if (item != null) {
                    matchItems.add(item);
                } else {
                    var fluid = fluidRegistry.get(rl);
                    if (fluid != null) {
                        matchFluids.add(fluid);
                    } else {
                        source.sendFailure(Component.literal("Unknown item or fluid: " + inputFilter));
                        return 0;
                    }
                }
            }

            if (!matchItems.isEmpty())
                predicate = predicate.and(r -> recipeHasInput(r, matchItems));
            if (!matchFluids.isEmpty())
                predicate = predicate.and(r -> recipeHasFluidInput(r, matchFluids));
            descParts.add("has input " + inputFilter);
        }

        if (noRecycling) {
            predicate = predicate.and(r -> !RECYCLING_TYPES.contains(r.getType().toString()));
            descParts.add("no recycling");
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

    private static int dumpPipeRecipes(CommandSourceStack source, boolean noRecycling) {
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
                if (PIPE_EXCLUDE_TYPES.contains(r.getType().toString()))
                    continue;
                if (noRecycling && RECYCLING_TYPES.contains(r.getType().toString()))
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

    private static boolean recipeHasFluidInput(Recipe<?> r, Set<net.minecraft.world.level.material.Fluid> matchFluids) {
        if (r instanceof GTRecipe gtRecipe) {
            for (var map : List.of(gtRecipe.inputs, gtRecipe.tickInputs)) {
                var contents = map.get(FluidRecipeCapability.CAP);
                if (contents != null)
                    for (var c : contents)
                        if (c.content instanceof FluidIngredient fi)
                            for (var stack : fi.getStacks())
                                if (matchFluids.contains(stack.getFluid()))
                                    return true;
            }
            return false;
        }
        if (r instanceof PotRecipe pot)
            return fsiMatches(pot.getFluidIngredient(), matchFluids);
        if (r instanceof VatRecipe vat)
            return fsiMatches(vat.getInputFluid(), matchFluids);
        if (r instanceof BarrelRecipe barrel)
            return fsiMatches(barrel.getInputFluid(), matchFluids);
        if (r instanceof MixingBowlRecipe bowl)
            return fsiMatches(bowl.getFluidIngredient(), matchFluids);
        if (r instanceof CastingRecipe casting)
            return fsiMatches(casting.getFluidIngredient(), matchFluids);
        if (r instanceof BlastFurnaceRecipe blast)
            return fsiMatches(blast.getInputFluid(), matchFluids);
        if (r instanceof BloomeryRecipe bloomery)
            return fsiMatches(bloomery.getInputFluid(), matchFluids);
        if (r instanceof AlloyRecipe alloy)
            for (var metal : alloy.getRanges().keySet())
                if (matchFluids.contains(metal.get().getFluid()))
                    return true;
        if (r instanceof ProcessingRecipe<?> proc)
            for (var fi : proc.getFluidIngredients())
                for (var stack : fi.getMatchingFluidStacks())
                    if (matchFluids.contains(stack.getFluid()))
                        return true;
        return false;
    }

    private static boolean fsiMatches(FluidStackIngredient fsi, Set<Fluid> matchFluids) {
        for (var fluid : fsi.ingredient().fluids())
            if (matchFluids.contains(fluid))
                return true;
        return false;
    }

    private static void writeDetailedRecipe(JsonWriter jw, Recipe<?> r, RegistryAccess registryAccess) throws IOException {
        jw.beginObject();
        jw.name("id").value(r.getId().toString());
        jw.name("type").value(r.getType().toString());

        if (r instanceof GTRecipe gtRecipe) {
            var outputContents = gtRecipe.outputs.get(ItemRecipeCapability.CAP);
            var inputContents = gtRecipe.inputs.get(ItemRecipeCapability.CAP);
            var fluidOutputContents = gtRecipe.outputs.get(FluidRecipeCapability.CAP);
            var fluidInputContents = gtRecipe.inputs.get(FluidRecipeCapability.CAP);
            var tickOutputContents = gtRecipe.tickOutputs.get(ItemRecipeCapability.CAP);
            var tickInputContents = gtRecipe.tickInputs.get(ItemRecipeCapability.CAP);
            var tickFluidOutputContents = gtRecipe.tickOutputs.get(FluidRecipeCapability.CAP);
            var tickFluidInputContents = gtRecipe.tickInputs.get(FluidRecipeCapability.CAP);

            boolean hasOutputs = (outputContents != null && !outputContents.isEmpty())
                    || (fluidOutputContents != null && !fluidOutputContents.isEmpty())
                    || (tickOutputContents != null && !tickOutputContents.isEmpty())
                    || (tickFluidOutputContents != null && !tickFluidOutputContents.isEmpty());
            boolean hasInputs = (inputContents != null && !inputContents.isEmpty())
                    || (fluidInputContents != null && !fluidInputContents.isEmpty())
                    || (tickInputContents != null && !tickInputContents.isEmpty())
                    || (tickFluidInputContents != null && !tickFluidInputContents.isEmpty());
            if (!hasOutputs)
                jw.name("warning_no_outputs").value(true);
            if (!hasInputs)
                jw.name("warning_no_inputs").value(true);

            writeGTItemContents(jw, "outputs", outputContents, false);
            writeGTFluidContents(jw, "fluid_outputs", fluidOutputContents, false);
            writeGTItemContents(jw, "tick_outputs", tickOutputContents, false);
            writeGTFluidContents(jw, "tick_fluid_outputs", tickFluidOutputContents, false);
            writeGTItemContents(jw, "ingredients", inputContents, true);
            writeGTFluidContents(jw, "fluid_ingredients", fluidInputContents, true);
            writeGTItemContents(jw, "tick_ingredients", tickInputContents, true);
            writeGTFluidContents(jw, "tick_fluid_ingredients", tickFluidInputContents, true);
        } else if (r instanceof AnvilRecipe anvil) {
            var output = anvil.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);

            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, anvil.getInput());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof SimplePotRecipe pot) {
            var outputFluid = pot.getDisplayFluid();
            var outputProviders = pot.getOutputProviders();
            if (outputFluid.isEmpty() && outputProviders.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!outputFluid.isEmpty())
                jw.name("fluid_output").value(outputFluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(outputFluid.getFluid()).toString());
            if (!outputProviders.isEmpty()) {
                jw.name("outputs").beginArray();
                for (var provider : outputProviders) {
                    var stack = provider.getEmptyStack();
                    if (!stack.isEmpty())
                        jw.value(stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                }
                jw.endArray();
            }
            jw.name("ingredients").beginArray();
            for (var ingredient : pot.getItemIngredients()) {
                jw.beginArray();
                writeIngredient(jw, ingredient);
                jw.endArray();
            }
            jw.endArray();
            writeFluidStackIngredient(jw, pot.getFluidIngredient());

        } else if (r instanceof JamPotRecipe jam) {
            var output = jam.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            for (var ingredient : jam.getItemIngredients()) {
                jw.beginArray();
                writeIngredient(jw, ingredient);
                jw.endArray();
            }
            jw.endArray();
            writeFluidStackIngredient(jw, jam.getFluidIngredient());

        } else if (r instanceof PotRecipe pot) {
            // SoupPotRecipe and any other subclasses: output is computed dynamically from ingredients at runtime
            jw.name("ingredients").beginArray();
            for (var ingredient : pot.getItemIngredients()) {
                jw.beginArray();
                writeIngredient(jw, ingredient);
                jw.endArray();
            }
            jw.endArray();
            writeFluidStackIngredient(jw, pot.getFluidIngredient());

        } else if (r instanceof VatRecipe vat) {
            writeBarrelVatRecipe(jw, vat.getInputItem(), vat.getInputFluid(),
                    vat.getOutputItem().getEmptyStack(), vat.getOutputFluid());
            var jarOutput = vat.getJarOutput();
            if (!jarOutput.isEmpty())
                jw.name("jar_output").value(jarOutput.getCount() + " " + ForgeRegistries.ITEMS.getKey(jarOutput.getItem()).toString());

        } else if (r instanceof BarrelRecipe barrel) {
            writeBarrelVatRecipe(jw, barrel.getInputItem(), barrel.getInputFluid(),
                    barrel.getOutputItem().getEmptyStack(), barrel.getOutputFluid());

        } else if (r instanceof LoomRecipe loom) {
            // Must be checked before SimpleItemRecipe: has a counted ItemStackIngredient
            var output = loom.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            var ing = loom.getItemStackIngredient();
            jw.name("ingredients").beginArray();
            jw.beginArray();
            for (var stack : ing.ingredient().getItems())
                jw.value(ing.count() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof SimpleItemRecipe simple) {
            // QuernRecipe, ScrapingRecipe, DryingRecipe, SmokingRecipe, StompingRecipe, PressRecipe, etc
            var output = simple.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, simple.getIngredient());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof HeatingRecipe heating) {
            var outputItem = heating.getResultItem(registryAccess);
            var outputFluid = heating.getDisplayOutputFluid();
            if (outputItem.isEmpty() && outputFluid.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!outputItem.isEmpty())
                jw.name("output").value(outputItem.getCount() + " " + ForgeRegistries.ITEMS.getKey(outputItem.getItem()).toString());
            if (!outputFluid.isEmpty())
                jw.name("fluid_output").value(outputFluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(outputFluid.getFluid()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, heating.getIngredient());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof OvenRecipe oven) {
            var output = oven.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, oven.getIngredient());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof MixingBowlRecipe bowl) {
            var outputItem = bowl.getResultItem(registryAccess);
            var outputFluid = bowl.getDisplayFluid();
            if (outputItem.isEmpty() && outputFluid.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!outputItem.isEmpty())
                jw.name("output").value(outputItem.getCount() + " " + ForgeRegistries.ITEMS.getKey(outputItem.getItem()).toString());
            if (!outputFluid.isEmpty())
                jw.name("fluid_output").value(outputFluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(outputFluid.getFluid()).toString());
            jw.name("ingredients").beginArray();
            for (var ingredient : bowl.getItemIngredients()) {
                jw.beginArray();
                writeIngredient(jw, ingredient);
                jw.endArray();
            }
            jw.endArray();
            writeFluidStackIngredient(jw, bowl.getFluidIngredient());

        } else if (r instanceof KnappingRecipe knapping) {
            var output = knapping.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            var ingredient = knapping.getIngredient();
            if (ingredient != null) {
                jw.name("ingredients").beginArray();
                jw.beginArray();
                writeIngredient(jw, ingredient);
                jw.endArray();
                jw.endArray();
            }

        } else if (r instanceof AlloyRecipe alloy) {
            jw.name("result_metal").value(alloy.getResult().getId().toString());
            jw.name("contents").beginArray();
            for (var entry : alloy.getRanges().entrySet()) {
                jw.beginObject();
                jw.name("metal").value(entry.getKey().id().toString());
                jw.name("min").value(entry.getValue().min());
                jw.name("max").value(entry.getValue().max());
                jw.endObject();
            }
            jw.endArray();

        } else if (r instanceof WeldingRecipe welding) {
            var output = welding.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, welding.getFirstInput());
            jw.endArray();
            jw.beginArray();
            writeIngredient(jw, welding.getSecondInput());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof CastingRecipe casting) {
            var output = casting.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, casting.getIngredient()); // mold ingredient
            jw.endArray();
            jw.endArray();
            writeFluidStackIngredient(jw, casting.getFluidIngredient());

        } else if (r instanceof BlastFurnaceRecipe blast) {
            var outputFluid = blast.getOutputFluid();
            if (outputFluid.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!outputFluid.isEmpty())
                jw.name("fluid_output").value(outputFluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(outputFluid.getFluid()).toString());
            var catalyst = blast.getCatalyst();
            if (!catalyst.isEmpty()) {
                jw.name("ingredients").beginArray();
                jw.beginArray();
                writeIngredient(jw, catalyst);
                jw.endArray();
                jw.endArray();
            }
            writeFluidStackIngredient(jw, blast.getInputFluid());

        } else if (r instanceof BloomeryRecipe bloomery) {
            var output = bloomery.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            var catalyst = bloomery.getCatalyst();
            if (!catalyst.ingredient().isEmpty()) {
                jw.name("ingredients").beginArray();
                jw.beginArray();
                for (var stack : catalyst.ingredient().getItems())
                    jw.value(catalyst.count() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                jw.endArray();
                jw.endArray();
            }
            writeFluidStackIngredient(jw, bloomery.getInputFluid());

        } else if (r instanceof SewingRecipe sewing) {
            var output = sewing.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());

        } else if (r instanceof GlassworkingRecipe glass) {
            var output = glass.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("ingredients").beginArray();
            jw.beginArray();
            writeIngredient(jw, glass.getBatchItem());
            jw.endArray();
            jw.endArray();

        } else if (r instanceof SimpleBlockRecipe sbr) {
            // ChiselRecipe, MattockRecipe, CollapseRecipe, BlockModRecipe
            var outputBlock = sbr.getBlockRecipeOutput();
            jw.name("output_block").value(ForgeRegistries.BLOCKS.getKey(outputBlock).toString());
            jw.name("input_blocks").beginArray();
            for (var block : sbr.getBlockIngredient().blocks())
                jw.value(ForgeRegistries.BLOCKS.getKey(block).toString());
            jw.endArray();
            Ingredient itemIng = null;
            if (sbr instanceof ChiselRecipe cr)
                itemIng = cr.getItemIngredient();
            else if (sbr instanceof MattockRecipe mr)
                itemIng = mr.getItemIngredient();
            else if (sbr instanceof BlockModRecipe bmr)
                itemIng = bmr.getInputItem();
            if (itemIng != null && !itemIng.isEmpty()) {
                jw.name("ingredients").beginArray();
                jw.beginArray();
                writeIngredient(jw, itemIng);
                jw.endArray();
                jw.endArray();
            }

        } else if (r instanceof FramingSawRecipe saw) {
            var output = saw.getResultItem(registryAccess);
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            jw.name("material_amount").value(saw.getMaterialAmount());
            var additives = saw.getAdditives();
            if (!additives.isEmpty()) {
                jw.name("additives").beginArray();
                for (var additive : additives) {
                    jw.beginArray();
                    for (var stack : additive.ingredient().getItems())
                        jw.value(additive.count() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    jw.endArray();
                }
                jw.endArray();
            }

        } else if (r instanceof ProcessingRecipe<?> proc) {
            // Create ProcessingRecipes: Greate mixing, Vintage Improvements, etc.
            var itemOutputs = proc.getRollableResults();
            var fluidOutputs = proc.getFluidResults();
            var itemInputs = proc.getIngredients();
            var fluidInputs = proc.getFluidIngredients();
            if (itemOutputs.isEmpty() && fluidOutputs.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (itemInputs.isEmpty() && fluidInputs.isEmpty())
                jw.name("warning_no_inputs").value(true);
            if (!itemOutputs.isEmpty()) {
                jw.name("outputs").beginArray();
                for (ProcessingOutput out : itemOutputs) {
                    var stack = out.getStack();
                    var entry = stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                    float chance = out.getChance();
                    jw.value(chance < 1f ? entry + " (" + String.format("%.0f%%", chance * 100) + ")" : entry);
                }
                jw.endArray();
            }
            if (!fluidOutputs.isEmpty()) {
                jw.name("fluid_outputs").beginArray();
                for (var fluid : fluidOutputs)
                    jw.value(fluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString());
                jw.endArray();
            }
            if (!itemInputs.isEmpty()) {
                jw.name("ingredients").beginArray();
                for (var ing : itemInputs) {
                    jw.beginArray();
                    writeIngredient(jw, ing);
                    jw.endArray();
                }
                jw.endArray();
            }
            if (!fluidInputs.isEmpty()) {
                jw.name("fluid_ingredients").beginArray();
                for (var fi : fluidInputs) {
                    jw.beginArray();
                    for (var stack : fi.getMatchingFluidStacks())
                        jw.value(fi.getRequiredAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString());
                    jw.endArray();
                }
                jw.endArray();
            }

        } else if (r instanceof ArchitectsCutterRecipe) {
            var output = r.getResultItem(registryAccess);
            var ingredients = r.getIngredients();
            if (output.isEmpty())
                jw.name("warning_no_outputs").value(true);
            if (!output.isEmpty())
                jw.name("output").value(output.getCount() + " " + ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            if (!ingredients.isEmpty()) {
                jw.name("ingredients").beginArray();
                for (var ingredient : ingredients) {
                    jw.beginArray();
                    writeIngredient(jw, ingredient);
                    jw.endArray();
                }
                jw.endArray();
            }

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
                writeIngredient(jw, ingredient);
                jw.endArray();
            }
            jw.endArray();
        }

        jw.endObject();
    }

    private static void writeGTItemContents(JsonWriter jw, String field, List<Content> contents, boolean slotArrays) throws IOException {
        if (contents == null || contents.isEmpty())
            return;
        jw.name(field).beginArray();
        for (var c : contents) {
            String chance = c.isChanced() ? " (" + String.format("%.0f%%", 100f * c.chance / c.maxChance) + ")" : "";
            if (slotArrays)
                jw.beginArray();
            if (c.content instanceof SizedIngredient si) {
                for (var stack : si.getItems())
                    jw.value(stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()) + chance);
            } else if (c.content instanceof Ingredient ing) {
                for (var stack : ing.getItems())
                    jw.value(ForgeRegistries.ITEMS.getKey(stack.getItem()) + chance);
            }
            if (slotArrays)
                jw.endArray();
        }
        jw.endArray();
    }

    private static void writeGTFluidContents(JsonWriter jw, String field, List<Content> contents, boolean slotArrays) throws IOException {
        if (contents == null || contents.isEmpty())
            return;
        jw.name(field).beginArray();
        for (var c : contents) {
            String chance = c.isChanced() ? " (" + String.format("%.0f%%", 100f * c.chance / c.maxChance) + ")" : "";
            if (slotArrays)
                jw.beginArray();
            if (c.content instanceof FluidIngredient fi) {
                for (var stack : fi.getStacks())
                    jw.value(stack.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(stack.getFluid()) + chance);
            }
            if (slotArrays)
                jw.endArray();
        }
        jw.endArray();
    }

    private static void writeIngredient(JsonWriter jw, Object content) throws IOException {
        if (content instanceof SizedIngredient si) {
            for (var stack : si.getItems())
                jw.value(stack.getCount() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        } else if (content instanceof Ingredient ing) {
            for (var stack : ing.getItems())
                jw.value(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        }
    }

    private static void writeFluidStackIngredient(JsonWriter jw, FluidStackIngredient fsi) throws IOException {
        if (fsi.amount() == 0)
            return;
        jw.name("fluid_ingredient").beginArray();
        for (var fluid : fsi.ingredient().fluids())
            jw.value(fsi.amount() + "mB " + ForgeRegistries.FLUIDS.getKey(fluid).toString());
        jw.endArray();
    }

    private static void writeBarrelVatRecipe(JsonWriter jw, ItemStackIngredient inputItem, FluidStackIngredient inputFluid, ItemStack outputItem, FluidStack outputFluid) throws IOException {
        if (outputItem.isEmpty() && outputFluid.isEmpty())
            jw.name("warning_no_outputs").value(true);
        if (!outputItem.isEmpty())
            jw.name("output").value(outputItem.getCount() + " " + ForgeRegistries.ITEMS.getKey(outputItem.getItem()).toString());
        if (!outputFluid.isEmpty())
            jw.name("fluid_output").value(outputFluid.getAmount() + "mB " + ForgeRegistries.FLUIDS.getKey(outputFluid.getFluid()).toString());
        if (!inputItem.ingredient().isEmpty()) {
            jw.name("ingredients").beginArray();
            jw.beginArray();
            for (var stack : inputItem.ingredient().getItems())
                jw.value(inputItem.count() + " " + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            jw.endArray();
            jw.endArray();
        }
        writeFluidStackIngredient(jw, inputFluid);
    }
}
