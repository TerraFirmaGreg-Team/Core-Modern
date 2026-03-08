package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.EntityEntry;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TFGEntities {

    public static void init() {
    }

    public static final EntityEntry<MoonRabbit> MOON_RABBIT = TFGCore.REGISTRATE.entity("moon_rabbit", MoonRabbit::makeMoonRabbit, MobCategory.CREATURE)
            .properties(p -> p.sized(1.0F, 1.3F).clientTrackingRange(10))
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .attributes(MoonRabbit::createAttributes)
            .renderer(() -> MoonRabbitRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoonRabbit::spawnRules)
            .register();

    public static final EntityEntry<TFCGlacianRam> GLACIAN_RAM = TFGCore.REGISTRATE.entity("glacian_ram", TFCGlacianRam::makeTFCGlacianRam, MobCategory.CREATURE)
            .properties(p -> p.sized(1f, 0.9f).clientTrackingRange(10))
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .attributes(TFCGlacianRam::createMobAttributes)
            .renderer(() -> TFCGlacianRamRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TFCGlacianRam::spawnRules)
            .register();

    public static final EntityEntry<TFCSniffer> SNIFFER = TFGCore.REGISTRATE.entity("sniffer", TFCSniffer::makeTFCSniffer, MobCategory.CREATURE)
            .properties(p -> p.sized(1.9f, 1.75f).clientTrackingRange(10))
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .attributes(TFCSniffer::createMobAttributes)
            .renderer(() -> TFCSnifferRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TFCSniffer::spawnRules)
            .register();

    public static final EntityEntry<TFCWraptor> WRAPTOR = TFGCore.REGISTRATE.entity("wraptor", TFCWraptor::makeTFCWraptor, MobCategory.CREATURE)
            .properties(p -> p.sized(0.8f, 2.2f).clientTrackingRange(10))
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .attributes(TFCWraptor::createMobAttributes)
            .renderer(() -> TFCWraptorRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TFCWraptor::spawnRules)
            .register();

    public static final EntityEntry<TFCSurfer> SURFER = TFGCore.REGISTRATE.entity("surfer", TFCSurfer::makeTFCSurfer, MobCategory.WATER_CREATURE)
            .properties(p -> p.sized(1.2f, 0.7f).clientTrackingRange(10))
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .attributes(TFCSurfer::getDefaultAttributes)
            .renderer(() -> c -> EntityRenderer.create(SurferModel::new, 0.6F).create(c))
            .spawnPlacement(SpawnPlacements.Type.IN_WATER, Heightmap.Types.OCEAN_FLOOR, TFCSurfer::spawnRules)
            .register();

    public static final EntityEntry<Rocket> TIER_1_DOUBLE_ROCKET = TFGCore.REGISTRATE.entity("tier_1_double_rocket", RocketHelper::makeRocket, MobCategory.MISC)
            .properties(p -> p.sized(1.1f, 4.6f).clientTrackingRange(10).fireImmune())
            .renderer(() -> RocketHelper::makeRocketRendererT1)
            .register();

    public static final EntityEntry<Rocket> TIER_2_DOUBLE_ROCKET = TFGCore.REGISTRATE.entity("tier_2_double_rocket", RocketHelper::makeRocket, MobCategory.MISC)
            .properties(p -> p.sized(1.1f, 4.8f).clientTrackingRange(10).fireImmune())
            .renderer(() -> RocketHelper::makeRocketRendererT2)
            .register();

    public static final EntityEntry<Rocket> TIER_3_DOUBLE_ROCKET = TFGCore.REGISTRATE.entity("tier_3_double_rocket", RocketHelper::makeRocket, MobCategory.MISC)
            .properties(p -> p.sized(1.1f, 5.5f).clientTrackingRange(10).fireImmune())
            .renderer(() -> RocketHelper::makeRocketRendererT3)
            .register();

    public static final EntityEntry<Rocket> TIER_4_DOUBLE_ROCKET = TFGCore.REGISTRATE.entity("tier_4_double_rocket", RocketHelper::makeRocket, MobCategory.MISC)
            .properties(p -> p.sized(1.1f, 7.0f).clientTrackingRange(10).fireImmune())
            .renderer(() -> RocketHelper::makeRocketRendererT4)
            .register();

    public static final EntityEntry<RNRPlow> RNR_PLOW = TFGCore.REGISTRATE.entity("rnr_plow", RNRPlow::new, MobCategory.MISC)
            .properties(p -> p.sized(1.3f, 1.4f))
            .renderer(() -> RNRPlowRenderer::new)
            .register();

    @SubscribeEvent
    public static void onEntityLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //RocketHelper.register(event);
        event.registerLayerDefinition(TFCGlacianRamModel.LAYER_LOCATION, TFCGlacianRamModel::createBodyLayer);
        event.registerLayerDefinition(RNRPlowModel.LAYER_LOCATION, RNRPlowModel::createLayer);
    }
}
