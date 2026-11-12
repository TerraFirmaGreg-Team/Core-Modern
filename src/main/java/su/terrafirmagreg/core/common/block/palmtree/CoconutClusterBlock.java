package su.terrafirmagreg.core.common.block.palmtree;

import java.util.List;
import java.util.function.Supplier;

import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

/**
 * Represents the coconut cluster block.
 * Special variation of cluster blocks that gives both green and brown coconuts.
 */
public class CoconutClusterBlock extends PalmClusterBlock {

    private record CoconutDrop(int count, Block type, boolean spawnFallingCoconut, boolean finalAge) {
    }

    private final Supplier<? extends Block> brownCoconut;
    private final Supplier<? extends Block> greenCoconut;

    /**
     * Constructs a new coconut cluster block.
     * @param properties Block properties.
     * @param tree Palm tree type.
     */
    public CoconutClusterBlock(Properties properties, PalmTrees tree) {
        super(properties, tree, tree.getClusterAgeProperty());
        this.brownCoconut = TFGBlocks_PalmTrees.BROWN_COCONUT;
        this.greenCoconut = TFGBlocks_PalmTrees.GREEN_COCONUT;
    }

    /**
     * Helper method to get the drop type, count and whether to spawn a falling coconut.
     * @param age Age of the cluster.
     * @return Drop type, count, whether to spawn a falling coconut, and whether the cluster is in its final age.
     */
    private CoconutDrop getDropForAge(int age) {
        Block type;
        boolean spawnFallingCoconut = false;
        boolean finalAge = false;
        int count = switch (age) {
            case 0, 1 -> {
                type = greenCoconut.get();
                yield 0;
            }
            case 2, 3 -> {
                type = greenCoconut.get();
                yield 3;
            }
            case 4 -> {
                type = brownCoconut.get();
                yield 3;
            }
            case 5 -> {
                type = brownCoconut.get();
                spawnFallingCoconut = true;
                yield 2;
            }
            case 6 -> {
                type = brownCoconut.get();
                spawnFallingCoconut = true;
                yield 1;
            }
            case 7 -> {
                type = brownCoconut.get();
                spawnFallingCoconut = true;
                finalAge = true;
                yield 0;
            }
            default -> {
                type = Blocks.AIR;
                yield 0;
            }
        };
        return new CoconutDrop(count, type, spawnFallingCoconut, finalAge);
    }

    /**
     * Allows clusters to break when hit by projectiles except snowballs.
     */
    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {

        BlockPos pos = hit.getBlockPos();

        if (!level.isClientSide() && !(projectile instanceof Snowball)) {
            CoconutDrop drop = getDropForAge(state.getValue(this.clusterAge));

            for (int i = 0; i < drop.count; i++) {
                spawnFallingPalmBlock((ServerLevel) level, pos, drop.type, 2);
            }

            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 2.0f, 0.1f);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, drop.type.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.5);
                if (drop.count == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.2);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        CoconutDrop drop = getDropForAge(state.getValue(this.clusterAge));

        if (drop.count != 0) {
            if (level instanceof ServerLevel serverLevel) {
                level.removeBlock(pos, false);
                final BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                getDrops(state, serverLevel, pos, entity, null, ItemStack.EMPTY).forEach(stack -> ItemHandlerHelper.giveItemToPlayer(player, stack));

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 0.5f, 2.0f);

                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, drop.type.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.5);

            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);

    }

    /**
     * Handles the growth and summoning of falling coconuts.
     */
    @Override
    public void onUpdate(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(ATTACHED)) {
            return;
        }

        ClimateRange range = tree.getClimateRange().get();
        int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        float temperature = Climate.getAverageTemperature(level, pos);
        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        final boolean dormant = tree.getStages()[month.ordinal()] == Lifecycle.DORMANT;

        if (range.checkBoth(hydration, temperature, false) && level instanceof ServerLevel serverLevel) {
            int age = state.getValue(this.clusterAge);
            CoconutDrop drop = getDropForAge(age);

            if (age < 4 && dormant && serverLevel.random.nextFloat() < 0.25f) {
                serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                spawnFallingPalmBlock(serverLevel, pos, TFGBlocks_PalmTrees.PALM_HUSK.get(), 1);
            }

            if (drop.finalAge) {
                serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                spawnFallingPalmBlock(serverLevel, pos, drop.type, 2);
            } else {
                age++;
                CoconutDrop nextDrop = getDropForAge(age);
                if (nextDrop.finalAge) {
                    serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                } else {
                    serverLevel.setBlock(pos, state.setValue(this.clusterAge, age), 2);
                }

                if (nextDrop.spawnFallingCoconut) {
                    spawnFallingPalmBlock(serverLevel, pos, nextDrop.type, 2);
                }
            }
        }
    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        if (!state.getValue(NATURAL))
            return;

        final ClimateRange range = tree.getClimateRange().get();
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, hydration));
        text.add(FarmlandBlock.getAverageTemperatureTooltip(level, pos, range, false));

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        Lifecycle lifecycle = tree.getStages()[month.ordinal()];
        text.add(Component.translatable("tfg.tooltip.lifecycle." + lifecycle.getSerializedName()));

        CoconutDrop drop = getDropForAge(state.getValue(this.clusterAge));
        if (drop.count != 0) {
            text.add(Component.translatable("tfg.tooltip.palm_tree.cluster_harvest",
                    Component.translatable(String.format("%s", drop.type.getName().getString())).withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE),
                    Component.literal(String.format("%d", drop.count)).withStyle(ChatFormatting.WHITE)));
        }
    }

}
