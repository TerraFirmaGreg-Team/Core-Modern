package su.terrafirmagreg.core.common.data;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.Greenhouse;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.GTGreenhousePortBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;


public class TFGBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TFGCore.MOD_ID);

	public static final RegistryObject<BlockEntityType<GTGreenhousePortBlockEntity>> GT_GREENHOUSE_PORT =
			BLOCK_ENTITIES.register("gt_greenhouse_port", () ->
					BlockEntityType.Builder.of(
							GTGreenhousePortBlockEntity::new,
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.STAINLESS_STEEL).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.COPPER).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.IRON).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.RUSTED_IRON).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.OXIDIZED_COPPER).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.WEATHERED_COPPER).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.EXPOSED_COPPER).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.WEATHERED_TREATED_WOOD).get(Greenhouse.BlockType.PORT).get(),
							FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.TREATED_WOOD).get(Greenhouse.BlockType.PORT).get()
					).build(null)
			);

	public static final RegistryObject<BlockEntityType<LargeNestBoxBlockEntity>> LARGE_NEST_BOX =
			BLOCK_ENTITIES.register("large_nest_box", () ->
					BlockEntityType.Builder.of(LargeNestBoxBlockEntity::new,
							TFGBlocks.LARGE_NEST_BOX.get()).build(null)
			);

}