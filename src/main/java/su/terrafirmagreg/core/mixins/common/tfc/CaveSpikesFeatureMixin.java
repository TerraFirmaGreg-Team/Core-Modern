package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.world.feature.cave.CaveSpikesFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.tags.ModBlockTags;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGFluids;

@Mixin(value = CaveSpikesFeature.class, remap = false)
public class CaveSpikesFeatureMixin {

    @ModifyArg(method = "placeSmallSpike(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;F)V", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;isBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/tags/TagKey;)Z"), index = 1)
    private TagKey<Block> tfg$placeSmallSpike(TagKey<Block> tag, @Local(argsOnly = true) WorldGenLevel level) {
        ResourceKey<Level> dim = level.getLevel().dimension();

        if (dim == Level.OVERWORLD) {
            return BlockTags.BASE_STONE_OVERWORLD;
        } else if (dim == Planet.MARS) {
            return ModBlockTags.MARS_STONE_REPLACEABLES;
        } else if (dim == Planet.VENUS) {
            return ModBlockTags.VENUS_STONE_REPLACEABLES;
        } else if (dim == Planet.MERCURY) {
            return ModBlockTags.MERCURY_STONE_REPLACEABLES;
        } else if (dim == Planet.GLACIO) {
            return ModBlockTags.GLACIO_STONE_REPLACEABLES;
        } else {
            return Tags.Blocks.STONE;
        }
    }

    /**
     * @author Pyritie
     * @reason Uses the different mars water fluid property, and adds support for normal air and mars water
     */
    @Overwrite
    protected void replaceBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
        final Block block = level.getBlockState(pos).getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR) {
            level.setBlock(pos, state, 3);
        } else if (block == Blocks.WATER || block == TFCBlocks.RIVER_WATER.get()) {
            level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.WATER)), 3);
        } else if (block == Blocks.LAVA) {
            level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.LAVA)), 3);
        } else if (block == TFGBlocks.MARS_WATER.get()) {
            level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(TFGFluids.MARS_WATER.getSource())), 3);
        }
    }

    /**
     * @author Pyritie
     * @reason See above
     */
    @Overwrite
    protected void replaceBlockWithoutFluid(WorldGenLevel level, BlockPos pos, BlockState state) {
        final Block block = level.getBlockState(pos).getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.WATER || block == TFCBlocks.RIVER_WATER.get()
                || block == Blocks.LAVA || block == TFGBlocks.MARS_WATER.get()) {
            level.setBlock(pos, state, 3);
        }
    }
}
