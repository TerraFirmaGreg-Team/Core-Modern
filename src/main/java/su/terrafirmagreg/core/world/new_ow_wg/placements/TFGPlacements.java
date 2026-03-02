package su.terrafirmagreg.core.world.new_ow_wg.placements;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;

public final class TFGPlacements {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TFGCore.MOD_ID);

    public static final RegistryObject<PlacementModifierType<PlacementModifier>> TUFF_RING = register("tuff_cone", () -> TuffRingPlacement.CODEC);
    public static final RegistryObject<PlacementModifierType<PlacementModifier>> TUYA = register("tuya", () -> TuyaPlacement.CODEC);

    private static <C extends PlacementModifier> RegistryObject<PlacementModifierType<C>> register(String name, PlacementModifierType<C> codec) {
        return PLACEMENT_MODIFIERS.register(name, () -> codec);
    }
}
