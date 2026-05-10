package su.terrafirmagreg.core.common.event;

import org.jetbrains.annotations.NotNull;

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
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocksAsphalt;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadEvent {
    private static final ResourceLocation ASPHALT_MIX_ID = TFGCore.id("asphalt_mix");
    /** Matches RNR wet-concrete style field pour: one spread operation per 1000 mB. */
    private static final int FIELD_POUR_MB = 1000;
    /** Sneak-use on base course: spot-repair one base block to hot asphalt. */
    private static final int PATCH_POUR_MB = 50;

    private enum AsphaltMixInteraction {
        NONE,
        /** Place pouring block above clicked base; spreads to nearby bases. */
        FIELD_POUR,
        /** Replace clicked base block with hot asphalt; small fluid cost. */
        PATCH_BASE_TO_HOT
    }

    private record SprayContext(InteractionHand hand, ItemStack stack) {
    }

    private AsphaltRoadEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        BlockState state = level.getBlockState(event.getPos());

        if (!level.isClientSide() && supportsRoadMarking(state) && handleSprayOnRoad(event, player, state)) {
            return;
        }

        if (!heldItemContainsAsphaltMix(held)) {
            return;
        }

        AsphaltMixInteraction mode = resolveAsphaltMixInteraction(level, event.getPos(), state, player);
        if (mode == AsphaltMixInteraction.NONE) {
            return;
        }

        int costMb = mode == AsphaltMixInteraction.FIELD_POUR ? FIELD_POUR_MB : PATCH_POUR_MB;
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, costMb)) {
            return;
        }

        /*
         * Do not cancel or setUseItem(DENY) on the client: Forge will then refuse to apply the server-side slot update
         * after IFluidHandlerItem drains/fills (KubeJS asphalt bucket, GregTech drums), causing the "two clicks to
         * update bucket" desync. Vanilla/GT fluid transfer is unchanged because we return early when mode == NONE.
         */
        if (!level.isClientSide()) {
            stopVanillaAndItemUse(event);
            switch (mode) {
                case FIELD_POUR -> handleFieldPourOnServer(event, level, player, hand, held);
                case PATCH_BASE_TO_HOT -> handlePatchBaseOnServer(event, level, player, hand, held);
                default -> {
                }
            }
        }
    }

    /**
     * Only when the clicked block is spreadable base: sneak = patch hot on that block; normal = field pour above it
     */
    private static AsphaltMixInteraction resolveAsphaltMixInteraction(Level level, BlockPos clicked, BlockState clickedState, Player player) {
        if (!clickedState.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return AsphaltMixInteraction.NONE;
        }
        if (player.isShiftKeyDown()) {
            return AsphaltMixInteraction.PATCH_BASE_TO_HOT;
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
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, FIELD_POUR_MB)) {
            return;
        }
        if (player.blockPosition().equals(clicked)) {
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
        if (!player.getAbilities().instabuild && !tryConsumeFluidMb(player, hand, held, FIELD_POUR_MB)) {
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
        if (!player.getAbilities().instabuild && !canAffordFluidDrain(held, PATCH_POUR_MB)) {
            return;
        }
        if (!level.setBlock(clicked, TFGBlocksAsphalt.ASPHALT_ROAD_HOT.getDefaultState(), Block.UPDATE_ALL)) {
            return;
        }
        if (!player.getAbilities().instabuild && !tryConsumeFluidMb(player, hand, held, PATCH_POUR_MB)) {
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
        if (stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(AsphaltRoadEvent::handlerHasNonEmptyAsphaltMix).orElse(false)) {
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
        Fluid mix = asphaltMixFluid();
        if (mix == Fluids.EMPTY) {
            return 0;
        }
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
        Fluid mix = asphaltMixFluid();
        if (mix == Fluids.EMPTY) {
            return false;
        }
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

    private static Fluid asphaltMixFluid() {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ASPHALT_MIX_ID);
        return fluid == null || fluid == Fluids.EMPTY ? Fluids.EMPTY : fluid;
    }

    private static boolean isAsphaltMixFluid(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        return ASPHALT_MIX_ID.equals(id);
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
            playSprayCanSound(event.getLevel(), event.getPos());
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
        playSprayCanSound(event.getLevel(), event.getPos());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(sprayHand, true);
        return true;
    }

    private static SprayContext resolveSprayContext(Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);
        if (isSprayCan(usedStack)) {
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

    private static boolean canUseSprayFromHand(Player player, InteractionHand sprayHand) {
        if (sprayHand == InteractionHand.MAIN_HAND) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainStack.isEmpty() || RoadMarkingStencilItem.patternFrom(mainStack).isPresent();
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

    /**
     * Match {@link com.gregtechceu.gtceu.common.item.ColorSprayBehaviour}: pass {@code null} as the player argument so
     * {@link Level#playSound} broadcasts to everyone in range. A non-null player can suppress or mis-route the packet
     * on the dedicated server / integrated server path, which made spray silent after we kept logic server-only.
     */
    private static void playSprayCanSound(Level level, BlockPos pos) {
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, pos, 0.85F, 1.0F);
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
