package su.terrafirmagreg.core.common.data;

import java.util.Locale;

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
import net.wanmine.wab.entity.render.EntityRenderer;
import net.wanmine.wab.entity.render.model.SurferModel;

import earth.terrarium.adastra.common.entities.vehicles.Rocket;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlow;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlowModel;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlowRenderer;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRam;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamModel;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamRenderer;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbit;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbitRenderer;
import su.terrafirmagreg.core.common.data.entities.rocket.RocketHelper;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSnifferRenderer;
import su.terrafirmagreg.core.common.data.entities.surfer.TFCSurfer;
import su.terrafirmagreg.core.common.data.entities.wraptor.TFCWraptor;
import su.terrafirmagreg.core.common.data.entities.wraptor.TFCWraptorRenderer;

public class TFGEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            TFGCore.MOD_ID);

    public static final RegistryObject<EntityType<MoonRabbit>> MOON_RABBIT = register("moon_rabbit", EntityType.Builder
            .of(MoonRabbit::makeMoonRabbit, MobCategory.CREATURE).sized(1.0F, 1.3F).clientTrackingRange(10));
    public static final RegistryObject<EntityType<TFCGlacianRam>> GLACIAN_RAM = register("glacian_ram",
            EntityType.Builder.of(TFCGlacianRam::makeTFCGlacianRam, MobCategory.CREATURE).sized(1f, .9f)
                    .clientTrackingRange(10));
    public static final RegistryObject<EntityType<TFCSniffer>> SNIFFER = register("sniffer", EntityType.Builder
            .of(TFCSniffer::makeTFCSniffer, MobCategory.CREATURE).sized(1.9F, 1.75F).clientTrackingRange(10));
    public static final RegistryObject<EntityType<TFCWraptor>> WRAPTOR = register("wraptor", EntityType.Builder
            .of(TFCWraptor::makeTFCWraptor, MobCategory.CREATURE).sized(0.8F, 2.2F).clientTrackingRange(10));
    public static final RegistryObject<EntityType<TFCSurfer>> SURFER = register("surfer", EntityType.Builder
            .of(TFCSurfer::makeTFCSurfer, MobCategory.WATER_CREATURE).sized(1.2F, 0.7F).clientTrackingRange(10));

    public static final RegistryObject<EntityType<Rocket>> TIER_1_DOUBLE_ROCKET = register("tier_1_double_rocket", EntityType.Builder
            .of(RocketHelper::makeRocket, MobCategory.MISC)
            .sized(1.1F, 4.6F)
            .clientTrackingRange(10)
            .fireImmune());

    public static final RegistryObject<EntityType<Rocket>> TIER_2_DOUBLE_ROCKET = register("tier_2_double_rocket", EntityType.Builder
            .of(RocketHelper::makeRocket, MobCategory.MISC)
            .sized(1.1F, 4.8F)
            .clientTrackingRange(10)
            .fireImmune());

    public static final RegistryObject<EntityType<Rocket>> TIER_3_DOUBLE_ROCKET = register("tier_3_double_rocket", EntityType.Builder
            .of(RocketHelper::makeRocket, MobCategory.MISC)
            .sized(1.1F, 5.5F)
            .clientTrackingRange(10)
            .fireImmune());

    public static final RegistryObject<EntityType<Rocket>> TIER_4_DOUBLE_ROCKET = register("tier_4_double_rocket", EntityType.Builder
            .of(RocketHelper::makeRocket, MobCategory.MISC)
            .sized(1.1F, 7.0F)
            .clientTrackingRange(10)
            .fireImmune());

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name,
            EntityType.Builder<E> builder) {
        return register(name, builder, true);
    }

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder,
            boolean serialize) {
        final String id = name.toLowerCase(Locale.ROOT);

        return ENTITIES.register(id, () -> {
            if (!serialize)
                builder.noSave();
            return builder.build(TFGCore.MOD_ID + ":" + id);
        });
    }

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(MOON_RABBIT.get(), MoonRabbit.createAttributes().build());
        event.put(GLACIAN_RAM.get(), TFCGlacianRam.createMobAttributes().build());
        event.put(SNIFFER.get(), TFCSniffer.createMobAttributes().build());
        event.put(WRAPTOR.get(), TFCWraptor.createMobAttributes().build());
        event.put(SURFER.get(), TFCSurfer.getDefaultAttributes().build());
    }

    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(
                MOON_RABBIT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoonRabbit::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                GLACIAN_RAM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCGlacianRam::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                SNIFFER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCSniffer::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                WRAPTOR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCWraptor::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                SURFER.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                TFCSurfer::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MOON_RABBIT.get(), MoonRabbitRenderer::new);
        event.registerEntityRenderer(GLACIAN_RAM.get(), TFCGlacianRamRenderer::new);
        event.registerEntityRenderer(SNIFFER.get(), TFCSnifferRenderer::new);
        event.registerEntityRenderer(WRAPTOR.get(), TFCWraptorRenderer::new);

        event.registerEntityRenderer(SURFER.get(), EntityRenderer.create(SurferModel::new, 0.6F));
        event.registerEntityRenderer(RNR_PLOW.get(), RNRPlowRenderer::new);

        event.registerEntityRenderer(TIER_1_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT1);
        event.registerEntityRenderer(TIER_2_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT2);
        event.registerEntityRenderer(TIER_3_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT3);
        event.registerEntityRenderer(TIER_4_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT4);

        // event.registerBlockEntityRenderer(TFGBlockEntities.LARGE_NEST_BOX.get(), ctx -> new
        // LargeNestBoxBlockEntityRenderer());
    }

    public static void onEntityLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //RocketHelper.register(event);
        event.registerLayerDefinition(TFCGlacianRamModel.LAYER_LOCATION, TFCGlacianRamModel::createBodyLayer);
        event.registerLayerDefinition(RNRPlowModel.LAYER_LOCATION, RNRPlowModel::createLayer);
    }

    public static final RegistryObject<EntityType<RNRPlow>> RNR_PLOW = ENTITIES.register("rnr_plow",
            () -> EntityType.Builder.of(RNRPlow::new, MobCategory.MISC)
                    .sized(1.3F, 1.4F)
                    .build(TFGCore.MOD_ID + ":rnr_plow"));
}
