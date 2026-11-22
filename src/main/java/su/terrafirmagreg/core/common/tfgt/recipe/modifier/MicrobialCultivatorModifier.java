package su.terrafirmagreg.core.common.tfgt.recipe.modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.MicrobialCultivatorMachine;
import su.terrafirmagreg.core.common.tfgt.machine.trait.InputHistoryTrait;

/** Modifier that adds bacteria fluids to the recipe output based on the syringe's contents. */
public final class MicrobialCultivatorModifier implements RecipeModifier {

    public static final MicrobialCultivatorModifier INSTANCE = new MicrobialCultivatorModifier();

    private record Yield(ResourceLocation fluid, int amount) {
    }

    private static final Map<String, List<Yield>> YIELDS = new HashMap<>();

    private MicrobialCultivatorModifier() {
    }

    public static void register(String[] entities, Object[][] fluids) {
        List<Yield> values = new ArrayList<>(fluids.length);
        for (Object[] entry : fluids) {
            String name = entry[0].toString();
            int amount = ((Number) entry[1]).intValue();

            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id == null)
                throw new IllegalArgumentException("Invalid fluid: " + name);

            values.add(new Yield(id, amount));
        }

        for (String entity : entities)
            YIELDS.put(entity, List.copyOf(values));
    }

    private static List<Yield> yieldsFor(String entityId) {
        return YIELDS.getOrDefault(entityId, List.of());
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        InputHistoryTrait history = machine.getTrait(InputHistoryTrait.class);
        if (history == null)
            return RecipeModifier.nullWrongType(MicrobialCultivatorMachine.class, machine);

        ItemStack syringe = history.getActiveInput();
        if (syringe == null || !syringe.hasTag() || !syringe.getTag().contains("mob_type", Tag.TAG_STRING))
            return ModifierFunction.IDENTITY;

        List<Content> fluids = fluidsFor(syringe.getTag().getString("mob_type"));
        return modified -> {
            GTRecipe copy = modified.copy();
            copy.outputs.put(FluidRecipeCapability.CAP, fluids);
            return copy;
        };
    }

    private static List<Content> fluidsFor(String mobType) {
        List<Yield> yields = yieldsFor(mobType);
        List<Content> fluids = new ArrayList<>(yields.size());
        int maxChance = ChanceLogic.getMaxChancedValue();
        for (Yield yield : yields) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(yield.fluid());
            if (fluid != null)
                fluids.add(new Content(FluidIngredient.of(fluid, yield.amount()), maxChance, maxChance));
        }
        return fluids;
    }
}
