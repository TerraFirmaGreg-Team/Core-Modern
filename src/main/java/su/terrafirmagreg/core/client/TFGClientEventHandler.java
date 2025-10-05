package su.terrafirmagreg.core.client;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
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
        event.registerSpriteSet(TFGParticles.RAILGUN_BOOM.get(), RailgunBoomProvider::new);
        event.registerSpriteSet(TFGParticles.RAILGUN_AMMO.get(), RailgunAmmoProvider::new);
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR.get(), OreProspectorProvider::new);
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR_VEIN.get(), OreProspectorVeinProvider::new);
        event.registerSpriteSet(TFGParticles.COOLING_STEAM.get(), CoolingSteamProvider::new);
    }

    @SuppressWarnings("removal")
    public static void clientSetup(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            MenuScreens.register(TFGContainers.LARGE_NEST_BOX.get(), LargeNestBoxScreen::new);

            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICICLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.DRY_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.REFLECTOR_BLOCK.get(), RenderType.translucent());
        });
    }

    private static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }

    @SuppressWarnings("ConstantConditions")
    private static void onItemTooltip(ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        final List<Component> text = event.getToolTip();
        if (!stack.isEmpty()) {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null) {
                egg.addTooltipInfo(text);
            }
        }
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {

    }
}
