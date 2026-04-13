package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;

/**
 * Mixin for FoodData record to add negative nutrients.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    /**
     * @author Mqrius & Redeix
     * @reason Round saturation to prevent floating point errors and add negative nutrient support.
     */
    @Overwrite(remap = false)
    public CompoundTag write() {
        FoodData self = (FoodData) (Object) this;

        final CompoundTag nbt = new CompoundTag();
        nbt.putInt("food", self.hunger());
        nbt.putFloat("sat", Math.round(self.saturation() * 100f) / 100f);
        nbt.putFloat("water", self.water());
        nbt.putFloat("decay", self.decayModifier());
        nbt.putFloat("grain", self.grain());
        nbt.putFloat("veg", self.vegetables());
        nbt.putFloat("fruit", self.fruit());
        nbt.putFloat("meat", self.protein());
        nbt.putFloat("dairy", self.dairy());

        FoodDataExtension.writeToNbt(self, nbt);

        return nbt;
    }
}
