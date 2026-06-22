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

import su.terrafirmagreg.core.common.food.nutrient.IExtendedNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.INutrientExtension;

/**
 * Mixin to add new nutrients to TFC's Nutrient enum. Including harmful and transient ones.
 * These nutrients are tracked and displayed but do not affect player health.
 * Use .tfg$isNegative() to check if a nutrient is harmful, or .tfg$isTransient() for transient ones.
 * <p>
 * For Kubejs support new nutrients should also be declared in {@link su.terrafirmagreg.core.mixins.common.kubejs_tfc.BuildFoodItemDataMixin}
 * and {@link su.terrafirmagreg.core.mixins.common.kubejs_tfc.FoodComponentFoodDataMixin}
 * and {@link IExtendedNutrientBuilder}
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

    @Unique
    private boolean tfg$transient = false;

    @Invoker(value = "<init>", remap = false)
    private static Nutrient tfg$invokeInit(String name, int ordinal, ChatFormatting color) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tfg$addExtendedNutrients(CallbackInfo ci) {
        var nutrients = new ArrayList<>(Arrays.asList(VALUES));

        // --------- Negative Nutrients -----------
        var toxins = tfg$invokeInit("TOXINS", nutrients.size(), ChatFormatting.LIGHT_PURPLE);
        ((NutrientMixin) (Object) toxins).tfg$negative = true;
        nutrients.add(toxins);

        var microplastics = tfg$invokeInit("MICROPLASTICS", nutrients.size(), ChatFormatting.DARK_AQUA);
        ((NutrientMixin) (Object) microplastics).tfg$negative = true;
        nutrients.add(microplastics);

        var parasites = tfg$invokeInit("PARASITES", nutrients.size(), ChatFormatting.DARK_RED);
        ((NutrientMixin) (Object) parasites).tfg$negative = true;
        nutrients.add(parasites);

        // --------- Transient Nutrients -----------
        // === Instant Effects ===
        var deadly = tfg$invokeInit("DEADLY", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) deadly).tfg$transient = true;
        nutrients.add(deadly);

        // === Mob Effects ===
        var cooling = tfg$invokeInit("COOLING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) cooling).tfg$transient = true;
        nutrients.add(cooling);

        var warming = tfg$invokeInit("WARMING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) warming).tfg$transient = true;
        nutrients.add(warming);

        var freezing = tfg$invokeInit("FREEZING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) freezing).tfg$transient = true;
        nutrients.add(freezing);

        var blazing = tfg$invokeInit("BLAZING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) blazing).tfg$transient = true;
        nutrients.add(blazing);

        var radiating = tfg$invokeInit("RADIATING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) radiating).tfg$transient = true;
        nutrients.add(radiating);

        var nauseating = tfg$invokeInit("NAUSEATING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) nauseating).tfg$transient = true;
        nutrients.add(nauseating);

        var parching = tfg$invokeInit("PARCHING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) parching).tfg$transient = true;
        nutrients.add(parching);

        var quenching = tfg$invokeInit("QUENCHING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) quenching).tfg$transient = true;
        nutrients.add(quenching);

        var bolstering = tfg$invokeInit("BOLSTERING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) bolstering).tfg$transient = true;
        nutrients.add(bolstering);

        var hearty = tfg$invokeInit("HEARTY", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) hearty).tfg$transient = true;
        nutrients.add(hearty);

        var rejuvenating = tfg$invokeInit("REJUVENATING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) rejuvenating).tfg$transient = true;
        nutrients.add(rejuvenating);

        // === Meal Effects ===
        var sugary = tfg$invokeInit("SUGARY", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) sugary).tfg$transient = true;
        nutrients.add(sugary);

        var spicy = tfg$invokeInit("SPICY", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) spicy).tfg$transient = true;
        nutrients.add(spicy);

        var fulfilling = tfg$invokeInit("FULFILLING", nutrients.size(), ChatFormatting.WHITE);
        ((NutrientMixin) (Object) fulfilling).tfg$transient = true;
        nutrients.add(fulfilling);

        VALUES = nutrients.toArray(new Nutrient[0]);
        TOTAL = VALUES.length;
    }

    @Override
    public boolean tfg$isNegative() {
        return this.tfg$negative;
    }

    @Override
    public boolean tfg$isTransient() {
        return this.tfg$transient;
    }
}
