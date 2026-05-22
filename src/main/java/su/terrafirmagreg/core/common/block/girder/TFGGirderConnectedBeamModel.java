package su.terrafirmagreg.core.common.block.girder;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.decoration.girder.GirderBlock;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.common.data.TFGPartialModels;

/***
 * Credit: Create: More Girders
 */
public class TFGGirderConnectedBeamModel extends TFGGirderConnectedTrussModel {
    private static final ModelProperty<PoleCtState> POLE_CT_PROPERTY = new ModelProperty<>();

    public TFGGirderConnectedBeamModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
            @NotNull BlockState state, @NotNull ModelData modelData) {
        ModelData data = super.getModelData(level, pos, state, modelData);
        if (state.getValue(GirderBlock.X) || state.getValue(GirderBlock.Z)) {
            return data;
        }
        boolean above = isSamePole(level.getBlockState(pos.above()), state);
        boolean below = isSamePole(level.getBlockState(pos.below()), state);
        if (!above && !below) {
            return data;
        }
        PoleCtState ctState;
        if (above && below) {
            ctState = PoleCtState.MIDDLE;
        } else if (above) {
            ctState = PoleCtState.BOTTOM;
        } else {
            ctState = PoleCtState.TOP;
        }
        return data.derive().with(POLE_CT_PROPERTY, ctState).build();
    }

    @Override
    protected List<BakedQuad> getBaseQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData data, @Nullable RenderType renderType) {
        PoleCtState ctState = data.get(POLE_CT_PROPERTY);
        if (ctState != null && state != null) {
            PartialModel partial = TFGPartialModels.getMetalGirderConnectedPole(state.getBlock(), ctState.modelKey());
            if (partial != null) {
                return partial.get().getQuads(state, side, rand, data, renderType);
            }
        }
        return super.getBaseQuads(state, side, rand, data, renderType);
    }

    private static boolean isSamePole(BlockState neighbor, BlockState self) {
        if (neighbor.getBlock() != self.getBlock())
            return false;
        return !neighbor.getValue(GirderBlock.X) && !neighbor.getValue(GirderBlock.Z);
    }

    public enum PoleCtState {
        TOP("top"),
        MIDDLE("middle"),
        BOTTOM("bottom");

        private final String key;

        PoleCtState(String key) {
            this.key = key;
        }

        public String modelKey() {
            return key;
        }
    }
}
