package su.terrafirmagreg.core.world.surface_rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.dries007.tfc.world.Codecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;

import java.util.List;

public final class FluorapatiteSurfaceRuleSource implements SurfaceRules.RuleSource {
	@Getter
	private final boolean isSurface;
	private final List<BlockState> palette;
	@Getter
	private final BlockState fallback;

	private FluorapatiteSurfaceRuleSource(boolean isSurface, BlockState fallback)
	{
		this.isSurface = isSurface;
		this.fallback = fallback;
	}

	public static final KeyDispatchDataCodec<FluorapatiteSurfaceRuleSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.create(inst -> inst.group(
		Codec.BOOL.fieldOf("is_surface").forGetter(FluorapatiteSurfaceRuleSource::isSurface),
		Codecs.BLOCK_STATE.fieldOf("fallback").forGetter(FluorapatiteSurfaceRuleSource::getFallback)).apply(inst, FluorapatiteSurfaceRuleSource::new)));


	@Override
	public @NotNull KeyDispatchDataCodec<FluorapatiteSurfaceRuleSource> codec() {
		return CODEC;
	}


	@Override
	public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
		return new FluorapatiteSurfaceRuleSource.FluorapatiteRule(rawRockWeight, fullPalette, () -> rockRuleSource.apply(context));
	}

	/*
	If is surface: use the rock type's sand color, map it to a fluorapatite color, use that sand block
	If is not surface: take surface depth, add 2 to array index mod 6 (so there will only be 3 sandstone colors including sand color)

	Add parameter so the surface rule doesn't have to figure out the index
	 */



}


