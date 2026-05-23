package su.terrafirmagreg.core.common.block.asphalt.event;

import org.jetbrains.annotations.NotNull;

import com.therighthon.rnr.common.RNRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocksAsphalt;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadPourEvent {

    private enum AsphaltMixInteraction {
        NONE,
        FIELD_POUR,
        PATCH_BASE_TO_HOT
    }

    private AsphaltRoadPourEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        BlockState state = level.getBlockState(event.getPos());

        if (!heldItemContainsAsphaltMix(held)) {
            return;
        }

        AsphaltMixInteraction mode = resolveAsphaltMixInteraction(level, event.getPos(), state, player);
        if (mode == AsphaltMixInteraction.NONE) {
            return;
        }

        int costMb = mode == AsphaltMixInteraction.FIELD_POUR
                ? AsphaltRoadHelper.FIELD_POUR_MB
                : AsphaltRoadHelper.PATCH_POUR_MB;
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, costMb)) {
            return;
        }

        stopVanillaAndItemUse(event);

        if (!level.isClientSide()) {
            switch (mode) {
                case FIELD_POUR -> handleFieldPourOnServer(event, level, player, hand, held);
                case PATCH_BASE_TO_HOT -> handlePatchBaseOnServer(event, level, player, hand, held);
                default -> {
                }
            }
        }
    }

    private static AsphaltMixInteraction resolveAsphaltMixInteraction(Level level, BlockPos clicked, BlockState clickedState, Player player) {
        if (!clickedState.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return AsphaltMixInteraction.NONE;
        }
        if (player.isShiftKeyDown()) {
            return AsphaltMixInteraction.PATCH_BASE_TO_HOT;
        }
        if (player.blockPosition().equals(clicked)) {
            return AsphaltMixInteraction.NONE;
        }
        BlockState above = level.getBlockState(clicked.above());
        if (!above.isAir() && !above.canBeReplaced()) {
            return AsphaltMixInteraction.NONE;
        }
        return AsphaltMixInteraction.FIELD_POUR;
    }

    private static void stopVanillaAndItemUse(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.DENY);
    }

    private static void handleFieldPourOnServer(PlayerInteractEvent.RightClickBlock event, Level level, Player player, InteractionHand hand, ItemStack held) {
        BlockPos clicked = event.getPos();
        BlockState baseState = level.getBlockState(clicked);
        if (!baseState.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return;
        }
        BlockPos pourPos = clicked.above();
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, AsphaltRoadHelper.FIELD_POUR_MB)) {
            return;
        }
        BlockState space = level.getBlockState(pourPos);
        if (!space.isAir() && !space.canBeReplaced()) {
            return;
        }
        BlockState pourState = TFGBlocksAsphalt.ASPHALT_ROAD_POURING.getDefaultState();
        if (!level.setBlock(pourPos, pourState, Block.UPDATE_ALL)) {
            return;
        }
        if (!player.getAbilities().instabuild && !tryConsumeFluidMb(player, hand, held, AsphaltRoadHelper.FIELD_POUR_MB)) {
            level.removeBlock(pourPos, false);
            return;
        }
        playAsphaltMixPourSound(level, pourPos);
        player.swing(hand, true);
    }

    private static void handlePatchBaseOnServer(PlayerInteractEvent.RightClickBlock event, Level level, Player player, InteractionHand hand, ItemStack held) {
        BlockPos clicked = event.getPos();
        BlockState baseState = level.getBlockState(clicked);
        if (!baseState.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return;
        }
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, AsphaltRoadHelper.PATCH_POUR_MB)) {
            return;
        }
        if (!level.setBlock(clicked, TFGBlocksAsphalt.ASPHALT_ROAD_HOT.getDefaultState(), Block.UPDATE_ALL)) {
            return;
        }
        if (!player.getAbilities().instabuild && !tryConsumeFluidMb(player, hand, held, AsphaltRoadHelper.PATCH_POUR_MB)) {
            level.setBlock(clicked, baseState, Block.UPDATE_ALL);
            return;
        }
        playAsphaltMixPourSound(level, clicked);
        player.swing(hand, true);
    }

    private static boolean heldItemContainsAsphaltMix(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(AsphaltRoadPourEvent::handlerHasNonEmptyAsphaltMix).orElse(false)) {
            return true;
        }
        return FluidUtil.getFluidContained(stack)
                .map(fs -> isAsphaltMixFluid(fs.getFluid()) && !fs.isEmpty())
                .orElse(false);
    }

    private static boolean handlerHasNonEmptyAsphaltMix(IFluidHandler handler) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack inTank = handler.getFluidInTank(i);
            if (!inTank.isEmpty() && isAsphaltMixFluid(inTank.getFluid())) {
                return true;
            }
        }
        return false;
    }

    private static boolean canAffordFluidDrain(ItemStack stack, int mb) {
        return simulateFluidDrain(stack, mb) >= mb;
    }

    private static int simulateFluidDrain(ItemStack stack, int mb) {
        Fluid mix = TFGFluids.ASPHALT_MIX.getSource();
        FluidStack want = new FluidStack(mix, mb);
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.drain(want, FluidAction.SIMULATE).getAmount())
                .orElseGet(() -> FluidUtil.getFluidContained(stack)
                        .filter(fs -> isAsphaltMixFluid(fs.getFluid()))
                        .map(FluidStack::getAmount)
                        .orElse(0));
    }

    private static boolean tryConsumeFluidMb(Player player, InteractionHand hand, ItemStack held, int mb) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Fluid mix = TFGFluids.ASPHALT_MIX.getSource();
        FluidStack want = new FluidStack(mix, mb);
        return held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(handler -> {
            if (handler.drain(want, FluidAction.SIMULATE).getAmount() < mb) {
                return false;
            }
            FluidStack drained = handler.drain(want, FluidAction.EXECUTE);
            if (drained.getAmount() < mb) {
                return false;
            }
            ItemStack updated = handler.getContainer();
            ItemStack inHand = player.getItemInHand(hand);
            if (inHand != updated) {
                player.setItemInHand(hand, updated);
            }
            return true;
        }).orElse(false);
    }

    private static void playAsphaltMixPourSound(Level level, BlockPos pourPos) {
        double x = pourPos.getX() + 0.5;
        double y = pourPos.getY() + 0.5;
        double z = pourPos.getZ() + 0.5;
        level.playSound(null, x, y, z, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 0.95F + level.getRandom().nextFloat() * 0.1F);
    }

    private static boolean isAsphaltMixFluid(Fluid fluid) {
        return fluid == TFGFluids.ASPHALT_MIX.getSource()
                || fluid == TFGFluids.ASPHALT_MIX.getFlowing();
    }
}
