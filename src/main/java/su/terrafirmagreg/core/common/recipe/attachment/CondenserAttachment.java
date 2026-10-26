package su.terrafirmagreg.core.common.recipe.attachment;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.dries007.tfc.common.recipes.ingredients.FluidIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.blockentity.appliances.CondenserBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.data.TFGTags;

public record CondenserAttachment(@NotNull FluidStack fluidOutput,
        @Nullable CoolantCondition coolantCondition) implements RecipeAttachment {

    public static final String TYPE = "condenser";

    public record CoolantCondition(
            @Nullable Float maxTemp,
            int amount,
            FluidIngredient ingredient) {

        public boolean test(FluidStack coolantInTank) {
            if (coolantInTank.isEmpty()) {
                return false;
            }
            if (amount > 0 && coolantInTank.getAmount() < amount) {
                return false;
            }
            if (amount == 0 && coolantInTank.getAmount() <= 0) {
                return false;
            }
            if (ingredient != null && !ingredient.test(coolantInTank.getFluid())) {
                return false;
            }
            if (maxTemp != null) {
                int fluidTemp = coolantInTank.getFluid().getFluidType().getTemperature(coolantInTank);
                return fluidTemp <= maxTemp;
            }
            return true;
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            if (maxTemp != null) {
                obj.addProperty("max_temp", maxTemp);
            }
            obj.addProperty("amount", amount);
            if (ingredient != null) {
                obj.add("ingredient", ingredient.toJson());
            }
            return obj;
        }

        public static CoolantCondition fromJson(JsonObject json) {
            Float maxTemp = null;
            if (json.has("max_temp")) {
                maxTemp = json.get("max_temp").getAsFloat();
            } else if (json.has("temp")) {
                maxTemp = json.get("temp").getAsFloat();
            }
            int amount = json.has("amount") ? json.get("amount").getAsInt() : 10;
            FluidIngredient ingredient;
            if (json.has("ingredient")) {
                ingredient = FluidIngredient.fromJson(json.get("ingredient"));
            } else if (json.has("fluid")) {
                ingredient = FluidIngredient.fromJson(json.get("fluid"));
            } else {
                ingredient = new FluidIngredient(List.of(new FluidIngredient.FluidTag(TFGTags.Fluids.CONDENSER_COOLANTS)));
            }
            return new CoolantCondition(maxTemp, amount, ingredient);
        }

        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeBoolean(maxTemp != null);
            if (maxTemp != null) {
                buffer.writeFloat(maxTemp);
            }
            buffer.writeVarInt(amount);
            ingredient.toNetwork(buffer);
        }

        public static CoolantCondition fromNetwork(FriendlyByteBuf buffer) {
            Float maxTemp = buffer.readBoolean() ? buffer.readFloat() : null;
            int amount = buffer.readVarInt();
            FluidIngredient ingredient = FluidIngredient.fromNetwork(buffer);
            return new CoolantCondition(maxTemp, amount, ingredient);
        }
    }

    public CondenserAttachment(@Nullable FluidStack fluidOutput, @Nullable CoolantCondition coolantCondition) {
        this.fluidOutput = fluidOutput == null ? FluidStack.EMPTY : fluidOutput;
        this.coolantCondition = coolantCondition;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Nullable
    public static CondenserBlockEntity getCondenser(CookingApplianceBlockEntity appliance) {
        Level level = appliance.getLevel();
        if (level == null) {
            return null;
        }
        BlockPos abovePos = appliance.getBlockPos().above();
        BlockEntity be = level.getBlockEntity(abovePos);
        if (be instanceof CondenserBlockEntity condenser) {
            return condenser;
        }
        return null;
    }

    @Override
    public boolean matches(CookingApplianceBlockEntity appliance, Level level) {
        CondenserBlockEntity condenser = getCondenser(appliance);
        if (condenser == null) {
            return false;
        }
        if (coolantCondition != null) {
            FluidStack coolantInTank = condenser.getCoolantTank().getFluid();
            if (!coolantCondition.test(coolantInTank)) {
                return false;
            }
        }
        return canFitOutputs(appliance);
    }

    @Override
    public boolean matchesInputs(CookingApplianceBlockEntity appliance) {
        CondenserBlockEntity condenser = getCondenser(appliance);
        if (condenser == null) {
            return false;
        }
        if (coolantCondition != null) {
            FluidStack coolantInTank = condenser.getCoolantTank().getFluid();
            return coolantCondition.test(coolantInTank);
        }
        return true;
    }

    @Override
    public boolean canFitOutputs(CookingApplianceBlockEntity appliance) {
        CondenserBlockEntity condenser = getCondenser(appliance);
        if (condenser == null) {
            return false;
        }
        if (fluidOutput.isEmpty()) {
            return true;
        }
        FluidStack current = condenser.getOutputTank().getFluid();
        int capacity = condenser.getOutputTank().getCapacity();
        if (current.isEmpty()) {
            return fluidOutput.getAmount() <= capacity;
        }
        return current.isFluidEqual(fluidOutput) && (current.getAmount() + fluidOutput.getAmount() <= capacity);
    }

    @Override
    public void assembleOutputs(CookingApplianceBlockEntity appliance) {
        CondenserBlockEntity condenser = getCondenser(appliance);
        if (condenser == null) {
            return;
        }
        if (coolantCondition != null && coolantCondition.amount() > 0) {
            condenser.getCoolantTank().drain(coolantCondition.amount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (!fluidOutput.isEmpty()) {
            FluidStack current = condenser.getOutputTank().getFluid();
            if (current.isEmpty()) {
                condenser.getOutputTank().setFluid(fluidOutput.copy());
            } else if (current.isFluidEqual(fluidOutput)) {
                current.grow(fluidOutput.getAmount());
            }
            condenser.fluidTankChanged();
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE);
        if (!fluidOutput.isEmpty()) {
            JsonObject fo = new JsonObject();
            fo.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid())).toString());
            fo.addProperty("amount", fluidOutput.getAmount());
            if (fluidOutput.hasTag()) {
                fo.addProperty("nbt", fluidOutput.getTag().toString());
            }
            obj.add("fluid_output", fo);
        }
        if (coolantCondition != null) {
            obj.add("coolant", coolantCondition.toJson());
        }
        return obj;
    }

    public static CondenserAttachment fromJson(JsonObject json) {
        FluidStack fluidOutput = FluidStack.EMPTY;
        if (json.has("fluid_output") && !json.get("fluid_output").isJsonNull()) {
            JsonObject fo = json.getAsJsonObject("fluid_output");
            ResourceLocation fluidId = ResourceLocation.parse(fo.get("fluid").getAsString());
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            int amount = fo.has("amount") ? fo.get("amount").getAsInt() : 1000;
            if (fluid != null) {
                fluidOutput = new FluidStack(fluid, amount);
            }
        }
        CoolantCondition coolantCondition = null;
        if (json.has("coolant") && !json.get("coolant").isJsonNull()) {
            coolantCondition = CoolantCondition.fromJson(json.getAsJsonObject("coolant"));
        }
        return new CondenserAttachment(fluidOutput, coolantCondition);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeFluidStack(fluidOutput);
        buffer.writeBoolean(coolantCondition != null);
        if (coolantCondition != null) {
            coolantCondition.toNetwork(buffer);
        }
    }

    public static CondenserAttachment fromNetwork(FriendlyByteBuf buffer) {
        FluidStack fluidOutput = buffer.readFluidStack();
        CoolantCondition coolantCondition = buffer.readBoolean() ? CoolantCondition.fromNetwork(buffer) : null;
        return new CondenserAttachment(fluidOutput, coolantCondition);
    }
}
