package su.terrafirmagreg.core.common.block.girder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Girders;

/***
 * Credit: Create: More Girders
 */
public class TFGGirderWrenchBehavior {
    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        Player player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (player.isSteppingCarefully())
            return;

        BlockState hovered = world.getBlockState(pos);
        if (!TFGBlocks_Girders.isAnyGirder(hovered) && !TFGBlocks_Girders.isAnyGirder(hovered))
            return;

        if (!AllItems.WRENCH.isIn(heldItem))
            return;

        Pair<Direction, Action> dirPair = getDirectionAndAction(result, world, pos);
        if (dirPair == null)
            return;

        Vec3 center = VecHelper.getCenterOf(pos);
        Vec3 edge = center.add(Vec3.atLowerCornerOf(dirPair.getFirst()
                .getNormal())
                .scale(0.4));
        Direction.Axis[] axes = Arrays.stream(Iterate.axes)
                .filter(axis -> axis != dirPair.getFirst()
                        .getAxis())
                .toArray(Direction.Axis[]::new);

        double normalMultiplier = dirPair.getSecond() == Action.PAIR ? 4 : 1;
        Vec3 corner1 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE)
                        .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE)
                        .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst()
                        .getNormal())
                        .scale(0.1 * normalMultiplier));

        normalMultiplier = dirPair.getSecond() == Action.HORIZONTAL ? 9 : 2;
        Vec3 corner2 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE)
                        .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE)
                        .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst()
                        .getOpposite()
                        .getNormal())
                        .scale(0.1 * normalMultiplier));

        Outliner.getInstance().showAABB("cmgGirderWrench", new AABB(corner1, corner2))
                .lineWidth(1 / 32f)
                .colored(new Color(95, 95, 255));
    }

    @Nullable
    private static Pair<Direction, Action> getDirectionAndAction(BlockHitResult result, Level world, BlockPos pos) {
        List<Pair<Direction, Action>> validDirections = getValidDirections(world, pos);

        if (validDirections.isEmpty())
            return null;

        // For any encased shaft variant (and copycat girder), only activate the bracket toggle
        // when looking at the top or bottom quarter of the block. This lets players wrench
        // the shaft out by looking at the middle 50%.
        BlockState hovered = world.getBlockState(pos);
        List<Pair<Direction, Action>> reachable;
        if (TFGBlocks_Girders.isAnyGirder(hovered)) {
            double hitY = result.getLocation().y - pos.getY();
            reachable = validDirections.stream()
                    .filter(pair -> {
                        if (pair.getFirst() == Direction.UP)
                            return hitY >= 0.75;
                        if (pair.getFirst() == Direction.DOWN)
                            return hitY <= 0.25;
                        return false;
                    })
                    .toList();
        } else {
            reachable = validDirections;
        }

        if (reachable.isEmpty())
            return null;

        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, result.getLocation(),
                reachable.stream()
                        .map(Pair::getFirst)
                        .toList());

        if (directions.isEmpty())
            return null;

        Direction dir = directions.get(0);
        return reachable.stream()
                .filter(pair -> pair.getFirst() == dir)
                .findFirst()
                .orElseGet(() -> Pair.of(dir, Action.SINGLE));
    }

    public static List<Pair<Direction, Action>> getValidDirections(BlockGetter level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);

        boolean isGirder = TFGBlocks_Girders.isAnyGirder(blockState);
        boolean isEncasedShaft = TFGBlocks_Girders.isAnyGirder(blockState);

        if (!isGirder && !isEncasedShaft)
            return Collections.emptyList();

        // vertical girder (pole) does not expose horizontal helpers
        if (isGirder && !blockState.getValue(GirderBlock.X) && !blockState.getValue(GirderBlock.Z))
            return Collections.emptyList();

        return Arrays.stream(Iterate.directions)
                .<Pair<Direction, Action>>mapMulti((direction, consumer) -> {
                    if (direction.getAxis() != Direction.Axis.Y)
                        return;

                    BlockState other = level.getBlockState(pos.relative(direction));
                    boolean otherIsGirder = TFGBlocks_Girders.isAnyGirder(other);
                    boolean otherIsEncasedShaft = TFGBlocks_Girders.isAnyGirder(other);
                    boolean otherIsHorizontalGirder = otherIsGirder
                            && other.getValue(GirderBlock.X) != other.getValue(GirderBlock.Z);

                    if (isEncasedShaft) {
                        // skip if neighbor is a CMG girder pole/cross (vertical orientation)
                        if (otherIsGirder && !otherIsHorizontalGirder)
                            return;
                        // pair with another encased shaft or a horizontal girder
                        if (otherIsEncasedShaft || otherIsHorizontalGirder) {
                            consumer.accept(Pair.of(direction, Action.PAIR));
                            return;
                        }
                        consumer.accept(Pair.of(direction, Action.SINGLE));
                        return;
                    }

                    // no other girder in target dir
                    if (!otherIsGirder) {
                        // pair with an encased shaft when this girder is horizontal (single-axis)
                        if (otherIsEncasedShaft) {
                            if (blockState.getValue(GirderBlock.X) != blockState.getValue(GirderBlock.Z))
                                consumer.accept(Pair.of(direction, Action.PAIR));
                            return;
                        }
                        if (!blockState.getValue(GirderBlock.X) ^ !blockState.getValue(GirderBlock.Z))
                            consumer.accept(Pair.of(direction, Action.SINGLE));
                        return;
                    }
                    // this girder is a pole or cross
                    if (blockState.getValue(GirderBlock.X) == blockState.getValue(GirderBlock.Z))
                        return;
                    // other girder is a pole or cross
                    if (other.getValue(GirderBlock.X) == other.getValue(GirderBlock.Z))
                        return;
                    // toggle up/down connection for both
                    consumer.accept(Pair.of(direction, Action.PAIR));

                })
                .toList();
    }

    public static boolean handleClick(Level level, BlockPos pos, BlockState state, BlockHitResult result) {
        Pair<Direction, Action> dirPair = getDirectionAndAction(result, level, pos);
        if (dirPair == null)
            return false;
        if (level.isClientSide)
            return true;

        boolean isGirder = TFGBlocks_Girders.isAnyGirder(state);
        boolean isEncasedShaft = TFGBlocks_Girders.isAnyGirder(state);

        if (isGirder && !state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z))
            return false;

        Direction dir = dirPair.getFirst();

        BlockPos otherPos = pos.relative(dir);
        BlockState other = level.getBlockState(otherPos);

        if (dir == Direction.UP) {
            level.setBlock(pos, cycleConnector(state, true), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR
                    && (TFGBlocks_Girders.isAnyGirder(other) || TFGBlocks_Girders.isAnyGirder(other)))
                level.setBlock(otherPos, cycleConnector(other, false), 2 | 16);
            return true;
        }

        if (dir == Direction.DOWN) {
            level.setBlock(pos, cycleConnector(state, false), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR
                    && (TFGBlocks_Girders.isAnyGirder(other) || TFGBlocks_Girders.isAnyGirder(other)))
                level.setBlock(otherPos, cycleConnector(other, true), 2 | 16);
            return true;
        }

        return true;
    }

    private static BlockState cycleConnector(BlockState state, boolean top) {
        if (TFGBlocks_Girders.isAnyGirder(state))
            return cycleByName(state, top ? "top" : "bottom");
        return postProcess(state.cycle(top ? GirderBlock.TOP : GirderBlock.BOTTOM));
    }

    private static BlockState cycleByName(BlockState state, String propertyName) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty(propertyName);
        if (prop instanceof BooleanProperty bp)
            return state.cycle(bp);
        return state;
    }

    private static BlockState postProcess(BlockState newState) {
        if (newState.getValue(GirderBlock.TOP) && newState.getValue(GirderBlock.BOTTOM))
            return newState;
        if (newState.getValue(GirderBlock.AXIS) != Direction.Axis.Y)
            return newState;
        return newState.setValue(GirderBlock.AXIS, newState.getValue(GirderBlock.X) ? Direction.Axis.X : Direction.Axis.Z);
    }

    private enum Action {
        SINGLE, PAIR, HORIZONTAL
    }
}
