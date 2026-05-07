package su.terrafirmagreg.core.common.block.asphalt;

import com.therighthon.rnr.RNRHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("deprecation")
public class AsphaltRoadStairsBlock extends StairBlock {
    public static final EnumProperty<AsphaltRoadMarkingColor> HORIZONTAL_COLOR = EnumProperty.create("horizontal_color", AsphaltRoadMarkingColor.class);
    public static final EnumProperty<AsphaltRoadMarkingColor> VERTICAL_COLOR = EnumProperty.create("vertical_color", AsphaltRoadMarkingColor.class);
    public static final EnumProperty<AsphaltRoadTopLayer> TOP_LAYER = EnumProperty.create("top_layer", AsphaltRoadTopLayer.class);

    public AsphaltRoadStairsBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
        registerDefaultState(defaultBlockState()
                .setValue(HORIZONTAL_COLOR, AsphaltRoadMarkingColor.NONE)
                .setValue(VERTICAL_COLOR, AsphaltRoadMarkingColor.NONE)
                .setValue(TOP_LAYER, AsphaltRoadTopLayer.HORIZONTAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_COLOR, VERTICAL_COLOR, TOP_LAYER);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = RNRHelpers.blockModRecipeCompatible(state, level, pos, player, hand, hit);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }
}
