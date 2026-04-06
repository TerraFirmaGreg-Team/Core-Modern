package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;

@Mixin(FoodData.class)
public class FoodDataMixin {

    // Round the saturation float so food stacks properly

    @Shadow(remap = false)
    private int hunger;
    @Shadow(remap = false)
    private float saturation;
    @Shadow(remap = false)
    private float water;
    @Shadow(remap = false)
    private float decayModifier;
    @Shadow(remap = false)
    private float grain;
    @Shadow(remap = false)
    private float vegetables;
    @Shadow(remap = false)
    private float fruit;
    @Shadow(remap = false)
    private float protein;
    @Shadow(remap = false)
    private float dairy;

    /**
     * @author Mqrius & Redeix
     * @reason Round saturation to prevent floatingpoint errors and add negative nutrient support
     */
    @Overwrite(remap = false)
    public CompoundTag write() {
        final CompoundTag nbt = new CompoundTag();
        nbt.putInt("food", hunger);
        nbt.putFloat("sat", Math.round(saturation * 100f) / 100f);
        nbt.putFloat("water", water);
        nbt.putFloat("decay", decayModifier);
        nbt.putFloat("grain", grain);
        nbt.putFloat("veg", vegetables);
        nbt.putFloat("fruit", fruit);
        nbt.putFloat("meat", protein);
        nbt.putFloat("dairy", dairy);

        FoodDataExtension.writeToNbt((FoodData) (Object) this, nbt);

        return nbt;
    }
}
