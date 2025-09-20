package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(MinecartFurnace.class)
public abstract class MinecartFurnaceMixin {

    @Shadow
    @Mutable
    private static Ingredient INGREDIENT;

    static {
        INGREDIENT = Ingredient.of(TFCTags.Items.FORGE_FUEL);
    }
}
