package su.terrafirmagreg.core.common.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRam;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamModel;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamRenderer;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbit;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbitRenderer;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSnifferRenderer;

import java.util.Locale;

public class TFGEntities {

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TFGCore.MOD_ID);

	public static final RegistryObject<EntityType<MoonRabbit>> MOON_RABBIT = register("moon_rabbit", EntityType.Builder.of(MoonRabbit::makeMoonRabbit, MobCategory.CREATURE).sized(1.0F, 1.3F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<TFCGlacianRam>> GLACIAN_RAM = register("glacian_ram",EntityType.Builder.of(TFCGlacianRam::makeTFCGlacianRam, MobCategory.CREATURE).sized(0.9f, 1.3f).clientTrackingRange(10));
	public static final RegistryObject<EntityType<TFCSniffer>> SNIFFER = register("sniffer", EntityType.Builder.of(TFCSniffer::makeTFCSniffer, MobCategory.CREATURE).sized(1.9F, 1.75F).clientTrackingRange(10));

	public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder)
	{
		return register(name, builder, true);
	}

	public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder, boolean serialize)
	{
		final String id = name.toLowerCase(Locale.ROOT);

		return ENTITIES.register(id, () -> {
			if (!serialize) builder.noSave();
			return builder.build(TFGCore.MOD_ID + ":" + id);
		});
	}

	public static void onAttributes(EntityAttributeCreationEvent event)
	{
		event.put(MOON_RABBIT.get(), MoonRabbit.createAttributes().build());
		event.put(GLACIAN_RAM.get(), TFCGlacianRam.createMobAttributes().build());
		event.put(SNIFFER.get(), TFCSniffer.createMobAttributes().build());
	}

	public static void onSpawnPlacement(SpawnPlacementRegisterEvent event)
	{
		event.register(MOON_RABBIT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoonRabbit::spawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
	}

	public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(MOON_RABBIT.get(), MoonRabbitRenderer::new);
		event.registerEntityRenderer(GLACIAN_RAM.get(), TFCGlacianRamRenderer::new);
		event.registerEntityRenderer(SNIFFER.get(), TFCSnifferRenderer::new);

	  //  event.registerBlockEntityRenderer(TFGBlockEntities.LARGE_NEST_BOX.get(), ctx -> new LargeNestBoxBlockEntityRenderer());
	}

	public static void onEntityLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event){
		event.registerLayerDefinition(TFCGlacianRamModel.LAYER_LOCATION, TFCGlacianRamModel::createBodyLayer);

	}
}
