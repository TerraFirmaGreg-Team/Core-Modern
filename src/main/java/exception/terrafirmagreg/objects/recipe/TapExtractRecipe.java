package exception.terrafirmagreg.objects.recipe;

import com.google.gson.JsonObject;
import exception.terrafirmagreg.TerraFirmaGreg;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TapExtractRecipe implements Recipe<Container> {
    public final Ingredient input;
    public final ItemStack itemOutput;
    public final ItemStack woodenItemOutput;
    public final Ingredient harvestItem;
    public final boolean collectBucket;
    public final int processingTime;
    public final String fluidColor;
    public final FluidStack displayFluid;
    public final int[] lifeCycles;
    private final ResourceLocation id;

    public TapExtractRecipe(ResourceLocation id, Ingredient input, ItemStack itemOutput, ItemStack woodenItemOutput, Ingredient harvestItem, boolean collectBucket, int processingTime, FluidStack displayFluid, String fluidColor, int[] lifeCycles) {
        this.id = id;
        this.input = input;
        this.itemOutput = itemOutput;
        this.woodenItemOutput = woodenItemOutput;
        this.harvestItem = harvestItem;
        this.collectBucket = collectBucket;
        this.processingTime = processingTime;
        this.fluidColor = fluidColor;
        this.displayFluid = displayFluid;
        this.lifeCycles = lifeCycles;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(Container container, RegistryAccess level) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess level) {
        return ItemStack.EMPTY;
    }

    public ItemStack getResultItem(BlockState blockState) {
        boolean woodResult = blockState.is(TerraFirmaGreg.WOODEN_SAP_COLLECTOR.get());
        return woodResult ? woodenItemOutput.copy() : itemOutput.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TerraFirmaGreg.TAP_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TerraFirmaGreg.TAP_RECIPE_TYPE.get();
    }

    public static class Serializer<T extends TapExtractRecipe> implements RecipeSerializer<T> {
        final TapExtractRecipe.Serializer.IRecipeFactory<T> factory;

        public Serializer(TapExtractRecipe.Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Nonnull
        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input;
            if (GsonHelper.isArrayNode(json, "log")) {
                input = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "log"));
            } else {
                input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "log"));
            }
            ItemStack metalTtemOutput = ItemStack.EMPTY;
            if (json.has("result")) {
                metalTtemOutput = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            } else if (json.has("metal_result")) {
                metalTtemOutput = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "metal_result"), true);
            }
            ItemStack woodenItemOutput = metalTtemOutput.copy();
            if (json.has("wooden_result")) {
                woodenItemOutput = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "wooden_result"), true);
            }
            Ingredient harvestItem = Ingredient.EMPTY;
            if (json.has("harvest_item")) {
                if (GsonHelper.isArrayNode(json, "harvest_item")) {
                    harvestItem = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "harvest_item"));
                } else {
                    harvestItem = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "harvest_item"));
                }
            }

            // TFC compatibility
            int[] lifeCycles = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            if (json.has("life_cycle")) {
                if (GsonHelper.isArrayNode(json, "life_cycle")) {
                    var jsonLifeCycles = GsonHelper.getAsJsonArray(json, "life_cycle");
                    for (int i = 0; i < jsonLifeCycles.size(); ++i) {
                        lifeCycles[i] = jsonLifeCycles.get(i).getAsInt();
                    }
                }
            }

            Fluid displayFluid = Fluids.WATER;
            if (json.has("display_fluid")) {
                JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "display_fluid");
                if (fluidJson.has("fluid")) {
                    displayFluid = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(fluidJson, "fluid"))));
                }
            }
            String fluidColor = json.has("fluid_color") ? json.get("fluid_color").getAsString() : "";

            return this.factory.create(id, input, metalTtemOutput, woodenItemOutput, harvestItem, GsonHelper.getAsBoolean(json, "collect_bucket", false), GsonHelper.getAsInt(json, "processing_time", 1000), new FluidStack(displayFluid, 1000), fluidColor, lifeCycles);
        }

        public T fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            try {
                return this.factory.create(id, Ingredient.fromNetwork(buffer), buffer.readItem(), buffer.readItem(), Ingredient.fromNetwork(buffer), buffer.readBoolean(), buffer.readInt(), buffer.readFluidStack(), buffer.readUtf(), buffer.readVarIntArray());
            } catch (Exception e) {
                TerraFirmaGreg.LOGGER.error("Error reading tap extract recipe from packet. " + id, e);
                throw e;
            }
        }

        public void toNetwork(@Nonnull FriendlyByteBuf buffer, T recipe) {
            try {
                recipe.input.toNetwork(buffer);
                buffer.writeItem(recipe.itemOutput);
                buffer.writeItem(recipe.woodenItemOutput);
                recipe.harvestItem.toNetwork(buffer);
                buffer.writeBoolean(recipe.collectBucket);
                buffer.writeInt(recipe.processingTime);
                buffer.writeFluidStack(recipe.displayFluid);
                buffer.writeUtf(recipe.fluidColor);
                buffer.writeVarIntArray(recipe.lifeCycles);
            } catch (Exception e) {
                TerraFirmaGreg.LOGGER.error("Error writing tap extract recipe to packet. " + recipe.getId(), e);
                throw e;
            }
        }

        public interface IRecipeFactory<T extends TapExtractRecipe> {
            T create(ResourceLocation id, Ingredient input, ItemStack metalItemOutput, ItemStack woodenItemOutput, Ingredient harvestItem, boolean fluidOutput, int processingTime, FluidStack displayFluid, String fluidColor, int[] lifeCycles);
        }
    }
}
