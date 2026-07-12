package su.terrafirmagreg.core.common.block.palmtree;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.rock.IFallableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PalmHuskBlock extends GroundcoverBlock implements IFallableBlock {

    public PalmHuskBlock(ExtendedProperties properties, VoxelShape shape, @Nullable Supplier<? extends Item> pickBlock) {
        super(properties, shape, pickBlock);
    }

    public static PalmHuskBlock twig(ExtendedProperties properties) {
        return new PalmHuskBlock(properties.flammable(60, 30), TWIG, null);
    }

    @Override
    public void onceFinishedFalling(Level level, BlockPos pos, FallingBlockEntity entity) {
    }
}
