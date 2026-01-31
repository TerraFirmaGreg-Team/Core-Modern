package su.terrafirmagreg.core.client;

import static earth.terrarium.adastra.client.forge.AdAstraClientForge.ITEM_RENDERERS;

import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;

import su.terrafirmagreg.core.common.data.*;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.container.ArtisanTableScreen;
import su.terrafirmagreg.core.common.data.container.LargeNestBoxScreen;
import su.terrafirmagreg.core.common.data.events.AdvancedOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.NormalOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.OreProspectorEvent;
import su.terrafirmagreg.core.common.data.events.WeakOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.particles.*;

@Mod.EventBusSubscriber(modid = "tfg", value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TFGClientEventHandler {

    public static final ResourceLocation TFCMetalBlockTexturePattern = ResourceLocation
            .fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern");

    @SuppressWarnings("removal")
    public TFGClientEventHandler() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(TFGClientEventHandler::clientSetup);
        bus.addListener(TFGClientEventHandler::registerSpecialModels);

        forgeBus.addListener(TFGClientEventHandler::onItemTooltip);

        bus.register(this);
    }

    @SubscribeEvent
    public static void onTooltip(@NotNull ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        var stack = event.getItemStack();

        // Check Weak helpers
        for (WeakOreProspectorEventHelper helper : OreProspectorEvent.getWeakOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Normal helpers
        for (NormalOreProspectorEventHelper helper : OreProspectorEvent.getNormalOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Advanced helpers
        for (AdvancedOreProspectorEventHelper helper : OreProspectorEvent.getAdvancedOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                // Determine the mode key based on centersOnly
                String modeKey = helper.isCentersOnly()
                        ? "tfg.tooltip.ore_prospector_mode_vein"
                        : "tfg.tooltip.ore_prospector_mode_block";

                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));

                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_xray",
                        Component.translatable(modeKey) // pass the localized "vein" or "per block"
                ).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }
    }

    @SubscribeEvent
    public void registerParticles(@NotNull RegisterParticleProvidersEvent event) {
        // railgun animation
        event.registerSpriteSet(TFGParticles.RAILGUN_BOOM.get(), RailgunBoomProvider::new);
        event.registerSpriteSet(TFGParticles.RAILGUN_AMMO.get(), RailgunAmmoProvider::new);
        // prospector
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR.get(), OreProspectorProvider::new);
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR_VEIN.get(), OreProspectorVeinProvider::new);
        event.registerSpriteSet(TFGParticles.COOLING_STEAM.get(), CoolingSteamProvider::new);
        // martian wind
        event.registerSpriteSet(TFGParticles.DARK_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xbe6621))); // avg color of red sand
        event.registerSpriteSet(TFGParticles.MEDIUM_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xc48456))); // avg color of ad astra mars sand
        event.registerSpriteSet(TFGParticles.LIGHT_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xcf9f59))); // avg color of ad astra venus sand
        // Other
        event.registerSpriteSet(TFGParticles.FISH_SCHOOL.get(), FishSchoolProvider::new);
    }

    @SuppressWarnings("removal")
    public static void clientSetup(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            MenuScreens.register(TFGContainers.LARGE_NEST_BOX.get(), LargeNestBoxScreen::new);
            MenuScreens.register(TFGContainers.ARTISAN_TABLE.get(), ArtisanTableScreen::new);

            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.SULFUR_FUMES.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.SULFUR_FUMES.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.GEYSER_SLURRY.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.GEYSER_SLURRY.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICICLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.DRY_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.REFLECTOR_BLOCK.get(), RenderType.translucent());
        });

        onRegisterItemRenderers(ITEM_RENDERERS::put);
    }

    public static void onRegisterItemRenderers(BiConsumer<Item, BlockEntityWithoutLevelRenderer> consumer) {
        consumer.accept(TFGItems.TIER_1_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE));
        consumer.accept(TFGItems.TIER_2_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE));
        consumer.accept(TFGItems.TIER_3_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE));
        consumer.accept(TFGItems.TIER_4_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE));
    }

    private static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {
    }

    @SuppressWarnings("ConstantConditions")
    private static void onItemTooltip(ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        final List<Component> text = event.getToolTip();
        if (!stack.isEmpty()) {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null) {
                egg.addTooltipInfo(text);
                return;
            }

            var foodProperties = stack.getFoodProperties(event.getEntity());
            if (foodProperties != null) {
                foodProperties.getEffects().forEach(effect -> event.getToolTip().add(getTooltip(effect.getFirst())));
            }
        }
    }

    // These are taken from TFC Aged Alcohol

    private static Component getTooltip(MobEffectInstance effectInstance) {
        return Component.literal(effectInstance.getEffect().getDisplayName().getString()
                + displayedPotency(effectInstance.getAmplifier()) + "(" + formatDuration(effectInstance) + ")").withStyle(effectInstance.getEffect().getCategory().getTooltipFormatting());
    }

    private static String displayedPotency(int amplifier) {
        return switch (amplifier + 1) {
            case 2 -> " II ";
            case 3 -> " III ";
            default -> " ";
        };
    }

    private static String formatDuration(MobEffectInstance effect) {
        return StringUtil.formatTickDuration(Mth.floor(effect.getDuration()));
    }
}
