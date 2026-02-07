package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockDisplayCategory;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.common.data.TFGBlocks;

public class TuffRegistryRock implements RegistryRock {
    @Override
    public @NotNull RockDisplayCategory displayCategory() {
        return RockDisplayCategory.SEDIMENTARY;
    }

    @Override
    public @NotNull Supplier<? extends Block> getBlock(Rock.@NotNull BlockType blockType) {
        return () -> Blocks.TUFF;
    }

    @Override
    public @NotNull Supplier<? extends Block> getAnvil() {
        return () -> null;
    }

    @Override
    public @NotNull Supplier<? extends SlabBlock> getSlab(Rock.@NotNull BlockType blockType) {
        return TFGBlocks.TUFF_SLAB;
    }

    @Override
    public @NotNull Supplier<? extends StairBlock> getStair(Rock.@NotNull BlockType blockType) {
        return TFGBlocks.TUFF_STAIRS;
    }

    @Override
    public @NotNull Supplier<? extends WallBlock> getWall(Rock.@NotNull BlockType blockType) {
        return TFGBlocks.TUFF_WALL;
    }

    @Override
    public @NotNull String getSerializedName() {
        return "tuff";
    }

    @Override
    public @NotNull MapColor color() {
        return MapColor.TERRACOTTA_GRAY;
    }
}
