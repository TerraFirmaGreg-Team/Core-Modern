package su.terrafirmagreg.core.compat.kjs.attachment;

import java.util.List;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.util.helpers.IngredientHelpers;

import net.dries007.tfc.common.recipes.ingredients.FluidIngredient;
import net.dries007.tfc.common.recipes.ingredients.IngredientType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.recipe.attachment.CondenserAttachment;
import su.terrafirmagreg.core.compat.kjs.CookingApplianceRecipeJS;

@SuppressWarnings("unused")
public class CondenserAttachmentJS implements AttachmentJS {

    private JsonObject fluidOutput = null;
    private JsonObject coolant = null;

    public CondenserAttachmentJS fluidOutput(Object fluid) {
        this.fluidOutput = CookingApplianceRecipeJS.parseFluidOutput(fluid, -1);
        return this;
    }

    public CondenserAttachmentJS fluidOutput(Object fluid, int amount) {
        this.fluidOutput = CookingApplianceRecipeJS.parseFluidOutput(fluid, amount);
        return this;
    }

    public CondenserAttachmentJS coolant() {
        return coolant(null, 10, null);
    }

    public CondenserAttachmentJS coolant(Object temp) {
        return coolant(temp, 10, null);
    }

    public CondenserAttachmentJS coolant(Object temp, Object amount) {
        return coolant(temp, amount, null);
    }

    @SuppressWarnings("unchecked")
    public CondenserAttachmentJS coolant(Object temp, Object amount, Object fluidId) {
        if (temp == null) {
            this.coolant = null;
            return this;
        }

        JsonObject obj = new JsonObject();
        if (temp instanceof Number num) {
            obj.addProperty("max_temp", num.floatValue());
        } else {
            try {
                obj.addProperty("max_temp", Float.parseFloat(temp.toString()));
            } catch (NumberFormatException ignored) {
            }
        }

        int amt = 10;
        if (amount instanceof Number num) {
            amt = num.intValue();
        } else if (amount != null) {
            amt = Integer.parseInt(amount.toString());
        }
        obj.addProperty("amount", amt);

        FluidIngredient ingredient;
        if (fluidId != null) {
            if (fluidId instanceof FluidIngredient fi) {
                ingredient = fi;
            } else if (fluidId instanceof TagKey<?> tk) {
                ingredient = new FluidIngredient(List.of(new FluidIngredient.FluidTag((TagKey<Fluid>) tk)));
            } else if (fluidId instanceof Fluid f) {
                ingredient = new FluidIngredient(List.of(new IngredientType.ObjEntry<>(f)));
            } else if (fluidId instanceof FluidStack fs) {
                ingredient = new FluidIngredient(List.of(new IngredientType.ObjEntry<>(fs.getFluid())));
            } else {
                String str = fluidId.toString().trim();
                if (str.startsWith("#")) {
                    TagKey<Fluid> tag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.parse(str.substring(1)));
                    ingredient = new FluidIngredient(List.of(new FluidIngredient.FluidTag(tag)));
                } else {
                    ResourceLocation res = ResourceLocation.parse(str);
                    Fluid f = ForgeRegistries.FLUIDS.getValue(res);
                    if (f != null && ForgeRegistries.FLUIDS.containsKey(res)) {
                        ingredient = new FluidIngredient(List.of(new IngredientType.ObjEntry<>(f)));
                    } else {
                        ingredient = IngredientHelpers.ofFluidIngredient(str);
                    }
                }
            }
        } else {
            ingredient = new FluidIngredient(List.of(new FluidIngredient.FluidTag(TFGTags.Fluids.CONDENSER_COOLANTS)));
        }

        if (ingredient != null) {
            obj.add("ingredient", ingredient.toJson());
        }

        this.coolant = obj;
        return this;
    }

    @Override
    public String getType() {
        return CondenserAttachment.TYPE;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getType());
        if (fluidOutput != null) {
            obj.add("fluid_output", fluidOutput);
        }
        if (coolant != null) {
            obj.add("coolant", coolant);
        }
        return obj;
    }
}
