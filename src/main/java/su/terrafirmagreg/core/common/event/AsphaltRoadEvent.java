package su.terrafirmagreg.core.common.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.therighthon.rnr.common.RNRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.material.Fluids;
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
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadDecal;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHotBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadPouringBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocksAsphalt;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadEvent {
    private static final ResourceLocation ASPHALT_MIX_ID = ResourceLocation.fromNamespaceAndPath("tfg", "asphalt_mix");
    /** Matches RNR wet-concrete block_mod cost: one pour per 1000 mB from multi-tank containers. */
    private static final int POUR_FLUID_MB = 1000;

    private record SprayContext(InteractionHand hand, ItemStack stack) {
    }

    private AsphaltRoadEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        BlockState state = level.getBlockState(event.getPos());

        // Prefer spray logic first on asphalt road blocks (server only; effects are authoritative).
        if (!level.isClientSide() && supportsRoadMarking(state) && handleSprayOnRoad(event, player, state)) {
            return;
        }

        if (!carriesAsphaltMix(held)) {
            return;
        }

        /*
         * GregTech drums use BlockItem: if we only cancel on the server, the client still predicts block placement and
         * plays place sounds. Cancel on both sides whenever we own this interaction (pour cell or finished road).
         */
        if (suppressDefaultUseForAsphaltMixContainer(level, event.getPos(), state)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            if (!level.isClientSide()) {
                handleAsphaltMixPourOnServer(event, level, player, hand, held);
            }
        }
    }

    /**
     * Blocks fluid/vanilla follow-up and GregTech drum {@link net.minecraft.world.item.BlockItem} placement when the
     * click is on a finished road or on a valid pour cell above spreadable base (RNR-style).
     */
    private static boolean suppressDefaultUseForAsphaltMixContainer(Level level, BlockPos clicked, BlockState ground) {
        if (isAsphaltRoadFamily(ground) && !ground.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return true;
        }
        return resolvePourPos(level, clicked, ground) != null;
    }

    /**
     * Stops vanilla fluid use, block activation, and {@link net.minecraft.world.item.BlockItem} placement (GregTech
     * drums). {@link PlayerInteractEvent#setCanceled(boolean)} alone is not always enough; item/block {@link Event.Result}
     * must be denied on both sides to avoid place sounds and client container desync (e.g. buckets vanishing on roads).
     */
    private static void stopVanillaAndItemUseForAsphaltMix(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.DENY);
    }

    private static void handleAsphaltMixPourOnServer(PlayerInteractEvent.RightClickBlock event, Level level, Player player, InteractionHand hand, ItemStack held) {
        BlockPos clicked = event.getPos();
        BlockState ground = level.getBlockState(clicked);
        if (isAsphaltRoadFamily(ground) && !ground.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        BlockPos pourPos = resolvePourPos(level, clicked, ground);
        if (pourPos == null) {
            return;
        }
        if (!canAffordPour(held, player)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        // Same guard as RNR wet concrete: {@link com.therighthon.rnr.RNRHelpers#blockModRecipeCompatible}
        BlockPos basePos = pourPos.below();
        if (player.blockPosition().equals(basePos)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        BlockState space = level.getBlockState(pourPos);
        if (!space.isAir() && !space.canBeReplaced()) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        if (!player.getAbilities().instabuild && !simulatePourDrain(held)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        BlockState pourState = TFGBlocksAsphalt.ASPHALT_ROAD_POURING.getDefaultState();
        if (!level.setBlock(pourPos, pourState, Block.UPDATE_ALL)) {
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        if (!player.getAbilities().instabuild && !tryConsumePourFluid(player, hand, held)) {
            level.removeBlock(pourPos, false);
            stopVanillaAndItemUseForAsphaltMix(event);
            return;
        }
        playAsphaltMixPourSound(level, pourPos);
        player.swing(hand, true);
        stopVanillaAndItemUseForAsphaltMix(event);
    }

    /**
     * Cell for the pouring source block: above a hit spreadable base (any hit face), or the clicked replaceable/air
     * block when it sits directly above spreadable base course.
     */
    @Nullable
    private static BlockPos resolvePourPos(Level level, BlockPos clicked, BlockState ground) {
        if (ground.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return clicked.above();
        }
        BlockPos base = clicked.below();
        if (level.getBlockState(base).is(RNRTags.Blocks.CONCRETE_SPREADABLE) && (ground.isAir() || ground.canBeReplaced())) {
            return clicked;
        }
        return null;
    }

    private static boolean carriesAsphaltMix(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(AsphaltRoadEvent::handlerContainsAsphaltMix).orElse(false)) {
            return true;
        }
        return FluidUtil.getFluidContained(stack).map(fs -> isAsphaltMixFluid(fs.getFluid())).orElse(false);
    }

    private static boolean handlerContainsAsphaltMix(IFluidHandler handler) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack inTank = handler.getFluidInTank(i);
            if (!inTank.isEmpty() && isAsphaltMixFluid(inTank.getFluid())) {
                return true;
            }
        }
        return false;
    }

    private static boolean canAffordPour(ItemStack stack, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return simulatedAsphaltMixDrainMb(stack) >= POUR_FLUID_MB;
    }

    private static int simulatedAsphaltMixDrainMb(ItemStack stack) {
        Fluid mix = asphaltMixFluid();
        if (mix == Fluids.EMPTY) {
            return 0;
        }
        FluidStack want = new FluidStack(mix, POUR_FLUID_MB);
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.drain(want, FluidAction.SIMULATE).getAmount())
                .orElseGet(() -> FluidUtil.getFluidContained(stack)
                        .filter(fs -> isAsphaltMixFluid(fs.getFluid()))
                        .map(FluidStack::getAmount)
                        .orElse(0));
    }

    private static boolean simulatePourDrain(ItemStack stack) {
        return simulatedAsphaltMixDrainMb(stack) >= POUR_FLUID_MB;
    }

    private static boolean tryConsumePourFluid(Player player, InteractionHand hand, ItemStack held) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Fluid mix = asphaltMixFluid();
        if (mix == Fluids.EMPTY) {
            return false;
        }
        FluidStack want = new FluidStack(mix, POUR_FLUID_MB);
        return held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(handler -> {
            if (handler.drain(want, FluidAction.SIMULATE).getAmount() < POUR_FLUID_MB) {
                return false;
            }
            FluidStack drained = handler.drain(want, FluidAction.EXECUTE);
            if (drained.getAmount() < POUR_FLUID_MB) {
                return false;
            }
            ItemStack updated = handler.getContainer();
            ItemStack inHand = player.getItemInHand(hand);
            // GregTech drums (and similar) mutate the held stack in place; replacing the same reference causes a
            // visible inventory flicker. Buckets that swap to a new ItemStack still need setItemInHand.
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

    private static Fluid asphaltMixFluid() {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ASPHALT_MIX_ID);
        return fluid == null || fluid == Fluids.EMPTY ? Fluids.EMPTY : fluid;
    }

    private static boolean isAsphaltMixFluid(Fluid fluid) {
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        return id != null && isAsphaltMixId(id);
    }

    private static boolean isAsphaltRoadFamily(BlockState state) {
        return supportsRoadMarking(state)
                || state.getBlock() instanceof AsphaltRoadHotBlock
                || state.getBlock() instanceof AsphaltRoadPouringBlock;
    }

    private static boolean handleSprayOnRoad(PlayerInteractEvent.RightClickBlock event, Player player, BlockState state) {
        SprayContext spray = resolveSprayContext(player, event.getHand());
        if (spray == null) {
            return false;
        }
        InteractionHand sprayHand = spray.hand();
        ItemStack sprayStack = spray.stack();

        if (isSolventSprayCan(sprayStack)) {
            if (currentDecal(state).isNone() && currentMarkingColor(state).isNone()) {
                return false;
            }
            event.getLevel().setBlockAndUpdate(event.getPos(), clearMarking(state));
            damageSprayCan(player, sprayStack, sprayHand);
            playSprayCanSound(event.getLevel(), player, event.getPos());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            player.swing(sprayHand, true);
            return true;
        }

        AsphaltRoadMarkingColor targetColor = sprayCanColor(sprayStack);

        AsphaltRoadDecal targetDecal = resolveTargetDecal(player, sprayHand);
        if (isSameMarking(state, targetDecal, targetColor)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        event.getLevel().setBlockAndUpdate(event.getPos(), applyMarking(state, targetDecal, targetColor));
        damageSprayCan(player, sprayStack, sprayHand);
        playSprayCanSound(event.getLevel(), player, event.getPos());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(sprayHand, true);
        return true;
    }

    private static SprayContext resolveSprayContext(Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);
        if (isSprayCan(usedStack)) {
            // RightClickBlock runs per hand: offhand pass must not skip the main-hand guard.
            if (!canUseSprayFromHand(player, usedHand)) {
                return null;
            }
            return new SprayContext(usedHand, usedStack);
        }
        InteractionHand oppositeHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack oppositeStack = player.getItemInHand(oppositeHand);
        if (isSprayCan(oppositeStack) && canUseSprayFromHand(player, oppositeHand)) {
            return new SprayContext(oppositeHand, oppositeStack);
        }
        return null;
    }

    /**
     * Spray in main hand: always allowed. Spray in off hand: only when main is empty or holding a road stencil.
     */
    private static boolean canUseSprayFromHand(Player player, InteractionHand sprayHand) {
        if (sprayHand == InteractionHand.MAIN_HAND) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainStack.isEmpty() || RoadMarkingStencilItem.patternFrom(mainStack).isPresent();
    }

    private static boolean isAsphaltMixId(ResourceLocation fluidId) {
        if (fluidId == null || !ASPHALT_MIX_ID.getNamespace().equals(fluidId.getNamespace())) {
            return false;
        }
        String path = fluidId.getPath();
        return path.equals(ASPHALT_MIX_ID.getPath());
    }

    /**
     * No stencil or line stencil: decal follows facing. Cross stencil: {@link AsphaltRoadDecal#CROSS}, ignores facing.
     */
    private static AsphaltRoadDecal resolveTargetDecal(Player player, InteractionHand sprayHand) {
        ItemStack opposite = player.getItemInHand(sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return RoadMarkingStencilItem.patternFrom(opposite)
                .map(pattern -> switch (pattern) {
                    case LINE -> decalFromPlayerFacing(player);
                    case CROSS -> AsphaltRoadDecal.CROSS;
                    case ARROW -> arrowFromPlayerFacing(player);
                })
                .orElseGet(() -> decalFromPlayerFacing(player));
    }

    private static AsphaltRoadDecal decalFromPlayerFacing(Player player) {
        Direction facing = player.getDirection();
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        return vertical ? AsphaltRoadDecal.LINE_VERTICAL : AsphaltRoadDecal.LINE_HORIZONTAL;
    }

    private static AsphaltRoadDecal arrowFromPlayerFacing(Player player) {
        return switch (player.getDirection()) {
            case NORTH -> AsphaltRoadDecal.ARROW_NORTH;
            case EAST -> AsphaltRoadDecal.ARROW_EAST;
            case SOUTH -> AsphaltRoadDecal.ARROW_SOUTH;
            default -> AsphaltRoadDecal.ARROW_WEST;
        };
    }

    private static void damageSprayCan(Player player, ItemStack held, InteractionHand sprayHand) {
        if (player.getAbilities().instabuild) {
            return;
        }
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(sprayHand));
    }

    private static void playSprayCanSound(Level level, Player player, BlockPos pos) {
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, player, pos, 0.85F, 1.0F);
    }

    private static AsphaltRoadMarkingColor sprayCanColor(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"gtceu".equals(id.getNamespace())) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String path = id.getPath();
        if (!path.endsWith("_dye_spray_can")) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String colorName = path.substring(0, path.length() - "_dye_spray_can".length());
        return AsphaltRoadMarkingColor.fromSerializedName(colorName);
    }

    private static boolean isSolventSprayCan(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "gtceu".equals(id.getNamespace()) && "solvent_spray_can".equals(id.getPath());
    }

    private static boolean isSprayCan(ItemStack stack) {
        return isSolventSprayCan(stack) || !sprayCanColor(stack).isNone();
    }

    private static boolean supportsRoadMarking(BlockState state) {
        return state.getBlock() instanceof AsphaltRoadBlock
                || state.getBlock() instanceof AsphaltRoadSlabBlock;
    }

    private static AsphaltRoadDecal currentDecal(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.DECAL);
        }
        return state.getValue(AsphaltRoadSlabBlock.DECAL);
    }

    private static AsphaltRoadMarkingColor currentMarkingColor(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.COLOR);
        }
        return state.getValue(AsphaltRoadSlabBlock.COLOR);
    }

    private static boolean isSameMarking(BlockState state, AsphaltRoadDecal targetDecal, AsphaltRoadMarkingColor targetColor) {
        return currentDecal(state) == targetDecal && currentMarkingColor(state) == targetColor;
    }

    private static BlockState applyMarking(BlockState state, AsphaltRoadDecal decal, AsphaltRoadMarkingColor color) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, decal).setValue(AsphaltRoadBlock.COLOR, color);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, decal).setValue(AsphaltRoadSlabBlock.COLOR, color);
    }

    private static BlockState clearMarking(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, AsphaltRoadDecal.NONE)
                    .setValue(AsphaltRoadBlock.COLOR, AsphaltRoadMarkingColor.NONE);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, AsphaltRoadDecal.NONE)
                .setValue(AsphaltRoadSlabBlock.COLOR, AsphaltRoadMarkingColor.NONE);
    }
}
