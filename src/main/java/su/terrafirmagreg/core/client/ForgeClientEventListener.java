package su.terrafirmagreg.core.client;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.util.Drinkable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.data.TFGPlant;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.events.AdvancedOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.NormalOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.OreProspectorEvent;
import su.terrafirmagreg.core.common.data.events.WeakOreProspectorEventHelper;
import su.terrafirmagreg.core.common.perf.SupportCache;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {
    private static final TagKey<Fluid> TFC_DRINKABLE_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfc", "drinkables"));

    /**
     * Evict client-side SupportCache chunk to prevent stale cache info.
     * Clients don't get placement/removal updates for chunks that aren't in range, so we can't trust the cache
     * for those chunks.
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            ChunkPos pos = event.getChunk().getPos();
            SupportCache.forLevel(level).evictChunk(pos.x, pos.z);
        }
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

    @SuppressWarnings({ "deprecation", "ConstantConditions" })
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
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

            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(capability -> {
                FluidStack fluidStack = capability.getFluidInTank(0);
                if (fluidStack.getFluid().is(TFC_DRINKABLE_TAG) && !ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).getNamespace().equals("tfcagedalcohol")) {
                    Drinkable drink = Drinkable.get(fluidStack.getFluid());
                    if (drink != null) {
                        drink.getEffects().forEach(effect -> event.getToolTip().add(getTooltip(new MobEffectInstance(effect.type(), effect.duration(), effect.amplifier()))));
                    }
                }
            });
        }
    }

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.Block event) {
        final BlockColor grassColor = (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final BlockColor tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final BlockColor grassBlockColor = (state, level, pos, tintIndex) -> state.getValue(ConnectedGrassBlock.SNOWY) || tintIndex != 1 ? -1 : grassColor.getColor(state, level, pos, tintIndex);

        event.register(tallGrassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());
        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD_PLANT).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.TANK_BROMELIAD).get());
        event.register(grassBlockColor,
                TFGBlocks_Earth.ALFISOL_GRASS.get(),
                TFGBlocks_Earth.ALFISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.OXISOL_GRASS.get(),
                TFGBlocks_Earth.OXISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.PODZOL_GRASS.get(),
                TFGBlocks_Earth.PODZOL_CLAY_GRASS.get());
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event) {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);

        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());
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
