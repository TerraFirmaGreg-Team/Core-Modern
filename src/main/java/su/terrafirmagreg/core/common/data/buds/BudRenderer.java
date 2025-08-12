package su.terrafirmagreg.core.common.data.buds;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BudRenderer {
	private static final Set<BudRenderer> MODELS = new HashSet<>();

	public static void create(Block block) {
		MODELS.add(new BudRenderer(block));
	}

	public static void reinitModels() {
		for (BudRenderer model : MODELS) {
			ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
			ResourceLocation modelId = blockId.withPrefix("block/");

			GTDynamicResourcePack.addBlockModel(modelId, new DelegatedModel(GTCEu.id("block/bud_indicator")));
			GTDynamicResourcePack.addBlockState(blockId, MultiVariantGenerator
				 .multiVariant(model.block, Variant.variant().with(VariantProperties.MODEL, modelId))
				 .with(PropertyDispatch.property(BlockStateProperties.FACING)
				     .select(Direction.DOWN, Variant.variant())
				     .select(Direction.UP, Variant.variant())
				     .select(Direction.NORTH,
				  	     Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
				     .select(Direction.SOUTH,
				  	     Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0))
				     .select(Direction.WEST,
				  	     Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
				     .select(Direction.EAST,
				  	     Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))));
			GTDynamicResourcePack.addItemModel(blockId, new DelegatedModel(modelId));
		}
	}

	private final Block block;

	protected BudRenderer(Block block) {
		this.block = block;
	}
}
