package su.terrafirmagreg.core.mixins.common.tfc.food;

import java.util.ArrayList;
import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.ChatFormatting;

import su.terrafirmagreg.core.common.food.nutrient.INegativeNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.INutrientExtension;

/**
 * Mixin to add new nutrients to TFC's Nutrient enum. Including harmful ones.
 * These nutrients are tracked and displayed but do not affect player health.
 * Use .tfg$isNegative() to check if a nutrient is harmful.
 * <p>
 * New nutrients should also be declared in {@link su.terrafirmagreg.core.mixins.common.kubejs_tfc.BuildFoodItemDataMixin}
 * and {@link su.terrafirmagreg.core.mixins.common.kubejs_tfc.FoodComponentFoodDataMixin}
 * and {@link INegativeNutrientBuilder}
 * Setting up builders for mixins is annoying. So I didn't.
 */
@Mixin(Nutrient.class)
public class NutrientMixin implements INutrientExtension {

    @Shadow(remap = false)
    @Final
    @Mutable
    public static Nutrient[] VALUES;

    @Shadow(remap = false)
    @Final
    @Mutable
    public static int TOTAL;

    @Unique
    private boolean tfg$negative = false;

    @Invoker(value = "<init>", remap = false)
    private static Nutrient tfg$invokeInit(String name, int ordinal, ChatFormatting color) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tfg$addNegativeNutrients(CallbackInfo ci) {
        var nutrients = new ArrayList<>(Arrays.asList(VALUES));

        var toxins = tfg$invokeInit("TOXINS", nutrients.size(), ChatFormatting.LIGHT_PURPLE);
        ((NutrientMixin) (Object) toxins).tfg$negative = true;
        nutrients.add(toxins);

        var microplastics = tfg$invokeInit("MICROPLASTICS", nutrients.size(), ChatFormatting.DARK_AQUA);
        ((NutrientMixin) (Object) microplastics).tfg$negative = true;
        nutrients.add(microplastics);

        var parasites = tfg$invokeInit("PARASITES", nutrients.size(), ChatFormatting.DARK_RED);
        ((NutrientMixin) (Object) parasites).tfg$negative = true;
        nutrients.add(parasites);

        VALUES = nutrients.toArray(new Nutrient[0]);
        TOTAL = VALUES.length;
    }

    @Override
    public boolean tfg$isNegative() {
        return this.tfg$negative;
    }
}
