/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common;

import com.google.common.collect.ImmutableSet;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TFCPoiTypes
{
	public static final DeferredRegister<PoiType> TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, TerraFirmaCraft.MOD_ID);
	public static final RegistryObject<PoiType> CLIMATE = TYPES.register("climate", () -> new PoiType(
		ImmutableSet.<BlockState>builder()
			.addAll(states(Blocks.SNOW))
			.addAll(states(TFCBlocks.SNOW_PILE.get()))
			.addAll(states(Blocks.ICE))
			.addAll(states(TFCBlocks.ICE_PILE.get()))
			.addAll(states(TFCBlocks.ICICLE.get()))
			.build(),
		0, 1));

    private static Iterable<BlockState> states(Block block)
    {
        return block.getStateDefinition().getPossibleStates();
    }
}
