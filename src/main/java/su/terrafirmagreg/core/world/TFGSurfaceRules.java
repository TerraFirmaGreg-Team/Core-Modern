package su.terrafirmagreg.core.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.registries.DeferredRegister;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.surface_rule.NeedsPostProcessingSurfaceRuleSource;

public class TFGSurfaceRules {
	public static final DeferredRegister<Codec<? extends SurfaceRules.RuleSource>> SURFACE_RULES = DeferredRegister.create(Registries.MATERIAL_RULE, TFGCore.MOD_ID);

	static {
		SURFACE_RULES.register("needs_post_processing", NeedsPostProcessingSurfaceRuleSource.CODEC::codec);
	}
}
