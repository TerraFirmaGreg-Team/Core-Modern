/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record BlockStateMapConfig(Map<Block, BlockState> states) implements FeatureConfiguration
{
    public static final Codec<BlockStateMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(

        Codecs.mapListCodec(Codecs.recordPairCodec(
            Codecs.BLOCK, "replace",
            Codecs.BLOCK_STATE, "with"
        )).fieldOf("states").forGetter(c -> c.states)
    ).apply(instance, BlockStateMapConfig::new));

    @Nullable
    public BlockState getState(BlockState stateIn)
    {
        return states.get(stateIn.getBlock());
    }
}
