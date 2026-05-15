package su.terrafirmagreg.core.common.data;

import com.google.common.collect.ImmutableSet;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;

public final class TFGPoiTypes {
    public static final DeferredRegister<PoiType> TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, TFGCore.MOD_ID);
    public static final RegistryObject<PoiType> CLIMATE = TYPES.register("climate", () -> new PoiType(
            ImmutableSet.<BlockState>builder()
                    .addAll(states(Blocks.SNOW))
                    .addAll(states(TFCBlocks.SNOW_PILE.get()))
                    .addAll(states(Blocks.ICE))
                    .addAll(states(TFCBlocks.ICE_PILE.get()))
                    .addAll(states(TFCBlocks.ICICLE.get()))
                    .build(),
            0, 1));

    private static Iterable<BlockState> states(Block block) {
        return block.getStateDefinition().getPossibleStates();
    }
}
