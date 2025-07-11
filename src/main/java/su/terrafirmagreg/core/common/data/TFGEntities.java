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
import su.terrafirmagreg.core.common.data.entities.MoonRabbit;
import su.terrafirmagreg.core.common.data.entities.MoonRabbitRenderer;

import java.util.Locale;

public class TFGEntities {

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TFGCore.MOD_ID);

	public static final RegistryObject<EntityType<MoonRabbit>> MOON_RABBIT = register("moon_rabbit", EntityType.Builder.of(MoonRabbit::makeMoonRabbit, MobCategory.CREATURE).sized(1.0F, 1.3F).clientTrackingRange(10));


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
	}

	public static void onSpawnPlacement(SpawnPlacementRegisterEvent event)
	{
		event.register(MOON_RABBIT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoonRabbit::spawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
	}

	public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(MOON_RABBIT.get(), MoonRabbitRenderer::new);
	}
}
