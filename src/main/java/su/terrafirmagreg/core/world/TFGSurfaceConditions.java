package su.terrafirmagreg.core.world;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.registries.DeferredRegister;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.surface_conditions.Noise3DThresholdSurfaceConditionSource;

public class TFGSurfaceConditions {

    public static final DeferredRegister<Codec<? extends SurfaceRules.ConditionSource>> SURFACE_CONDITIONS = DeferredRegister
            .create(Registries.MATERIAL_CONDITION, TFGCore.MOD_ID);

    static {
        SURFACE_CONDITIONS.register("noise3d_threshold", Noise3DThresholdSurfaceConditionSource.CODEC::codec);
    }
}
