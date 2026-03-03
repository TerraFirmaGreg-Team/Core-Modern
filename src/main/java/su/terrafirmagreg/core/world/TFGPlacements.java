package su.terrafirmagreg.core.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.placements.CinderConePlacement;
import su.terrafirmagreg.core.world.placements.IntertidalPlacement;
import su.terrafirmagreg.core.world.placements.TuffRingPlacement;
import su.terrafirmagreg.core.world.placements.TuyaPlacement;

public final class TFGPlacements {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TFGCore.MOD_ID);

    public static final RegistryObject<PlacementModifierType<TuffRingPlacement>> TUFF_RING = register("tuff_cone", () -> TuffRingPlacement.PLACEMENT_CODEC);
    public static final RegistryObject<PlacementModifierType<TuyaPlacement>> TUYA = register("tuya", () -> TuyaPlacement.PLACEMENT_CODEC);
    public static final RegistryObject<PlacementModifierType<IntertidalPlacement>> INTERTIDAL = register("intertidal", () -> IntertidalPlacement.PLACEMENT_CODEC);
    public static final RegistryObject<PlacementModifierType<CinderConePlacement>> CINDER_CONE = register("cinder_cone", () -> CinderConePlacement.PLACEMENT_CODEC);

    private static <C extends PlacementModifier> RegistryObject<PlacementModifierType<C>> register(String name, PlacementModifierType<C> codec) {
        return PLACEMENT_MODIFIERS.register(name, () -> codec);
    }
}
