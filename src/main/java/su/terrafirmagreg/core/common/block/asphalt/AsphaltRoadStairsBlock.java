package su.terrafirmagreg.core.common.block.asphalt;

import java.util.function.Supplier;

import com.therighthon.rnr.RNRHelpers;
import com.therighthon.rnr.RoadsAndRoofs;
import com.therighthon.rnr.common.block.PathStairBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("deprecation")
public class AsphaltRoadStairsBlock extends PathStairBlock implements SimpleWaterloggedBlock {

    public AsphaltRoadStairsBlock(Supplier<BlockState> referenceBlockState, BlockBehaviour.Properties properties) {
        super(referenceBlockState, properties, RoadsAndRoofs.FAST_PATH_SPEED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = RNRHelpers.blockModRecipeCompatible(state, level, pos, player, hand, hit);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }
}
